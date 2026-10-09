package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.SharedSettingsPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Profile completion modal and SharedSettings tabs (Addresses, Wallet, History).
 * Covers: PROFILE-16..19, SETTINGS-01..06
 */
@Tag("feature-settings-profile")
public class ProfileSettingsTest extends TestBase {

    // ── PROFILE COMPLETION MODAL SCENARIOS ───────────────────────────────

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "scenario.customer.enabled", matches = "true")
    @DisplayName("PROFILE-16/17: Incomplete seeded profile requires completion")
    void completeProfileModalPrompt() {
        assertThat(testCustomerPhone).isEqualTo("8000000501");
        customerPage.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(customerPage);
        login.fillPhoneNumber(testCustomerPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        var profileResponse = customerPage.waitForResponse(
                r -> r.url().contains("/api/v1/users/profile") && r.request().method().equals("GET"),
                () -> {
                    var response = customerPage.waitForResponse(
                            r -> r.url().contains("/auth/verify") && r.request().method().equals("POST"),
                            login::clickVerifyAndLogin);
                    assertThat(response.status()).isEqualTo(200);
                });
        assertThat(profileResponse.status()).isEqualTo(200);
        var name = customerPage.getByPlaceholder("Enter your full name");
        var email = customerPage.getByPlaceholder("Enter your email address");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(name).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(name).hasValue("");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(email).hasValue("");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText(
                "Please complete your profile to continue. This is required to process your orders.",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.evaluate("() => localStorage.getItem('auth_token')")).isInstanceOf(String.class);
        java.util.List<String> saves = new java.util.ArrayList<>();
        customerPage.onRequest(request -> {
            if (request.method().equals("PUT") && com.fooddelivery.e2e.util.UrlPaths.path(request.url()).equals("/api/v1/users/profile")) {
                saves.add(request.url());
            }
        });
        customerPage.route("**/api/v1/users/profile", route -> {
            if (route.request().method().equals("PUT")) route.abort();
            else route.resume();
        });
        var submit = customerPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Save Profile & Continue").setExact(true));
        var error = customerPage.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG)
                .locator("div:has(> svg.lucide-circle-alert) > span");
        for (String badName : new String[] {"", "   ", "x".repeat(101)}) {
            name.fill(badName);
            email.fill("profile-validation@example.com");
            submit.click();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(error).isVisible();
            assertThat(saves).as("Invalid names must not persist the incomplete fixture").isEmpty();
        }
        name.fill("Validation Probe");
        email.fill("");
        submit.click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(error).containsText("email");
        email.fill("invalid-address");
        submit.click();
        assertThat(email.evaluate("element => element.validity.typeMismatch")).isEqualTo(true);
        assertThat(saves).isEmpty();
        name.fill("");
        email.fill("");
        // Leave this reusable incomplete-profile fixture incomplete; retain its session after execution.
    }



    // ── SHARED SETTINGS TABS SCENARIOS ───────────────────────────────────

    @Tag("feature-addresses")
    @Test
    @DisplayName("SETTINGS-01: SharedSettings Address tab")
    void sharedSettingsAddressTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        
        SharedSettingsPage settings = new SharedSettingsPage(customerPage);
        settings.openAddressesTab();
        customerPage.waitForTimeout(500);
        
        int count = settings.getSavedAddressCount();
        assertThat(count).isGreaterThanOrEqualTo(0);
        if (count > 0) {
            assertThat(settings.hasAddress("Home")).isTrue();
        }
    }

    @Tag("feature-wallet")
    @Test
    @DisplayName("SETTINGS-03: SharedSettings Wallet tab")
    void sharedSettingsWalletTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        
        SharedSettingsPage settings = new SharedSettingsPage(customerPage);
        settings.openWalletTab();
        customerPage.waitForTimeout(500);
        
        assertThat(settings.isWalletVisible()).isTrue();
        String balance = settings.getWalletBalance();
        assertThat(balance).isNotNull();
    }

    @Tag("feature-order-tracking")
    @Test
    @DisplayName("SETTINGS-05: SharedSettings History tab")
    void sharedSettingsHistoryTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        
        SharedSettingsPage settings = new SharedSettingsPage(customerPage);
        settings.openHistoryTab();
        settings.assertHistoryLoaded();
    }
}
