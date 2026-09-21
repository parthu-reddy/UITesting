package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for SharedSettingsView (reused by Customer, Restaurant, Rider).
 * Maps to: {@code SharedSettingsView.tsx} with tabs: Addresses, Wallet, History.
 * <p>
 * Also covers sub-components: {@code SettingsAddressesTab.tsx}, {@code SettingsWalletTab.tsx},
 * {@code SettingsHistoryTab.tsx}, {@code CustomerSettingsScreen.tsx}
 * </p>
 */
public class SharedSettingsPage {

    private final Page page;

    public SharedSettingsPage(Page page) {
        this.page = page;
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openAddressesTab() {
        page.locator("button:has-text('Addresses'), [role='tab']:has-text('Addresses')").first().click();
        page.waitForTimeout(300);
    }

    public void openWalletTab() {
        page.locator("button:has-text('Wallet'), [role='tab']:has-text('Wallet')").first().click();
        page.waitForTimeout(300);
    }

    public void openHistoryTab() {
        page.locator("button:has-text('History'), [role='tab']:has-text('History')").first().click();
        page.waitForTimeout(300);
    }

    // ── Wallet tab ───────────────────────────────────────────────────────

    public String getWalletBalance() {
        return page.locator("text=Wallet Balance").locator("xpath=..").locator("span").first().innerText().trim();
    }

    public boolean isWalletVisible() {
        return page.locator("text=Wallet Balance").isVisible();
    }

    // ── Addresses tab ────────────────────────────────────────────────────

    public int getSavedAddressCount() {
        return page.locator("[data-testid='address-card'], .address-card").count();
    }

    public boolean hasAddress(String label) {
        return page.locator("text=" + label).isVisible();
    }

    // ── History tab ──────────────────────────────────────────────────────

    public boolean isTransactionHistoryVisible() {
        return page.locator("text=Transaction History, text=Order History, table").first().isVisible();
    }

    // ── Logout ───────────────────────────────────────────────────────────

    public void clickLogout() {
        page.locator("button:has-text('Log Out'), button:has-text('Logout')").first().click();
        page.waitForTimeout(500);
    }
}
