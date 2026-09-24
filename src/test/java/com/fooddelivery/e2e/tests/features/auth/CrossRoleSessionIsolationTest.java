package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("ui-only")
@Tag("session-isolation")
public class CrossRoleSessionIsolationTest extends TestBase {

    @BeforeEach
    void loginCustomerAndRestaurant() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
    }

    @Test
    @DisplayName("SESSION-12/16/17: Customer and restaurant remain isolated across reload")
    void simultaneousRolesRemainIsolatedAcrossReload() {
        assertCustomerRoleOnly();
        assertRestaurantRoleOnly();

        customerPage.reload();
        restaurantPage.reload();
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        assertCustomerRoleOnly();
        assertRestaurantRoleOnly();
    }

    @Test
    @DisplayName("SESSION-12: Customer logout does not terminate restaurant session")
    void customerLogoutDoesNotAffectRestaurantSession() {
        customerPage.getByTitle("Profile Settings",
                new Page.GetByTitleOptions().setExact(true)).click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Log Out").setExact(true)).click();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Order Food")).first()).isVisible();

        restaurantPage.reload();
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();
        assertRestaurantRoleOnly();
    }

    /** The restaurant board's first column: a <section> named "Incoming, <n> orders" (KanbanColumn.tsx). */
    private static final Page.GetByRoleOptions INCOMING_COLUMN = new Page.GetByRoleOptions()
            .setName(Pattern.compile("^Incoming, \\d+ orders?$"));

    private void assertCustomerRoleOnly() {
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))).first()).containsText("Home:");
        assertThat(customerPage.getByRole(AriaRole.REGION, INCOMING_COLUMN)).hasCount(0);
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Accept Order").setExact(true))).hasCount(0);
    }

    private void assertRestaurantRoleOnly() {
        assertThat(restaurantPage.getByRole(AriaRole.REGION, INCOMING_COLUMN)).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
        assertThat(restaurantPage.getByText("View Cart",
                new Page.GetByTextOptions().setExact(true))).hasCount(0);
    }
}
