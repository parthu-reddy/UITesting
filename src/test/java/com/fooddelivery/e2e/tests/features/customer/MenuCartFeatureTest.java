package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
@Tag("feature-cart-checkout")
@Tag("feature-catalog")
public class MenuCartFeatureTest extends TestBase {

    @Test
    @DisplayName("MENU-01: Menu displays items with prices and without restaurant controls")
    void menuDisplaysItemsWithoutRestaurantEditingControls() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        // Navigate to Brand 1 Outlet 1
        NearbyOutletPage nearbyOutletPage = new NearbyOutletPage(customerPage);
        nearbyOutletPage.openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        
        // Verify menu items appear
        Locator dishControls = customerPage.locator("[data-menu-item]").first();
        dishControls.waitFor();
        
        // First item has a nonempty name and rupee price
        String itemName = dishControls.locator("h4").innerText();
        assertThat(itemName).isNotBlank();
        
        String priceText = dishControls.locator("span").filter(new Locator.FilterOptions()
                .setHasText(java.util.regex.Pattern.compile("^₹[0-9]"))).first().innerText();
        assertThat(priceText).contains("₹");
        
        // Verify restaurant editing controls are absent
        boolean hasEditButton = customerPage.locator("button:has-text('Edit Item')").isVisible();
        assertThat(hasEditButton).isFalse();
    }

    @Test
    @DisplayName("CART-01: Add, increment, decrement, and empty cart")
    void addIncrementDecrementAndEmptyCart() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        NearbyOutletPage nearbyOutletPage = new NearbyOutletPage(customerPage);
        nearbyOutletPage.openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();

        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();

        assertThat(cart.getItemCount()).isEqualTo(1);
        Locator drawer = customerPage.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(drawer.locator("output")).hasText("1");
        cart.incrementItem(0);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(drawer.locator("output")).hasText("2");
        cart.removeItem(0);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(drawer.locator("output")).hasText("1");
        cart.removeItem(0);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(drawer.getByText("Your cart is empty",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(drawer.locator("output")).hasCount(0);
    }
}
