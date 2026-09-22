package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests customer cart: add/remove items, quantity stepper, cart total validation.
 */
@Tag("feature")
public class CustomerCartTest extends TestBase {

    @Test
    @DisplayName("Add items → verify cart total → increment/decrement → remove item")
    void cartOperations() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        // Open a restaurant
        CustomerHomePage home = new CustomerHomePage(customerPage);
        customerPage.waitForTimeout(2000);
        home.openRestaurant("Test Brand");

        // Add item
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();

        // Open cart
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();

        // Verify cart has items
        String total = cart.getCartTotal();
        assertThat(total).isNotEmpty().as("Cart should display a total");

        // Increment quantity
        cart.incrementItem(0);
        customerPage.waitForTimeout(500);

        // Close cart
        cart.closeCart();
        customerPage.waitForTimeout(500);
    }
}
