package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests restaurant rejecting/cancelling an order.
 */
@Tag("flow")
public class RestaurantRejectFlowTest extends TestBase {

    @Test
    @DisplayName("Restaurant cancels order → Customer sees cancelled")
    void restaurantCancelsOrder() {
        // Login customer
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        // wait handled by selectHomeFromOpenDialog

        // Login restaurant
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        // Customer places order
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();
        customerPage.waitForTimeout(3000);

        // Restaurant cancels
        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        actions.cancelOrder();

        // Customer sees cancelled
        customerPage.reload();
        customerPage.waitForTimeout(3000);
        assertThat(customerPage.content()).containsAnyOf("Cancelled", "cancelled", "failed");
    }
}
