package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.pages.common.Portal;
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
@Tag("feature-cart-checkout")
public class CustomerCartTest extends TestBase {

    @Test
    @DisplayName("CART-14-16: Items from two restaurants remain in independent carts")
    void twoRestaurantsCreateIndependentCartsWithoutReplacementDialog() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
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

        // Each outlet heading belongs to its own Surface, which contains that cart's rows,
        // item total and Checkout. Change Brand 1 while Brand 2 stays exactly unchanged.
        Locator firstGroup = cart.getByText(firstOutlet,
                new Locator.GetByTextOptions().setExact(true)).locator("xpath=../..");
        Locator secondGroup = cart.getByText(secondOutlet,
                new Locator.GetByTextOptions().setExact(true)).locator("xpath=../..");
        assertThat(firstGroup.locator("output")).hasText("1");
        assertThat(secondGroup.locator("output")).hasText("1");
        String secondSubtotal = subtotal(secondGroup).innerText();
        firstGroup.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName("Add one " + firstItemName).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(firstGroup.locator("output")).hasText("2");
        assertThat(secondGroup.locator("output")).hasText("1");
        assertThat(subtotal(secondGroup)).hasText(secondSubtotal);
        assertThat(secondGroup.getByText(secondItemName,
                new Locator.GetByTextOptions().setExact(true))).isVisible();

        firstGroup.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName("Remove one " + firstItemName).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(firstGroup.locator("output")).hasText("1");
        firstGroup.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName("Remove one " + firstItemName).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.getByText(firstOutlet,
                new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(secondGroup.locator("output")).hasText("1");
        assertThat(subtotal(secondGroup)).hasText(secondSubtotal);
        assertThat(cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName("Checkout").setExact(true))).hasCount(1);

    }

    private Locator subtotal(Locator group) {
        return group.getByText("Item total", new Locator.GetByTextOptions().setExact(true))
                .locator("..").locator("span").last();
    }

    private Locator firstOrderableItem() {
        Locator item = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(item).isVisible();
        return customerPage.locator("[data-menu-item=\"" + item.getAttribute("data-menu-item") + "\"]");
    }
}
