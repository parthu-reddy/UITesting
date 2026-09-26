package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("cart-ui")
public class MenuCartUiTest extends TestBase {
    private double parseInr(String text) {
        if (text.contains("FREE")) return 0;
        java.util.regex.Matcher amount = Pattern.compile("₹([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        if (!amount.find()) throw new AssertionError("No INR amount in: " + text);
        return Double.parseDouble(amount.group(1).replace(",", ""));
    }
    @BeforeEach
    void openMenu() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
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
    void allMenuItemsHaveNamesPositivePricesAndCategories() {
        Locator rows = customerPage.locator("[data-menu-item]");
        org.assertj.core.api.Assertions.assertThat(rows.count()).isGreaterThan(0);
        for (int index = 0; index < rows.count(); index++) {
            Locator row = rows.nth(index);
            org.assertj.core.api.Assertions.assertThat(row.locator("h4").innerText().trim()).isNotEmpty();
            String price = row.locator("span").filter(new Locator.FilterOptions()
                    .setHasText(Pattern.compile("^₹[0-9]"))).last().innerText().trim();
            org.assertj.core.api.Assertions.assertThat(price).matches("^₹[0-9]+(?:\\.[0-9]{1,2})?$");
            double amount = Double.parseDouble(price.substring(1));
            org.assertj.core.api.Assertions.assertThat(amount).isGreaterThan(0);
            org.assertj.core.api.Assertions.assertThat(row.innerText()).doesNotContain("null", "undefined");
        }

        Locator categories = customerPage.locator("section").filter(new Locator.FilterOptions()
                .setHas(customerPage.locator("[data-menu-item]"))).locator("h5");
        org.assertj.core.api.Assertions.assertThat(categories.count()).isGreaterThan(0);
        for (int index = 0; index < categories.count(); index++) {
            org.assertj.core.api.Assertions.assertThat(categories.nth(index).innerText().trim()).isNotEmpty();
        }
    }

    @Test void menuDescriptionsUseSecondaryTextStyling() {
        Locator descriptions = customerPage.locator("[data-menu-item] p.text-\\[11\\.5px\\]");
        org.assertj.core.api.Assertions.assertThat(descriptions.count())
                .as("seeded menu should expose at least one item description")
                .isGreaterThan(0);
        for (int index = 0; index < descriptions.count(); index++) {
            Locator description = descriptions.nth(index);
            String text = description.innerText().trim();
            org.assertj.core.api.Assertions.assertThat(text)
                    .isNotBlank()
                    .doesNotContain("null", "undefined");
            double descriptionSize = Double.parseDouble(((String) description.evaluate(
                    "element => getComputedStyle(element).fontSize")).replace("px", ""));
            double nameSize = Double.parseDouble(((String) description.locator("xpath=../div[1]/h4")
                    .evaluate("element => getComputedStyle(element).fontSize")).replace("px", ""));
            org.assertj.core.api.Assertions.assertThat(descriptionSize).isLessThan(nameSize);
            org.assertj.core.api.Assertions.assertThat(description.getAttribute("class"))
                    .contains("line-clamp-2");
        }
    }

    @Test void everyMenuItemHasLoadedImageOrFallback() {
        Locator rows = customerPage.locator("[data-menu-item]");
        for (int index = 0; index < rows.count(); index++) {
            Locator image = rows.nth(index).locator("img");
            assertThat(image).hasCount(1);
            assertThat(image).hasAttribute("alt", "");
            int naturalWidth = ((Number) image.evaluate("element => element.naturalWidth")).intValue();
            org.assertj.core.api.Assertions.assertThat(naturalWidth)
                    .as("menu row %s image should load instead of leaving a broken visual", index)
                    .isGreaterThan(0);
        }
    }

    @Test void dietaryMarkersAndPrepTimesNeverInventValues() {
        Locator rows = customerPage.locator("[data-menu-item]");
        int dietaryMarkers = 0;
        int prepTimes = 0;
        for (int index = 0; index < rows.count(); index++) {
            Locator row = rows.nth(index);
            Locator markers = row.locator("[role='img'][aria-label]");
            for (int marker = 0; marker < markers.count(); marker++) {
                org.assertj.core.api.Assertions.assertThat(markers.nth(marker).getAttribute("aria-label"))
                        .isIn("Vegetarian", "Non-vegetarian");
                dietaryMarkers++;
            }
            java.util.regex.Matcher prep = Pattern.compile("Ready in\\s+(\\d+) min")
                    .matcher(row.innerText());
            if (prep.find()) {
                org.assertj.core.api.Assertions.assertThat(Integer.parseInt(prep.group(1))).isPositive();
                prepTimes++;
            }
        }
        org.assertj.core.api.Assertions.assertThat(dietaryMarkers)
                .as("seeded menu should include classified dietary markers").isGreaterThan(0);
        org.assertj.core.api.Assertions.assertThat(prepTimes)
                .as("seeded menu should include explicitly configured preparation times").isGreaterThan(0);
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
        cart.locator("button:has(svg.lucide-x)").click();
        assertThat(cart).isHidden();
        assertThat(customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true))).isHidden();
    }
    @Test void fiveSequentialIncrementsReachQuantitySix() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText().trim();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        Locator increment = row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true));
        for (int click = 0; click < 5; click++) increment.click();
        assertThat(row.locator("output")).hasText("6");
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

    @Test void twoDistinctItemsProduceExactSubtotal() {
        Locator orderable = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true))));
        org.assertj.core.api.Assertions.assertThat(orderable.count()).isGreaterThanOrEqualTo(2);

        Locator first = customerPage.locator("[data-menu-item=\"" + orderable.nth(0).getAttribute("data-menu-item") + "\"]");
        Locator second = customerPage.locator("[data-menu-item=\"" + orderable.nth(1).getAttribute("data-menu-item") + "\"]");
        String firstName = first.locator("h4").innerText().trim();
        String secondName = second.locator("h4").innerText().trim();
        double firstPrice = parseInr(first.locator("span").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^₹[0-9]"))).last().innerText());
        double secondPrice = parseInr(second.locator("span").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^₹[0-9]"))).last().innerText());

        first.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        second.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();

        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart.getByText(firstName, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.getByText(secondName, new Locator.GetByTextOptions().setExact(true))).isVisible();
        String subtotalText = cart.getByText("Subtotal", new Locator.GetByTextOptions().setExact(true))
                .locator("..").locator("span").last().innerText();
        org.assertj.core.api.Assertions.assertThat(parseInr(subtotalText))
                .isEqualTo(firstPrice + secondPrice);
    }

    @Test void cartPersistsAfterSettingsNavigation() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText().trim();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();

        CustomerDashboardPage.openProfileSettings(customerPage);
        assertThat(customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Close settings")).click();

        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart.getByText(name, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasText("1");
    }

    @Test void freeDeliveryTrackerShowsProgressAfterAddingItem() {
        Locator row = firstOrderableItem();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();

        Locator progress = customerPage.getByRole(AriaRole.PROGRESSBAR,
                new Page.GetByRoleOptions().setName("Progress towards free delivery").setExact(true));
        assertThat(progress).isVisible();
        String value = progress.getAttribute("aria-valuenow");
        org.assertj.core.api.Assertions.assertThat(Integer.parseInt(value)).isBetween(0, 100);
        Locator message = customerPage.getByText(Pattern.compile(
                "(?:Add ₹[0-9,.]+ for Free Delivery!|Free Delivery Unlocked!)"));
        assertThat(message.first()).isVisible();
    }

    @Test void freeDeliveryCanBeUnlockedThroughCartAdditions() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText().trim();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        Locator progress = customerPage.getByRole(AriaRole.PROGRESSBAR,
                new Page.GetByRoleOptions().setName("Progress towards free delivery").setExact(true));
        assertThat(progress).isVisible();

        Locator increment = row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true));
        for (int attempt = 0; attempt < 30 && Integer.parseInt(progress.getAttribute("aria-valuenow")) < 100; attempt++) {
            increment.click(new Locator.ClickOptions().setDelay(100));
        }

        assertThat(progress).hasAttribute("aria-valuenow", "100");
        assertThat(customerPage.getByText("Free Delivery Unlocked! 🎉",
                new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    /**
     * The bill is the checkout sheet's, not the cart drawer's. The drawer shows "Item total"
     * only; it used to print its own SGST/CGST estimate, a second bill that disagreed with the
     * server's quote until it landed (CustomerCartDrawer.tsx). The sheet's lines are "Item
     * total", "Delivery fee" (FREE at zero), "Platform fee" (only when charged) and "GST &
     * restaurant charges" (only once quoted), and they must add up to "Total".
     */
    @Test void checkoutTotalEqualsItemTotalFeesAndTaxes() {
        Locator row = firstOrderableItem();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.clickPlaceOrder();
        PaymentModalPage payment = new PaymentModalPage(customerPage);
        org.assertj.core.api.Assertions.assertThat(payment.isPayEnabled())
                .as("the server quote must land before the bill can be checked")
                .isTrue();
        Locator sheet = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Checkout").setExact(true));

        double itemTotal = billLine(sheet, "Item total");
        double delivery = billLine(sheet, "Delivery fee");
        Locator platformLabel = sheet.getByText("Platform fee", new Locator.GetByTextOptions().setExact(true));
        double platform = platformLabel.count() == 0 ? 0 : billLine(sheet, "Platform fee");
        double gst = billLine(sheet, "GST & restaurant charges");
        double total = parseInr(sheet.getByText("Total", new Locator.GetByTextOptions().setExact(true))
                .locator("..").innerText());

        org.assertj.core.api.Assertions.assertThat(total)
                .isCloseTo(itemTotal + delivery + platform + gst,
                        org.assertj.core.data.Offset.offset(0.01));
    }

    /** One AmountBreakdown line: the label sits one level inside the row that holds the amount. */
    private double billLine(Locator sheet, String label) {
        return parseInr(sheet.getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=../..").innerText());
    }
}
