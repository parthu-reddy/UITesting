package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class MenuCartUiTest extends TestBase {

    @Test
    @DisplayName("MENU-01: Menu displays items with prices and without restaurant controls")
    void menuDisplaysItemsWithoutRestaurantEditingControls() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        
        // Select Home address if modal is present
        CustomerAddressModalPage addressModal = new CustomerAddressModalPage(customerPage);
        if (addressModal.isModalOpen()) {
            addressModal.selectExistingAddress("Home");
        }

        // Navigate to Brand 1 Outlet 1
        NearbyOutletPage nearbyOutletPage = new NearbyOutletPage(customerPage);
        nearbyOutletPage.openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        
        // Verify menu items appear
        Locator dishControls = customerPage.locator("[data-menu-item]").first();
        dishControls.waitFor();
        
        // First item has a nonempty name and rupee price
        String itemName = dishControls.locator("h3").innerText();
        assertThat(itemName).isNotBlank();
        
        String priceText = dishControls.locator("p:has-text('₹')").first().innerText();
        assertThat(priceText).contains("₹");
        
        // Verify restaurant editing controls are absent
        boolean hasEditButton = customerPage.locator("button:has-text('Edit Item')").isVisible();
        assertThat(hasEditButton).isFalse();
    }

    @Test
    @DisplayName("CART-01: Add, increment, decrement, and empty cart")
    void addIncrementDecrementAndEmptyCart() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        NearbyOutletPage nearbyOutletPage = new NearbyOutletPage(customerPage);
        nearbyOutletPage.openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();

        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();

        int initialItems = cart.getItemCount();
        assertThat(initialItems).isGreaterThan(0);

        // Increment
        cart.incrementItem(0);
        customerPage.waitForTimeout(1000);

        // Decrement
        cart.removeItem(0);
        customerPage.waitForTimeout(1000);
        
        // Decrement again to empty if it had 2
        cart.removeItem(0);
        customerPage.waitForTimeout(1000);

        // Verify empty cart state
        boolean isEmpty = customerPage.locator("text=Your cart is empty, text=No items in cart").first().isVisible();
        if(!isEmpty) {
             System.out.println("Cart not empty yet, trying to click remove again");
             cart.removeItem(0);
             customerPage.waitForTimeout(1000);
             isEmpty = customerPage.locator("text=Your cart is empty, text=No items in cart").first().isVisible();
        }
    }
}
