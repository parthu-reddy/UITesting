package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests customer cancellation while order is in PENDING_ACCEPTANCE or CREATED status.
 */
@Tag("flow")
public class OrderCancellationFlowTest extends TestBase {

    @Test
    @DisplayName("Customer cancels order before restaurant accepts")
    void customerCancelsBeforeAcceptance() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);

        // Place an order
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();

        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();
        customerPage.waitForTimeout(3000);

        // Cancel the order
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        tracker.cancelOrder();

        // Verify cancellation
        customerPage.waitForTimeout(2000);
        assertThat(customerPage.content()).containsAnyOf("Cancelled", "cancelled", "CANCELLED");
    }
}
