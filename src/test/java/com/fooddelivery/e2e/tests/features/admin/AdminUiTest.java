package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class AdminUiTest extends TestBase {

    @Test
    @DisplayName("ADMIN-01: Verify Admin Dashboard loads")
    void verifyAdminDashboardUI() {
        adminPage.navigate(TestConfig.APP_URL);
        
        // Use loginAs with profile for Admin if needed, or just standard login
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        
        adminPage.waitForTimeout(2000);
        
        // Verify a dashboard element is visible
        boolean isAdminDashboardVisible = adminPage.locator("text=Platform Overview").isVisible() ||
                                          adminPage.locator("text=System Admin").isVisible() ||
                                          adminPage.locator("text=Analytics").isVisible();
                                          
        assertThat(isAdminDashboardVisible).isTrue();
    }

    @Test
    @DisplayName("ADMIN-02: Verify Admin Routing persists on page reload")
    void verifyAdminRoutingPersistence() {
        adminPage.navigate(TestConfig.APP_URL);
        
        // Login as Admin
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        adminPage.waitForTimeout(2000);
        
        // Navigate to the User Management tab using the sidebar
        adminPage.locator("text=User Management").click();
        
        // Wait for the URL to reflect the React Router path
        adminPage.waitForURL("**/admin/users");
        assertThat(adminPage.url()).contains("/admin/users");
        
        // Verify the User Management view is actually rendered (e.g., looking for the role filter)
        assertThat(adminPage.locator("text=ALL ROLES").first().isVisible()).isTrue();
        
        // Reload the page
        adminPage.reload();
        adminPage.waitForTimeout(2000);
        
        // Verify URL is STILL /admin/users after reload
        assertThat(adminPage.url()).contains("/admin/users");
        
        // Verify we didn't get kicked back to the default tab and User Management is still rendered
        assertThat(adminPage.locator("text=ALL ROLES").first().isVisible()).isTrue();
    }
}
