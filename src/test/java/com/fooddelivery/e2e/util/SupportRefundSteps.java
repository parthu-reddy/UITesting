package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.ChatWidgetPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * The support-refund steps shared by the flows that raise and decide chat support tickets: the
 * customer's item quote and ticket, the administrator's decision in the refund queue, and the
 * authoritative reads of tickets, refunds, payment and ledger. Moved unchanged out of
 * SupportRefundResolutionFlowTest so AdminRefundRetryFlowTest does not copy them.
 */
public final class SupportRefundSteps {
    private final Page customerPage;
    private final Page adminPage;
    private final String customerPhone;
    /** The customer's latest chat history read (page 0), parsed on the test thread, never inside the callback. */
    private final java.util.concurrent.atomic.AtomicReference<com.microsoft.playwright.Response> chatHistory =
            new java.util.concurrent.atomic.AtomicReference<>();

    public SupportRefundSteps(Page customerPage, Page adminPage, String customerPhone) {
        this.customerPage = customerPage;
        this.adminPage = adminPage;
        this.customerPhone = customerPhone;
        customerPage.onResponse(r -> {
            if (r.request().method().equals("GET") && r.status() == 200
                    && UrlPaths.path(r.url()).matches("/api/v1/chat/sessions/[^/]+/messages") && r.url().contains("page=0"))
                chatHistory.set(r);
        });
    }

    // ── Customer actions ────────────────────────────────────────────────────

    public static BigDecimal itemQuote(Map<?, ?> order) {
        return OrderMoneyChecks.amount(order, "itemTotal").add(OrderMoneyChecks.amount(order, "sgst"))
                .add(OrderMoneyChecks.amount(order, "cgst"));
    }

