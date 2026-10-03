package com.fooddelivery.e2e.tests.features.restaurant;

import com.microsoft.playwright.Locator;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class PartnerOperationsUiTest extends TestBase {

    @Test
    @DisplayName("CAMPAIGN-01: Restaurant reaches the Campaigns tab")
    void verifyCampaignManagement() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        // Which screen depends on whether this owner advertises yet; RestaurantCampaignsLiveTest checks
        // that it is the right one against /api/v1/advertisers/me.
        dashboard.openCampaignsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(dashboard.campaignsScreen()).isVisible();
    }

    @Test
    @DisplayName("EARNINGS-01: Rider views wallet and earnings")
    void verifyRiderEarnings() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        
        Locator earnings = riderPage.locator("text=Today’s Earnings").first();
        earnings.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        assertThat(earnings.isVisible()).isTrue();
    }
}
