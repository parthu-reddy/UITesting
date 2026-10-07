package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminRefundQueuePage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.OrderMoneyChecks;
import com.fooddelivery.e2e.util.SupportRefundSteps;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** One owner-authorized Dev ticket; visible UI only, no order creation or refund approval. */
@Tag("ui-only")
@Tag("owned-refund-visual")
@EnabledIfSystemProperty(named = "visual.refund.order.id", matches = "6fbe0289-645e-4f1c-ae0d-caab21689070")
public class OwnedRefundVisualUiTest extends TestBase {
    private static final String ORDER = "6fbe0289-645e-4f1c-ae0d-caab21689070";
    private static final String REASON = "E2E visual audit 6fbe0289: owned dummy ticket";
    private static final String NOTE = "E2E visual audit: reject this owned dummy request; no refund approved.";
    private final Path evidence = Path.of(System.getProperty("visual.audit.dir",
            "e2e-plan/_handoff/evidence/123-owned-refund-visual"));

    @Test
    void ownedTicketDetailsConfirmationAndRejectedAudit() throws Exception {
        Map<?, ?> manifest = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", Files.readString(
                Path.of("e2e-plan/_handoff/fixtures/122-visual-owned-" + ORDER + ".json")));
        org.assertj.core.api.Assertions.assertThat(manifest.get("orderId")).isEqualTo(ORDER);
        org.assertj.core.api.Assertions.assertThat(manifest.get("customerPhone")).isEqualTo(testCustomerPhone);
        org.assertj.core.api.Assertions.assertThat(manifest.get("restaurantPhone")).isEqualTo(testRestaurantPhone);
        org.assertj.core.api.Assertions.assertThat(manifest.get("riderPhone")).isEqualTo(testRiderPhone);
        var orderWrites = new AtomicInteger();
        var resolutionWrites = new AtomicInteger();
        var resolutionBody = new java.util.concurrent.atomic.AtomicReference<String>();
        customerPage.onRequest(request -> {
            if (request.method().equals("POST") && UrlPaths.path(request.url()).equals("/api/v1/orders"))
                orderWrites.incrementAndGet();
        });
        adminPage.onRequest(request -> {
            if (request.method().equals("POST") && UrlPaths.path(request.url())
                    .startsWith("/api/v1/internal/admin/refunds/")) {
                resolutionWrites.incrementAndGet();
                resolutionBody.set(request.postData());
            }
        });
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("orderId", ORDER);
        facts.put("reason", REASON);
        facts.put("note", NOTE);
        facts.put("ownerAuthorized", true);
        facts.put("cleanupPerformed", false);
        facts.put("createdInInvocation", false);
        facts.put("rejectedInInvocation", false);
        try {
            customerPage.setViewportSize(1280, 800);
            new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            org.assertj.core.api.Assertions.assertThat(openReceiptAndReadRefunds()).isEmpty();
            Locator receipt = new CustomerOrderTrackerPage(customerPage, ORDER).tracker();
            BigDecimal quote = receiptLine(receipt, "Items", false).add(receiptLine(receipt, "GST", true));
            org.assertj.core.api.Assertions.assertThat(quote).isPositive();
            facts.put("quotedAmount", quote.toPlainString());

            adminPage.setViewportSize(1280, 800);
            loginAsAdmin();
            new AdminPortalPage(adminPage).openRefundsTab();
            List<?> tickets = selectAllAndRead();
            List<Map<?, ?>> owned = owned(tickets);
            org.assertj.core.api.Assertions.assertThat(owned).hasSizeLessThanOrEqualTo(1);
            if (owned.isEmpty()) {
                new SupportRefundSteps(customerPage, adminPage, testCustomerPhone)
                        .submitItemTicket(ORDER, REASON, quote);
                assertThat(customerPage.getByText("Refund Request Submitted",
                        new Page.GetByTextOptions().setExact(true))).isVisible();
                assertThat(customerPage.getByText("Under review by support. No refund has been approved yet.",
                        new Page.GetByTextOptions().setExact(true))).isVisible();
                facts.put("createdInInvocation", true);
                new CustomerOrderChatPage(customerPage).closeChat();
                // Returning through the nav re-reads the real queue; no direct endpoint or reload setup.
                new AdminPortalPage(adminPage).openSupportTab();
                new AdminPortalPage(adminPage).openRefundsTab();
                tickets = selectAllAndRead();
                owned = owned(tickets);
                org.assertj.core.api.Assertions.assertThat(owned).hasSize(1);
            }
            Map<?, ?> ticket = owned.get(0);
            String ticketId = String.valueOf(ticket.get("id"));
            org.assertj.core.api.Assertions.assertThat(ticket.get("reason")).isEqualTo(REASON);
            org.assertj.core.api.Assertions.assertThat(ticket.get("status")).isIn("OPEN", "REJECTED");
            org.assertj.core.api.Assertions.assertThat(new BigDecimal(ticket.get("refundAmount").toString()))
                    .isEqualByComparingTo(quote);
            facts.put("ticketId", ticketId);
            facts.put("status", ticket.get("status"));
            saveFacts(facts);
            Locator row = adminPage.getByRole(AriaRole.TABLE).locator("tbody tr")
                    .filter(new Locator.FilterOptions().setHasText(REASON));
            assertThat(row).hasCount(1);
            assertThat(row).containsText(ORDER.substring(0, 8));
            capture("admin-owned-queue");
            row.click();
            Locator details = adminPage.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Ticket Details").setExact(true));
            assertThat(details).isVisible();
            assertThat(details.locator("../..").getByText(REASON,
                    new Locator.GetByTextOptions().setExact(true))).isVisible();
            if (ticket.get("status").equals("OPEN")) {
                capture("admin-owned-details");
                AdminRefundQueuePage queue = new AdminRefundQueuePage(adminPage);
                queue.openResolutionConfirmation(true);
                org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.parseInr(queue.confirmationDialog().innerText()))
                        .isEqualByComparingTo(quote);
                assertThat(queue.confirmationDialog()).containsText(ORDER.substring(0, 8).toUpperCase());
                capture("admin-owned-approval-cancel");
                queue.cancelConfirmation();
                org.assertj.core.api.Assertions.assertThat(resolutionWrites.get()).isZero();
                adminPage.getByLabel("Resolution notes", new Page.GetByLabelOptions().setExact(true)).fill(NOTE);
                queue.openResolutionConfirmation(false);
                capture("admin-owned-rejection-confirm");
                Response rejected = adminPage.waitForResponse(response -> response.request().method().equals("POST")
                                && UrlPaths.path(response.url()).equals("/api/v1/internal/admin/refunds/" + ticketId + "/resolve"),
                        () -> queue.confirmationDialog().getByRole(AriaRole.BUTTON,
                                new Locator.GetByRoleOptions().setName("Reject refund").setExact(true)).click());
                org.assertj.core.api.Assertions.assertThat(rejected.status()).isEqualTo(200);
                Map<?, ?> body = (Map<?, ?>) adminPage.evaluate("text => JSON.parse(text)", rejected.text());
                org.assertj.core.api.Assertions.assertThat(body.get("id")).isEqualTo(ticketId);
                org.assertj.core.api.Assertions.assertThat(body.get("orderId")).isEqualTo(ORDER);
                org.assertj.core.api.Assertions.assertThat(body.get("status")).isEqualTo("REJECTED");
                org.assertj.core.api.Assertions.assertThat(body.get("resolutionNotes")).isEqualTo(NOTE);
                org.assertj.core.api.Assertions.assertThat(body.get("resolvedBy")).isNotNull();
                org.assertj.core.api.Assertions.assertThat(body.get("resolvedAt")).isNotNull();
                facts.put("rejectedInInvocation", true);
                facts.put("status", "REJECTED");
                saveFacts(facts);
                org.assertj.core.api.Assertions.assertThat(resolutionBody.get())
                        .as("capture the UI request body at request time").isNotNull();
                Map<?, ?> sent = (Map<?, ?>) adminPage.evaluate("text => JSON.parse(text)", resolutionBody.get());
                org.assertj.core.api.Assertions.assertThat(sent.get("approved")).isEqualTo(false);
                assertThat(row).containsText("Rejected");
                row.click();
            } else {
                org.assertj.core.api.Assertions.assertThat(ticket.get("resolutionNotes")).isEqualTo(NOTE);
                org.assertj.core.api.Assertions.assertThat(ticket.get("resolvedBy")).isNotNull();
                org.assertj.core.api.Assertions.assertThat(ticket.get("resolvedAt")).isNotNull();
            }
            assertThat(adminPage.getByText("Resolution Audit", new Page.GetByTextOptions().setExact(true))).isVisible();
            assertThat(adminPage.getByText(NOTE, new Page.GetByTextOptions().setExact(false))).isVisible();
            assertThat(adminPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Approve Refund").setExact(true))).hasCount(0);
            capture("admin-owned-rejected-audit");
            org.assertj.core.api.Assertions.assertThat(openReceiptAndReadRefunds()).as("a rejected request creates no refund").isEmpty();
            org.assertj.core.api.Assertions.assertThat(orderWrites.get()).isZero();
            org.assertj.core.api.Assertions.assertThat(resolutionWrites.get())
                    .isEqualTo(Boolean.TRUE.equals(facts.get("rejectedInInvocation")) ? 1 : 0);
            facts.put("refundsBefore", 0);
            facts.put("refundsAfter", 0);
            facts.put("passed", true);
        } finally {
            facts.put("orderWrites", orderWrites.get());
            facts.put("resolutionWrites", resolutionWrites.get());
            saveFacts(facts);
        }
    }

    private List<?> openReceiptAndReadRefunds() {
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator history = customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']");
        assertThat(history).isVisible();
        assertThat(history).containsText("Delivered");
        Response refunds = customerPage.waitForResponse(response -> response.request().method().equals("GET")
                        && UrlPaths.path(response.url()).equals("/api/v1/money/customer/orders/" + ORDER + "/refunds"),
                history::click);
        org.assertj.core.api.Assertions.assertThat(refunds.status()).isEqualTo(200);
        Locator receipt = new CustomerOrderTrackerPage(customerPage, ORDER).tracker();
        assertThat(receipt.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
        assertThat(receipt).containsText("Paid via CARD");
        return (List<?>) customerPage.evaluate("text => JSON.parse(text)", refunds.text());
    }

    private List<?> selectAllAndRead() {
        adminPage.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Status Filter").setExact(true)).click();
        Response response = adminPage.waitForResponse(r -> r.request().method().equals("GET")
                        && UrlPaths.path(r.url()).equals("/api/v1/internal/admin/refunds")
                        && !java.util.Objects.toString(java.net.URI.create(r.url()).getRawQuery(), "").contains("status="),
                () -> adminPage.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName("All Tickets").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        return (List<?>) ((Map<?, ?>) adminPage.evaluate("text => JSON.parse(text)", response.text())).get("content");
    }

    private List<Map<?, ?>> owned(List<?> tickets) {
        return tickets.stream().<Map<?, ?>>map(value -> (Map<?, ?>) value)
                .filter(ticket -> ORDER.equals(ticket.get("orderId"))).toList();
    }

    private BigDecimal receiptLine(Locator receipt, String label, boolean optional) {
        Locator value = receipt.getByText(label, new Locator.GetByTextOptions().setExact(true));
        if (optional && value.count() == 0) return BigDecimal.ZERO;
        assertThat(value).hasCount(1);
        return OrderMoneyChecks.parseInr(value.locator("..").locator("..").innerText());
    }

    private void capture(String name) throws Exception {
        Files.createDirectories(evidence);
        adminPage.waitForCondition(() -> (Boolean)adminPage.evaluate(
                "() => [...document.querySelectorAll('[role=dialog]')].every(el => Number(getComputedStyle(el).opacity) === 1)"));
        adminPage.screenshot(new Page.ScreenshotOptions().setPath(evidence.resolve(name + ".png")).setFullPage(true));
        Files.writeString(evidence.resolve(name + ".txt"), adminPage.locator("body").innerText());
        org.assertj.core.api.Assertions.assertThat((Boolean) adminPage.evaluate(
                "() => document.documentElement.scrollWidth <= innerWidth")).isTrue();
    }

    private void saveFacts(Map<String, Object> facts) throws Exception {
        Files.createDirectories(evidence);
        Files.writeString(evidence.resolve("owned-ticket-disposition.json"),
                (String) customerPage.evaluate("data => JSON.stringify(data, null, 2)", facts));
    }
}
