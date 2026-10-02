package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminSupportTicketsPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed resolution coverage for Admin Support Tickets.
 *
 * <p>The administrator authenticates against the deployed application. Once authenticated, the
 * support, refund-resolution, chat, and intervention requests below are fulfilled inside the
 * browser. This proves the production UI's real confirmation and audit payload without changing
 * a shared support ticket or refund.</p>
 */
@Tag("admin")
@Tag("admin-support-refund")
@Tag("browser-routed")
public class AdminSupportTicketResolutionRoutedUiTest extends TestBase {

    private static final String SUPPORT_TICKETS_PATH =
            "/api/v1/internal/admin/orders/intervention/support-tickets";
    private static final String INTERVENTION_PATH = "/api/v1/internal/admin/orders/intervention";
    private static final String REFUND_PATH = "/api/v1/internal/admin/refunds";
    private static final String CHAT_SESSIONS_PATH = "/api/v1/chat/sessions";
    private static final String TICKET_ID = "f1000000-0000-4000-8000-000000000011";
    private static final String ORDER_ID = "f1000000-0000-4000-8000-000000000101";
    private static final String CUSTOMER_ID = "f1000000-0000-4000-8000-000000000201";
    private static final String SESSION_ID = "f1000000-0000-4000-8000-000000000301";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";
    private static final double VERIFIED_QUOTE = 125.50;
    private static final String RESOLUTION_FAILURE = "Fixture resolver rejected the support ticket";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-SAFE-01: cancelled rejection is inert, then a fulfilled rejection stays inside the browser fixture")
    void rejectionConfirmationAndFulfilledResolutionUseOnlyTheFixture() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportResolutionFixture fixture = new SupportResolutionFixture();
        registerFixtureRoutes(fixture);
        AdminSupportTicketsPage support = openFixtureSupportQueue(fixture);

        support.selectTicket(0);
        assertThat(support.isTicketDetailOpen()).isTrue();
        support.fillResolutionNotes("Customer evidence does not support a refund");

        support.openRejectConfirmation();
        assertThat(support.confirmationDialog().getByText("Reject this request?",
                new Locator.GetByTextOptions().setExact(true)).isVisible()).isTrue();
        support.cancelConfirmation();
        assertThat(fixture.resolutionWrites.get())
                .as("cancelling an admin support decision cannot call the resolver")
                .isZero();

        support.openRejectConfirmation();
        support.confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Reject request").setExact(true)).click();
        waitFor(() -> fixture.resolutionWrites.get() == 1,
                "the fulfilled browser route receives the confirmed rejection");

