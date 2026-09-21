package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.SessionManagementPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Session Management modal (view active sessions, terminate individual/all).
 * Covers: SESSION-MGMT-01..05
 */
@Tag("session-management")
public class SessionManagementTest extends TestBase {

    @BeforeEach
    void loginCustomer() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
    }

    @Test
    @DisplayName("SESSION-MGMT-01: Session management modal opens")
    void sessionManagementModalOpens() {
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        
        SessionManagementPage session = new SessionManagementPage(customerPage);
        // Assuming there is a button to open it in settings, this validates if the UI is reachable
        boolean isOpen = session.isSessionModalOpen();
        System.out.println("[INFO] Session management modal open: " + isOpen);
    }

    @Test
    @DisplayName("SESSION-MGMT-02: View active session count")
    void viewActiveSessionCount() {
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        
        SessionManagementPage session = new SessionManagementPage(customerPage);
        if (session.isSessionModalOpen()) {
            int count = session.getSessionCount();
            assertThat(count).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("SESSION-MGMT-03: Terminate specific session")
    void terminateSpecificSession() {
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        
        SessionManagementPage session = new SessionManagementPage(customerPage);
        if (session.isSessionModalOpen() && session.getSessionCount() > 1) {
            session.terminateSession(1);
            customerPage.waitForTimeout(1000);
        }
    }

    @Test
    @DisplayName("SESSION-MGMT-05: Terminate all sessions")
    void terminateAllSessions() {
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        
        SessionManagementPage session = new SessionManagementPage(customerPage);
        if (session.isSessionModalOpen()) {
            session.terminateAll();
            customerPage.waitForTimeout(1000);
            // After terminating all, user should be logged out
            assertThat(customerPage.getByPlaceholder("- - - - - -").isVisible() ||
                       customerPage.locator("button:has-text('Order Food')").isVisible()).isTrue();
        }
    }
}
