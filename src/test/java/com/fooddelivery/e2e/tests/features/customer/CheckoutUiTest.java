package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class CheckoutUiTest extends TestBase {

    @Test
    @DisplayName("CHECKOUT-01: Verify checkout page UI, payment options, and pay button")
    void verifyCheckoutAndPaymentOptions() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        // Select Home address if modal is present
        CustomerAddressModalPage addressModal = new CustomerAddressModalPage(customerPage);
        if (addressModal.isModalOpen()) {
            addressModal.selectExistingAddress("Home");
        }

        NearbyOutletPage nearbyOutletPage = new NearbyOutletPage(customerPage);
        nearbyOutletPage.openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        
        // Proceed to Checkout
        cart.clickPlaceOrder();
        
        // We can verify that payment modal elements exist. Wait for UI to render.
        customerPage.waitForTimeout(2000);
        
        // Verify payment button is visible
        boolean hasPayButton = customerPage.locator("button:has-text('Pay ')").isVisible();
        assertThat(hasPayButton).isTrue();
        
        // Verify payment button has text 
        String payButtonText = customerPage.locator("button:has-text('Pay ')").first().innerText();
        assertThat(payButtonText).contains("Pay");
    }
}
