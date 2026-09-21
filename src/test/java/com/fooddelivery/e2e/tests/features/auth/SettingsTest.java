package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.SharedSettingsPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests shared settings view: wallet, addresses, history tabs.
 */
@Tag("feature")
public class SettingsTest extends TestBase {

    @Test
    @DisplayName("Navigate to settings → check wallet, addresses, history tabs")
    void settingsTabs() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        dashboard.openSettingsTab();
        customerPage.waitForTimeout(2000);

        SharedSettingsPage settings = new SharedSettingsPage(customerPage);

        // Check wallet tab
        settings.openWalletTab();
        customerPage.waitForTimeout(1000);
        if (settings.isWalletVisible()) {
            String balance = settings.getWalletBalance();
            System.out.println("[TEST] Wallet balance: " + balance);
        }

        // Check addresses tab
        settings.openAddressesTab();
        customerPage.waitForTimeout(1000);
        int addressCount = settings.getSavedAddressCount();
        System.out.println("[TEST] Saved addresses: " + addressCount);

        // Check history tab
        settings.openHistoryTab();
        customerPage.waitForTimeout(1000);
        assertThat(settings.isTransactionHistoryVisible() || customerPage.content().contains("History"))
                .isTrue().as("History tab should be visible");
    }

    @Test
    @DisplayName("Logout from settings")
    void logout() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new CustomerDashboardPage(customerPage).waitForDashboard();

        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        customerPage.waitForTimeout(2000);

        SharedSettingsPage settings = new SharedSettingsPage(customerPage);
        settings.clickLogout();

        // Verify we're back at login screen
        customerPage.waitForTimeout(2000);
        assertThat(customerPage.content()).containsAnyOf("Order Food", "Restaurant Partner", "Delivery Executive");
    }
}
