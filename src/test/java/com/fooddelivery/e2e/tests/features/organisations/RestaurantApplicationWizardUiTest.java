package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.BusinessPlatformFixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
@Tag("business-platform") @Tag("bp-o5") @Tag("ui-only")
public class RestaurantApplicationWizardUiTest extends TestBase {
    @Test void approvedApplicationOpensItsOutletWithoutAnotherPersonLogin() {
        String phone = BusinessPlatformFixture.phone("restaurant", "9999");
        String brand = BusinessPlatformFixture.approvedRestaurant(restaurantPage, adminPage, phone, testAdminPhone);
        PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage); launcher.open(); launcher.awaitChangedState(Portal.RESTAURANT, "Available"); launcher.choose(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX, new com.microsoft.playwright.Page.GetByRoleOptions().setName("Outlet").setExact(true))).containsText(brand + " Outlet");
    }
}
