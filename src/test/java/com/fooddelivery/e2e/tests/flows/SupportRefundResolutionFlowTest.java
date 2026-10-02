package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.ChatWidgetPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.OrderMoneyChecks;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Support-ticket refund decisions on owned, already-delivered orders: a reduced award with the
 * restaurant at fault, a denial, and refusal of a second refund for an item already refunded.
 * Customers raise tickets through the real chat path (item quote, then "Submit Refund Request" --
 * the only customer support entry the UI renders); the administrator decides in the refund queue.
 * Money is asserted on the refund, payment and ledger, not on the decision screen.
 *
 * <p>Requires {@code -Dsupport.partial.order.id} and {@code -Dsupport.deny.order.id}: delivered CARD
 * orders whose manifests are in target/lifecycle. No order is created. Methods run in order:
 * scenario 3 needs scenario 1's completed award on the partial order.
 */
@Tag("flow") @Tag("support-refund")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SupportRefundResolutionFlowTest extends TestBase {
    private static final String PARTIAL_ORDER = System.getProperty("support.partial.order.id", "").trim();
    private static final String DENY_ORDER = System.getProperty("support.deny.order.id", "").trim();
    private static final BigDecimal AWARD = new BigDecimal("12.00");

    @BeforeEach
    void ownedDeliveredOrders() throws java.io.IOException {
        Assumptions.assumeFalse(PARTIAL_ORDER.isEmpty() || DENY_ORDER.isEmpty(),
                "support refund flow needs -Dsupport.partial.order.id and -Dsupport.deny.order.id");
        for (String id : List.of(PARTIAL_ORDER, DENY_ORDER)) {
            String manifest = Files.readString(Path.of("target/lifecycle", id + ".json"));
            org.assertj.core.api.Assertions.assertThat(manifest)
                    .contains("\"" + id + "\"").contains(testCustomerPhone).contains(testRestaurantPhone);
        }
        customerPage.route("**/api/v1/orders", route -> {
            if (route.request().method().equals("POST")) throw new AssertionError("Support decisions must not create an order");
            route.resume();
        });
        loginAsAdmin();
    }

    @Test @Order(1)
    @DisplayName("SUPPORT-REFUND-01: reduced award on an item ticket, restaurant at fault")
    void reducedAwardWithRestaurantFault() throws java.io.IOException {
        openDeliveredOrder(PARTIAL_ORDER);
        Map<?, ?> order = RefundRecoveryChecks.order(customerPage, PARTIAL_ORDER);
        org.assertj.core.api.Assertions.assertThat(refunds(PARTIAL_ORDER)).as("fresh partial fixture").isEmpty();
        BigDecimal itemQuote = itemQuote(order);

        submitItemTicket(PARTIAL_ORDER, "E2E support award " + PARTIAL_ORDER.substring(0, 8), itemQuote);
        Map<?, ?> ticket = awaitTicket(PARTIAL_ORDER, "OPEN");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.amount(ticket, "refundAmount")).isEqualByComparingTo(itemQuote);
        org.assertj.core.api.Assertions.assertThat(ticket.get("requestedRefundItems")).as("item-level ticket").isNotNull();

        // Admin: award less than the quote, attributing the fault to the restaurant.
        Locator panel = openQueueTicket(PARTIAL_ORDER);
        chooseResolution(panel, true);
        panel.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Fault Attribution")).click();
        adminPage.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Restaurant Fault (Chargeback)").setExact(true)).click();
        panel.getByLabel("Override amount").fill(AWARD.toPlainString());
        panel.getByLabel("Resolution notes").fill("E2E reduced award: one item partially affected");
        confirmDecision(true, "₹12.00");

        Map<?, ?> refund = awaitCompletedRefund(PARTIAL_ORDER, AWARD);
        Map<?, ?> resolved = awaitTicket(PARTIAL_ORDER, "RESOLVED");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.amount(resolved, "refundAmount")).isEqualByComparingTo(AWARD);
        org.assertj.core.api.Assertions.assertThat(resolved.get("resolutionNotes")).isEqualTo("E2E reduced award: one item partially affected");
        org.assertj.core.api.Assertions.assertThat(resolved.get("resolvedBy")).isNotNull();

        // Restaurant pays its share of the award: payout x (award / total), as LedgerBookkeeper books it.
        BigDecimal ratio = AWARD.divide(OrderMoneyChecks.amount(order, "totalAmount"), 4, RoundingMode.HALF_UP);
        Map<?, ?> money = awaitMoney(PARTIAL_ORDER, "PARTIALLY_REFUNDED", AWARD);
        BigDecimal clawback = OrderMoneyChecks.amount(money, "restaurantPayout").multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        org.assertj.core.api.Assertions.assertThat(clawback).isPositive();
        org.assertj.core.api.Assertions.assertThat(categoryTotal(money, "CLAWBACK", "RESTAURANT_PAYABLE", "DEBIT"))
                .isEqualByComparingTo(clawback);
        record(PARTIAL_ORDER, "award", Map.of("refundId", refund.get("id"), "award", AWARD, "clawback", clawback));
    }

    @Test @Order(2)
    @DisplayName("SUPPORT-REFUND-02: denied item request moves no money")
    void deniedRequestMovesNoMoney() throws java.io.IOException {
        openDeliveredOrder(DENY_ORDER);
        org.assertj.core.api.Assertions.assertThat(refunds(DENY_ORDER)).as("fresh denial fixture").isEmpty();
        Map<?, ?> order = RefundRecoveryChecks.order(customerPage, DENY_ORDER);
        Map<?, ?> moneyBefore = money(DENY_ORDER);
        int ledgerLinesBefore = ((List<?>) moneyBefore.get("ledgerLines")).size();

        submitItemTicket(DENY_ORDER, "E2E denial: claims food was cold", itemQuote(order));
        awaitTicket(DENY_ORDER, "OPEN");

        Locator panel = openQueueTicket(DENY_ORDER);
        chooseResolution(panel, false);
        panel.getByLabel("Resolution notes").fill("E2E denial: delivery photo shows sealed hot packaging");
        confirmDecision(false, null);

        Map<?, ?> rejected = awaitTicket(DENY_ORDER, "REJECTED");
        org.assertj.core.api.Assertions.assertThat(rejected.get("resolutionNotes")).isEqualTo("E2E denial: delivery photo shows sealed hot packaging");
        org.assertj.core.api.Assertions.assertThat(rejected.get("resolvedBy")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(refunds(DENY_ORDER)).as("a denial creates no refund").isEmpty();
        Map<?, ?> moneyAfter = money(DENY_ORDER);
        org.assertj.core.api.Assertions.assertThat(moneyAfter.get("paymentStatus")).isEqualTo(moneyBefore.get("paymentStatus"));
        org.assertj.core.api.Assertions.assertThat(((List<?>) moneyAfter.get("ledgerLines")).size()).isEqualTo(ledgerLinesBefore);
        record(DENY_ORDER, "denial", Map.of("ticketId", rejected.get("id"), "refunds", 0));
    }

    @Test @Order(3)
    @DisplayName("SUPPORT-REFUND-03: an item already refunded cannot be quoted or refunded again")
    void refundedItemCannotBeRefundedAgain() throws java.io.IOException {
        openDeliveredOrder(PARTIAL_ORDER);
        org.assertj.core.api.Assertions.assertThat(refunds(PARTIAL_ORDER)).as("needs SUPPORT-REFUND-01's completed award").hasSize(1);
        Map<?, ?> resolved = awaitTicket(PARTIAL_ORDER, "RESOLVED");

        Locator reply = requestItemQuote(PARTIAL_ORDER, "E2E second request for the same item");
        assertThat(reply).hasAttribute("data-message-type", "REFUND_ERROR");
        assertThat(reply).containsText("ITEM_ALREADY_REFUNDED");

        Map<?, ?> body = (Map<?, ?>) RefundRecoveryChecks.read(adminPage, "/api/v1/internal/admin/refunds?status=OPEN&size=50").get("body");
        org.assertj.core.api.Assertions.assertThat(((List<?>) body.get("content")).stream().map(t -> (Map<?, ?>) t)
                .filter(t -> PARTIAL_ORDER.equals(t.get("orderId")))).as("no ticket for a refused quote").isEmpty();
        org.assertj.core.api.Assertions.assertThat(refunds(PARTIAL_ORDER)).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(awaitTicket(PARTIAL_ORDER, "RESOLVED").get("id")).isEqualTo(resolved.get("id"));
        record(PARTIAL_ORDER, "item-consumed", Map.of("refunds", 1, "error", "ITEM_ALREADY_REFUNDED"));
    }

    // ── Customer actions ────────────────────────────────────────────────────

    private static BigDecimal itemQuote(Map<?, ?> order) {
        return OrderMoneyChecks.amount(order, "itemTotal").add(OrderMoneyChecks.amount(order, "sgst"))
                .add(OrderMoneyChecks.amount(order, "cgst"));
    }

    private void openDeliveredOrder(String id) {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + id + "']").click();
        assertThat(new CustomerOrderTrackerPage(customerPage, id).tracker()).isVisible();
    }

    /** Selects every item, asks chat for a quote and returns the reply: a quote or a refund error. */
    private Locator requestItemQuote(String id, String reason) {
        ChatWidgetPage chat = new ChatWidgetPage(customerPage);
        chat.openRefundRequest(id);
        customerPage.waitForCondition(() -> chat.refundItemCheckboxes().count() > 0);
        for (Locator box : chat.refundItemCheckboxes().all()) box.check();
        chat.fillRefundReason(reason);
        Locator replies = customerPage.locator("[data-testid='chat-message'][data-message-type='REFUND_QUOTE_RESPONSE'],"
                + "[data-testid='chat-message'][data-message-type='REFUND_ERROR']");
        int before = replies.count();
        chat.submitRefundRequest();
        assertThat(replies).hasCount(before + 1, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(20000));
        return replies.last();
    }

    /** The customer's real support path: an item quote in chat, then "Submit Refund Request" on it. */
    private void submitItemTicket(String id, String reason, BigDecimal expectedQuote) {
        Locator quote = requestItemQuote(id, reason);
        assertThat(quote).hasAttribute("data-message-type", "REFUND_QUOTE_RESPONSE");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.parseInr(quote.innerText())).isEqualByComparingTo(expectedQuote);
        quote.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit Refund Request").setExact(true)).click();
    }

    // ── Admin actions ───────────────────────────────────────────────────────

    private Locator openQueueTicket(String orderId) {
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

    private void chooseResolution(Locator panel, boolean approved) {
        panel.getByRole(AriaRole.GROUP, new Locator.GetByRoleOptions().setName("Resolution").setExact(true))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve" : "Reject").setExact(true)).click();
    }

    /** Opens the amount-naming confirmation and commits the decision. */
    private void confirmDecision(boolean approved, String amountInTitle) {
        adminPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(approved ? "Approve Refund" : "Reject Refund").setExact(true)).click();
        Locator dialog = adminPage.getByRole(AriaRole.DIALOG);
        dialog.waitFor();
        if (amountInTitle != null) assertThat(dialog).containsText(amountInTitle);
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve refund" : "Reject refund").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    // ── Authoritative reads ─────────────────────────────────────────────────

    private List<?> refunds(String id) {
        return (List<?>) RefundRecoveryChecks.read(customerPage, "/api/v1/money/customer/orders/" + id + "/refunds").get("body");
    }

    private Map<?, ?> money(String id) {
        return (Map<?, ?>) RefundRecoveryChecks.read(adminPage, "/api/v1/internal/admin/orders/" + id + "/money").get("body");
    }

    /** The single ticket for this order in {@code status}; tickets arrive asynchronously from chat. */
    private Map<?, ?> awaitTicket(String orderId, String status) {
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

    private Map<?, ?> awaitCompletedRefund(String id, BigDecimal amount) {
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
    private Map<?, ?> awaitMoney(String id, String paymentStatus, BigDecimal totalRefunded) {
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

    private static BigDecimal categoryTotal(Map<?, ?> money, String category, String ownerType, String direction) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object value : (List<?>) money.get("ledgerLines")) {
            Map<?, ?> line = (Map<?, ?>) value;
            if (!category.equals(line.get("category")) || !direction.equals(line.get("direction"))) continue;
            if (ownerType != null && !ownerType.equals(line.get("ownerType"))) continue;
            total = total.add(OrderMoneyChecks.amount(line, "amount"));
        }
        return total;
    }

    private void record(String orderId, String scenario, Map<String, ?> facts) throws java.io.IOException {
        StringBuilder json = new StringBuilder("{\"orderId\":\"").append(orderId).append("\",\"scenario\":\"").append(scenario)
                .append("\",\"newOrderCreated\":false");
        facts.forEach((k, v) -> json.append(",\"").append(k).append("\":\"").append(v).append('"'));
        Files.writeString(Path.of("target/lifecycle", orderId + "-support-" + scenario + ".json"), json.append('}').toString());
    }
}
