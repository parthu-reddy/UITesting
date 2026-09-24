package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;

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
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
    }

    public void openWalletTab() {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Store Credit").setExact(true)).click();
    }

    public void openHistoryTab() {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
    }

    // ── Wallet tab ───────────────────────────────────────────────────────

    public String getWalletBalance() {
        return page.getByText("Available Balance", new Page.GetByTextOptions().setExact(true))
                .locator("..").locator("h2").innerText().trim();
    }

    public boolean isWalletVisible() {
        return page.getByText("Available Balance", new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    // ── Addresses tab ────────────────────────────────────────────────────

    /**
     * One card per saved address (SettingsAddressesTab.tsx): each leads with a map-pin, inside
     * the tab's `flex-col gap-3` list. The empty state has a map-pin too, but outside that list.
     */
    public int getSavedAddressCount() {
        return page.locator("[data-screen='settings'] div.flex.flex-col.gap-3 > div:has(svg.lucide-map-pin)").count();
    }

    public boolean hasAddress(String label) {
        return page.locator("text=" + label).isVisible();
    }

    // ── History tab ──────────────────────────────────────────────────────

    public boolean isTransactionHistoryVisible() {
        Locator selectedHistory = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true));
        if (!"true".equals(selectedHistory.getAttribute("aria-selected"))) return false;

        Locator definedState = page.getByText("Loading history...", new Page.GetByTextOptions().setExact(true))
                .or(page.getByText("No order history found.", new Page.GetByTextOptions().setExact(true)))
                .or(page.locator("[data-screen='settings'] .space-y-4 > button").first());
        return definedState.first().isVisible();
    }

    // ── Logout ───────────────────────────────────────────────────────────

    public void clickLogout() {
        page.locator("button:has-text('Log Out'), button:has-text('Logout')").first().click();
        page.waitForTimeout(500);
    }
}
