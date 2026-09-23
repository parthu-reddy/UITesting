package com.fooddelivery.e2e.tests.features.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Proves the three operational roles remain isolated in simultaneous browser contexts. */
@Tag("ui-only")
public class ResilienceRegressionUiTest extends TestBase {

    @Test
    @DisplayName("ISOLATION-05: Fresh browser context starts unauthenticated")
    void freshContextStartsAtRoleSelector() {
        customerPage.navigate(TestConfig.APP_URL);

        assertThat(customerPage.locator("button:has-text('Order Food'):visible").first()).isVisible();
        assertThat(customerPage.locator("button:has-text('Restaurant Partner'):visible").first()).isVisible();
        assertThat(customerPage.locator("button:has-text('Delivery Executive'):visible").first()).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
    }

    @Test
    @DisplayName("ISOLATION-01: Customer, restaurant and rider sessions stay isolated")
    void allOperationalRolesRemainIsolated() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();

        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
        assertThat(restaurantPage.getByText("Menu Stock Toggles",
                new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^(Offline|Online Duty)$"))).first())
                .isVisible();

        assertThat(customerPage.getByText("Menu Stock Toggles",
                new Page.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).hasCount(0);
    }
}
