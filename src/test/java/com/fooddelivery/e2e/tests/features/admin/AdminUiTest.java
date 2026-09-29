package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class AdminUiTest extends TestBase {

    @Test
    @DisplayName("ADMIN-01: Verify Admin Dashboard loads")
    void verifyAdminDashboardUI() {
        loginAsAdmin();
        
        // The admin shell: its "Admin" heading and the sidebar (AdminPortal.tsx). It lands on
        // the fleet map.
        boolean isAdminDashboardVisible = adminPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Admin").setExact(true)).isVisible()
                && adminPage.getByText("Live Operations", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible();

        assertThat(isAdminDashboardVisible).isTrue();
    }

    @Test
    @DisplayName("ADMIN-02: Verify Admin Routing persists on page reload")
    void verifyAdminRoutingPersistence() {
        loginAsAdmin();
        
        // Navigate to the User Management tab using the sidebar
        new AdminPortalPage(adminPage).openUsersTab();
        
        // Wait for the URL to reflect the React Router path
        adminPage.waitForURL("**/admin/users");
        assertThat(adminPage.url()).contains("/admin/users");
        
        // Verify the User Management view is actually rendered (e.g., looking for the role filter)
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByText("ALL ROLES",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))).isVisible();
        
        // Reload the page
        adminPage.reload();
        new AdminPortalPage(adminPage).waitForPortal();
        
        // Verify URL is STILL /admin/users after reload
        assertThat(adminPage.url()).contains("/admin/users");
        
        // Verify we didn't get kicked back to the default tab and User Management is still rendered
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByText("ALL ROLES",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
