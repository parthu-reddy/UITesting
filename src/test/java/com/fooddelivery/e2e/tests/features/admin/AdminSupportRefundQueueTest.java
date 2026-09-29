package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminRefundQueuePage;
import com.fooddelivery.e2e.pages.admin.AdminSupportTicketsPage;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("admin-support-refund")
public class AdminSupportRefundQueueTest extends TestBase {

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("SUPPORT-QUEUE-01..09: Each support status returns a rendered queue or explicit empty state")
    void supportTicketQueue() {
        Response initial = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=OPEN")
                                && "GET".equals(r.request().method()),
                portal::openSupportTab);
        assertThat(initial.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);

        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);
        assertThat(support.isSupportVisible()).isTrue();
        assertSupportState(support, "OPEN");

        checkSupportStatus(support, "IN_REVIEW", support::openInReviewTickets);
        checkSupportStatus(support, "RESOLVED", support::openResolvedTickets);
        checkSupportStatus(support, "REJECTED", support::openRejectedTickets);
        checkSupportStatus(support, "OPEN", support::openOpenTickets);
    }

    @Test
    @DisplayName("SUPPORT-ADV: Reject confirmation can be canceled without resolving a live ticket")
    void rejectSupportTicketConfirmationCanBeCanceled() {
        Response initial = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=OPEN")
                                && "GET".equals(r.request().method()),
                portal::openSupportTab);
        assertThat(initial.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);

        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);
        support.openOpenTickets();
        int count = support.getTicketCount();
        assumeTrue(count > 0, "No open support-ticket fixture is available; rejection requires a disposable ticket");

        support.selectTicket(0);
        assertThat(support.isTicketDetailOpen()).isTrue();
        support.fillResolutionNotes("E2E confirmation check; cancel before submitting");

        AtomicInteger resolutionRequests = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("POST".equals(request.method())
                    && request.url().contains("/api/v1/internal/admin/refunds/")
                    && request.url().endsWith("/resolve")) {
                resolutionRequests.incrementAndGet();
            }
        });
        support.openRejectConfirmation();
        assertThat(support.confirmationDialog().isVisible()).isTrue();
        assertThat(support.confirmationDialog().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Reject request").setExact(true))
                .isVisible()).isTrue();
        support.cancelConfirmation();
        assertThat(support.confirmationDialog().isVisible()).isFalse();
        assertThat(resolutionRequests.get()).isZero();
    }

    @Test
    @DisplayName("REFUND-QUEUE-01..07: Refund queue is explicit and both final actions require confirmation")
    void refundQueueActionsRequireConfirmation() {
        Response initial = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/refunds")
                                && r.url().contains("status=OPEN")
                                && "GET".equals(r.request().method()),
                portal::openRefundsTab);
        assertThat(initial.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);

        AdminRefundQueuePage refunds = new AdminRefundQueuePage(adminPage);
        assertThat(refunds.isRefundQueueVisible()).isTrue();
        int refundCount = refunds.getRefundCount();
        if (refundCount == 0) {
            assertThat(refunds.isQueueEmpty()).isTrue();
            assumeTrue(false,
                    "The queue empty state passed, but a disposable refund ticket is required for confirmation coverage");
        }

        refunds.openRefundTicket(0);
        assertThat(adminPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Ticket Details").setExact(true)).isVisible())
                .isTrue();

        AtomicInteger resolutionRequests = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("POST".equals(request.method())
                    && request.url().contains("/api/v1/internal/admin/refunds/")
                    && request.url().endsWith("/resolve")) {
                resolutionRequests.incrementAndGet();
            }
        });

        refunds.openResolutionConfirmation(true);
        assertTrue(refunds.confirmationDialog().isVisible());
        refunds.cancelConfirmation();
        assertThat(refunds.confirmationDialog().isVisible()).isFalse();

        refunds.openResolutionConfirmation(false);
        assertTrue(refunds.confirmationDialog().isVisible());
        refunds.cancelConfirmation();
        assertThat(refunds.confirmationDialog().isVisible()).isFalse();
        assertThat(resolutionRequests.get()).isZero();
    }

    private void checkSupportStatus(AdminSupportTicketsPage support, String status, Runnable openTab) {
        String queryStatus = status;
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention/support-tickets")
                                && r.url().contains("status=" + queryStatus)
                                && "GET".equals(r.request().method()),
                openTab);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);
        assertSupportState(support, status);
    }

    private void assertSupportState(AdminSupportTicketsPage support, String status) {
        String header = support.getStatusHeader();
        assertThat(header).startsWith(status.replace('_', ' ') + " Tickets (");
        int declaredCount = Integer.parseInt(header.replaceAll("^.*Tickets \\(|\\)$", ""));
        assertThat(support.getTicketCount()).isEqualTo(declaredCount);
        if (declaredCount == 0) {
            assertThat(support.isEmptyStateVisible()).isTrue();
        } else {
            assertThat(support.isEmptyStateVisible()).isFalse();
        }
    }
}
