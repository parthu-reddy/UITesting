package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class PartnerOperationsUiTest extends TestBase {

    @Test
    @DisplayName("CAMPAIGN-01: Restaurant accesses campaign management")
    void verifyCampaignManagement() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        
        restaurantPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Ad Campaigns").setExact(true)).click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                restaurantPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Ad Spending History")))
                .isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                restaurantPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("New Campaign").setExact(true)))
                .isVisible();
    }

    @Test
    @DisplayName("EARNINGS-01: Rider views wallet and earnings")
    void verifyRiderEarnings() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        
        boolean isEarningsVisible = riderPage.locator("text=Today’s Earnings").isVisible();
        assertThat(isEarningsVisible).isTrue();
    }
}
