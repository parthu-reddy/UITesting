package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RestaurantNavigationUiTest extends TestBase {

    @Test
    @DisplayName("REST-NAV-01-05/07: Restaurant sections render without state bleed")
    void restaurantSectionsRender() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner",
                testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        clickTab(Pattern.compile("^Orders.*"));
        waitVisible(restaurantPage.getByText("Incoming", new Page.GetByTextOptions().setExact(true)));

        clickTab(Pattern.compile("^Menu$"));
        waitVisible(restaurantPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("Today.s menu"))));

        clickTab(Pattern.compile("^Campaigns$"));
        waitVisible(restaurantPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Ad Spending History")));

        clickTab(Pattern.compile("Earnings"));
        waitVisible(restaurantPage.getByText("Net Earnings", new Page.GetByTextOptions().setExact(true)));

        clickTab(Pattern.compile("Reviews"));
        waitVisible(restaurantPage.getByRole(AriaRole.REGION,
                new Page.GetByRoleOptions().setName("What customers said")));

        clickTab(Pattern.compile("^Orders.*"));
        waitVisible(restaurantPage.getByText("In the kitchen", new Page.GetByTextOptions().setExact(true)));
    }

    private void clickTab(Pattern name) {
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(name)).click();
    }

    private void waitVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        assertThat(locator.isVisible()).isTrue();
    }
}
