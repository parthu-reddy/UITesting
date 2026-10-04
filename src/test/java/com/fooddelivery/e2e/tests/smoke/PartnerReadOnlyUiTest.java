package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("partner-ui")
public class PartnerReadOnlyUiTest extends TestBase {
    private void login(Page page, Portal role, String phone) {
        page.navigate(TestConfig.APP_URL); new LoginPage(page).login(phone).openPortal(role);
    }
    @Test void riderHistoryDateCanBeCleared() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Completed Deliveries"))).isVisible();
        Locator date=riderPage.getByLabel("Filter completed deliveries by date");
        date.fill("2000-01-01"); assertThat(date).hasValue("2000-01-01");
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Clear").setExact(true)).click();
        assertThat(date).hasValue("");
    }
    @Test void riderHistoryShowsEmptyStateForOldDate() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator date=riderPage.getByLabel("Filter completed deliveries by date");
        date.fill("2000-01-01");
        assertThat(riderPage.getByText("No completed deliveries found.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("Try selecting a different date.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
    }
    // The history assertion provisions its own completed order through the real three-actor flow;
    // this is the one test in this UI-smoke class that writes order and rider-payout history.
    @Test void riderCompletedTripShowsRestaurantPayoutAndDate() {
        CompletedDeliveryFixture.Result deliveredOrder = CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Online Duty$")))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator trip = riderPage.locator("button").filter(new Locator.FilterOptions()
                .setHasText("ORDER #" + deliveredOrder.shortOrderId()))
                .filter(new Locator.FilterOptions().setHasText("Delivered"));
        trip.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(trip).containsText("Delivered");
        java.util.regex.Matcher payout = Pattern.compile("\\+₹([0-9,]+(?:\\.[0-9]{2})?)")
                .matcher(trip.innerText());
        org.assertj.core.api.Assertions.assertThat(payout.find())
                .as("completed trip shows the rider payout").isTrue();
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(payout.group(1).replace(",", "")))
                .as("completed trip payout is positive").isPositive();
        Locator details = trip.locator("p");
        org.assertj.core.api.Assertions.assertThat(details.nth(1).innerText().trim())
                .as("completed trip restaurant name").isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(details.nth(2).innerText().trim())
                .as("completed trip date").isNotEmpty();
    }
    @Test void riderTodayEarningsIsNonNegativeCurrency() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        Locator earnings = riderPage.getByText(Pattern.compile("^(Today’s earnings|Paid today)$"))
                .locator("..").locator("span").nth(1);
        assertThat(earnings).hasText(Pattern.compile("^₹[0-9,]+(?:\\.[0-9]{2})?$"));
        String amount = earnings.innerText().trim().substring(1).replace(",", "");
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(amount)).isGreaterThanOrEqualTo(0);
    }
    @Test void riderSettingsCanCloseWithoutChanges() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Rider Settings"))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings").setExact(true)).click();
        assertThat(riderPage.getByText(Pattern.compile("^(Today’s earnings|Paid today)$"))).isVisible();
    }
    @Test void restaurantEarningsPanel() {
        login(restaurantPage,Portal.RESTAURANT,testRestaurantPhone);
        restaurantPage.getByText("Earnings",new Page.GetByTextOptions().setExact(true)).first().click();
        assertThat(restaurantPage.getByText("Net Earnings",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(restaurantPage.getByText("Pending Balance",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(restaurantPage.getByText("Clawbacks",new Page.GetByTextOptions().setExact(true))).isVisible();
    }
    @Test void restaurantProfileCanCloseWithoutChanges() {
        login(restaurantPage,Portal.RESTAURANT,testRestaurantPhone);
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings").setExact(true)).click();
        assertThat(restaurantPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Menu").setExact(true))).isVisible();
    }

    @Test void restaurantStockControlsRenderWithoutToggling() {
        login(restaurantPage,Portal.RESTAURANT,testRestaurantPhone);
        restaurantPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Menu").setExact(true)).click();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Today.s menu")))).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.SWITCH).first()).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.SWITCH).first()).hasAttribute("aria-checked",Pattern.compile("true|false"));
    }
    // The unsaved-draft check moved to RestaurantCampaignsLiveTest.draftIsDiscardedOnCancel: the
    // New Campaign button exists only once the owner has an advertiser, which this smoke cannot know.
    @Test void riderVerificationAndWalletSectionsRender() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Document Verification"))).isVisible();
        Locator verification = riderPage.getByText("Verification Status",
                new Page.GetByTextOptions().setExact(true)).locator("..");
        assertThat(verification).containsText(Pattern.compile("Documents: (Approved|Pending)"));
        assertThat(verification).containsText(Pattern.compile("Bank: (Approved|Pending)"));
        Locator phone = riderPage.locator("input[type=tel]");
        assertThat(phone).hasValue(testRiderPhone);
        assertThat(phone).isDisabled();
        Locator walletHeading = riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Earnings Wallet"));
        assertThat(walletHeading).isVisible();
        Locator balance = walletHeading.locator("..").locator("span").last();
        assertThat(balance).hasText(Pattern.compile("^₹[0-9]+(?:\\.[0-9]{2})?$"));
        String amount = balance.innerText().trim().substring(1).replace(",", "");
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(amount)).isGreaterThanOrEqualTo(0);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Sign Out").setExact(true))).isVisible();
    }
    @Test void riderProfileValuesArePopulatedWithoutEditing() {
        login(riderPage,Portal.DELIVERY,testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(riderPage.locator("input[type=text]").first()).not().hasValue("");
        assertThat(riderPage.locator("input[type=email]").first()).not().hasValue("");
        assertThat(riderPage.getByPlaceholder("e.g. KA01AB1234")).not().hasValue("");
        Locator vehicleType = riderPage.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Vehicle Type").setExact(true));
        assertThat(vehicleType).isVisible();
        assertThat(vehicleType).not().hasText("");
    }
}
