package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Real UI and backend OTP tests using the same Dev autofill control as manual login. */
@Tag("smoke")
@Tag("login")
public class LoginSmokeTest extends TestBase {
    enum Account {
        CUSTOMER("Order Food", "Deliver to"),
        RESTAURANT("Restaurant Partner", "Updates every 5 s"),
        DELIVERY("Delivery Executive", "Trips Completed"),
        ADMIN("System Admin", "Live Operations");

        final String label, dashboardText;
        Account(String label, String dashboardText) {
            this.label = label;
            this.dashboardText = dashboardText;
        }
    }

    static Stream<Account> accounts() {
        String roles = System.getProperty("login.roles");
        return roles == null ? Arrays.stream(Account.values())
                : Arrays.stream(roles.split(",")).map(String::trim).map(Account::valueOf);
    }



    private Page pageFor(Account account) {
        return switch (account) {
            case CUSTOMER -> customerPage;
            case RESTAURANT -> restaurantPage;
            case DELIVERY -> riderPage;
            case ADMIN -> adminPage;
        };
    }

    private String phoneFor(Account account) {
        return switch (account) {
            case CUSTOMER -> testCustomerPhone;
            case RESTAURANT -> testRestaurantPhone;
            case DELIVERY -> testRiderPhone;
            case ADMIN -> testAdminPhone;
        };
    }

    @org.junit.jupiter.api.Test
    void isolatedContextsStartWithoutAuthentication() {
        for (Page page : getAllPages()) {
            page.navigate(TestConfig.APP_URL);
            assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Order Food\\b"))).first()).isVisible();
            assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
            assertThat(page.evaluate("() => localStorage.getItem('user_profile')")).isNull();
        }
    }

    @ParameterizedTest(name = "{0}: valid OTP opens the correct dashboard")
    @MethodSource("accounts")
    void successfulLogin(Account account) {
        Page page = pageFor(account);
        String phone = phoneFor(account);
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        if (account == Account.ADMIN) {
            login.loginAs(account.label, phone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        } else {
            login.loginAs(account.label, phone);
        }
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first())
                .isVisible();
        assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role"))
                .isEqualTo(account.name());
        Object identity = page.evaluate("""
                () => {
                    const token = localStorage.getItem('auth_token');
                    const profile = JSON.parse(localStorage.getItem('user_profile'));
                    const claims = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
                    return {phone: claims.phone, roles: claims.roles, subject: claims.sub,
                        profileId: profile.id, profilePhone: profile.phone};
                }
                """);
        java.util.Map<?, ?> identityFields = (java.util.Map<?, ?>) identity;
        assertThat(identityFields.get("phone")).isEqualTo(phone);
        assertThat(identityFields.get("profilePhone")).isEqualTo(phone);
        assertThat(identityFields.get("subject")).isEqualTo(identityFields.get("profileId"));
        assertThat(identityFields.get("subject")).isInstanceOf(String.class);
        assertThat(identityFields.get("roles")).isInstanceOf(java.util.List.class);
        assertThat(((java.util.List<?>) identityFields.get("roles")).contains(account.name())).isTrue();
    }

    @ParameterizedTest(name = "{0}: rejected login stays logged out")
    @MethodSource("accounts")
    void failedLogin(Account account) {
        Page page = pageFor(account);
        String phone = phoneFor(account);
        page.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(page);
        login.selectRole(account.label);
        login.fillPhoneNumber(phone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        var input = page.getByPlaceholder("- - - - - -");
        assertThat(input).hasValue(java.util.regex.Pattern.compile("[0-9]{6}"));
        boolean inactive = "inactive".equals(System.getProperty("login.rejection"));
        if (inactive) {
            assertThat(account).isEqualTo(Account.CUSTOMER);
            assertThat(phone).isEqualTo("8000000503");
        } else {
            // Mutate the actual code so the wrong-OTP case cannot use a valid OTP.
            String valid = input.inputValue();
            login.fillOtp((valid.charAt(0) == '0' ? "1" : "0") + valid.substring(1));
        }
        Response response = page.waitForResponse(
                r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                login::clickVerifyAndLogin);
        if (inactive) assertThat(response.status()).isEqualTo(403);
        else assertThat(response.status()).isIn(400, 401, 403, 422);
        assertThat(page.locator("div:has(> svg.lucide-circle-alert) > span")).isVisible();
        if (inactive) assertThat(page.locator("div:has(> svg.lucide-circle-alert) > span"))
                .containsText("inactive");
        assertThat(input).isVisible();
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(page.evaluate("() => localStorage.getItem('user_profile')")).isNull();
    }
}
