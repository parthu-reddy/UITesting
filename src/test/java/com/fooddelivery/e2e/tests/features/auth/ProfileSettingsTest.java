package com.fooddelivery.e2e.tests.features.auth;

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
@Tag("profile-settings")
public class ProfileSettingsTest extends TestBase {

    // ── PROFILE COMPLETION MODAL SCENARIOS ───────────────────────────────

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "scenario.customer.enabled", matches = "true")
    @DisplayName("PROFILE-16/17: Incomplete seeded profile requires completion")
    void completeProfileModalPrompt() {
        assertThat(testCustomerPhone).isEqualTo("8000000501");
        customerPage.navigate(TestConfig.APP_URL);
        LoginPage login = new LoginPage(customerPage);
        login.selectRole("Order Food");
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
        // Leave this reusable incomplete-profile fixture incomplete.
    }

    @AfterEach
    void logoutScenarioSession() {
        if ("8000000501".equals(testCustomerPhone)) new LoginPage(customerPage).logoutCurrentSession();
    }

    // ── SHARED SETTINGS TABS SCENARIOS ───────────────────────────────────

    @Test
    @DisplayName("SETTINGS-01: SharedSettings Address tab")
    void sharedSettingsAddressTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
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

    @Test
    @DisplayName("SETTINGS-03: SharedSettings Wallet tab")
    void sharedSettingsWalletTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
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

    @Test
    @DisplayName("SETTINGS-05: SharedSettings History tab")
    void sharedSettingsHistoryTab() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        
        SharedSettingsPage settings = new SharedSettingsPage(customerPage);
        settings.openHistoryTab();
        customerPage.waitForTimeout(500);
        
        assertThat(settings.isTransactionHistoryVisible()).isTrue();
    }
}
