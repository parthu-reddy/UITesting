package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminRefundPolicyPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed policy coverage for the admin refund queue.
 *
 * <p>The administrator signs in to the deployed application normally. Once authenticated, this
 * class fulfills every request below {@code /api/v1/internal/admin/refunds} in the browser. It
 * therefore exercises the production bundle and its confirmation state without reading or
 * changing a real refund ticket.</p>
 */
@Tag("browser-routed")
@Tag("feature-refunds-support")
public class AdminRefundPolicyRoutedUiTest extends TestBase {

    private static final String REFUND_API_PATH = "/api/v1/internal/admin/refunds";
    private static final String CUSTOMER_ID = "f1000000-0000-4000-8000-000000000001";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";
    private static final String RESOLUTION_FAILURE = "Fixture resolver rejected the refund";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("ADMIN-REFUND-POLICY-01: no, zero, and invalid quotes disable approval while rejection remains available")
    void invalidNoAndZeroQuotesCannotBeApprovedButCanBeRejected() {
        List<TicketFixture> tickets = List.of(
                new TicketFixture("f1000000-0000-4000-8000-000000000011", "Fixture no refund quote", null),
                new TicketFixture("f1000000-0000-4000-8000-000000000012", "Fixture zero refund quote", 0.0),
                new TicketFixture("f1000000-0000-4000-8000-000000000013", "Fixture invalid refund quote", -1.0)
        );
        RefundFixture fixture = new RefundFixture(tickets);
        AdminRefundPolicyPage refunds = openRefundQueue(fixture);

        for (TicketFixture ticket : tickets) {
            refunds.selectTicketWithReason(ticket.reason());
            PlaywrightAssertions.assertThat(refunds.quoteUnavailableAlert()).isVisible();
            PlaywrightAssertions.assertThat(refunds.approvalChoice()).isDisabled();
            PlaywrightAssertions.assertThat(refunds.approveRefundButton()).isDisabled();

            // A bad quote blocks money movement only. It must not strand the support ticket:
            // rejection still opens its ordinary confirmation, which we cancel safely.
            PlaywrightAssertions.assertThat(refunds.rejectionChoice()).isEnabled();
            refunds.rejectionChoice().click();
            PlaywrightAssertions.assertThat(refunds.rejectRefundButton()).isEnabled();
            refunds.rejectRefundButton().click();
            PlaywrightAssertions.assertThat(refunds.confirmationDialog()).isVisible();
            PlaywrightAssertions.assertThat(refunds.confirmationDialog()).containsText("Reject this refund?");
            refunds.cancelConfirmation();
            refunds.closeDetails();
        }

        assertThat(fixture.ticketReads.get()).isGreaterThanOrEqualTo(1);
        assertThat(fixture.resolveWrites.get()).as("canceled rejections never call the resolver").isZero();
        assertThat(fixture.unexpectedRefundRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-REFUND-POLICY-02: quote cap validation, canceled confirmation, and a fulfilled success never reach a live resolver")
    void validQuoteCapsOverrideAndUsesOnlyTheFulfilledResolverResponse() {
        TicketFixture ticket = new TicketFixture(
                "f1000000-0000-4000-8000-000000000021", "Fixture capped refund quote", 125.50);
        RefundFixture fixture = new RefundFixture(List.of(ticket));
        AdminRefundPolicyPage refunds = openRefundQueue(fixture);

        refunds.selectTicketWithReason(ticket.reason());
        PlaywrightAssertions.assertThat(refunds.approvalChoice()).isEnabled();
        assertThat(adminPage.getByText("₹125.50", new Page.GetByTextOptions().setExact(true)).count())
                .as("the verified quote is displayed in rupees")
                .isGreaterThanOrEqualTo(1);

        refunds.overrideAmount().fill("125.51");
        refunds.approveRefundButton().click();
        PlaywrightAssertions.assertThat(refunds.statusMessages()).containsText(
                "The override amount must be a positive amount no greater than the verified refund quote.");
        assertThat(fixture.resolveWrites.get()).as("an amount above the quote cannot open a resolver request").isZero();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).isHidden();

        refunds.overrideAmount().fill("125.50");
        refunds.approveRefundButton().click();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).isVisible();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).containsText("Approve a refund of ₹125.50?");
        refunds.cancelConfirmation();
        assertThat(fixture.resolveWrites.get()).as("canceling confirmation sends no write").isZero();

