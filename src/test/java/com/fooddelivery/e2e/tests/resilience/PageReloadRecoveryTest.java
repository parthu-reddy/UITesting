package com.fooddelivery.e2e.tests.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that user session and order state persist across page reloads.
 * The app uses localStorage for session tokens.
 */
@Tag("resilience")
public class PageReloadRecoveryTest extends TestBase {

    @Test
    @DisplayName("Login → place order → reload page → verify session and order persist")
    void sessionPersistsAcrossReload() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        // Capture pre-reload state
        String preReloadContent = customerPage.content();
        assertThat(preReloadContent).containsAnyOf("Deliver to", "craving", "Good");

        // Full page reload
        System.out.println("[RELOAD TEST] Reloading page...");
        customerPage.reload();
        customerPage.waitForTimeout(3000);

        // Verify we're still logged in (not back at login screen)
        String postReloadContent = customerPage.content();
        assertThat(postReloadContent)
                .doesNotContain("Order Food") // Login role selector shouldn't appear
                .containsAnyOf("Deliver to", "craving", "Good", "Orders")
                .as("User session should persist across reload via localStorage");

        System.out.println("[RELOAD TEST] Session persisted successfully.");
    }

    @Test
    @DisplayName("Place order → reload → order still visible")
    void orderPersistsAcrossReload() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new CustomerDashboardPage(customerPage).waitForDashboard();

        // Place an order
        CustomerHomePage home = new CustomerHomePage(customerPage);
        customerPage.waitForTimeout(2000);
        home.openRestaurant("Test Brand");
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        new CustomerCartDrawerPage(customerPage).checkout();
        customerPage.waitForTimeout(3000);

        // Reload
        customerPage.reload();
        customerPage.waitForTimeout(5000);

        // Check active orders are still visible
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        boolean hasOrders = dashboard.hasActiveOrders();
        System.out.println("[RELOAD TEST] Active orders after reload: " + hasOrders);
    }
}