    public void openDeliveredOrder(String id) {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(customerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        new com.fooddelivery.e2e.pages.customer.CustomerOrderHistoryPage(customerPage).pageToOrder(id, 20).click();
        assertThat(new CustomerOrderTrackerPage(customerPage, id).tracker()).isVisible();
        // Fail on the precondition, not on a missing dialog a minute later.
        java.time.Instant updated = java.time.Instant.parse((String) RefundRecoveryChecks.order(customerPage, id).get("updatedAt"));
        org.assertj.core.api.Assertions.assertThat(java.time.Duration.between(updated, java.time.Instant.now()))
                .as("order %s must be within the two-hour post-delivery chat window (last updated %s)", id, updated)
                .isLessThan(java.time.Duration.ofHours(2));
    }

    /** Selects every item, asks chat for a quote and returns the reply: a quote or a refund error. */
    public Locator requestItemQuote(String id, String reason) {
        ChatWidgetPage chat = new ChatWidgetPage(customerPage);
        chat.openRefundRequest(id);
        customerPage.waitForCondition(() -> chat.refundItemCheckboxes().count() > 0);
        for (Locator box : chat.refundItemCheckboxes().all()) box.check();
        chat.fillRefundReason(reason);
        Locator replies = customerPage.locator("[data-testid='chat-message'][data-message-type='REFUND_QUOTE_RESPONSE'],"
                + "[data-testid='chat-message'][data-message-type='REFUND_ERROR']");
        // Count only after the earlier replies are on screen: history arrives asynchronously after the session opens,
        // and counting first saw 0 of an order's 2 earlier replies (2026-10-08, without the 400 ms slow-mo).
        customerPage.waitForCondition(() -> chatHistory.get() != null,
                new Page.WaitForConditionOptions().setTimeout(20000));
        Map<?, ?> history = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", chatHistory.get().text());
        long earlier = ((List<?>) ((Map<?, ?>) history.get("data")).get("content")).stream().map(m -> (Map<?, ?>) m)
                .filter(m -> List.of("REFUND_QUOTE_RESPONSE", "REFUND_ERROR").contains(m.get("messageType"))).count();
        assertThat(replies).hasCount((int) earlier, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(20000));
        int before = replies.count();
        chat.submitRefundRequest();
        assertThat(replies).hasCount(before + 1, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(20000));
        return replies.last();
    }

    /** The customer's real support path: an item quote in chat, then "Submit Refund Request" on it. */
    public void submitItemTicket(String id, String reason, BigDecimal expectedQuote) {
        Locator quote = requestItemQuote(id, reason);
        assertThat(quote).hasAttribute("data-message-type", "REFUND_QUOTE_RESPONSE");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.parseInr(quote.innerText())).isEqualByComparingTo(expectedQuote);
        quote.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit Refund Request").setExact(true)).click();
        // The customer is told the request is pending, never that money moved (moved here from the deleted
        // OwnedRefundVisualUiTest so every support/refund flow checks it).
        assertThat(customerPage.getByText("Refund Request Submitted", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByText("Under review by support. No refund has been approved yet.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    // ── Admin actions ───────────────────────────────────────────────────────

    public Locator openQueueTicket(String orderId) {
        adminPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/refunds");
        adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Refund Exception Queue").setExact(true)).waitFor();
        Locator row = adminPage.getByRole(AriaRole.TABLE, new Page.GetByRoleOptions().setName("Refund tickets awaiting a decision"))
                .locator("tbody tr").filter(new Locator.FilterOptions().setHasText(orderId.substring(0, 8)));
        assertThat(row).hasCount(1, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(20000));
        row.click();
        Locator details = adminPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ticket Details"));
        details.waitFor();
        return adminPage.locator("body");
    }

    public void chooseResolution(Locator panel, boolean approved) {
        panel.getByRole(AriaRole.GROUP, new Locator.GetByRoleOptions().setName("Resolution").setExact(true))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve" : "Reject").setExact(true)).click();
    }

    /** Opens the amount-naming confirmation and commits the decision. */
    public void confirmDecision(boolean approved, String amountInTitle) {
        adminPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(approved ? "Approve Refund" : "Reject Refund").setExact(true)).click();
        Locator dialog = adminPage.getByRole(AriaRole.DIALOG);
        dialog.waitFor();
        if (amountInTitle != null) assertThat(dialog).containsText(amountInTitle);
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve refund" : "Reject refund").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    // ── Authoritative reads ─────────────────────────────────────────────────

    public List<?> refunds(String id) {
        return (List<?>) RefundRecoveryChecks.read(customerPage, "/api/v1/money/customer/orders/" + id + "/refunds").get("body");
    }

    public Map<?, ?> money(String id) {
        return (Map<?, ?>) RefundRecoveryChecks.read(adminPage, "/api/v1/internal/admin/orders/" + id + "/money").get("body");
    }

    /** The single ticket for this order in {@code status}; tickets arrive asynchronously from chat. */
    public Map<?, ?> awaitTicket(String orderId, String status) {
        final Page page = adminPage;
        java.util.concurrent.atomic.AtomicReference<Map<?, ?>> found = new java.util.concurrent.atomic.AtomicReference<>();
        page.waitForCondition(() -> {
            Map<?, ?> body = (Map<?, ?>) RefundRecoveryChecks.read(page, "/api/v1/internal/admin/refunds?status=" + status + "&size=50").get("body");
            List<Map<?, ?>> mine = ((List<?>) body.get("content")).stream().<Map<?, ?>>map(t -> (Map<?, ?>) t)
                    .filter(t -> orderId.equals(t.get("orderId"))).toList();
            org.assertj.core.api.Assertions.assertThat(mine.size()).as("tickets for %s in %s", orderId, status).isLessThanOrEqualTo(1);
            if (mine.isEmpty()) return false;
            found.set(mine.get(0));
            return true;
        }, new Page.WaitForConditionOptions().setTimeout(30000));
        return found.get();
    }

    public Map<?, ?> awaitCompletedRefund(String id, BigDecimal amount) {
        java.util.concurrent.atomic.AtomicReference<Map<?, ?>> found = new java.util.concurrent.atomic.AtomicReference<>();
        customerPage.waitForCondition(() -> {
            for (Object value : refunds(id)) {
                Map<?, ?> refund = (Map<?, ?>) value;
                if (OrderMoneyChecks.amount(refund, "amount").compareTo(amount) != 0) continue;
                org.assertj.core.api.Assertions.assertThat(refund.get("destination")).isEqualTo("ORIGINAL_METHOD");
                org.assertj.core.api.Assertions.assertThat(refund.get("status")).isIn("REQUESTED", "PROCESSING", "COMPLETED");
                if (!"COMPLETED".equals(refund.get("status"))) return false;
                org.assertj.core.api.Assertions.assertThat(refund.get("completedAt")).isNotNull();
                found.set(refund);
                return true;
            }
            return false;
        }, new Page.WaitForConditionOptions().setTimeout(30000));
        return found.get();
    }

    /** Waits for the payment status and checks the ledger: REFUND lines balance and equal the total refunded. */
    public Map<?, ?> awaitMoney(String id, String paymentStatus, BigDecimal totalRefunded) {
        java.util.concurrent.atomic.AtomicReference<Map<?, ?>> found = new java.util.concurrent.atomic.AtomicReference<>();
        adminPage.waitForCondition(() -> {
            Map<?, ?> money = money(id);
            if (!paymentStatus.equals(money.get("paymentStatus"))) return false;
            BigDecimal credit = categoryTotal(money, "REFUND", null, "CREDIT");
            BigDecimal debit = categoryTotal(money, "REFUND", null, "DEBIT");
            if (credit.compareTo(totalRefunded) != 0) return false;
            org.assertj.core.api.Assertions.assertThat(debit).isEqualByComparingTo(credit);
            found.set(money);
            return true;
        }, new Page.WaitForConditionOptions().setTimeout(30000));
        return found.get();
    }

    public static BigDecimal categoryTotal(Map<?, ?> money, String category, String ownerType, String direction) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object value : (List<?>) money.get("ledgerLines")) {
            Map<?, ?> line = (Map<?, ?>) value;
            if (!category.equals(line.get("category")) || !direction.equals(line.get("direction"))) continue;
            if (ownerType != null && !ownerType.equals(line.get("ownerType"))) continue;
            total = total.add(OrderMoneyChecks.amount(line, "amount"));
        }
        return total;
    }

    public void record(String orderId, String scenario, Map<String, ?> facts) throws java.io.IOException {
        StringBuilder json = new StringBuilder("{\"orderId\":\"").append(orderId).append("\",\"scenario\":\"").append(scenario)
                .append("\",\"newOrderCreated\":false");
        facts.forEach((k, v) -> json.append(",\"").append(k).append("\":\"").append(v).append('"'));
        Files.writeString(Path.of("target/lifecycle", orderId + "-support-" + scenario + ".json"), json.append('}').toString());
    }
}
