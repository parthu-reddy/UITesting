package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RestaurantUiTest extends TestBase {

    @Test
    @DisplayName("REST-01: Verify Restaurant Dashboard loads and basic elements are visible")
    void verifyRestaurantDashboardUI() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        
        // Wait for page to settle
        restaurantPage.waitForTimeout(2000);
        
        // Verify dashboard header or a main element is visible
        boolean isDashboardVisible = restaurantPage.locator("text=Active Orders").isVisible() ||
                                     restaurantPage.locator("text=Orders").first().isVisible() ||
                                     restaurantPage.locator("text=Past Orders").first().isVisible();
                                     
        assertThat(isDashboardVisible).isTrue();
    }
}
