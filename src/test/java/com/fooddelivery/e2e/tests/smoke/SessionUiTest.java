package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** All actions and assertions use rendered UI; no backend access or storage manipulation. */
@Tag("session-ui")
public class SessionUiTest extends TestBase {
    private Page pageFor(LoginSmokeTest.Account account) {
        return switch (account) {
            case CUSTOMER -> customerPage;
            case RESTAURANT -> restaurantPage;
            case DELIVERY -> riderPage;
            case ADMIN -> adminPage;
        };
    }
    
    private String phoneFor(LoginSmokeTest.Account account) {
        return switch (account) {
            case CUSTOMER -> testCustomerPhone;
            case RESTAURANT -> testRestaurantPhone;
            case DELIVERY -> testRiderPhone;
            case ADMIN -> testAdminPhone;
        };
    }
    
    @ParameterizedTest(name = "{0}: reload retains dashboard; logout and reload stay signed out")
    @EnumSource(value = LoginSmokeTest.Account.class, names = {"CUSTOMER", "RESTAURANT", "DELIVERY"})
    void reloadAndLogout(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).loginAs(account.label, phoneStr);
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        if (account == LoginSmokeTest.Account.CUSTOMER)
            new com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage(page).selectHomeFromOpenDialog();
        page.reload();
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        // A separate fresh context must not inherit the authenticated session.
        Page isolated = account == LoginSmokeTest.Account.CUSTOMER ? adminPage : customerPage;
        isolated.navigate(TestConfig.APP_URL);
        assertThat(isolated.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Order Food")).first()).isVisible();
        assertThat(isolated.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isHidden();
        if (account == LoginSmokeTest.Account.ADMIN) {
            // Current admin logout icon has no accessible name; source-grounded fallback.
            page.locator("button:has(svg.lucide-log-out)").click();
        } else {
            if (account == LoginSmokeTest.Account.CUSTOMER) CustomerDashboardPage.openProfileSettings(page);
            else page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                    .setName(account == LoginSmokeTest.Account.DELIVERY ? "Sign Out" : "Log Out").setExact(true)).click();
        }
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Order Food")).first()).isVisible();
        page.reload();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Order Food")).first()).isVisible();
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isHidden();
    }
}
