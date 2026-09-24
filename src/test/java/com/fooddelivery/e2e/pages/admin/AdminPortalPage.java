package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/**
 * Page Object for the Admin Portal shell.
 * Maps to: {@code AdminPortal.tsx}
 */
public class AdminPortalPage {

    private final Page page;

    public AdminPortalPage(Page page) {
        this.page = page;
    }

    /** The admin shell's "Admin" heading (AdminPortal.tsx). */
    public void waitForPortal() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Admin").setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    // ── Sidebar navigation ───────────────────────────────────────────────
    //
    // SidebarNav renders one <button> per item inside the admin <nav>. The labels are
    // AdminPortal.tsx's, verbatim. "Manual Interventions" may carry a count badge, so the label
    // is matched at the start of the name.

    private void nav(String label) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.NAVIGATION)
                .getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName(Pattern.compile("^" + Pattern.quote(label) + "\\b")))
                .click();
        page.waitForTimeout(300);
    }

    public void openLiveOpsTab() { nav("Live Operations"); }

    public void openSupportTab() { nav("Support Tickets"); }

    public void openInterventionsTab() { nav("Manual Interventions"); }

    public void openUsersTab() { nav("User Management"); }

    public void openCategoriesTab() { nav("Categories"); }

    public void openFleetTab() { nav("Fleet Map"); }

    public void openLedgerTab() { nav("Ledger Entries"); }

    public void openPayoutsTab() { nav("Pending Payouts"); }

    public void openMoneyOperationsTab() { nav("Money Operations"); }

    public void openReviewsTab() { nav("Review Moderation"); }

    /**
     * Admin has no campaigns screen: campaigns are a RESTAURANT tab (RestaurantTabPanels ->
     * CampaignManagement). See e2e-plan/NOT-DEFECTS/README.md.
     */
    public void openCampaignsTab() {
        throw new UnsupportedOperationException("The admin portal has no campaigns screen; campaigns are a restaurant tab");
    }

    public void openRefundsTab() { nav("Refund Queue"); }
}
