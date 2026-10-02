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
        login.selectRole(account.label);
        return login;
    }

    @ParameterizedTest(name = "{0}: phone required, digits only, maximum ten digits")
    @EnumSource(LoginSmokeTest.Account.class)
    void phoneValidation(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        LoginPage login = open(page, account);
        List<String> requests = new ArrayList<>();
        page.onRequest(r -> { if (r.url().contains("/auth/initiate")) requests.add(r.url()); });
        login.clickSendOtp();
        var phone = page.getByPlaceholder("9876543210");
        assertThat(phone.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(phone).isFocused();
        assertThat(page.getByPlaceholder("- - - - - -")).isHidden();
        login.fillPhoneNumber("abc" + phoneStr + "999");
        assertThat(phone).hasValue(phoneStr);
        assertThat(requests).isEmpty();
        // A malformed UI submission must never request an OTP; abort any regression before it
        // can send an invalid number to the backend or create test data.
        page.route("**/auth/initiate?**", route -> route.abort());
        for (String shortPhone : new String[] {"1", "1234567", "12345678", "123456789"}) {
            login.fillPhoneNumber(shortPhone);
            login.clickSendOtp();
            var error = page.locator("div:has(> svg.lucide-circle-alert) > span");
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
        page.onRequest(r -> { if (r.url().contains("/auth/verify")) requests.add(r.url()); });
        login.clickVerifyAndLogin();
        var otp = page.getByPlaceholder("- - - - - -");
        assertThat(otp.evaluate("e => e.validity.valueMissing")).isEqualTo(true);
        assertThat(otp).isFocused();
        page.route("**/auth/verify?**", route -> route.abort());
        for (String shortOtp : new String[] {"1", "123", "12345"}) {
            login.fillOtp(shortOtp);
            login.clickVerifyAndLogin();
            var error = page.locator("div:has(> svg.lucide-circle-alert) > span");
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
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
    }

    @org.junit.jupiter.api.Test
    void unallowlistedAdministratorCannotRetrieveDevOtpOrRegister() {
        LoginPage login = open(adminPage, LoginSmokeTest.Account.ADMIN);
        assertThat(adminPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create account").setExact(true))).hasCount(0);
        login.fillPhoneNumber("1000000099");
        Response initiated = adminPage.waitForResponse(
                r -> r.url().contains("/auth/initiate") && r.request().method().equals("POST"), login::clickSendOtp);
        assertThat(initiated.status()).isEqualTo(200);
        assertThat(initiated.headerValue("X-Dev-OTP-Available")).isNull();
        login.waitForOtpInput();
        assertThat(adminPage.getByTestId("dev-otp-autofill")).hasCount(0);
        Object lookupStatus = adminPage.evaluate("""
                async () => (await fetch('/api/v1/internal/auth/admin/otp?phoneNumber=1000000099&serviceName=ADMIN',
                    {headers: {'X-Calling-Service': 'ADMIN', Accept: 'application/json'}, credentials: 'omit'})).status
                """);
        assertThat(((Number) lookupStatus).intValue()).isEqualTo(403);
        assertThat(adminPage.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(adminPage.evaluate("() => localStorage.getItem('user_profile')")).isNull();
    }

    @org.junit.jupiter.api.Test
    @Tag("auth-rate-limit")
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "auth.limits.enabled", matches = "true")
    void verificationLimitResponseKeepsTheBrowserLoggedOut() {
        LoginPage login = open(customerPage, LoginSmokeTest.Account.CUSTOMER);
        login.fillPhoneNumber(testCustomerPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        // Dev deliberately permits one million attempts. This is UI error-contract coverage;
        // backend tests separately prove the default production attempt limits.
        customerPage.route("**/auth/verify?**", route -> route.fulfill(
                new com.microsoft.playwright.Route.FulfillOptions().setStatus(400)
                        .setContentType("application/json").setBody(
                                "{\"success\":false,\"message\":\"Too many failed attempts. Please request a new OTP.\"}")));
        Response rejected = customerPage.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(rejected.status()).isEqualTo(400);
        assertThat(customerPage.locator("div:has(> svg.lucide-circle-alert) > span"))
                .containsText("Too many failed attempts");
        assertThat(customerPage.getByPlaceholder("- - - - - -")).isVisible();
        assertThat(customerPage.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(customerPage.evaluate("() => localStorage.getItem('user_profile')")).isNull();
    }

    @org.junit.jupiter.api.Test
    @Tag("auth-rate-limit")
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "auth.limits.enabled", matches = "true")
    void otpRequestLimitResponseLeavesResendAvailable() {
        LoginPage login = open(customerPage, LoginSmokeTest.Account.CUSTOMER);
        login.fillPhoneNumber(testCustomerPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        customerPage.route("**/auth/initiate?**", route -> route.fulfill(
                new com.microsoft.playwright.Route.FulfillOptions().setStatus(429)
                        .setContentType("application/json").setBody(
                                "{\"success\":false,\"message\":\"Too many login attempts. Please try again later.\"}")));
        Response limited = customerPage.waitForResponse(
                r -> r.url().contains("/auth/initiate") && r.request().method().equals("POST"),
                login::clickResendSmsCode);
        assertThat(limited.status()).isEqualTo(429);
        assertThat(customerPage.locator("div:has(> svg.lucide-circle-alert) > span")).containsText("Too many login attempts");
        assertThat(customerPage.getByPlaceholder("- - - - - -")).isVisible();
        assertThat(customerPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Resend SMS Code").setExact(true))).isEnabled();
        assertThat(customerPage.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(customerPage.evaluate("() => localStorage.getItem('user_profile')")).isNull();
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
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(rejected.status()).isEqualTo(400);
        assertThat(page.locator("div:has(> svg.lucide-circle-alert) > span")).containsText("expired");
        assertThat(page.getByPlaceholder("- - - - - -")).isVisible();
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(page.evaluate("() => localStorage.getItem('user_profile')")).isNull();
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
                    r -> r.url().contains("/auth/initiate") && r.request().method().equals("POST"),
                    login::clickResendSmsCode);
            assertThat(resent.status()).isEqualTo(200);
            login.clickAutofillCode();
            latest = page.getByPlaceholder("- - - - - -").inputValue();
        }
        assertThat(latest).as("Resend must supply a distinct challenge for the replacement scenario")
                .isNotEqualTo(previous);
        login.fillOtp(previous);
        Response rejected = page.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(rejected.status()).isEqualTo(400);
        assertThat(page.locator("div:has(> svg.lucide-circle-alert) > span")).isVisible();
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        login.fillOtp(latest);
        Response accepted = page.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(accepted.status()).isEqualTo(200);
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role")).isEqualTo(account.name());
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
                r -> r.url().contains("/auth/initiate") && r.request().method().equals("POST"),
                login::clickResendSmsCode);
        assertThat(resent.ok()).isTrue();
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
