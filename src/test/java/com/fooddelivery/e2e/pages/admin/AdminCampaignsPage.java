package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code CampaignManagement.tsx, AdPerformanceDashboard.tsx}
 * <p>
 * Also covers: {@code CampaignCard.tsx}, {@code CreateCampaignModal.tsx}
 * </p>
 * <p>
 * Full campaign lifecycle: list, create, toggle, wallet top-up, ad performance.
 * </p>
 */
public class AdminCampaignsPage {
    private final Page page;
    public AdminCampaignsPage(Page page) { this.page = page; }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isCampaignsVisible() {
        return page.locator("text=Campaigns, text=Ad Performance, text=Campaign").first().isVisible();
    }

    public int getCampaignCount() {
        return page.locator("[data-testid='campaign-card'], .campaign-row").count();
    }

    // ── Create campaign ──────────────────────────────────────────────────

    public void openCreateCampaignModal() {
        page.locator("button:has-text('New Campaign'), button:has-text('Create Campaign'), button:has(svg.lucide-plus)").first().click();
        page.waitForTimeout(500);
    }

    public void fillCampaignName(String name) {
        page.locator("input[placeholder*='Campaign'], input[placeholder*='name'], label:has-text('Name') + input, label:has-text('Name') ~ input").first().fill(name);
    }

    public void fillDailyBudget(String amount) {
        page.locator("label:has-text('Daily') ~ input, input[placeholder*='Daily']").first().fill(amount);
    }

    public void fillTotalBudget(String amount) {
        page.locator("label:has-text('Total') ~ input, label:has-text('Lifetime') ~ input, input[placeholder*='Total']").first().fill(amount);
    }

    public void fillBidAmount(String amount) {
        page.locator("label:has-text('Bid') ~ input, input[placeholder*='Bid']").first().fill(amount);
    }

    public void submitCampaign() {
        page.locator("button:has-text('Create'), button:has-text('Submit')").first().click();
        page.waitForTimeout(2000);
    }

    // ── Campaign actions ─────────────────────────────────────────────────

    public void toggleCampaignStatus(int index) {
        page.locator("[data-testid='campaign-card'], .campaign-row").nth(index)
                .locator("button:has-text('Pause'), button:has-text('Resume'), button:has-text('Activate')").first().click();
        page.waitForTimeout(1000);
    }

    // ── Wallet ───────────────────────────────────────────────────────────

    public String getWalletBalance() {
        return page.locator("text=Balance, text=Wallet").locator("xpath=..").locator("span, p").last().innerText().trim();
    }

    public void openWalletTopup() {
        page.locator("button:has-text('Top Up'), button:has-text('Add Funds'), button:has(svg.lucide-wallet)").first().click();
        page.waitForTimeout(500);
    }

    public void fillTopupAmount(String amount) {
        page.locator("input[placeholder*='Amount'], input[type='number']").first().fill(amount);
    }

    // ── Performance dashboard ────────────────────────────────────────────

    public void openAdPerformance() {
        page.locator("button:has-text('Performance'), button:has-text('Analytics'), button:has(svg.lucide-trending-up)").first().click();
        page.waitForTimeout(500);
    }

    public boolean isPerformanceDashboardVisible() {
        return page.locator("text=Ad Performance, text=Impressions, text=Clicks").first().isVisible();
    }
}
