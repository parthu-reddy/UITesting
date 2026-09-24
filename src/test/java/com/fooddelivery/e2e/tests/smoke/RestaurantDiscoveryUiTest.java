package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Read-only restaurant-browser coverage; no cart or order is created. */
@Tag("catalog-ui")
public class RestaurantDiscoveryUiTest extends TestBase {

    private Locator openCustomerHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        Locator cards = customerPage.locator("button:has(h5)");
        cards.first().waitFor();
        return cards;
    }

    @Test
    void multipleRestaurantBrandsAndDisplayedDistancesAreValid() {
        Locator cards = openCustomerHome();
        assertThat(cards.count()).isGreaterThanOrEqualTo(2);

        Set<String> brands = new HashSet<>();
        Pattern distancePattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km");
        for (int index = 0; index < cards.count(); index++) {
            Locator card = cards.nth(index);
            String brand = card.locator("h5").innerText().trim();
            assertThat(brand).isNotEmpty();
            brands.add(brand);

            Matcher distance = distancePattern.matcher(card.innerText());
            assertThat(distance.find()).as("restaurant card %s displays a km distance", index).isTrue();
            assertThat(Double.parseDouble(distance.group(1))).isGreaterThanOrEqualTo(0);
        }
        assertThat(brands).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void searchNoResultsAndClearRestoreRestaurantCards() {
        Locator cards = openCustomerHome();
        int initialCount = cards.count();
        String brand = cards.first().locator("h5").innerText().trim();
        Locator search = customerPage.getByPlaceholder("Search restaurants or cuisines");

        search.fill(brand);
        customerPage.waitForTimeout(400);
        assertThat(cards.count()).isGreaterThan(0).isLessThanOrEqualTo(initialCount);
        for (int index = 0; index < cards.count(); index++) {
            assertThat(cards.nth(index).locator("h5").innerText()).containsIgnoringCase(brand);
        }

        search.fill("xyzqwerty123-no-kitchen");
        customerPage.waitForTimeout(400);
        assertThat(cards.count()).isZero();
        assertThat(customerPage.getByText("No Kitchens Found").isVisible()).isTrue();

        customerPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Clear Filters").setExact(true)).click();
        cards.first().waitFor();
        assertThat(search.inputValue()).isEmpty();
        assertThat(cards.count()).isEqualTo(initialCount);
    }

    @Test
    void categoryFilterCanBeSelectedAndCleared() {
        Locator cards = openCustomerHome();
        int initialCount = cards.count();
        // Chips are the cuisines on the list, in a group labelled "Filter by cuisine". The row is
        // drawn only when there are at least two cuisines to choose between.
        Locator filterSection = customerPage.getByRole(com.microsoft.playwright.options.AriaRole.GROUP,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Filter by cuisine"));
        Locator categoryButtons = filterSection.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON);
        assertThat(categoryButtons.count()).isGreaterThanOrEqualTo(2);

        Locator category = categoryButtons.filter(new Locator.FilterOptions().setHasNotText("All")).first();
        String categoryName = category.innerText().trim();
        assertThat(categoryName).isNotEmpty();
        category.click();
        org.junit.jupiter.api.Assertions.assertEquals("true", category.getAttribute("aria-pressed"));
        assertThat(cards.count()).isLessThanOrEqualTo(initialCount);

        Locator all = categoryButtons.filter(new Locator.FilterOptions().setHasText(Pattern.compile("^All$"))).first();
        all.click();
        org.junit.jupiter.api.Assertions.assertEquals("true", all.getAttribute("aria-pressed"));
        cards.first().waitFor();
        assertThat(cards.count()).isGreaterThan(0).isLessThanOrEqualTo(initialCount);
    }
}
