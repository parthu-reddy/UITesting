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

/** Person-login validation in isolated browser contexts; portal access is chosen separately. */
@Tag("login") @Tag("ui-only")
public class LoginValidationTest extends TestBase {


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
    
    private LoginPage open(Page page, LoginSmokeTest.Account account) {
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        return login;
    }

    @ParameterizedTest(name = "{0}: phone required, digits only, maximum ten digits")
    @EnumSource(LoginSmokeTest.Account.class)
    void phoneValidation(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        LoginPage login = open(page, account);
        List<String> requests = new ArrayList<>();
        page.onRequest(r -> { if (r.url().contains("/api/v1/auth/otp")) requests.add(r.url()); });
        login.clickSendOtp();
        var phone = page.getByPlaceholder("9876543210");
        assertThat(phone.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(phone).isFocused();
        assertThat(page.getByPlaceholder("- - - - - -")).isHidden();
        login.fillPhoneNumber("abc" + phoneStr + "999");
        assertThat(phone).hasValue(phoneStr);
        assertThat(requests).isEmpty();
        // Observe requests emitted by the normal form; never intercept or inject a response.
        for (String shortPhone : new String[] {"1", "1234567", "12345678", "123456789"}) {
            login.fillPhoneNumber(shortPhone);
            login.clickSendOtp();
            var error = page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT);
            page.waitForCondition(() -> error.isVisible() || !requests.isEmpty(),
                    new Page.WaitForConditionOptions().setTimeout(5000));
            assertThat(requests).as("Short phone %s must be rejected before OTP initiation", shortPhone).isEmpty();
            assertThat(error).isVisible();
            assertThat(page.getByPlaceholder("- - - - - -")).isHidden();
        }
    }

    @ParameterizedTest(name = "{0}: empty OTP blocked, input normalized, Back returns to phone")
    @EnumSource(LoginSmokeTest.Account.class)
    void otpValidationAndBack(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        LoginPage login = open(page, account);
        login.fillPhoneNumber(phoneStr);
        login.clickSendOtp();
        login.waitForOtpInput();
        List<String> requests = new ArrayList<>();
        page.onRequest(r -> { if (r.url().contains("/api/v1/auth/session")) requests.add(r.url()); });
        login.clickVerifyAndLogin();
        var otp = page.getByPlaceholder("- - - - - -");
        assertThat(otp.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(otp).isFocused();
        for (String shortOtp : new String[] {"1", "123", "12345"}) {
            login.fillOtp(shortOtp);
            login.clickVerifyAndLogin();
            var error = page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT);
            page.waitForCondition(() -> error.isVisible() || !requests.isEmpty(),
                    new Page.WaitForConditionOptions().setTimeout(5000));
            assertThat(requests).as("A short OTP must not reach verification").isEmpty();
            assertThat(error).isVisible();
        }
        login.fillOtp("ab12-345678");
        assertThat(otp).hasValue("123456");
        login.clickBackButton();
        assertThat(page.getByPlaceholder("9876543210")).hasValue(phoneStr);
        assertThat(otp).isHidden();
        assertThat(requests).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @Tag("slow-auth")
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "auth.slow.enabled", matches = "true")
    void expiredOtpIsRejectedWithoutCreatingASession() {
        Page page = customerPage;
        LoginPage login = open(page, LoginSmokeTest.Account.CUSTOMER);
        login.fillPhoneNumber(testCustomerPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        // AuthService stores the server OTP for five minutes. Changing browser time cannot
        // expire it; wait beyond its real lifetime without touching Redis or the test account.
        page.waitForTimeout(305_000);
        Response rejected = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/session") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(rejected.status()).isEqualTo(400);
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)).containsText("expired");
        assertThat(page.getByPlaceholder("- - - - - -")).isVisible();

    }

    @ParameterizedTest(name = "{0}: resend replaces the old OTP and the latest code authenticates")
    @EnumSource(LoginSmokeTest.Account.class)
    void resendRejectsThePreviousCode(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        LoginPage login = open(page, account);
        login.fillPhoneNumber(phoneFor(account));
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        String previous = page.getByPlaceholder("- - - - - -").inputValue();
        String latest = previous;
        // The random six-digit generator can produce the same value again. Allocate a
        // distinguishable new challenge, bounded to three requests; never retry verification.
        for (int attempt = 0; attempt < 3 && latest.equals(previous); attempt++) {
            Response resent = page.waitForResponse(
                    r -> r.url().contains("/api/v1/auth/otp") && r.request().method().equals("POST"),
                    login::clickResendSmsCode);
            assertThat(resent.status()).isEqualTo(200);
            login.clickAutofillCode();
            latest = page.getByPlaceholder("- - - - - -").inputValue();
        }
        assertThat(latest).as("Resend must supply a distinct challenge for the replacement scenario")
                .isNotEqualTo(previous);
        login.fillOtp(previous);
        Response rejected = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/session") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(rejected.status()).isEqualTo(400);
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)).isVisible();
        login.fillOtp(latest);
        Response accepted = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/session") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(accepted.status()).isEqualTo(200);
        page.waitForCondition(() -> !page.url().contains("/login"));
        login.openPortal(account.portal);
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).isVisible();
    }

    @ParameterizedTest(name = "{0}: resent OTP authenticates the selected account")
    @EnumSource(LoginSmokeTest.Account.class)
    void resendOtp(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        LoginPage login = open(page, account);
        login.fillPhoneNumber(phoneStr);
        login.clickSendOtp();
        login.waitForOtpInput();
        Response resent = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/otp") && r.request().method().equals("POST"),
                login::clickResendSmsCode);
        assertThat(resent.ok()).isTrue();
        login.clickAutofillCode();
        assertThat(page.getByPlaceholder("- - - - - -")).hasValue(Pattern.compile("[0-9]{6}"));
        Response verified = page.waitForResponse(
                r -> r.url().contains("/api/v1/auth/session") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(verified.ok()).isTrue();
        page.waitForCondition(() -> !page.url().contains("/login"));
        login.openPortal(account.portal);
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).isVisible();
    }
}
