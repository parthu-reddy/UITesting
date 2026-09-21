package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the shared Transaction History Table.
 * Maps to: {@code TransactionHistoryTable.tsx}
 * <p>
 * Shared table used by settings (wallet tab), admin ledger, rider earnings.
 * </p>
 */
public class TransactionHistoryTablePage {

    private final Page page;

    public TransactionHistoryTablePage(Page page) {
        this.page = page;
    }

    public boolean isTableVisible() {
        return page.locator("table, [data-testid='transaction-table']").first().isVisible();
    }

    public int getRowCount() {
        return page.locator("table tbody tr, [data-testid='transaction-row']").count();
    }

    public String getRowAmount(int index) {
        return page.locator("table tbody tr, [data-testid='transaction-row']").nth(index)
                .locator("td").last().innerText().trim();
    }
}
