package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Admin Portal shell.
 * Maps to: {@code AdminPortal.tsx}
 */
public class AdminPortalPage {

    private final Page page;

    public AdminPortalPage(Page page) {
        this.page = page;
    }

    public void waitForPortal() {
        page.locator("text=Admin, text=Live Ops, text=Users, text=Dashboard").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openLiveOpsTab() {
        page.locator("button:has-text('Live Ops'), [role='tab']:has-text('Live Ops')").first().click();
        page.waitForTimeout(300);
    }

    public void openUsersTab() {
        page.locator("button:has-text('Users'), [role='tab']:has-text('Users')").first().click();
        page.waitForTimeout(300);
    }

    public void openLedgerTab() {
        page.locator("button:has-text('Ledger'), [role='tab']:has-text('Ledger')").first().click();
        page.waitForTimeout(300);
    }

    public void openReviewsTab() {
        page.locator("button:has-text('Reviews'), [role='tab']:has-text('Reviews')").first().click();
        page.waitForTimeout(300);
    }

    public void openCampaignsTab() {
        page.locator("button:has-text('Campaigns'), [role='tab']:has-text('Campaigns')").first().click();
        page.waitForTimeout(300);
    }

    public void openSupportTab() {
        page.locator("button:has-text('Support'), [role='tab']:has-text('Support')").first().click();
        page.waitForTimeout(300);
    }

    public void openRefundsTab() {
        page.locator("button:has-text('Refunds'), [role='tab']:has-text('Refunds')").first().click();
        page.waitForTimeout(300);
    }

    public void openFleetTab() {
        page.locator("button:has-text('Fleet'), [role='tab']:has-text('Fleet')").first().click();
        page.waitForTimeout(300);
    }
}
