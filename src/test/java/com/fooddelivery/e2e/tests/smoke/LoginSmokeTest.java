package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Real UI and backend OTP tests; requires the development OTP autofill feature. */
@Tag("smoke")
@Tag("login")
public class LoginSmokeTest extends TestBase {
    enum Account {
        CUSTOMER("Order Food", TestConfig.CUSTOMER_PHONE, "Deliver to"),
        RESTAURANT("Restaurant Partner", TestConfig.RESTAURANT_PHONE, "Menu Stock Toggles"),
        DELIVERY("Delivery Executive", TestConfig.RIDER_PHONE, "Today’s Earnings"),
        ADMIN("System Admin", TestConfig.ADMIN_PHONE, "Live Operations");

        final String label, phone, dashboardText;
        Account(String label, String phone, String dashboardText) {
            this.label = label;
            this.phone = phone;
            this.dashboardText = dashboardText;
        }
    }

    private Page pageFor(Account account) {
        return switch (account) {
            case CUSTOMER -> customerPage;
            case RESTAURANT -> restaurantPage;
            case DELIVERY -> riderPage;
            case ADMIN -> adminPage;
        };
    }

    @ParameterizedTest(name = "{0}: valid OTP opens the correct dashboard")
    @EnumSource(Account.class)
    void successfulLogin(Account account) {
        Page page = pageFor(account);
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        if (account == Account.ADMIN) {
            login.loginAs(account.label, account.phone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        } else {
            login.loginAs(account.label, account.phone);
        }
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first())
                .isVisible();
        assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role"))
                .isEqualTo(account.name());
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')"))
                .isInstanceOf(String.class);
    }

    @ParameterizedTest(name = "{0}: wrong OTP is rejected and stays logged out")
    @EnumSource(Account.class)
    void failedLogin(Account account) {
        Page page = pageFor(account);
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        login.selectRole(account.label);
        login.fillPhoneNumber(account.phone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        var input = page.getByPlaceholder("- - - - - -");
        assertThat(input).hasValue(java.util.regex.Pattern.compile("[0-9]{6}"));
        // Mutate the actual code so the negative case cannot accidentally use a valid OTP.
        String valid = input.inputValue();
        login.fillOtp((valid.charAt(0) == '0' ? "1" : "0") + valid.substring(1));
        Response response = page.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        assertThat(response.status()).isIn(400, 401, 403, 422);
        assertThat(page.locator("div:has(> svg.lucide-circle-alert) > span")).isVisible();
        assertThat(input).isVisible();
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(page.evaluate("() => localStorage.getItem('user_profile')")).isNull();
    }
}