        assertThat(fixture.resolutionRequestBody.get())
                .contains("\"approved\":false")
                .contains("\"notes\":\"Customer evidence does not support a refund\"")
                .contains("\"faultType\":\"UNKNOWN\"")
                .doesNotContain("overrideAmount");
        assertThat(fixture.resolutionActor.get())
                .as("the UI retains the authenticated audit actor")
                .isEqualTo(authenticatedAdminId);
        adminPage.getByText("No tickets found", new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        assertThat(fixture.supportReads.get()).isGreaterThanOrEqualTo(2);
        assertFixtureWasExclusive(fixture);
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-SAFE-02: approval confirms the verified quote and preserves its audit payload")
    void approvalUsesTheVerifiedQuoteAndTheAuthenticatedAuditActor() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportResolutionFixture fixture = new SupportResolutionFixture();
        registerFixtureRoutes(fixture);
        AdminSupportTicketsPage support = openFixtureSupportQueue(fixture);

        support.selectTicket(0);
        assertThat(support.isTicketDetailOpen()).isTrue();
        support.fillResolutionNotes("Verified missing item refund amount");
        adminPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Resolve Ticket").setExact(true)).click();
        Locator confirmation = support.confirmationDialog();
        assertThat(confirmation.getByText("Approve the request for ₹125.50?",
                new Locator.GetByTextOptions().setExact(true)).isVisible()).isTrue();
        confirmation.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Approve request").setExact(true)).click();
        waitFor(() -> fixture.resolutionWrites.get() == 1,
                "the fulfilled browser route receives the confirmed approval");

        assertThat(fixture.resolutionRequestBody.get())
                .contains("\"approved\":true")
                .contains("\"notes\":\"Verified missing item refund amount\"")
                .contains("\"faultType\":\"UNKNOWN\"")
                .contains("\"overrideAmount\":125.5");
        assertThat(fixture.resolutionActor.get()).isEqualTo(authenticatedAdminId);
        adminPage.getByText("No tickets found", new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        assertFixtureWasExclusive(fixture);
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-SAFE-03: a rejected resolver response keeps the selected ticket and audit note ready for recovery")
    void failedResolverKeepsTheSelectedTicketAndAuditNoteOpen() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportResolutionFixture fixture = SupportResolutionFixture.failingResolution();
        registerFixtureRoutes(fixture);
        AdminSupportTicketsPage support = openFixtureSupportQueue(fixture);

        support.selectTicket(0);
        String auditNote = "Evidence needs a corrected refund amount before approval.";
        support.fillResolutionNotes(auditNote);
        support.resolveTicketButton().click();
        Locator confirmation = support.confirmationDialog();
        assertThat(confirmation.getByText("Approve the request for ₹125.50?",
                new Locator.GetByTextOptions().setExact(true)).isVisible()).isTrue();
        confirmation.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Approve request").setExact(true)).click();
        waitFor(() -> fixture.resolutionWrites.get() == 1,
                "the browser fixture receives the failed resolver request");

        PlaywrightAssertions.assertThat(support.statusMessages()).containsText(RESOLUTION_FAILURE);
        assertThat(support.isTicketDetailOpen())
                .as("a rejected resolver response cannot close the selected ticket")
                .isTrue();
        assertThat(support.resolutionNotes().inputValue())
                .as("the operator's audit note remains available for correction and retry")
                .isEqualTo(auditNote);
        assertThat(support.resolveTicketButton().isEnabled())
                .as("the form recovers after the rejected resolver response")
                .isTrue();
        assertThat(support.rejectRequestButton().isEnabled())
                .as("the alternate resolution control also recovers after the rejected resolver response")
                .isTrue();
        assertThat(support.statusMessages().innerText())
                .as("a rejected resolver response cannot be announced as a completed approval")
                .doesNotContain("Ticket successfully approved");
        assertThat(fixture.resolutionRequestBody.get())
                .contains("\"approved\":true")
                .contains("\"notes\":\"" + auditNote + "\"")
                .contains("\"overrideAmount\":125.5");
        assertThat(fixture.resolutionActor.get()).isEqualTo(authenticatedAdminId);
        assertThat(fixture.supportReads.get())
                .as("a failed resolution does not refresh the queue as though the ticket closed")
                .isEqualTo(1);
        assertFixtureWasExclusive(fixture);
    }

    private AdminSupportTicketsPage openFixtureSupportQueue(SupportResolutionFixture fixture) {
        portal.openSupportTab();
        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);
        adminPage.waitForCondition(() -> fixture.supportReads.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(support.isSupportVisible()).isTrue();
        assertThat(support.getTicketCount()).isEqualTo(1);
        return support;
    }

    private void registerFixtureRoutes(SupportResolutionFixture fixture) {
        // Registration happens after normal login. Every post-login REST request for this page
        // remains local, including the portal's intervention-count poll and the embedded chat.
        adminPage.route(url -> url.contains("/api/v1/"), fixture::handleApi);
        adminPage.routeWebSocket(url -> url.contains("/ws/chat"), socket -> {
            fixture.webSocketConnections.incrementAndGet();
            socket.onMessage(frame -> {
                String message = frame.text();
                if (message != null && message.stripLeading().replace("\r\n", "\n").startsWith("CONNECT\n")) {
                    socket.send("CONNECTED\nversion:1.2\n\n\0");
                } else if (message != null && message.contains("destination:/app/chat.send/")) {
                    fixture.sentChatFrames.incrementAndGet();
                }
            });
        });
    }

    private String authenticatedAdminId() {
        Object value = adminPage.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).id");
        assertThat(value).as("normal admin login stores its authenticated actor").isInstanceOf(String.class);
        return (String) value;
    }

    private void waitFor(java.util.function.BooleanSupplier condition, String expectation) {
        adminPage.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(condition.getAsBoolean()).as(expectation).isTrue();
    }

