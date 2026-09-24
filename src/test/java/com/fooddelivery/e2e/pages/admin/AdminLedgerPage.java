package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminLedgerView.tsx, LedgerStatementPanel.tsx}
 * <p>
 * Admin ledger: transaction search, filtering (transactionId, ownerId, ownerType,
 * category, direction), pagination, statement detail panel, copy actions.
 * </p>
 */
public class AdminLedgerPage {
    private final Page page;
    public AdminLedgerPage(Page page) { this.page = page; }

    // Maps to AdminLedgerView.tsx: a filter form over a table, paged by chevron buttons.

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isLedgerVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Ledger Entries").setExact(true)).isVisible();
    }

    // ── Filters ──────────────────────────────────────────────────────────

    public void filterByTransactionId(String txnId) {
        page.getByPlaceholder("Transaction ID").fill(txnId);
    }

    public void filterByOwnerId(String ownerId) {
        page.getByPlaceholder("Owner ID").fill(ownerId);
    }

    /** The three filters are custom Selects, named by their placeholders; options are the enum values. */
    private void choose(String comboName, String option) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(comboName).setExact(true)).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(option).setExact(true)).click();
    }

    public void selectOwnerType(String type) { choose("All Owner Types", type); }

    /** Category options read the enum with spaces: DELIVERY_FEE shows as "DELIVERY FEE". */
    public void selectCategory(String category) { choose("All Categories", category.replace('_', ' ')); }

    public void selectDirection(String direction) { choose("All Directions", direction); }

    public void applyFilter() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Apply Filters").setExact(true)).click();
        page.waitForTimeout(2000);
    }

    public void clearFilters() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Clear").setExact(true)).click();
        page.waitForTimeout(1000);
    }

    // ── Transaction list ─────────────────────────────────────────────────

    /** Real rows only: loading and "No ledger transactions found." are one full-width cell. */
    private com.microsoft.playwright.Locator rows() {
        return page.locator("table tbody tr:not(:has(td[colspan]))");
    }

    public int getTransactionCount() {
        return rows().count();
    }

    /**
     * The ledger has no statement or detail panel: a row's only action is copying its
     * transaction id. LEDGER-ADV-11 is recorded in e2e-plan/NOT-DEFECTS/README.md.
     */
    public void openStatement(int index) {
        throw new UnsupportedOperationException("AdminLedgerView has no statement/detail panel");
    }

    public boolean isStatementPanelOpen() {
        throw new UnsupportedOperationException("AdminLedgerView has no statement/detail panel");
    }

    // ── Pagination: icon-only chevrons either side of "Page n of m" ────

    public void nextPage() {
        page.locator("button:has(svg.lucide-chevron-right)").first().click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        page.locator("button:has(svg.lucide-chevron-left)").first().click();
        page.waitForTimeout(500);
    }

    public String getPageInfo() {
        return page.getByText(java.util.regex.Pattern.compile("^Page \\d+ of \\d+$")).first().innerText().trim();
    }
}
