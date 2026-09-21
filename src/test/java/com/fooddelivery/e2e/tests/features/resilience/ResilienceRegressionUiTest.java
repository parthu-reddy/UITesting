package com.fooddelivery.e2e.tests.features.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class ResilienceRegressionUiTest extends TestBase {

    @Test
    @DisplayName("ISOLATION-01: Verify isolated contexts")
    void verifyIsolatedContexts() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", TestConfig.RESTAURANT_PHONE);

        // Verify that one is Customer and the other is Restaurant
        assertThat(customerPage.locator("text=Deliver to").isVisible()).isTrue();
        assertThat(restaurantPage.locator("text=Incoming").isVisible()).isTrue();
    }

    @Test
    @DisplayName("ISOLATION-02: Customer and rider in separate contexts")
    void verifyCustomerAndRiderIsolated() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", TestConfig.RIDER_PHONE);

        // Verify independent sessions
        assertThat(customerPage.locator("text=Deliver to").isVisible()).isTrue();
        assertThat(riderPage.locator("text=Delivery Dashboard").isVisible() ||
                   riderPage.locator("text=Go Online").isVisible() ||
                   riderPage.locator("text=Complete Setup").isVisible()).isTrue();
    }

    @Test
    @DisplayName("ISOLATION-03: All three roles in separate contexts")
    void verifyAllRolesIsolated() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", TestConfig.RESTAURANT_PHONE);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", TestConfig.RIDER_PHONE);

        // Verify all 3 dashboards independent
        assertThat(customerPage.locator("text=Deliver to").isVisible()).isTrue();
        assertThat(restaurantPage.locator("text=Incoming").isVisible()).isTrue();
        assertThat(riderPage.locator("text=Delivery Dashboard").isVisible() ||
                   riderPage.locator("text=Go Online").isVisible() ||
                   riderPage.locator("text=Complete Setup").isVisible()).isTrue();
    }

    @Test
    @DisplayName("RECOVERY-01: Page Refresh During Checkout")
    void verifyPageRefreshPersistence() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        
        // Simulating the reload
        customerPage.reload();
        
        // Verify customer dashboard loads correctly without needing to re-login
        assertThat(customerPage.locator("text=Deliver to").isVisible()).isTrue();
    }
}
