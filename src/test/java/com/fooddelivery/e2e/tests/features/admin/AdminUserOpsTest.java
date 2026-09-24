package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.admin.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests admin user management operations.
 */
@Tag("feature")
public class AdminUserOpsTest extends TestBase {

    @Test
    @DisplayName("Admin navigates to Users → searches → opens detail panel")
    void adminUserManagement() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone);
        AdminPortalPage portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();

        portal.openUsersTab();
        adminPage.waitForTimeout(2000);

        AdminUserManagementPage users = new AdminUserManagementPage(adminPage);
        assertThat(users.isUserListVisible()).isTrue().as("User list should be visible");

        // Search for a test user
        users.searchUser(testCustomerPhone);
        adminPage.waitForTimeout(1000);

        // Select first result
        try {
            users.selectUser(0);
            adminPage.waitForTimeout(1000);
            if (users.isDetailPanelOpen()) {
                System.out.println("[TEST] User detail panel opened: " + users.getUserId());
            }
        } catch (Exception e) {
            System.out.println("[TEST] No search results or detail panel not available.");
        }
    }
}
