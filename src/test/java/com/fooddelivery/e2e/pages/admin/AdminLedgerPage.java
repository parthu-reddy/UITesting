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

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isLedgerVisible() {
        return page.locator("text=Ledger, text=Transactions, text=Statement").first().isVisible();
    }

    // ── Filters ──────────────────────────────────────────────────────────

    public void filterByTransactionId(String txnId) {
        page.locator("input[placeholder*='Transaction'], input[placeholder*='txn']").first().fill(txnId);
    }

    public void filterByOwnerId(String ownerId) {
        page.locator("input[placeholder*='Owner ID'], input[placeholder*='owner']").first().fill(ownerId);
    }

    public void selectOwnerType(String type) {
        page.locator("select").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHas(page.locator("option:has-text('Owner Type'), option:has-text('Type')"))).first()
                .selectOption(type);
    }

    public void selectCategory(String category) {
        page.locator("select").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHas(page.locator("option:has-text('Category'), option:has-text('All Categories')"))).first()
                .selectOption(category);
    }

    public void selectDirection(String direction) {
        page.locator("select").filter(new com.microsoft.playwright.Locator.FilterOptions()
                .setHas(page.locator("option:has-text('Direction'), option:has-text('All Directions')"))).first()
                .selectOption(direction);
    }

    public void applyFilter() {
        page.locator("button:has-text('Search'), button:has-text('Apply'), button:has(svg.lucide-search)").first().click();
        page.waitForTimeout(2000);
    }

    public void clearFilters() {
        page.locator("button:has-text('Clear'), button:has-text('Reset')").first().click();
        page.waitForTimeout(1000);
    }

    public void selectDateRange(String from, String to) {
        page.locator("input[type='date']").first().fill(from);
        page.locator("input[type='date']").last().fill(to);
        page.waitForTimeout(500);
    }

    // ── Transaction list ─────────────────────────────────────────────────

    public int getTransactionCount() {
        return page.locator("table tbody tr, [data-testid='ledger-row']").count();
    }

    public void openStatement(int index) {
        page.locator("table tbody tr, [data-testid='ledger-row']").nth(index).click();
        page.waitForTimeout(500);
    }

    public boolean isStatementPanelOpen() {
        return page.locator("text=Statement, text=Transaction Detail, text=Entries").first().isVisible();
    }

    // ── Pagination ───────────────────────────────────────────────────────

    public void nextPage() {
        page.locator("button:has(svg.lucide-chevron-right), button:has-text('Next')").first().click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        page.locator("button:has(svg.lucide-chevron-left), button:has-text('Prev')").first().click();
        page.waitForTimeout(500);
    }

    public String getPageInfo() {
        return page.locator("text=Page").first().innerText().trim();
    }
}