        // Confirm once only after proving cancellation is inert. The fulfilled route is the sole
        // resolver response available to this browser context; it turns the queue empty locally.
        refunds.approveRefundButton().click();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).isVisible();
        refunds.confirmationDialog().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions()
                                .setName("Approve refund").setExact(true))
                .click();

        adminPage.waitForCondition(() -> fixture.resolveWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        PlaywrightAssertions.assertThat(refunds.statusMessages()).containsText(
                "Ticket f1000000 resolved successfully");
        PlaywrightAssertions.assertThat(refunds.queueEmptyHeading()).isVisible();

        assertThat(fixture.resolveRequestBody.get())
                .contains("\"approved\":true")
                .contains("\"overrideAmount\":125.5");
        assertThat(fixture.resolveUserId.get()).as("the app preserved the authenticated audit actor").isNotBlank();
        assertThat(fixture.unexpectedRefundRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-REFUND-POLICY-03: a failed resolution preserves the operator form and never announces success")
    void failedResolutionKeepsTheAuditNoteAndTicketOpen() {
        TicketFixture ticket = new TicketFixture(
                "f1000000-0000-4000-8000-000000000031", "Fixture resolver failure", 125.50);
        RefundFixture fixture = RefundFixture.failingResolution(List.of(ticket));
        AdminRefundPolicyPage refunds = openRefundQueue(fixture);

        refunds.selectTicketWithReason(ticket.reason());
        String auditNote = "Payment evidence needs a corrected refund amount.";
        refunds.resolutionNotes().fill(auditNote);
        refunds.approveRefundButton().click();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).isVisible();
        refunds.confirmationDialog().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions()
                                .setName("Approve refund").setExact(true))
                .click();

        adminPage.waitForCondition(() -> fixture.resolveWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        PlaywrightAssertions.assertThat(refunds.statusMessages()).containsText(RESOLUTION_FAILURE);
        PlaywrightAssertions.assertThat(refunds.detailsHeading()).isVisible();
        PlaywrightAssertions.assertThat(refunds.resolutionNotes()).isVisible();
        PlaywrightAssertions.assertThat(refunds.approveRefundButton()).isEnabled();
        assertThat(refunds.resolutionNotes().inputValue())
                .as("the operator's audit note remains available after the rejected request")
                .isEqualTo(auditNote);
        assertThat(refunds.statusMessages().innerText())
                .as("a failed resolver response cannot be presented as a closed ticket")
                .doesNotContain("resolved successfully");
        assertThat(fixture.resolveRequestBody.get())
                .contains("\"approved\":true")
                .contains("\"notes\":\"" + auditNote + "\"");
        assertThat(fixture.ticketReads.get())
                .as("a failed resolution must not refetch the queue as though the ticket closed")
                .isEqualTo(1);
        assertThat(fixture.unexpectedRefundRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-REFUND-POLICY-04: a confirmed rejection sends the final audited payload and closes only the browser fixture ticket")
    void confirmedRejectionUsesTheFixtureAndDoesNotInventAnApproval() {
        TicketFixture ticket = new TicketFixture(
                "f1000000-0000-4000-8000-000000000041", "Fixture confirmed rejection", 125.50);
        RefundFixture fixture = new RefundFixture(List.of(ticket));
        AdminRefundPolicyPage refunds = openRefundQueue(fixture);

        refunds.selectTicketWithReason(ticket.reason());
        String auditNote = "The order evidence does not support the requested refund.";
        refunds.resolutionNotes().fill(auditNote);
        refunds.rejectionChoice().click();
        refunds.rejectRefundButton().click();
        PlaywrightAssertions.assertThat(refunds.confirmationDialog()).containsText("Reject this refund?");
        refunds.confirmationDialog().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions()
                                .setName("Reject refund").setExact(true))
                .click();

        adminPage.waitForCondition(() -> fixture.resolveWrites.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        PlaywrightAssertions.assertThat(refunds.statusMessages()).containsText(
                "Ticket f1000000 resolved successfully");
        PlaywrightAssertions.assertThat(refunds.queueEmptyHeading()).isVisible();
        assertThat(fixture.resolveRequestBody.get())
                .contains("\"approved\":false")
                .contains("\"notes\":\"" + auditNote + "\"")
                .contains("\"faultType\":\"UNKNOWN\"")
                .doesNotContain("overrideAmount");
        assertThat(fixture.resolveUserId.get()).as("the app preserves the authenticated audit actor").isNotBlank();
        assertThat(fixture.ticketReads.get())
                .as("a fulfilled rejection closes and locally refetches the fixture queue")
                .isGreaterThanOrEqualTo(2);
        assertThat(fixture.unexpectedRefundRequests.get()).isZero();
    }

    private AdminRefundPolicyPage openRefundQueue(RefundFixture fixture) {
        adminPage.route(AdminRefundPolicyRoutedUiTest::isRefundApiUrl, fixture::handle);
        portal.openRefundsTab();

        AdminRefundPolicyPage refunds = new AdminRefundPolicyPage(adminPage);
        refunds.waitForQueue();
        adminPage.waitForCondition(() -> fixture.ticketReads.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        return refunds;
    }

    private static boolean isRefundApiUrl(String url) {
        return pathOf(url).startsWith(REFUND_API_PATH);
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private record TicketFixture(String id, String reason, Double refundAmount) {
        private String asJson(String status) {
            String amount = refundAmount == null ? "" : ",\n                  \"refundAmount\": " + refundAmount;
            String resolved = ("RESOLVED".equals(status) || "REJECTED".equals(status))
                    ? ",\n                  \"resolvedAt\": \"" + FIXTURE_TIME + "\""
                    : "";
            return """
                    {
                      "id": "%s",
                      "orderId": "%s",
                      "customerId": "%s",
                      "reason": "%s",
                      "status": "%s",
                      "createdAt": "%s"%s%s
                    }
                    """.formatted(id, orderIdFor(id), CUSTOMER_ID, reason, status, FIXTURE_TIME, amount, resolved);
        }
    }

    private static String orderIdFor(String ticketId) {
        return ticketId.substring(0, ticketId.length() - 1) + "9";
    }

    private static final class RefundFixture {
        private final List<TicketFixture> initialTickets;
        private final String resolutionFailure;
        private final AtomicBoolean resolved = new AtomicBoolean(false);
        private final AtomicInteger ticketReads = new AtomicInteger();
        private final AtomicInteger resolveWrites = new AtomicInteger();
        private final AtomicInteger unexpectedRefundRequests = new AtomicInteger();
        private final AtomicReference<String> resolveRequestBody = new AtomicReference<>();
        private final AtomicReference<String> resolveUserId = new AtomicReference<>();

        private RefundFixture(List<TicketFixture> initialTickets) {
            this(initialTickets, null);
        }

        private RefundFixture(List<TicketFixture> initialTickets, String resolutionFailure) {
            this.initialTickets = initialTickets;
            this.resolutionFailure = resolutionFailure;
        }

        private static RefundFixture failingResolution(List<TicketFixture> initialTickets) {
            return new RefundFixture(initialTickets, RESOLUTION_FAILURE);
        }

        private void handle(Route route) {
            Request request = route.request();
            String path = pathOf(request.url());

            if ("GET".equals(request.method()) && REFUND_API_PATH.equals(path)) {
                ticketReads.incrementAndGet();
                fulfillJson(route, 200, ticketPageJson(resolved.get() ? List.of() : initialTickets));
                return;
            }

            if ("POST".equals(request.method()) && isExpectedResolvePath(path)) {
                resolveWrites.incrementAndGet();
                resolveRequestBody.set(request.postData());
                resolveUserId.set(request.headerValue("X-User-Id"));
                if (resolutionFailure != null) {
                    fulfillJson(route, 409, "{\"message\":\"" + resolutionFailure + "\"}");
                    return;
                }
                resolved.set(true);
                String status = request.postData() != null && request.postData().contains("\"approved\":true")
                        ? "RESOLVED" : "REJECTED";
                fulfillJson(route, 200, initialTickets.get(0).asJson(status));
                return;
            }

            // There is deliberately no route.continue()/resume() branch below this API prefix.
            // An unanticipated endpoint is made visible to the test and cannot leave the browser.
            unexpectedRefundRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Blocked by browser refund fixture\"}");
        }

        private boolean isExpectedResolvePath(String path) {
            return initialTickets.size() == 1
                    && path.equals(REFUND_API_PATH + "/" + initialTickets.get(0).id() + "/resolve");
        }

        private static String ticketPageJson(List<TicketFixture> tickets) {
            String content = tickets.stream()
                    .map(ticket -> ticket.asJson("OPEN"))
                    .collect(Collectors.joining(","));
            return """
                    {
                      "content": [%s],
                      "totalElements": %d,
                      "totalPages": 1,
                      "numberOfElements": %d,
                      "first": true,
                      "last": true,
                      "number": 0,
                      "size": 20,
                      "empty": %s
                    }
                    """.formatted(content, tickets.size(), tickets.size(), tickets.isEmpty());
        }
    }
}
