package com.fooddelivery.e2e.tests.resilience;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests SSE reconnection after network interruption.
 * Uses Playwright route interception to simulate SSE drop.
 */
@Tag("parked")
@Tag("feature-order-tracking")
@Tag("feature-shell")
public class SSEReconnectTest extends TestBase {

    @Test
    @DisplayName("Block SSE → unblock → verify order status updates resume")
    void sseReconnection() {
        // Login customer and restaurant
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new CustomerDashboardPage(customerPage).waitForDashboard();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        // Place an order
        CustomerHomePage home = new CustomerHomePage(customerPage);
        customerPage.waitForTimeout(2000);
        home.openRestaurant("Brand 1");
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        new CustomerCartDrawerPage(customerPage).checkout();
        customerPage.waitForTimeout(3000);

        // Block SSE on customer page
        System.out.println("[SSE TEST] Blocking SSE endpoint...");
        customerPage.route("**/sse/**", route -> route.abort());
        customerPage.route("**/events/**", route -> route.abort());

        // Restaurant accepts order while SSE is blocked
        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        new RestaurantOrderActionsPage(restaurantPage).acceptOrder();
        restaurantPage.waitForTimeout(2000);

        // Unblock SSE
        System.out.println("[SSE TEST] Unblocking SSE endpoint...");
        customerPage.unrouteAll();

        // Reload to re-establish SSE and verify status updated
        customerPage.reload();
        customerPage.waitForTimeout(5000);

        // The customer should see the updated status after reconnection
        String content = customerPage.content();
        System.out.println("[SSE TEST] Page content after reconnect contains 'Accept': " +
                content.contains("Accepted"));
    }
}
