package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Shared login form validation exercised independently in each role context. */
@Tag("login")
public class LoginValidationTest extends TestBase {
    private Page pageFor(LoginSmokeTest.Account account) {
        return switch (account) {
            case CUSTOMER -> customerPage;
            case RESTAURANT -> restaurantPage;
            case DELIVERY -> riderPage;
            case ADMIN -> adminPage;
        };
    }
    private LoginPage open(Page page, LoginSmokeTest.Account account) {
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        login.selectRole(account.label);
        return login;
    }

    @ParameterizedTest(name = "{0}: phone required, digits only, maximum ten digits")
    @EnumSource(LoginSmokeTest.Account.class)
    void phoneValidation(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        LoginPage login = open(page, account);
        List<String> requests = new ArrayList<>();
        page.onRequest(r -> { if (r.url().contains("/auth/initiate")) requests.add(r.url()); });
        login.clickSendOtp();
        var phone = page.getByPlaceholder("9876543210");
        assertThat(phone.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(phone).isFocused();
        assertThat(page.getByPlaceholder("- - - - - -")).isHidden();
        login.fillPhoneNumber("abc" + account.phone + "999");
        assertThat(phone).hasValue(account.phone);
        assertThat(requests).isEmpty();
    }

    @ParameterizedTest(name = "{0}: empty OTP blocked, input normalized, Back returns to phone")
    @EnumSource(LoginSmokeTest.Account.class)
    void otpValidationAndBack(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        LoginPage login = open(page, account);
        login.fillPhoneNumber(account.phone);
        login.clickSendOtp();
        login.waitForOtpInput();
        List<String> requests = new ArrayList<>();
        page.onRequest(r -> { if (r.url().contains("/auth/verify")) requests.add(r.url()); });
        login.clickVerifyAndLogin();
        var otp = page.getByPlaceholder("- - - - - -");
        assertThat(otp.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(otp).isFocused();
        login.fillOtp("ab12-345678");
        assertThat(otp).hasValue("123456");
        login.clickBackButton();
        assertThat(page.getByPlaceholder("9876543210")).hasValue(account.phone);
        assertThat(otp).isHidden();
        assertThat(requests).isEmpty();
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
    }

    @ParameterizedTest(name = "{0}: resent OTP authenticates the selected account")
    @EnumSource(LoginSmokeTest.Account.class)
    void resendOtp(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        LoginPage login = open(page, account);
        login.fillPhoneNumber(account.phone);
        login.clickSendOtp();
        login.waitForOtpInput();
        // Wait for the second dev OTP fetch to avoid autofilling stale React state.
        Response fetched = page.waitForResponse(
                r -> r.url().contains("/auth/admin/otp") && r.request().method().equals("GET"),
                login::clickResendSmsCode);
        assertThat(fetched.ok()).isTrue();
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("SMS GATEWAY"))).waitFor();
        login.clickAutofillCode();
        assertThat(page.getByPlaceholder("- - - - - -")).hasValue(Pattern.compile("[0-9]{6}"));
        Response verified = page.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(verified.ok()).isTrue();
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role")).isEqualTo(account.name());
    }
}
