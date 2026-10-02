package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.RequestOptions;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Real UI login/reload/logout, with read-only verification that the logged-out JWT is revoked. */
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
    @EnumSource(LoginSmokeTest.Account.class)
    void reloadAndLogout(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        String phoneStr = phoneFor(account);
        page.navigate(TestConfig.APP_URL);
        if (account == LoginSmokeTest.Account.ADMIN) {
            new LoginPage(page).loginAs(account.label, phoneStr, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        } else new LoginPage(page).loginAs(account.label, phoneStr);
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        if (account == LoginSmokeTest.Account.CUSTOMER)
            new com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage(page).selectHomeFromOpenDialog();
        page.reload();
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        // A separate fresh context must not inherit the authenticated session.
        Page isolated = account == LoginSmokeTest.Account.CUSTOMER ? adminPage : customerPage;
        isolated.navigate(TestConfig.APP_URL);
        assertThat(isolated.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Order Food\\b"))).first()).isVisible();
        assertThat(isolated.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isHidden();
        org.assertj.core.api.Assertions.assertThat(isolated.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        org.assertj.core.api.Assertions.assertThat(isolated.evaluate("() => localStorage.getItem('user_profile')")).isNull();
        Response logout = page.waitForResponse(response -> response.url().endsWith("/api/v1/internal/auth/logout")
                && response.request().method().equals("POST"), () -> {
        if (account == LoginSmokeTest.Account.ADMIN) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log out").setExact(true)).click();
        } else {
            if (account == LoginSmokeTest.Account.CUSTOMER) CustomerDashboardPage.openProfileSettings(page);
            else page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Profile settings").setExact(true)).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                    .setName(account == LoginSmokeTest.Account.DELIVERY ? "Sign Out" : "Log Out").setExact(true)).click();
        }
        });
        org.assertj.core.api.Assertions.assertThat(logout.status()).isEqualTo(200);
        String authorization = logout.request().headerValue("authorization");
        org.assertj.core.api.Assertions.assertThat(authorization != null && authorization.startsWith("Bearer ")).isTrue();
        var revoked = page.request().get(TestConfig.APP_URL.replaceAll("/$", "") + "/api/v1/internal/auth/sessions",
                RequestOptions.create().setHeader("Authorization", authorization).setHeader("Accept", "application/json"));
        org.assertj.core.api.Assertions.assertThat(revoked.status()).as("UI logout must revoke this test JWT immediately").isEqualTo(401);
        revoked.dispose();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Order Food\\b"))).first()).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => localStorage.getItem('user_profile')")).isNull();
        page.reload();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Order Food\\b"))).first()).isVisible();
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isHidden();
    }
    @ParameterizedTest(name = "Signed-out deep link: {0}")
    @ValueSource(strings = {"/customer/settings", "/restaurant/settings", "/delivery/settings", "/admin/users"})
    void unauthenticatedDeepLinksExposeOnlyLogin(String path) {
        customerPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + path);
        assertThat(customerPage).hasURL(Pattern.compile(".*/login$"));
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Order Food\\b")))).isVisible();
        for (LoginSmokeTest.Account account : LoginSmokeTest.Account.values()) {
            assertThat(customerPage.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true))).hasCount(0);
        }
        org.assertj.core.api.Assertions.assertThat(customerPage.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        org.assertj.core.api.Assertions.assertThat(customerPage.evaluate("() => localStorage.getItem('user_profile')")).isNull();
    }

    @ParameterizedTest(name = "{0}: another role's deep link redirects to own dashboard")
    @EnumSource(LoginSmokeTest.Account.class)
    void authenticatedRoleCannotOpenAnotherDashboard(LoginSmokeTest.Account account) {
        Page page = pageFor(account);
        page.navigate(TestConfig.APP_URL);
        if (account == LoginSmokeTest.Account.ADMIN) {
            new LoginPage(page).loginAs(account.label, phoneFor(account), TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        } else new LoginPage(page).loginAs(account.label, phoneFor(account));
        assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        if (account == LoginSmokeTest.Account.CUSTOMER)
            new com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage(page).selectHomeFromOpenDialog();
        String own = routeFor(account);
        for (LoginSmokeTest.Account other : LoginSmokeTest.Account.values()) {
            if (other == account) continue;
            page.navigate(TestConfig.APP_URL.replaceAll("/$", "") + routeFor(other));
            assertThat(page).hasURL(Pattern.compile(".*" + own + "(?:/.*)?$"));
            assertThat(page.getByText(account.dashboardText, new Page.GetByTextOptions().setExact(true)).first()).isVisible();
            assertThat(page.getByText(other.dashboardText, new Page.GetByTextOptions().setExact(true))).hasCount(0);
            org.assertj.core.api.Assertions.assertThat(page.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).role"))
                    .isEqualTo(account.name());
        }
    }

    private String routeFor(LoginSmokeTest.Account account) {
        return "/" + (account == LoginSmokeTest.Account.DELIVERY ? "delivery" : account.name().toLowerCase(java.util.Locale.ROOT));
    }

    @org.junit.jupiter.api.Test
    void unsignedAdministratorHeadersCannotReadSessions() {
        var response = customerPage.request().get(TestConfig.APP_URL.replaceAll("/$", "") + "/api/v1/internal/auth/sessions",
                RequestOptions.create().setHeader("Accept", "application/json")
                    .setHeader("X-User-Id", "00000000-0000-0000-0000-000000000001")
                    .setHeader("X-User-Roles", "ADMIN")
                    .setHeader("X-User-Phone", "1000000002")
                    .setHeader("X-Session-Id", "00000000-0000-0000-0000-000000000002"));
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(401);
        response.dispose();
    }

}
