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
        assertThat(riderPage.getByText("Verification Status",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Earnings Wallet"))).isVisible();
        assertThat(riderPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Sign Out").setExact(true))).isVisible();
    }
}
