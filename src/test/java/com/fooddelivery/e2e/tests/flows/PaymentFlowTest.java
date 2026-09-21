package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the COD payment flow.
 */
@Tag("flow")
public class PaymentFlowTest extends TestBase {

    @Test
    @DisplayName("Place COD order and verify payment method badge")
    void codPaymentFlow() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        // wait handled by selectHomeFromOpenDialog

        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        new CustomerCartDrawerPage(customerPage).checkout();
        customerPage.waitForTimeout(3000);

        // Verify payment method is shown
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String paymentInfo = tracker.getPaymentMethod();
        assertThat(paymentInfo).containsIgnoringCase("cod").as("Payment method should show COD");
    }
}
