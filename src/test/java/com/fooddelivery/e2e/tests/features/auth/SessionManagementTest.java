package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.SessionManagementPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.RequestOptions;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Non-destructive checks for the inline Logged-in Devices settings section. */
@Tag("session-management")
@Tag("ui-only")
public class SessionManagementTest extends TestBase {

    private SessionManagementPage sessions;
    private List<?> serverSessions;
    private BrowserContext secondContext;
    private Page secondPage;
    private BrowserContext thirdContext;
    private Page thirdPage;


    @AfterEach
    void closeSecondBrowser() {
        if (thirdContext != null) thirdContext.close();
        if (secondContext != null) secondContext.close();
    }

    @BeforeEach
    void openCustomerSettings(TestInfo testInfo) {
        if (testInfo.getTestMethod().orElseThrow().isAnnotationPresent(EnabledIfSystemProperty.class)) {
            testCustomerPhone = System.getProperty("registration.customer.phone");
            assertThat(System.getProperty("customer.phone")).isEqualTo(testCustomerPhone);
            com.fooddelivery.e2e.util.FreshCustomerFixture.registerWithAddress(customerPage, testCustomerPhone, "Home");
        } else {
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        }
        Response response = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/internal/auth/sessions")
                && r.request().method().equals("GET"), () -> CustomerDashboardPage.openProfileSettings(customerPage));
        assertThat(response.status()).isEqualTo(200);
        Map<?, ?> body = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", response.text());
        assertThat(body.get("success")).isEqualTo(true);
        serverSessions = (List<?>) body.get("data");
        assertThat(serverSessions).isNotEmpty();
        String currentId = (String) customerPage.evaluate("""
                () => JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1]
                    .replace(/-/g, '+').replace(/_/g, '/'))).sessionId
                """);
        assertThat(serverSessions.stream().anyMatch(row -> currentId.equals(((Map<?, ?>) row).get("sessionId"))))
                .as("The server must list the current browser's session, not only another device").isTrue();
        sessions = new SessionManagementPage(customerPage);
        assertThat(sessions.section()).isVisible();
        sessions.waitForSessions();
    }

    @Test
    @DisplayName("SESSION-MGMT-01/02: Logged-in Devices renders current session")
    void currentSessionIsVisible() {
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(sessions.sessionRows()).hasCount(serverSessions.size());
        assertThat(sessions.getSessionCount())
                .as("The browser that loaded settings must appear as an active session")
                .isGreaterThanOrEqualTo(1);

        Locator current = sessions.sessionRows().first();
        assertThat(current).containsText("Last Active:");
        assertThat(current.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true))).isVisible();
        assertThat(current.innerText()).doesNotContain("undefined", "null");
    }

    @Test
    @DisplayName("SESSION-MGMT-05: Session list exposes only explicit per-device removal")
    void sessionActionsMatchCurrentUiContract() {
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true)).first()).isVisible();
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Terminate All").setExact(true))).hasCount(0);
        assertThat(sessions.section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("End All Sessions").setExact(true))).hasCount(0);
    }
    @Test
    @DisplayName("SESSION-MGMT-03/04: Remove only this test's second browser; first stays authenticated")
    @EnabledIfSystemProperty(named = "session.fixture.case", matches = "device-removal")
    void removeOwnSecondDeviceRevokesOnlyThatSession() {
        // Never use a random first row: both sessions must be owned by this test.
        assertThat(serverSessions).hasSize(1);
        String firstId = (String) ((Map<?, ?>) serverSessions.get(0)).get("sessionId");
        openSecondDevice();
        String secondId = (String) secondPage.evaluate("""
                () => JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1]
                    .replace(/-/g, '+').replace(/_/g, '/'))).sessionId
                """);
        assertThat(firstId).isNotEqualTo(secondId);
        Response listed = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/internal/auth/sessions")
                && r.request().method().equals("GET"), () -> customerPage.reload());
        assertThat(listed.status()).isEqualTo(200);
        Map<?, ?> listedBody = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", listed.text());
        List<?> before = (List<?>) listedBody.get("data");
        assertThat(before).hasSize(2);
        assertThat(before.stream().map(row -> (String) ((Map<?, ?>) row).get("sessionId")).toList())
                .containsExactlyInAnyOrder(firstId, secondId);
        sessions = new SessionManagementPage(customerPage);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(sessions.sessionRows()).hasCount(2);
        Locator ownSecond = sessions.sessionRows().filter(new Locator.FilterOptions().setHasText("Windows • Firefox"));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(ownSecond).hasCount(1);
        Response remaining = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/internal/auth/sessions")
                && r.request().method().equals("GET"), () -> {
            Response removed = customerPage.waitForResponse(r -> r.url().endsWith("/api/v1/internal/auth/sessions/" + secondId)
                    && r.request().method().equals("DELETE"), () -> ownSecond.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Remove").setExact(true)).click());
            assertThat(removed.status()).isEqualTo(200);
        });
        assertThat(remaining.status()).isEqualTo(200);
        Map<?, ?> remainingBody = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", remaining.text());
        List<?> after = (List<?>) remainingBody.get("data");
        assertThat(after).hasSize(1);
        assertThat(((Map<?, ?>) after.get(0)).get("sessionId")).isEqualTo(firstId);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(sessions.sessionRows()).hasCount(1);
        Map<?, ?> denied = (Map<?, ?>) secondPage.evaluate("""
                async () => {
                    const res = await fetch('/api/v1/internal/auth/sessions', {
                        headers: {Authorization: `Bearer ${localStorage.getItem('auth_token')}`}, credentials: 'omit'
                    });
                    return {status: res.status};
                }
                """);
        assertThat(((Number) denied.get("status")).intValue()).isEqualTo(401);
        customerPage.reload();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(sessions.section()).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(sessions.sessionRows()).hasCount(1);
    }

    @Test
    @DisplayName("SESSION-MODAL-01: Cancel leaves both existing devices authenticated")
    @EnabledIfSystemProperty(named = "session.fixture.case", matches = "limit-cancel")
    void sessionLimitCancelPreservesExistingDevices() {
        Locator modal = reachSessionLimit();
        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Close dialog").setExact(true)).click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(modal).hasCount(0);
        assertLoggedOut(thirdPage);
        assertThat(sessionIds(customerPage)).containsExactlyInAnyOrder(sessionId(customerPage), sessionId(secondPage));
        assertThat(protectedStatus(secondPage)).isEqualTo(200);
    }

    @Test
    @DisplayName("SESSION-MODAL-02: Replace only the selected test device and authenticate the third")
    @EnabledIfSystemProperty(named = "session.fixture.case", matches = "limit-replacement")
    void sessionLimitReplacementRevokesSelectedDeviceOnly() {
        Locator modal = reachSessionLimit();
        String firstId = sessionId(customerPage);
        String secondId = sessionId(secondPage);
        Locator selected = modal.locator("div.flex.items-center.justify-between")
                .filter(new Locator.FilterOptions().setHasText("Windows • Firefox"));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(selected).hasCount(1);
        Response replaced = thirdPage.waitForResponse(r -> r.request().method().equals("POST")
                && java.net.URI.create(r.url()).getPath().endsWith("/auth/verify")
                && r.url().contains("removeSessionId=" + secondId),
                () -> selected.getByTitle("Log out from this device").click());
        assertThat(replaced.status()).isEqualTo(200);
        new SavedDeliveryAddressPage(thirdPage).selectHomeFromOpenDialog();
        new CustomerDashboardPage(thirdPage).waitForDashboard();
        Map<?, ?> claims = (Map<?, ?>) thirdPage.evaluate("""
                () => JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
                """);
        assertThat(claims.get("phone")).isEqualTo(testCustomerPhone);
        assertThat(claims.get("sub")).isEqualTo(customerPage.evaluate("""
                () => JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).sub
                """));
        assertThat(((List<?>) claims.get("roles")).stream().map(Object::toString).toList()).contains("CUSTOMER");
        String thirdId = sessionId(thirdPage);
        assertThat(thirdId).isNotIn(firstId, secondId);
        assertThat(protectedStatus(secondPage)).isEqualTo(401);
        assertThat(sessionIds(customerPage)).containsExactlyInAnyOrder(firstId, thirdId);
        assertThat(sessionIds(thirdPage)).containsExactlyInAnyOrder(firstId, thirdId);
    }

    private void openSecondDevice() {
        secondContext = newDevice("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0");
        secondPage = secondContext.newPage();
        secondPage.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);
        secondPage.navigate(TestConfig.APP_URL);
        new LoginPage(secondPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(secondPage).selectHomeFromOpenDialog();
    }

    private BrowserContext newDevice(String userAgent) {
        return browser.newContext(new Browser.NewContextOptions()
                .setPermissions(List.of("geolocation", "notifications"))
                .setGeolocation(TestConfig.GEO_LAT, TestConfig.GEO_LNG).setUserAgent(userAgent));
    }

    private Locator reachSessionLimit() {
        assertThat(serverSessions).hasSize(1);
        String firstId = sessionId(customerPage);
        openSecondDevice();
        String secondId = sessionId(secondPage);
        assertThat(firstId).isNotEqualTo(secondId);
        assertThat(sessionIds(customerPage)).containsExactlyInAnyOrder(firstId, secondId);
        thirdContext = newDevice("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/605.1.15 Version/17.0 Safari/605.1.15");
        thirdPage = thirdContext.newPage();
        thirdPage.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);
        thirdPage.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(thirdPage);
        login.selectRole("Order Food");
        login.fillPhoneNumber(testCustomerPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        Response conflict = thirdPage.waitForResponse(r -> r.request().method().equals("POST")
                && java.net.URI.create(r.url()).getPath().endsWith("/auth/verify"), login::clickVerifyAndLogin);
        assertThat(conflict.status()).isEqualTo(409);
        Map<?, ?> body = (Map<?, ?>) thirdPage.evaluate("text => JSON.parse(text)", conflict.text());
        List<?> devices = (List<?>) ((Map<?, ?>) body.get("data")).get("activeSessions");
        assertThat(devices.stream().map(row -> (String) ((Map<?, ?>) row).get("sessionId")).toList())
                .containsExactlyInAnyOrder(firstId, secondId);
        Locator modal = thirdPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Active sessions").setExact(true));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(modal).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(modal).containsText("Session Limit Reached");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(modal).containsText("Windows • Firefox");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(modal.getByTitle("Log out from this device")).hasCount(2);
        assertLoggedOut(thirdPage);
        return modal;
    }

    private void assertLoggedOut(Page page) {
        assertThat(page.evaluate("() => localStorage.getItem('auth_token')")).isNull();
        assertThat(protectedStatus(page)).isEqualTo(401);
    }

    private String sessionId(Page page) {
        return (String) page.evaluate("""
                () => JSON.parse(atob(localStorage.getItem('auth_token').split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).sessionId
                """);
    }

    private int protectedStatus(Page page) {
        return ((Number) page.evaluate("""
                async () => (await fetch('/api/v1/internal/auth/sessions', {
                    headers: localStorage.getItem('auth_token') ? {Authorization: `Bearer ${localStorage.getItem('auth_token')}`} : {},
                    credentials: 'omit'
                })).status
                """)).intValue();
    }

    private List<String> sessionIds(Page page) {
        Map<?, ?> body = (Map<?, ?>) page.evaluate("""
                async () => {
                    const res = await fetch('/api/v1/internal/auth/sessions', {
                        headers: {Authorization: `Bearer ${localStorage.getItem('auth_token')}`}, credentials: 'omit'
                    });
                    return {status: res.status, body: await res.json()};
                }
                """);
        assertThat(((Number) body.get("status")).intValue()).isEqualTo(200);
        return ((List<?>) ((Map<?, ?>) body.get("body")).get("data")).stream()
                .map(row -> (String) ((Map<?, ?>) row).get("sessionId")).toList();
    }

}
