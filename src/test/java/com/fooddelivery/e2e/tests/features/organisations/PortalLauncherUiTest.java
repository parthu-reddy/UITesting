package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("ui-only")
@Tag("feature-organisations")
public class PortalLauncherUiTest extends TestBase {
    @Test void ownerSwitchesApprovedPortalsAndSeesDeliveryStart() {
        new LoginPage(restaurantPage).login("9000000001"); PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage);
        launcher.open(); launcher.state(Portal.CUSTOMER, "Available"); launcher.state(Portal.RESTAURANT, "Available");
        launcher.state(Portal.BUSINESS, "Available"); launcher.state(Portal.DELIVERY, "Get started"); launcher.choose(Portal.RESTAURANT);
        launcher.open(); launcher.choose(Portal.BUSINESS); assertThat(restaurantPage.getByTestId("organisation-role")).containsText("Owner");
        launcher.open(); launcher.choose(Portal.CUSTOMER);
    }
    @Test void restaurantWithoutBrandIsInvitedToStart() {
        new LoginPage(restaurantPage).login("9000000011"); PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage);
        launcher.open(); launcher.state(Portal.RESTAURANT, "Not started");
        assertThat(launcher.tile(Portal.RESTAURANT).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Get started with a restaurant").setExact(true))).isVisible();
    }
    @Test void pendingRestaurantOpensItsApplicationStatus() {
        new LoginPage(restaurantPage).login("9000000012"); PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage);
        launcher.open(); launcher.state(Portal.RESTAURANT, "In review"); launcher.tile(Portal.RESTAURANT).getByRole(AriaRole.BUTTON).click();
        new RestaurantBrandRegistrationPage(restaurantPage).assertStatus("Awaiting admin review");
    }
    @Test void rejectedRiderSeesActionAndReason() {
        new LoginPage(riderPage).login("7000000032"); PortalLauncherPage launcher = new PortalLauncherPage(riderPage);
        launcher.open(); launcher.state(Portal.DELIVERY, "Action needed");
        assertThat(launcher.tile(Portal.DELIVERY)).containsText("Seeded application requires corrected documents.");
        launcher.tile(Portal.DELIVERY).getByRole(AriaRole.BUTTON).click(); assertThat(riderPage.getByTestId("delivery-application")).isVisible();
        assertThat(riderPage.getByText("Changes requested", new Page.GetByTextOptions().setExact(true))).isVisible();
    }
}
