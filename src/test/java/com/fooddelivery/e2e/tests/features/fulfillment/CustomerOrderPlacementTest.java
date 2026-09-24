package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class CustomerOrderPlacementTest extends TestBase {

    @Test
    @DisplayName("CUSTOMER-01: Customer can successfully place an order")
    void customerPlacesOrderSuccessfully() {
        String uniqueRiderPhone = "7000000001";
        String uniqueCustomerPhone = testCustomerPhone;

        // Ensure rider is online to satisfy backend availability checks
        com.fooddelivery.e2e.util.StateSetupHelper.ensureRiderIsOnline(riderPage, uniqueRiderPhone);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", uniqueCustomerPhone);
        
        CustomerHomePage customerHome = new CustomerHomePage(customerPage);
        customerHome.selectAddress("Home");
        customerHome.openRestaurant("Brand 1");
        
        CustomerMenuViewPage customerMenu = new CustomerMenuViewPage(customerPage);
        customerMenu.selectNearestOutlet();
        customerMenu.addQuickPrepItemToCart();
        customerMenu.clickViewCart();
        
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();
        
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        tracker.waitForTracker();
        
        String orderId = tracker.getOrderId();
        assertThat(orderId).isNotEmpty();
    }
}
