package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
@Tag("feature-restaurant-orders")
@Tag("feature-shell")
public class RestaurantUiTest extends TestBase {

    @Test
    @DisplayName("REST-01: Verify Restaurant Dashboard loads and basic elements are visible")
    void verifyRestaurantDashboardUI() {
        restaurantPage.navigate(TestConfig.APP_URL);
        // 9000000011..13 have no approved brand: they get the application page, not this dashboard
        // (PortalLauncherUiTest, UnapprovedOutletHiddenTest). 14 is approved with an inactive outlet.
        if (testRestaurantPhone.equals("9000000014")) {
            var brands = restaurantPage.waitForResponse(
                    r -> com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/brands")
                            && r.request().method().equals("GET"),
                    () -> new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT));
            assertThat(brands.status()).isEqualTo(200);
            assertThat(restaurantPage.evaluate("body => JSON.parse(body).success", brands.text())).isEqualTo(true);
            assertThat(((Number) restaurantPage.evaluate("body => JSON.parse(body).data.length", brands.text())).intValue())
                    .isEqualTo(1);
        } else {
            new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        }
        
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                restaurantPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Orders").setExact(true))).isVisible();
        switch (testRestaurantPhone) {
            case "9000000014" -> {
                restaurantPage.getByRole(AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Outlet").setExact(true)).click();
                restaurantPage.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName("E2E Inactive Outlet").setExact(true)).click();
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        restaurantPage.getByRole(AriaRole.BUTTON,
                                new Page.GetByRoleOptions().setName("Inactive").setExact(true)))
                        .hasAttribute("aria-pressed", "false");
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        restaurantPage.getByText("STORE OFFLINE",
                                new Page.GetByTextOptions().setExact(true))).isVisible();
            }
            default -> { /* Seeded baseline dashboard asserted above. */ }
        }
    }



    @Test
    @DisplayName("REST-02: Verify Restaurant Routing persists on page reload")
    void verifyRestaurantRoutingPersistence() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone, "Test Restaurant", "restaurant@example.com").openPortal(Portal.RESTAURANT);
        
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        
        // Dismiss any dashboard overlays
        restaurantPage.keyboard().press("Escape");
        restaurantPage.waitForTimeout(500);
        
        // Reload the page
        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        
        // Verify URL is STILL /restaurant after reload
        assertThat(restaurantPage.url()).contains("/restaurant");
        
        // Verify dashboard is still visible
        dashboard.waitForDashboard();
    }
}
