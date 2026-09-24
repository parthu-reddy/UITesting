package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** UI contract for the product's independent per-restaurant carts. */
@Tag("ui-only")
@Tag("cart")
public class CustomerCartTest extends TestBase {

    @Test
    @DisplayName("CART-14-16: Items from two restaurants remain in independent carts")
    void twoRestaurantsCreateIndependentCartsWithoutReplacementDialog() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        String firstOutlet = new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand 1");
        Locator firstItem = firstOrderableItem();
        String firstItemName = firstItem.locator("h4").innerText().trim();
        firstItem.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Back to restaurants").setExact(true)).click();
        String secondOutlet = new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand 2");
        Locator secondItem = firstOrderableItem();
        String secondItemName = secondItem.locator("h4").innerText().trim();
        secondItem.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();

        assertThat(customerPage.getByRole(AriaRole.DIALOG)).hasCount(0);
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();

        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();
        assertThat(cart.locator("h4")).hasText("Your Carts");
        assertThat(cart.getByText(firstOutlet, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.getByText(secondOutlet, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.getByText(firstItemName, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.getByText(secondItemName, new Locator.GetByTextOptions().setExact(true))).isVisible();
        // One Checkout per restaurant's cart. The button is named just "Checkout" -- the outlet
        // is the heading of the group it sits in (CustomerCartDrawer.tsx), not part of its name.
        assertThat(cart.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Checkout").setExact(true))).hasCount(2);
        assertThat(cart.getByText("Replace cart", new Locator.GetByTextOptions().setExact(false))).hasCount(0);
    }

    private Locator firstOrderableItem() {
        Locator item = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(item).isVisible();
        return customerPage.locator("[data-menu-item=\"" + item.getAttribute("data-menu-item") + "\"]");
    }
}
