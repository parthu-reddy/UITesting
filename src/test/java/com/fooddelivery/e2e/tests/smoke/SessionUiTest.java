package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** UI-only reload, logout, session isolation and portal access; backend probes are deferred. */
@Tag("session-ui")
public class SessionUiTest extends TestBase {
    private Page pageFor(LoginSmokeTest.Account account) { return switch(account) {
        case CUSTOMER -> customerPage; case RESTAURANT -> restaurantPage; case DELIVERY -> riderPage; case ADMIN -> adminPage;
    }; }
    private String phoneFor(LoginSmokeTest.Account account) { return switch(account) {
        case CUSTOMER -> testCustomerPhone; case RESTAURANT -> testRestaurantPhone; case DELIVERY -> testRiderPhone; case ADMIN -> testAdminPhone;
    }; }
    private void login(Page page,LoginSmokeTest.Account account) {
        LoginPage login=new LoginPage(page);
        if(account==LoginSmokeTest.Account.ADMIN) login.login(phoneFor(account),TestConfig.ADMIN_PROFILE_NAME,TestConfig.ADMIN_PROFILE_EMAIL);
        else login.login(phoneFor(account));
        login.openPortal(account.portal);
    }
    @ParameterizedTest(name="{0}: reload retains the portal; logout stays signed out") @EnumSource(LoginSmokeTest.Account.class)
    void reloadAndLogout(LoginSmokeTest.Account account) {
        Page page=pageFor(account); login(page,account);
        assertThat(page.getByText(account.dashboardText,new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        if(account==LoginSmokeTest.Account.CUSTOMER) new SavedDeliveryAddressPage(page).selectHomeFromOpenDialog();
        page.reload(); assertThat(page.getByText(account.dashboardText,new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        Page isolated=account==LoginSmokeTest.Account.CUSTOMER?adminPage:customerPage;
        isolated.navigate(TestConfig.APP_URL); assertThat(isolated.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
        var logout=page.waitForResponse(r -> r.url().endsWith("/api/v1/auth/logout")&&r.request().method().equals("POST"),() -> {
            if(account==LoginSmokeTest.Account.ADMIN) page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Log out").setExact(true)).click();
            else {
                if(account==LoginSmokeTest.Account.CUSTOMER) new CustomerDashboardPage(page).openSettingsTab();
                else page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
                page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(account==LoginSmokeTest.Account.DELIVERY?"Sign Out":"Log Out").setExact(true)).click();
            }
        });
        org.assertj.core.api.Assertions.assertThat(logout.status()).isEqualTo(200);
        assertThat(page.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible(); page.reload();
        assertThat(page.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).hasCount(0);
    }
    @ParameterizedTest(name="Signed-out deep link: {0}") @ValueSource(strings={"/customer/settings","/restaurant/settings","/delivery/settings","/admin/users","/business","/delivery-onboarding"})
    void unauthenticatedDeepLinksExposeOnlyLogin(String path) {
        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+path); assertThat(customerPage).hasURL(Pattern.compile(".*/login$"));
        assertThat(customerPage.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
    }
    @ParameterizedTest(name="{0}: a partner person can also use Customer") @EnumSource(value=LoginSmokeTest.Account.class,names={"RESTAURANT","DELIVERY","ADMIN"})
    void authenticatedPartnerCanOpenCustomerPortal(LoginSmokeTest.Account account) {
        Page page=pageFor(account); login(page,account); new LoginPage(page).openPortal(Portal.CUSTOMER);
        assertThat(page.getByText("Deliver to",new Page.GetByTextOptions().setExact(true)).first()).isVisible();
    }
    @ParameterizedTest(name="Customer-only person: {0} returns to portal availability") @ValueSource(strings={"/restaurant","/delivery"})
    void customerWithoutApprovalCannotOpenPartnerDashboard(String path) {
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$","")+path); assertThat(customerPage).hasURL(Pattern.compile(".*/portals$"));
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Your portals").setExact(true))).isVisible();
    }
}
