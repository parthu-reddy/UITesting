package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Visible person login and a separate portal choice. Internal token assertions are deferred. */
@Tag("smoke")
@Tag("feature-auth")
public class LoginSmokeTest extends TestBase {
    enum Account {
        CUSTOMER(Portal.CUSTOMER,"Deliver to"), RESTAURANT(Portal.RESTAURANT,"Updates every 5 s"),
        DELIVERY(Portal.DELIVERY,"Trips Completed"), ADMIN(Portal.ADMIN,"Live Operations");
        final Portal portal; final String dashboardText;
        Account(Portal portal,String dashboardText) { this.portal=portal; this.dashboardText=dashboardText; }
    }
    static Stream<Account> accounts() {
        String selected=System.getProperty("login.roles");
        return selected==null?Arrays.stream(Account.values()):Arrays.stream(selected.split(",")).map(String::trim).map(Account::valueOf);
    }
    private Page pageFor(Account account) { return switch(account) {
        case CUSTOMER -> customerPage; case RESTAURANT -> restaurantPage; case DELIVERY -> riderPage; case ADMIN -> adminPage;
    }; }
    private String phoneFor(Account account) { return switch(account) {
        case CUSTOMER -> testCustomerPhone; case RESTAURANT -> testRestaurantPhone; case DELIVERY -> testRiderPhone; case ADMIN -> testAdminPhone;
    }; }
    @org.junit.jupiter.api.Test void isolatedContextsStartWithoutAuthentication() {
        for(Page page:getAllPages()) {
            page.navigate(TestConfig.APP_URL);
            assertThat(page.getByLabel("PHONE NUMBER",new Page.GetByLabelOptions().setExact(true))).isVisible();
            assertThat(page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).hasCount(0);
        }
    }
    @ParameterizedTest(name="{0}: person login opens the chosen portal") @MethodSource("accounts")
    void successfulLogin(Account account) {
        Page page=pageFor(account); LoginPage login=new LoginPage(page);
        if(account==Account.ADMIN) login.login(phoneFor(account),TestConfig.ADMIN_PROFILE_NAME,TestConfig.ADMIN_PROFILE_EMAIL);
        else login.login(phoneFor(account));
        login.openPortal(account.portal);
        assertThat(page.getByText(account.dashboardText,new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).isVisible();
    }
    @ParameterizedTest(name="{0}: rejected person login stays on the OTP form") @MethodSource("accounts")
    void failedLogin(Account account) {
        Page page=pageFor(account); page.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/login");
        LoginPage login=new LoginPage(page); login.fillPhoneNumber(phoneFor(account)); login.clickSendOtp(); login.waitForOtpInput(); login.clickAutofillCode();
        var input=page.getByLabel("ENTER SECURE CODE"); assertThat(input).hasValue(Pattern.compile("[0-9]{6}"));
        boolean inactive="inactive".equals(System.getProperty("login.rejection"));
        if(!inactive) { String code=input.inputValue(); login.fillOtp((code.charAt(0)=='0'?"1":"0")+code.substring(1)); }
        Response response=page.waitForResponse(r -> r.url().endsWith("/api/v1/auth/session")&&r.request().method().equals("POST"),login::clickVerifyAndLogin);
        org.assertj.core.api.Assertions.assertThat(response.status()).isIn(400,401,403,422);
        assertThat(page.getByRole(AriaRole.ALERT).first()).isVisible(); assertThat(input).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).hasCount(0);
    }
}
