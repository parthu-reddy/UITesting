package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.CheckoutAvailability;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import java.time.Duration;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MenuCartUiTest extends TestBase {
    private String selectedOutlet;

    private double parseInr(String text) {
        if (text.contains("FREE")) return 0;
        java.util.regex.Matcher amount = Pattern.compile("₹([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        if (!amount.find()) throw new AssertionError("No INR amount in: " + text);
        return Double.parseDouble(amount.group(1).replace(",", ""));
    }
    private long parseInrCents(String text) {
        java.util.regex.Matcher amount = Pattern.compile("₹([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        if (!amount.find()) throw new AssertionError("No INR amount in: " + text);
        return new java.math.BigDecimal(amount.group(1).replace(",", "")).movePointRight(2).longValueExact();
    }
    @BeforeEach
    void openMenu() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        selectedOutlet = new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
    }
    @Tag("feature-catalog")
    @Test
    void menuDisplaysItemsWithoutRestaurantEditingControls() {
        Locator first = customerPage.locator("[data-menu-item]").first();
        assertThat(first.locator("h4")).not().isEmpty();
        assertThat(first).containsText("₹");
        assertThat(customerPage.locator("[data-menu-item] input[type=checkbox]")).hasCount(0);
        assertThat(customerPage.locator("[data-menu-item] button[aria-label^='Edit ']")).hasCount(0);
    }
    @Tag("feature-catalog")
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

    @Tag("feature-catalog")
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

    @Tag("feature-catalog")
    @Test void everyMenuItemHasLoadedImageOrFallback() {
        Locator rows = customerPage.locator("[data-menu-item]");
        org.assertj.core.api.Assertions.assertThat(rows.count()).isGreaterThan(0);
        for (int index = 0; index < rows.count(); index++) {
            Locator row = rows.nth(index);
            Locator slot = row.locator("div.relative.overflow-hidden").first();
            slot.scrollIntoViewIfNeeded();
            Locator image = row.locator("img");
            long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
            boolean settled = false;
            while (System.nanoTime() < deadline) {
                if (image.count() == 0) {
                    // ImageLoader removes a failed photo and its failed brand mark, leaving its
                    // visible paper-sunken placeholder in the same image slot.
                    Locator placeholder = slot.locator("div.absolute.inset-0");
                    assertThat(placeholder).isVisible();
                    settled = true;
                    break;
                }
                assertThat(image).hasAttribute("alt", "");
                int naturalWidth = ((Number) image.evaluate("element => element.naturalWidth")).intValue();
                if (naturalWidth > 0) {
                    settled = true;
                    break;
                }
                customerPage.waitForTimeout(100);
            }
            org.assertj.core.api.Assertions.assertThat(settled)
                    .as("menu row %s should load its photo or render ImageLoader's fallback", index)
                    .isTrue();
        }
    }

    @Tag("feature-catalog")
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

    @Tag("feature-cart-checkout")
    @Tag("feature-catalog")
    @Test
    void outOfStockItemsCannotBeAdded() {
        Locator existing = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions().setHasText("Out of stock"));
        if (existing.count() > 0) {
            assertUnavailable(existing.first());
            return;
        }
        Locator orderable = firstOrderableItem();
        String itemId = orderable.getAttribute("data-menu-item");
        String itemName = orderable.locator("h4").innerText().trim();
        // Establish one unavailable item only when no retained fixture exists. Never restore stock.
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login("9000000001").openPortal(Portal.RESTAURANT);
        RestaurantDashboardPage restaurant = new RestaurantDashboardPage(restaurantPage);
        restaurant.waitForDashboard();
        restaurant.selectOutlet(selectedOutlet);
        restaurant.openMenuTab();
        Locator stockSwitch = restaurantPage.getByRole(AriaRole.SWITCH,
                new Page.GetByRoleOptions().setName(itemName + " available").setExact(true));
        assertThat(stockSwitch).isVisible();
        assertThat(stockSwitch).hasAttribute("aria-checked", "true");
        setStockAvailability(stockSwitch, false);
        // The open outlet lives in the URL (/customer/restaurant/:id, useCustomerRoute), so a reload reopens the
        // same menu rather than the feed; this branch ran only on a freshly reset Dev (2026-10-10) and still
        // expected the feed.
        customerPage.reload();
        org.assertj.core.api.Assertions.assertThat(new CustomerMenuViewPage(customerPage).getSelectedOutletName())
                .isEqualTo(selectedOutlet);
        assertUnavailable(customerPage.locator("[data-menu-item=\"" + itemId + "\"]"));
    }

    private void assertUnavailable(Locator unavailable) {
        assertThat(unavailable).isVisible();
        assertThat(unavailable).containsText("Out of stock");
        assertThat(unavailable.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true))).hasCount(0);
        assertThat(unavailable.locator("output")).hasCount(0);
    }

    @Tag("feature-catalog")
    @Test
    void customerOutletSelectorExposesNoManagementActions() {
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet").setExact(true)).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Select Outlet Location").setExact(true));
        assertThat(dialog).isVisible();
        assertThat(dialog.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("edit outlet|manage|delete outlet|add outlet", Pattern.CASE_INSENSITIVE)))).hasCount(0);
        assertThat(dialog.locator("input, [role='switch']")).hasCount(0);
    }

    /**
     * The open outlet lives in the URL (/customer/restaurant/:id, useCustomerRoute), so a reload
     * re-renders that outlet's menu -- through the routed catalog -- instead of the feed.
     */
    private void assertReloadKeepsTheSelectedOutlet() {
        String outletUrl = customerPage.url();
        org.assertj.core.api.Assertions.assertThat(outletUrl).matches(".*/customer/restaurant/[^/?#]+.*");
        customerPage.reload();
        org.assertj.core.api.Assertions.assertThat(customerPage.url()).isEqualTo(outletUrl);
        assertThat(customerPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(selectedOutlet).setExact(true)))
                .isVisible();
    }

    @Tag("feature-catalog")
    @Test
    @Tag("routed-ui")
    void catalogFailureOffersRetryAndRecoversTheSelectedOutlet() {
        customerPage.route("**/api/v1/restaurants/*/catalog/items", route -> route.fulfill(
                new Route.FulfillOptions().setStatus(502).setContentType("application/json")
                        .setBody("{\"success\":false,\"message\":\"Test catalog outage\"}")));
        assertReloadKeepsTheSelectedOutlet();
        assertThat(customerPage.getByText("Couldn't load menu", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.locator("[data-menu-item]")).hasCount(0);
        assertThat(customerPage.getByText("Menu unavailable", new Page.GetByTextOptions().setExact(true))).hasCount(0);
        customerPage.unroute("**/api/v1/restaurants/*/catalog/items");
        Response retried = customerPage.waitForResponse(r -> r.url().endsWith("/catalog/items")
                && r.request().method().equals("GET"), () -> customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Try again").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(retried.status()).isEqualTo(200);
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
        assertThat(customerPage.getByText("Couldn't load menu", new Page.GetByTextOptions().setExact(true))).hasCount(0);
    }

    @Tag("feature-catalog")
    @Test
    @Tag("routed-ui")
    void successfulEmptyCatalogShowsAnExplicitEmptyState() {
        customerPage.route("**/api/v1/restaurants/*/catalog/items", route -> route.fulfill(
                new Route.FulfillOptions().setStatus(200).setContentType("application/json")
                        .setBody("{\"success\":true,\"message\":\"Menu items retrieved\",\"data\":[],\"timestamp\":\"2026-09-29T10:00:00Z\"}")));
        assertReloadKeepsTheSelectedOutlet();
        assertThat(customerPage.getByText("Menu unavailable", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.locator("[data-menu-item]")).hasCount(0);
        assertThat(customerPage.getByText("Couldn't load menu", new Page.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Try again").setExact(true))).hasCount(0);
    }

    private void setStockAvailability(Locator stockSwitch, boolean available) {
        boolean current = Boolean.parseBoolean(stockSwitch.getAttribute("aria-checked"));
        if (current == available) return;

        com.microsoft.playwright.Response response = restaurantPage.waitForResponse(
                // The availability switch sends PUT /outlets/{id}/menu-items/{id}/stock (menuStore.ts setStock);
                // POST /menu-overrides/ is the price/name editor and never fires here.
                candidate -> candidate.request().method().equals("PUT")
                        && candidate.url().matches(".*/api/v1/outlets/[^/]+/menu-items/[^/]+/stock$"),
                stockSwitch::click);
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("restaurant stock update should be accepted before checking customer availability")
                .isBetween(200, 299);
        assertThat(stockSwitch).hasAttribute("aria-checked", Boolean.toString(available));
    }
    @Tag("feature-cart-checkout")
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
        Locator choice = customerPage.locator(
                "[data-menu-item]:has([data-testid='add-to-cart-button'])").first();
        assertThat(choice).isVisible();
        return customerPage.locator("[data-menu-item=\"" + choice.getAttribute("data-menu-item") + "\"]");
    }
    @Tag("feature-cart-checkout")
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
    @Tag("feature-cart-checkout")
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
    @Tag("feature-cart-checkout")
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
    @Tag("feature-cart-checkout")
    @Test void cartSubtotalAndCloseReopen() {
        Locator row = firstOrderableItem();
        String name = row.locator("h4").innerText();
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        String unitPrice = cart.getByText(name, new Locator.GetByTextOptions().setExact(true)).locator("..").locator("p").innerText();
        assertThat(cart.getByText("Item total", new Locator.GetByTextOptions().setExact(true)).locator("..")).containsText(unitPrice);
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

    @Tag("feature-cart-checkout")
    @Test void twoDistinctItemsProduceExactSubtotal() {
        Locator orderable = customerPage.locator(
                "[data-menu-item]:has([data-testid='add-to-cart-button'])");
        orderable.nth(1).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        org.assertj.core.api.Assertions.assertThat(orderable.count()).isGreaterThanOrEqualTo(2);

        Locator first = customerPage.locator("[data-menu-item=\"" + orderable.nth(0).getAttribute("data-menu-item") + "\"]");
        Locator second = customerPage.locator("[data-menu-item=\"" + orderable.nth(1).getAttribute("data-menu-item") + "\"]");
        String firstName = first.locator("h4").innerText().trim();
        String secondName = second.locator("h4").innerText().trim();
        long firstPrice = parseInrCents(first.locator("span").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^₹[0-9]"))).last().innerText());
        long secondPrice = parseInrCents(second.locator("span").filter(new Locator.FilterOptions()
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
        assertThat(cart.locator("output")).hasText(new String[] { "1", "1" });
        for (String itemName : new String[] { firstName, secondName }) {
            Locator itemRow = cart.getByText(itemName, new Locator.GetByTextOptions().setExact(true))
                    .locator("xpath=../..");
            long expectedPrice = itemName.equals(firstName) ? firstPrice : secondPrice;
            org.assertj.core.api.Assertions.assertThat(parseInrCents(itemRow.locator("p").innerText()))
                    .isEqualTo(expectedPrice);
        }
        String subtotalText = cart.getByText("Item total", new Locator.GetByTextOptions().setExact(true))
                .locator("..").locator("span").last().innerText();
        org.assertj.core.api.Assertions.assertThat(parseInrCents(subtotalText))
                .isEqualTo(firstPrice + secondPrice);
    }

    @Tag("feature-cart-checkout")
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

    @Tag("feature-cart-checkout")
    @Test void freeDeliveryTrackerShowsProgressAfterAddingItem() {
        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            Locator row = firstOrderableItem();
            Map<?, ?> quote = successfulQuote(() -> row.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click());
            Locator progress = freeDeliveryProgress();
            double threshold = ((Number) quote.get("minAmountForFreeDelivery")).doubleValue();
            double subtotal = ((Number) quote.get("subtotal")).doubleValue();
            org.assertj.core.api.Assertions.assertThat(threshold).isPositive();
            int expected = (int) Math.round(Math.min(100, subtotal / threshold * 100));
            assertThat(progress).hasAttribute("aria-valuenow", Integer.toString(expected));
            assertThat(customerPage.getByText(Pattern.compile(
                    "(?:Add ₹[0-9,.]+ for Free Delivery!|Free Delivery Unlocked!)")).first()).isVisible();
        }
    }

    @Tag("feature-cart-checkout")
    @Test void freeDeliveryCanBeUnlockedThroughCartAdditions() {
        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            Locator row = firstOrderableItem();
            String name = row.locator("h4").innerText().trim();
            Map<?, ?> quote = successfulQuote(() -> row.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click());
            double threshold = ((Number) quote.get("minAmountForFreeDelivery")).doubleValue();
            double unit = ((Number) quote.get("subtotal")).doubleValue();
            org.assertj.core.api.Assertions.assertThat(threshold).isPositive();
            org.assertj.core.api.Assertions.assertThat(unit).isPositive();
            int requiredQuantity = (int) Math.ceil(threshold / unit);
            org.assertj.core.api.Assertions.assertThat(requiredQuantity)
                    .as("Fixture must reach configured free delivery with at most 50 units").isBetween(1, 50);
            Locator increment = row.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true));
            for (int quantity = 1; quantity < requiredQuantity; quantity++) {
                increment.click(new Locator.ClickOptions().setDelay(100));
                assertThat(row.locator("output")).hasText(Integer.toString(quantity + 1));
            }
            customerPage.waitForCondition(() -> freeDeliveryProgress().count() == 1
                    && "100".equals(freeDeliveryProgress().getAttribute("aria-valuenow")));
            assertThat(freeDeliveryProgress()).hasAttribute("aria-valuenow", "100");
            assertThat(customerPage.getByText("Free Delivery Unlocked! 🎉",
                    new Page.GetByTextOptions().setExact(true))).isVisible();
        }
    }

    private Locator freeDeliveryProgress() {
        return customerPage.getByRole(AriaRole.PROGRESSBAR,
                new Page.GetByRoleOptions().setName("Progress towards free delivery").setExact(true));
    }

    private java.util.Map<?, ?> successfulQuote(Runnable action) {
        Response response = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/orders/quote")
                && r.request().method().equals("POST"), action);
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        java.util.Map<?, ?> body = (java.util.Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", response.text());
        org.assertj.core.api.Assertions.assertThat(body.get("success")).isEqualTo(true);
        return (java.util.Map<?, ?>) body.get("data");
    }

    /**
     * The bill is the checkout sheet's, not the cart drawer's. The drawer shows "Item total"
     * only; it used to print its own SGST/CGST estimate, a second bill that disagreed with the
     * server's quote until it landed (CustomerCartDrawer.tsx). The sheet's lines are "Item
     * total", "Delivery fee" (FREE at zero), "Platform fee" (only when charged) and "GST &
     * restaurant charges" (only once quoted), and they must add up to "Total".
     */
    @Tag("feature-cart-checkout")
    @Test void checkoutTotalEqualsItemTotalFeesAndTaxes() {
        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            Locator row = firstOrderableItem();
            row.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
            customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
            CheckoutAvailability.requireDeliveryAvailable(
                    CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));
            PaymentModalPage payment = new PaymentModalPage(customerPage);
            payment.waitForOpen();
            // The Place order button stays disabled until a payment method is selected. Quote
            // readiness is represented by the final quoted bill lines, not button enablement.
            payment.waitForFinalQuote();
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
    }

    /** One AmountBreakdown line: the label sits one level inside the row that holds the amount. */
    private double billLine(Locator sheet, String label) {
        return parseInr(sheet.getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=../..").innerText());
    }
}
