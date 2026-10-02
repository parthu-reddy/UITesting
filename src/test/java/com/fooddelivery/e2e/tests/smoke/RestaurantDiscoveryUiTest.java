package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.regex.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Live browsing and explicit routed UI contracts; no order is created. */
@Tag("catalog-ui")
public class RestaurantDiscoveryUiTest extends TestBase {
    private Response nearby;
    private Locator cards() { return customerPage.locator("button:has(h5)"); }
    private void signInHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
    }
    private Locator openCustomerHome() {
        customerPage.onResponse(r -> {
            if (java.net.URI.create(r.url()).getPath().equals("/api/v1/restaurants/nearby")
                    && r.request().method().equals("GET")) nearby = r;
        });
        signInHome();
        cards().first().waitFor();
        customerPage.waitForCondition(() -> nearby != null);
        assertThat(nearby.status()).isEqualTo(200);
        return cards();
    }
    @Test void multipleRestaurantBrandsAndDisplayedDistancesAreValid() {
        Locator cards = openCustomerHome();
        assertThat(cards.count()).isGreaterThanOrEqualTo(2);
        Matcher radius = Pattern.compile("[?&]radius=([0-9.]+)").matcher(nearby.url());
        assertThat(radius.find()).as("Nearby request declares the discovery radius").isTrue();
        double discoveryRadius = Double.parseDouble(radius.group(1));
        assertThat(discoveryRadius).isPositive();
        int total = ((Number) customerPage.evaluate("text => JSON.parse(text).data.length", nearby.text())).intValue();
        while (cards.count() < total) {
            int before = cards.count();
            cards.last().scrollIntoViewIfNeeded();
            customerPage.waitForCondition(() -> cards.count() > before);
        }
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(cards).hasCount(total);
        Set<String> brands = new HashSet<>();
        for (Locator card : cards.all()) {
            String brand = card.locator("h5").innerText().trim();
            assertThat(brand).isNotEmpty(); brands.add(brand);
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km").matcher(card.innerText());
            assertThat(distance.find()).as("Restaurant displays a km distance").isTrue();
            // Backend rounds to one decimal. Discovery currently requests 10 km; delivery is 5 km.
            assertThat(Double.parseDouble(distance.group(1))).isBetween(0.0, discoveryRadius + 0.05);
        }
        assertThat(brands).hasSizeGreaterThanOrEqualTo(2);
    }
    @Test void everyRestaurantCardHasALoadedNamedCoverImage() {
        assertCoverVisuals(openCustomerHome(), false);
    }
    @Test void searchNoResultsAndClearRestoreRestaurantCards() {
        Locator cards = openCustomerHome();
        String brand = cards.first().locator("h5").innerText().trim();
        Locator search = customerPage.getByPlaceholder("Search restaurants or cuisines");
        search.fill(brand);
        customerPage.waitForCondition(() -> cards.count() > 0 && cards.all().stream()
                .allMatch(card -> card.locator("h5").innerText().toLowerCase().contains(brand.toLowerCase())));
        for (Locator card : cards.all()) assertThat(card.locator("h5").innerText()).containsIgnoringCase(brand);
        search.fill("xyzqwerty123-no-kitchen");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("No Kitchens Found")).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(cards).hasCount(0);
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clear Filters").setExact(true)).click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(search).hasValue("");
        cards.first().waitFor();
        // Lazy rendering resets to six on filter changes; the old page's card count is not the contract.
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(cards.first().locator("h5")).hasText(brand);
    }
    @Test void categoryFilterCanBeSelectedAndCleared() {
        Locator cards = openCustomerHome();
        Locator buttons = customerPage.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions()
                .setName("Filter by cuisine")).getByRole(AriaRole.BUTTON);
        assertThat(buttons.count()).isGreaterThanOrEqualTo(2);
        Locator category = buttons.filter(new Locator.FilterOptions().setHasNotText("All")).first();
        String cuisine = category.innerText().trim();
        assertThat(cuisine).isNotBlank(); category.click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(category).hasAttribute("aria-pressed", "true");
        customerPage.waitForCondition(() -> cards.count() > 0 && cards.all().stream()
                .allMatch(card -> card.locator("p").first().innerText().trim().equals(cuisine)));
        for (Locator card : cards.all()) assertThat(card.locator("p").first().innerText().trim()).isEqualTo(cuisine);
        Locator all = buttons.filter(new Locator.FilterOptions().setHasText(Pattern.compile("^All$"))).first();
        all.click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(all).hasAttribute("aria-pressed", "true");
        cards.first().waitFor();
        assertThat(cards.count()).isGreaterThan(0);
    }
    @Test void cuisineSearchMatchesTheRenderedCuisine() {
        Locator cards = openCustomerHome();
        String cuisine = cards.first().locator("p").first().innerText().trim();
        assertThat(cuisine).isNotBlank();
        customerPage.getByPlaceholder("Search restaurants or cuisines").fill(cuisine);
        customerPage.waitForCondition(() -> cards.count() > 0 && cards.all().stream().allMatch(card ->
                card.locator("p").first().innerText().toLowerCase().contains(cuisine.toLowerCase())));
        for (Locator card : cards.all()) assertThat(card.locator("p").first().innerText()).containsIgnoringCase(cuisine);
    }
    @Test
    @Tag("routed-ui")
    void failedCoverImagesRenderNamedFallbacks() {
        customerPage.route("**/*", route -> {
            if (route.request().resourceType().equals("image")) route.abort(); else route.resume();
        });
        assertCoverVisuals(openCustomerHome(), true);
    }
    @Test
    @Tag("routed-ui")
    void nearbyFailureShowsRetryAndRecoversWithoutClaimingOutOfRange() {
        customerPage.route("**/api/v1/restaurants/nearby?*", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(503).setContentType("application/json")
                .setBody("{\"success\":false,\"message\":\"Test nearby outage\"}")));
        signInHome();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("Couldn't load restaurants")).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("Out of Range", new Page.GetByTextOptions().setExact(true))).hasCount(0);
        customerPage.unroute("**/api/v1/restaurants/nearby?*");
        Response retried = customerPage.waitForResponse(r -> java.net.URI.create(r.url()).getPath().equals("/api/v1/restaurants/nearby"),
                () -> customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Try again").setExact(true)).click());
        assertThat(retried.status()).isEqualTo(200);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(cards().first()).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("Couldn't load restaurants")).hasCount(0);
    }
    @Test
    @Tag("routed-ui")
    void emptyNearbyAreaOffersAnAddressChange() {
        customerPage.route("**/api/v1/restaurants/nearby?*", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setContentType("application/json").setBody("{\"success\":true,\"data\":[]}")));
        signInHome();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("Out of Range", new Page.GetByTextOptions().setExact(true))).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(cards()).hasCount(0);
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change Address").setExact(true)).click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByRole(AriaRole.DIALOG)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b")))).isVisible();
    }
    private void assertCoverVisuals(Locator cards, boolean requireFallback) {
        assertThat(cards.count()).isGreaterThan(0);
        for (Locator card : cards.all()) {
            card.scrollIntoViewIfNeeded();
            customerPage.waitForCondition(() -> {
                Locator image = card.locator("img"), fallback = card.locator("div[role='img'][aria-label]");
                return (!requireFallback && image.count() == 1 && ((Number) image.evaluate("e => e.naturalWidth")).intValue() > 0)
                        || (image.count() == 0 && fallback.count() == 1 && fallback.isVisible());
            });
            if (card.locator("img").count() == 1) assertThat(card.locator("img").getAttribute("alt")).isNotBlank();
            else assertThat(card.locator("div[role='img'][aria-label]").getAttribute("aria-label")).isNotBlank();
        }
    }
}
