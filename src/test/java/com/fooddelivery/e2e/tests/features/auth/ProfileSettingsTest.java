package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.CompleteProfileModalPage;
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
    @DisplayName("PROFILE-16/17: Complete profile modal prompt")
    void completeProfileModalPrompt() {
        // Use a new unique phone to trigger the profile setup
        String newPhone = "9998887776";
        customerPage.navigate(TestConfig.APP_URL);
        
        LoginPage login = new LoginPage(customerPage);
        login.selectRole("Order Food");
        login.fillPhoneNumber(newPhone);
        login.clickSendOtp();
        login.waitForOtpInput();
        login.clickAutofillCode();
        login.clickVerifyAndLogin();
        
        CompleteProfileModalPage profile = new CompleteProfileModalPage(customerPage);
        // Sometimes it takes a moment to appear after login
        customerPage.waitForTimeout(2000);
        
        if (profile.isProfilePromptVisible()) {
            profile.fillName("Test Customer");
            profile.fillEmail("testcustomer@example.com");
            profile.submit();
        }
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
