package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("partner-ui")
public class PartnerReadOnlyUiTest extends TestBase {
    private void login(Page page, String role, String phone) {
        page.navigate(TestConfig.APP_URL); new LoginPage(page).loginAs(role, phone);
    }
    @Test void riderHistoryDateCanBeCleared() {
        login(riderPage,"Delivery Executive",testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Completed Deliveries"))).isVisible();
        Locator date=riderPage.getByLabel("Filter completed deliveries by date");
        date.fill("2000-01-01"); assertThat(date).hasValue("2000-01-01");
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Clear").setExact(true)).click();
        assertThat(date).hasValue("");
    }
    @Test void riderHistoryShowsEmptyStateForOldDate() {
        login(riderPage,"Delivery Executive",testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator date=riderPage.getByLabel("Filter completed deliveries by date");
        date.fill("2000-01-01");
        assertThat(riderPage.getByText("No completed deliveries found.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("Try selecting a different date.",
                new Page.GetByTextOptions().setExact(true))).isVisible();
    }
    @Test void riderCompletedTripShowsRestaurantPayoutAndDate() {
        login(riderPage,"Delivery Executive",testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator trip = riderPage.locator("button").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("ORDER #[0-9a-fA-F]{8}"))).first();
        assertThat(trip).isVisible();
        assertThat(trip).containsText("Delivered");
        assertThat(trip).containsText(Pattern.compile("\\+₹[0-9]+(?:\\.[0-9]{2})?"));
        Locator details = trip.locator("p");
        org.assertj.core.api.Assertions.assertThat(details.nth(1).innerText().trim())
                .as("completed trip restaurant name").isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(details.nth(2).innerText().trim())
                .as("completed trip date").isNotEmpty();
    }
    @Test void riderTodayEarningsIsNonNegativeCurrency() {
        login(riderPage,"Delivery Executive",testRiderPhone);
        Locator earnings = riderPage.getByText("Today’s Earnings",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("span").last();
        assertThat(earnings).hasText(Pattern.compile("^₹[0-9]+(?:\\.[0-9]{2})?$"));
        String amount = earnings.innerText().trim().substring(1).replace(",", "");
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(amount)).isGreaterThanOrEqualTo(0);
    }
    @Test void riderSettingsCanCloseWithoutChanges() {
        login(riderPage,"Delivery Executive",testRiderPhone);
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Rider Settings"))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings").setExact(true)).click();
        assertThat(riderPage.getByText("Today’s Earnings",new Page.GetByTextOptions().setExact(true))).isVisible();
    }
    @Test void restaurantEarningsPanel() {
        login(restaurantPage,"Restaurant Partner",testRestaurantPhone);
        restaurantPage.getByText("Earnings",new Page.GetByTextOptions().setExact(true)).first().click();
        assertThat(restaurantPage.getByText("Net Earnings",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(restaurantPage.getByText("Pending Balance",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(restaurantPage.getByText("Clawbacks",new Page.GetByTextOptions().setExact(true))).isVisible();
    }
    @Test void restaurantProfileCanCloseWithoutChanges() {
        login(restaurantPage,"Restaurant Partner",testRestaurantPhone);
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings").setExact(true)).click();
        assertThat(restaurantPage.getByText("Menu Stock Toggles",new Page.GetByTextOptions().setExact(true)).first()).isVisible();
    }

    @Test void restaurantStockControlsRenderWithoutToggling() {
        login(restaurantPage,"Restaurant Partner",testRestaurantPhone);
        restaurantPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Menu Stock Toggles").setExact(true)).click();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("In-Stock Dish Toggles"))).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.SWITCH).first()).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.SWITCH).first()).hasAttribute("aria-checked",Pattern.compile("true|false"));
    }
    @Test void campaignDraftCanBeCancelled() {
        login(restaurantPage,"Restaurant Partner",testRestaurantPhone);
        restaurantPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Ad Campaigns").setExact(true)).click();
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("New Campaign").setExact(true)).click();
        Locator dialog=restaurantPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog.getByText("New Ad Campaign",new Locator.GetByTextOptions().setExact(true))).isVisible();
        dialog.getByPlaceholder("e.g. Summer Special Boost").fill("Unsaved E2E draft");
        dialog.getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(dialog).isHidden();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Ad Spending History"))).isVisible();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Unsaved E2E draft").setExact(true))).isHidden();
    }
    @Test void riderVerificationAndWalletSectionsRender() {
        login(riderPage,"Delivery Executive",testRiderPhone);
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
        login(riderPage,"Delivery Executive",testRiderPhone);
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
