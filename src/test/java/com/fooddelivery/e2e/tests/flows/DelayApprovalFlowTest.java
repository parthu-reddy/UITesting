package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import org.junit.jupiter.api.*;

/**
 * Tests the delay approval flow: Restaurant requests delay → Customer approves/rejects.
 */
@Tag("flow")
public class DelayApprovalFlowTest extends TestBase {

    @Test
    @DisplayName("Restaurant requests delay → Customer approves")
    void restaurantRequestsDelayCustomerApproves() {
        // Login both
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        // Removed wait as selectHomeFromOpenDialog handles it

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", TestConfig.RESTAURANT_PHONE);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        // Customer places order, restaurant accepts and requests delay
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        new CustomerCartDrawerPage(customerPage).checkout();
        customerPage.waitForTimeout(3000);

        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        actions.acceptOrder();
        actions.requestDelay();

        // Customer approves delay
        customerPage.reload();
        customerPage.waitForTimeout(3000);
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        tracker.approveDelay();
        customerPage.waitForTimeout(2000);
    }
}
