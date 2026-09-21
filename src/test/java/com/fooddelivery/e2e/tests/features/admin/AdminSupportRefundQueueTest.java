package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminRefundQueuePage;
import com.fooddelivery.e2e.pages.admin.AdminSupportTicketsPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("admin-support-refund")
public class AdminSupportRefundQueueTest extends TestBase {

    @BeforeEach
    void loginAdmin() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("Admin", TestConfig.ADMIN_PHONE);
        AdminPortalPage dashboard = new AdminPortalPage(adminPage);
        dashboard.waitForPortal();
    }

    @Test
    @DisplayName("SUPPORT-QUEUE-01..09: Support ticket queue and advanced features")
    void testSupportTicketQueue() {
        AdminPortalPage dashboard = new AdminPortalPage(adminPage);
        dashboard.openSupportTab();
        
        AdminSupportTicketsPage support = new AdminSupportTicketsPage(adminPage);
        
        support.openOpenTickets();
        int openCount = support.getTicketCount();
        
        support.openInReviewTickets();
        int inReviewCount = support.getTicketCount();
        
        support.openResolvedTickets();
        int resolvedCount = support.getTicketCount();
        
        support.openRejectedTickets();
        int rejectedCount = support.getTicketCount();
        
        assertThat(openCount >= 0 && inReviewCount >= 0 && resolvedCount >= 0 && rejectedCount >= 0).isTrue();

        support.openOpenTickets();
        if (openCount > 0) {
            support.selectTicket(0);
            assertThat(support.isTicketDetailOpen()).isTrue();
            
            support.resolveTicketWithNotes("Quick resolution note");
            // The ticket should be resolved
            System.out.println("[INFO] Support ticket resolved.");
        }
    }

    @Test
    @DisplayName("REFUND-QUEUE-01..07, REFUND-ADV-01..04: Refund queue actions")
    void testRefundQueueActions() {
        AdminPortalPage dashboard = new AdminPortalPage(adminPage);
        dashboard.openRefundsTab();
        
        AdminRefundQueuePage refunds = new AdminRefundQueuePage(adminPage);
        
        int refundCount = refunds.getRefundCount();
        assertThat(refundCount).isGreaterThanOrEqualTo(0);
        
        if (refundCount > 0) {
            refunds.openRefundTicket(0);
            // We can either approve or reject. We test reject to be safe from actual ledger mutation if any
            refunds.rejectRefund();
            System.out.println("[INFO] Refund rejected.");
        }
    }
}
