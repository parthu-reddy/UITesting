package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.*;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests admin user management operations.
 */
@Tag("admin")
@Tag("feature")
public class AdminUserOpsTest extends TestBase {

    @Test
    @DisplayName("Admin navigates to Users → searches → opens detail panel")
    void adminUserManagement() {
        loginAsAdmin();
        AdminPortalPage portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();

        portal.openUsersTab();
        adminPage.waitForTimeout(2000);

        AdminUserManagementPage users = new AdminUserManagementPage(adminPage);
        assertThat(users.isUserListVisible()).isTrue().as("User list should be visible");

        Response search = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/users/by-phone")
                                && r.url().contains("phone=" + testCustomerPhone)
                                && "GET".equals(r.request().method()),
                () -> users.searchUser(testCustomerPhone));
        assertThat(search.status()).isEqualTo(200);
        assertThat(users.getUserCount()).isEqualTo(1);
        assertThat(users.getUserCardText(0)).contains(testCustomerPhone);

        users.selectUser(0);
        assertThat(users.isDetailPanelOpen()).isTrue();
        assertThat(users.getUserId()).matches("^[0-9a-fA-F-]{36}$");
        assertThat(users.getUserPhone()).isEqualTo(testCustomerPhone);
        assertThat(users.getUserRole()).contains("CUSTOMER");
    }
}
