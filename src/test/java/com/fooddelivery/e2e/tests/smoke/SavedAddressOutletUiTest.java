package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.fooddelivery.e2e.pages.customer.CustomerOutletSelectorModalPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Saved-address/outlet selection, cart confirmations and quote binding; no order submission. */
@Tag("catalog-ui")
public class SavedAddressOutletUiTest extends TestBase {



    private SavedDeliveryAddressPage selectInitialHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        SavedDeliveryAddressPage address = new SavedDeliveryAddressPage(customerPage);
        address.selectHomeFromOpenDialog();
        return address;
    }

    private Locator openAddressDialog() {
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog.getByText("Select Delivery Location",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        return dialog;
    }

    @Test
    void existingHomeAndNearbyBrand1Outlet() {
        selectInitialHome();
        String outletName = new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator selected = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(outletName));
        assertThat(selected.locator("svg.lucide-check")).isVisible();
    }

    @Test
    void homeAddressModalCanBeReopenedAndDismissedTwice() {
        selectInitialHome();

        for (int attempt = 0; attempt < 2; attempt++) {
            Locator dialog = openAddressDialog();
            assertThat(dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b")))).isVisible();
            customerPage.keyboard().press("Escape");
            assertThat(dialog).isHidden();
            assertThat(customerPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).containsText("Home:");
        }
    }

    @Test
    void homeAddressPersistsAcrossReload() {
        selectInitialHome();
        customerPage.reload();

        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).containsText("Home:");
    }

    @Test
    void visibleBrand1OutletDistancesAreNumeric() {
        selectInitialHome();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Brand 1\\b"))).first().click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();

        Locator choices = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        assertThat(choices.first()).isVisible();
        for (int index = 0; index < choices.count(); index++) {
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away")
                    .matcher(choices.nth(index).innerText());
            org.assertj.core.api.Assertions.assertThat(distance.find())
                    .as("outlet %s exposes a numeric distance", index).isTrue();
            org.assertj.core.api.Assertions.assertThat(Double.parseDouble(distance.group(1))).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void switchBetweenTwoNearbyBrand1Outlets() {
        selectInitialHome();
        String firstOutlet = new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage)
                .openBrand1AndSelectNearby();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        Locator choices = dialog.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        int alternateIndex = -1;
        String alternateName = null;
        for (int index = 0; index < choices.count(); index++) {
            String text = choices.nth(index).innerText();
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away").matcher(text);
            String name = choices.nth(index).locator("p").first().innerText().trim();
            if (!name.equals(firstOutlet) && distance.find() && Double.parseDouble(distance.group(1)) < 5.0) {
                alternateIndex = index;
                alternateName = name;
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(alternateIndex)
                .as("Brand1 must expose a second outlet below 5 km for outlet switching")
                .isGreaterThanOrEqualTo(0);
        choices.nth(alternateIndex).click();
        assertThat(dialog).isHidden();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator selected = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(alternateName));
        assertThat(selected.locator("svg.lucide-check")).isVisible();
    }

    @Test
    void outletSelectorArrowDownMovesFocus() {
        selectInitialHome();
        new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        Locator choices = dialog.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        assertThat(choices.nth(1)).isVisible();
        choices.first().focus();
        customerPage.keyboard().press("ArrowDown");
        org.assertj.core.api.Assertions.assertThat((Boolean) choices.nth(1)
                .evaluate("element => document.activeElement === element"))
                .as("ArrowDown should move focus to the next outlet option")
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"saved", "gps"})
    void reloadKeepsAddressIdentityAndCoordinatesConsistent(String selection) {
        selectInitialHome();
        Map<String, String> expected = (Map<String, String>) customerPage.evaluate("""
            () => Object.fromEntries(['deliveryAddressId','deliveryAddress','deliveryLat','deliveryLng']
                .map(key => [key, localStorage.getItem(key)]))
            """);
        org.assertj.core.api.Assertions.assertThat(expected.get("deliveryAddressId")).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(expected.get("deliveryLat")).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(expected.get("deliveryLng")).isNotBlank();
        Map<String, String> stale = new HashMap<>(expected);
        if (selection.equals("saved")) {
            stale.put("deliveryAddress", "Stale browser address");stale.put("deliveryLat", "30");stale.put("deliveryLng", "80");
        } else {
            expected.put("deliveryAddressId", "");expected.put("deliveryAddress", "Current Location: E2E reload check");
            stale = new HashMap<>(expected);
        }
        // Only browser state is changed. Saved server addresses are never edited or deleted.
        customerPage.evaluate("values => Object.entries(values).forEach(([key,value]) => localStorage.setItem(key,value))", stale);
        customerPage.reload();
        Locator dialog = openAddressDialog();
        // Waiting for the fetched Home row proves the address response has reached hook state,
        // so an initial persisted GPS label cannot produce a false pass before reconciliation.
        assertThat(dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b")))).isVisible();
        customerPage.keyboard().press("Escape");assertThat(dialog).isHidden();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText(expected.get("deliveryAddress"));
        Map<?, ?> actual = (Map<?, ?>) customerPage.evaluate("""
            () => Object.fromEntries(['deliveryAddressId','deliveryAddress','deliveryLat','deliveryLng']
                .map(key => [key, localStorage.getItem(key)]))
            """);
        org.assertj.core.api.Assertions.assertThat(actual).isEqualTo(expected);
    }

    private final AtomicInteger orderPosts = new AtomicInteger();

    @BeforeEach
    void preventOrderSubmission() {
        customerPage.route("**/api/v1/orders", route -> {
            if (!route.request().method().equals("POST")) { route.resume();return; }
            orderPosts.incrementAndGet();
            route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                    .setBody("{\"success\":false,\"message\":\"Address test must not submit orders\"}"));
        });
    }
    @AfterEach
    void assertNoOrderSubmission() { org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isZero(); }

    private Locator firstOrderableItem() {
        Locator candidate = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(candidate).isVisible();
        return customerPage.locator("[data-menu-item=\"" + candidate.getAttribute("data-menu-item") + "\"]");
    }
    private String addItem() {
        Locator row = firstOrderableItem();String name = row.locator("h4").innerText().trim();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        assertThat(row.locator("output")).hasText("1");return name;
    }
    private Locator cart() {
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();return cart;
    }
    private Locator confirmation(String title) {
        return customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(title).setExact(true));
    }
    private void assertOutlet(String name) {
        assertThat(customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(name).setExact(true))).isVisible();
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancel", "confirm"})
    void outletSwitchWithCartRequiresDecisionAndPreservesOtherCart(String decision) {
        SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        selectInitialHome();
        String otherOutlet = new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand 2");
        String otherItem = addItem();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Back to restaurants").setExact(true)).click();
        String currentOutlet = new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        String currentItem = addItem();
        CustomerOutletSelectorModalPage selector = new CustomerOutletSelectorModalPage(customerPage);
        selector.open();String alternative = null;
        for (Locator choice : selector.choices().all()) {
            String name = choice.locator("p").first().innerText().trim();
            if (!name.equals(currentOutlet) && choice.isEnabled()) { alternative = name;break; }
        }
        org.assertj.core.api.Assertions.assertThat(alternative).as("Second nearby Brand1 outlet fixture").isNotNull();
        selector.selectOutlet(alternative);
        Locator confirm = confirmation("Switch outlet?");assertThat(confirm).isVisible();
        assertThat(confirm).containsText("Your cart from " + currentOutlet + " will be cleared.");
        if (decision.equals("cancel")) {
            confirm.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
            assertThat(confirm).isHidden();assertThat(selector.dialog()).isVisible();
            customerPage.keyboard().press("Escape");assertThat(selector.dialog()).isHidden();
            assertOutlet(currentOutlet);
        } else {
            confirm.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Switch outlet").setExact(true)).click();
            assertThat(confirm).isHidden();assertThat(selector.dialog()).isHidden();assertOutlet(alternative);
        }
        Locator cart = cart();
        assertThat(cart.getByText(otherOutlet, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.getByText(otherItem, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasText(decision.equals("cancel") ? new String[]{"1","1"} : new String[]{"1"});
        if (decision.equals("cancel")) {
            assertThat(cart.getByText(currentOutlet, new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(cart.getByText(currentItem, new Locator.GetByTextOptions().setExact(true))).isVisible();
        } else {
            assertThat(cart.getByText(currentOutlet, new Locator.GetByTextOptions().setExact(true))).hasCount(0);
            assertThat(cart.getByText(currentItem, new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        }
    }

    @Test
    void selectingSameOutletKeepsCartWithoutConfirmation() {
        SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        selectInitialHome();String outlet = new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        String item = addItem();CustomerOutletSelectorModalPage selector = new CustomerOutletSelectorModalPage(customerPage);
        selector.open();selector.selectOutlet(outlet);assertThat(selector.dialog()).isHidden();
        assertThat(confirmation("Switch outlet?")).hasCount(0);assertOutlet(outlet);
        Locator cart = cart();assertThat(cart.getByText(item, new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart.locator("output")).hasText("1");
    }

    @Test
    @Tag("routed-ui")
    void keyboardWrapHomeEndSkipDisabledOutlets() {
        selectInitialHome();
        // Preserve real outlet IDs/names; deterministic distances guarantee a disabled gap.
        // This is a routed keyboard UI contract, not deployed distance computation proof.
        customerPage.route("**/api/v1/restaurants/brands/*/outlets?*", route -> {
            com.microsoft.playwright.APIResponse original = route.fetch();
            org.assertj.core.api.Assertions.assertThat(original.status()).isEqualTo(200);
            String body = (String) customerPage.evaluate("""
                text => {
                    const payload = JSON.parse(text);
                    if (payload.data.length < 3) throw new Error('Keyboard fixture needs three real outlets');
                    payload.data = payload.data.map((outlet,index) => ({...outlet, distance: index === 0 ? 1 : index === 2 ? 2 : 6}));
                    return JSON.stringify(payload);
                }
                """, original.text());
            route.fulfill(new Route.FulfillOptions().setResponse(original).setBody(body));
        });
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        CustomerOutletSelectorModalPage selector = new CustomerOutletSelectorModalPage(customerPage);selector.open();
        Locator enabled = selector.dialog().locator("button:not([disabled])").filter(new Locator.FilterOptions().setHasText("km away"));
        Locator disabled = selector.dialog().locator("button[disabled]").filter(new Locator.FilterOptions().setHasText("km away"));
        org.assertj.core.api.Assertions.assertThat(enabled.count()).isGreaterThan(1);
        org.assertj.core.api.Assertions.assertThat(disabled.count()).as("Far-outlet fixture must exist").isPositive();
        enabled.first().focus();customerPage.keyboard().press("End");assertThat(enabled.last()).isFocused();
        customerPage.keyboard().press("ArrowDown");assertThat(enabled.first()).isFocused();
        customerPage.keyboard().press("ArrowUp");assertThat(enabled.last()).isFocused();
        customerPage.keyboard().press("Home");assertThat(enabled.first()).isFocused();
        for (int i = 1; i < enabled.count(); i++) {
            customerPage.keyboard().press("ArrowDown");assertThat(enabled.nth(i)).isFocused();
        }
        for (Locator option : disabled.all()) { assertThat(option).isDisabled();assertThat(option).not().isFocused(); }
    }


    private boolean quoteForAddress(Response response, String addressId) {
        return response.url().endsWith("/api/v1/orders/quote") && response.request().method().equals("POST")
                && response.request().postData().contains(addressId)
                && response.request().postData().contains("menuItemId");
    }
    private Map<?,?> json(String text) {
        return (Map<?,?>) customerPage.evaluate("text => JSON.parse(text)", text);
    }

    @Test
    void changingSavedAddressConfirmsCartClearAndQuotesNewAddress() {
        try (SeededRiderDuty duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            selectInitialHome();String homeId = (String) customerPage.evaluate("localStorage.getItem('deliveryAddressId')");
            new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
            duty.assertReadyForCheckout();
            final String[] item = new String[1];
            Response initialQuote = customerPage.waitForResponse(r -> quoteForAddress(r, homeId), () -> item[0] = addItem());
            org.assertj.core.api.Assertions.assertThat(initialQuote.status()).isEqualTo(200);
            String initialQuoteId = (String) ((Map<?,?>) json(initialQuote.text()).get("data")).get("quoteId");
            Locator dialog = openAddressDialog();Locator work = dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^Work\\b")));
            assertThat(work).isVisible();work.click();Locator confirm = confirmation("Change delivery address?");
            assertThat(confirm).isVisible();confirm.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
            assertThat(confirm).isHidden();assertThat(dialog).isVisible();customerPage.keyboard().press("Escape");
            org.assertj.core.api.Assertions.assertThat(customerPage.evaluate("localStorage.getItem('deliveryAddressId')")).isEqualTo(homeId);
            Locator oldCart = cart();assertThat(oldCart.getByText(item[0],new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(oldCart.locator("output")).hasText("1");oldCart.locator("button:has(svg.lucide-x)").click();assertThat(oldCart).isHidden();
            dialog = openAddressDialog();dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^Work\\b"))).click();
            Locator change = confirmation("Change delivery address?");assertThat(change).isVisible();
            Response workQuote = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/orders/quote")
                    && r.request().method().equals("POST") && !r.request().postData().contains(homeId),
                    () -> change.getByRole(AriaRole.BUTTON,
                            new Locator.GetByRoleOptions().setName("Change address").setExact(true)).click());
            assertThat(change).isHidden();assertThat(dialog).isHidden();
            assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).containsText("Work:");
            String workId = (String) customerPage.evaluate("localStorage.getItem('deliveryAddressId')");
            org.assertj.core.api.Assertions.assertThat(workId).isNotBlank().isNotEqualTo(homeId);
            org.assertj.core.api.Assertions.assertThat(json(workQuote.request().postData()).get("deliveryAddressId")).isEqualTo(workId);
            // The seeded Work point is outside this Brand1 outlet's service area: a fresh
            // rejection is correct, and must not reuse the successful Home quote.
            org.assertj.core.api.Assertions.assertThat(workQuote.status()).isEqualTo(400);
            org.assertj.core.api.Assertions.assertThat(json(workQuote.text()).get("errorCode")).isEqualTo("OUT_OF_SERVICE_AREA");
            org.assertj.core.api.Assertions.assertThat(customerPage.evaluate("""
                id => Object.values(JSON.parse(localStorage.getItem('food_delivery_carts_v2') || '{}')[id] || {})
                    .every(cart => cart.items.length === 0)
                """, homeId)).isEqualTo(true);
            dialog = openAddressDialog();dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b"))).click();assertThat(dialog).isHidden();
            org.assertj.core.api.Assertions.assertThat(customerPage.evaluate("localStorage.getItem('deliveryAddressId')")).isEqualTo(homeId);
            duty.assertReadyForCheckout();
            Response finalQuote = customerPage.waitForResponse(r -> quoteForAddress(r,homeId), this::addItem);
            org.assertj.core.api.Assertions.assertThat(finalQuote.status()).isEqualTo(200);
            org.assertj.core.api.Assertions.assertThat(json(finalQuote.request().postData()).get("deliveryAddressId")).isEqualTo(homeId);
            Map<?,?> data = (Map<?,?>) json(finalQuote.text()).get("data");
            org.assertj.core.api.Assertions.assertThat((String)data.get("quoteId")).isNotBlank().isNotEqualTo(initialQuoteId);
            Locator finalCart = cart();assertThat(finalCart.getByText(item[0],new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(finalCart.locator("output")).hasText("1");
        }
    }

}
