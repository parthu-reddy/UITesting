package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("cart-ui")
public class MenuCartUiTest extends TestBase {
    @BeforeEach
    void openMenu() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        assertThat(customerPage.getByText("Menu items", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
    }
    @Test
    void menuDisplaysItemsWithoutRestaurantEditingControls() {
        Locator first = customerPage.locator("[data-menu-item]").first();
        assertThat(first.locator("h4")).not().isEmpty();
        assertThat(first).containsText("₹");
        assertThat(customerPage.locator("[data-menu-item] input[type=checkbox]")).hasCount(0);
        assertThat(customerPage.locator("[data-menu-item] button[aria-label^='Edit ']")).hasCount(0);
    }
    @Test
    void outOfStockItemsCannotBeAdded() {
        Locator unavailable = customerPage.locator("[data-menu-item]")
                .filter(new Locator.FilterOptions().setHasText("Out of stock"));
        assertThat(unavailable.first()).isVisible();
        assertThat(unavailable.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true))).hasCount(0);
        assertThat(unavailable.locator("output")).hasCount(0);
    }
    @Test
    void addIncrementDecrementAndEmptyCart() {
        Locator row = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        // Lack of an orderable item is a recorded environment/data failure, never silently skipped.
        assertThat(row).isVisible();
        String itemId = row.getAttribute("data-menu-item");
        row = customerPage.locator("[data-menu-item=\"" + itemId + "\"]");
        String name = row.locator("h4").innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();
        assertThat(cart.getByText(name, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasText("1");
        // UI cart mutations intentionally ignore events within 50ms; use a normal-duration click.
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.locator("output")).hasText("2");
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.locator("output")).hasText("1");
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true)).click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.getByText("Your cart is empty", new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasCount(0);
    }

    private Locator firstOrderableItem() {
        Locator choice = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(choice).isVisible();
        return customerPage.locator("[data-menu-item=\"" + choice.getAttribute("data-menu-item") + "\"]");
    }
    @Test void removeOnlyItemFromCart() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true))
                .click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.getByText("Your cart is empty", new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasCount(0);
    }
    @Test void menuQuantityControlsAndRemoval() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true))
                .click(new Locator.ClickOptions().setDelay(100));
        assertThat(row.locator("output")).hasText("2");
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true))
                .click(new Locator.ClickOptions().setDelay(100));
        assertThat(row.locator("output")).hasText("1");
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true))
                .click(new Locator.ClickOptions().setDelay(100));
        assertThat(row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true))).isVisible();
        assertThat(customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true))).isHidden();
    }
    @Test void cartSubtotalAndCloseReopen() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        String unitPrice = cart.getByText(name, new Locator.GetByTextOptions().setExact(true)).locator("..").locator("p").innerText();
        assertThat(cart.getByText("Subtotal", new Locator.GetByTextOptions().setExact(true)).locator("..")).containsText(unitPrice);
        assertThat(cart.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("Delivering To"))).containsText("Home:");
        cart.locator("button:has(svg.lucide-x)").click();
        assertThat(cart).isHidden();
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        assertThat(cart.getByText(name, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasText("1");
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove one " + name).setExact(true))
                .click(new Locator.ClickOptions().setDelay(100));
        assertThat(cart.getByText("Your cart is empty", new Locator.GetByTextOptions().setExact(true))).isVisible();
    }
}
