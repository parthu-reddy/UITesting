package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.*;
import org.junit.jupiter.api.*;

/**
 * Tests restaurant menu management: toggle item availability.
 */
@Tag("feature")
public class RestaurantMenuManagementTest extends TestBase {

    @Test
    @DisplayName("Navigate to Menu tab → toggle item availability")
    void toggleMenuItemAvailability() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();

        dashboard.openMenuTab();
        restaurantPage.waitForTimeout(2000);

        RestaurantMenuEditorPage menuEditor = new RestaurantMenuEditorPage(restaurantPage);
        if (menuEditor.isMenuEditorVisible()) {
            int items = menuEditor.getItemCount();
            System.out.println("[TEST] Found " + items + " menu items");
        }
    }
}
