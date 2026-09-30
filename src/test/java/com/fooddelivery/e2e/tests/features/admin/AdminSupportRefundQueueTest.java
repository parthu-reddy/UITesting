package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminSupportTicketsPage;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("admin")
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
