package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Payouts workflow.
 * Maps to: {@code AdminPayoutsPage.tsx, PayoutQueue.tsx, PayoutHistory.tsx, PayoutDrawer.tsx,
 *            PayoutDetail.tsx, PayoutCreateDialog.tsx, PayoutActionDialogs.tsx, PayoutLinesPanel.tsx}
 */
public class AdminPayoutsPage {

    private final Page page;

    public AdminPayoutsPage(Page page) {
        this.page = page;
    }

    public boolean isPayoutsVisible() {
        return page.locator("text=Payouts, text=Payout Queue, text=Pending Payouts").first().isVisible();
    }

    public void openPendingQueue() {
        page.locator("button:has-text('Pending'), button:has-text('Queue')").first().click();
        page.waitForTimeout(500);
    }

    public void openHistory() {
        page.locator("button:has-text('History'), button:has-text('Completed')").first().click();
        page.waitForTimeout(500);
    }

    public void openHistoryPayout(int index) {
        page.locator("[data-testid='payout-row'], tr").nth(index + 1).click();
        page.waitForTimeout(500);
    }

    public void openPendingPayout(int index) {
        // The queue uses Surface cards containing 'Unsettled Balance'
        page.locator("div:has-text('Unsettled Balance')").filter(new com.microsoft.playwright.Locator.FilterOptions().setHas(page.locator("div.cursor-pointer"))).nth(index).click();
        // Alternative fallback just in case
        if (!page.locator("text=Create Payout for Balance").isVisible()) {
             page.locator(".cursor-pointer:has(text('Unsettled Balance'))").nth(index).click();
        }
        page.waitForTimeout(500);
    }

    public void createPayout() {
        page.locator("button:has-text('Create Payout'), button:has-text('New Payout')").first().click();
        page.waitForTimeout(500);
    }

    public void forceCreatePayout() {
        page.locator("label:has-text('Force create')").first().locator("input[type='checkbox']").click();
    }

    public void confirmCreateDraftPayout() {
        page.locator("button:has-text('Create Draft Payout')").first().click();
        page.waitForTimeout(1000);
    }

    public void approvePayout() {
        page.locator("button:has-text('Approve')").first().click();
        page.waitForTimeout(1000);
    }

    public void rejectPayout() {
        page.locator("button:has-text('Reject')").first().click();
        page.waitForTimeout(1000);
    }
}
