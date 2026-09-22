package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Delivery Dashboard shell.
 * Maps to: {@code DeliveryDashboard.tsx}
 * <p>
 * Also covers sub-components: {@code RiderHeader.tsx}, {@code RiderStatsBar.tsx},
 * {@code RiderNotices.tsx}, {@code RiderPrompts.tsx}, {@code DeliveryAvailableJobs.tsx}
 * </p>
 */
public class DeliveryDashboardPage {

    private final Page page;

    public DeliveryDashboardPage(Page page) {
        this.page = page;
    }

    public void waitForDashboard() {
        page.waitForCondition(() -> 
            page.getByText("Trips Completed").isVisible() || 
            page.getByText("Offline").isVisible() || 
            page.getByText("Online Duty").isVisible(),
            new Page.WaitForConditionOptions().setTimeout(15000));
    }

    public void goOnline() {
        Locator toggleBtn = page.locator("button:has-text('Offline'), button:has-text('Online Duty')").first();
        if ("Offline".equals(toggleBtn.innerText().trim())) {
            toggleBtn.click();
            page.getByText("Online Duty").waitFor();
        }
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openActiveTab() {
        page.locator("button:has-text('Active'), [role='tab']:has-text('Active')").first().click();
        page.waitForTimeout(300);
    }

    public void openHistoryTab() {
        page.locator("button:has-text('Trips Completed')").first().click();
        page.waitForTimeout(300);
    }

    public void openSettingsTab() {
        page.locator("button:has-text('Settings'), [role='tab']:has-text('Settings')").first().click();
        page.waitForTimeout(300);
    }

    public void openEarningsTab() {
        page.locator("button:has-text('Earnings'), [role='tab']:has-text('Earnings')").first().click();
        page.waitForTimeout(300);
    }

    public void openWalletTab() {
        page.locator("button:has-text('Wallet'), [role='tab']:has-text('Wallet')").first().click();
        page.waitForTimeout(300);
    }
}