    private static void assertFixtureWasExclusive(SupportResolutionFixture fixture) {
        assertThat(fixture.unexpectedApiRequests.get())
                .as("every post-login API request must be explicitly handled by the local fixture")
                .isZero();
        assertThat(fixture.unexpectedResolverWrites.get()).isZero();
        assertThat(fixture.sentChatFrames.get())
                .as("the resolution test does not send a chat message")
                .isZero();
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

    private static final class SupportResolutionFixture {
        private final String resolutionFailure;
        private final AtomicBoolean resolved = new AtomicBoolean(false);
        private final AtomicInteger supportReads = new AtomicInteger();
        private final AtomicInteger resolutionWrites = new AtomicInteger();
        private final AtomicInteger unexpectedResolverWrites = new AtomicInteger();
        private final AtomicInteger unexpectedApiRequests = new AtomicInteger();
        private final AtomicInteger sentChatFrames = new AtomicInteger();
        private final AtomicInteger webSocketConnections = new AtomicInteger();
        private final AtomicReference<String> resolutionRequestBody = new AtomicReference<>();
        private final AtomicReference<String> resolutionActor = new AtomicReference<>();

        private SupportResolutionFixture() {
            this(null);
        }

        private SupportResolutionFixture(String resolutionFailure) {
            this.resolutionFailure = resolutionFailure;
        }

        private static SupportResolutionFixture failingResolution() {
            return new SupportResolutionFixture(RESOLUTION_FAILURE);
        }

        private void handleApi(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if ("GET".equals(method) && SUPPORT_TICKETS_PATH.equals(path)) {
                supportReads.incrementAndGet();
                fulfillJson(route, 200, supportTicketPage());
                return;
            }
            if ("GET".equals(method) && INTERVENTION_PATH.equals(path)) {
                fulfillJson(route, 200, emptyInterventionPage());
                return;
            }
            if (path.startsWith(REFUND_PATH + "/")) {
                handleResolver(route, request, path);
                return;
            }
            if (path.startsWith(CHAT_SESSIONS_PATH)) {
                handleChat(route, request, path);
                return;
            }

            unexpectedApiRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected admin support fixture request\"}");
        }

        private void handleResolver(Route route, Request request, String path) {
            if ("POST".equals(request.method()) && (REFUND_PATH + "/" + TICKET_ID + "/resolve").equals(path)) {
                resolutionWrites.incrementAndGet();
                String requestBody = request.postData();
                resolutionRequestBody.set(requestBody);
                resolutionActor.set(request.headerValue("X-User-Id"));
                if (resolutionFailure != null) {
                    fulfillJson(route, 409, "{\"message\":\"" + resolutionFailure + "\"}");
                    return;
                }
                resolved.set(true);
                fulfillJson(route, 200, supportTicket(
                        requestBody != null && requestBody.contains("\"approved\":true") ? "RESOLVED" : "REJECTED",
                        true));
                return;
            }
            unexpectedResolverWrites.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected refund resolver request\"}");
        }

        private void handleChat(Route route, Request request, String path) {
            if ("POST".equals(request.method()) && CHAT_SESSIONS_PATH.equals(path)) {
                fulfillJson(route, 201, """
                        {
                          "success": true,
                          "message": "fixture",
                          "data": {
                            "sessionId": "%s",
                            "sessionType": "ORDER",
                            "referenceId": "%s",
                            "isActive": true,
                            "createdAt": "%s",
                            "participants": []
                          },
                          "timestamp": "%s"
                        }
                        """.formatted(SESSION_ID, ORDER_ID, FIXTURE_TIME, FIXTURE_TIME));
                return;
            }
            if ("GET".equals(request.method()) && (CHAT_SESSIONS_PATH + "/" + SESSION_ID + "/messages").equals(path)) {
                fulfillJson(route, 200, """
                        {
                          "success": true,
                          "message": "fixture",
                          "data": {
                            "content": [], "totalElements": 0, "totalPages": 1,
                            "last": true, "size": 50, "number": 0,
                            "first": true, "numberOfElements": 0, "empty": true
                          },
                          "timestamp": "%s"
                        }
                        """.formatted(FIXTURE_TIME));
                return;
            }
            unexpectedApiRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected chat request in support fixture\"}");
        }

        private String supportTicketPage() {
            String content = resolved.get() ? "" : supportTicket("OPEN", false);
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
                    """.formatted(content, resolved.get() ? 0 : 1, resolved.get() ? 0 : 1, resolved.get());
        }

        private static String emptyInterventionPage() {
            return """
                    {
                      "content": [], "totalElements": 0, "totalPages": 1,
                      "last": true, "size": 20, "number": 0,
                      "first": true, "numberOfElements": 0, "empty": true
                    }
                    """;
        }

        private static String supportTicket(String status, boolean includeResolvedAt) {
            String resolvedAt = includeResolvedAt ? ", \"resolvedAt\": \"" + FIXTURE_TIME + "\"" : "";
            return """
                    {
                      "id": "%s",
                      "orderId": "%s",
                      "customerId": "%s",
                      "reason": "Fixture missing item support request",
                      "status": "%s",
                      "refundAmount": %s,
                      "createdAt": "%s"%s
                    }
                    """.formatted(TICKET_ID, ORDER_ID, CUSTOMER_ID, status, VERIFIED_QUOTE, FIXTURE_TIME, resolvedAt);
        }
    }
}
