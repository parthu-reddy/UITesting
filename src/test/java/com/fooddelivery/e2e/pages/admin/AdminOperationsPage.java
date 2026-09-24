package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Operations page (Reconciliation, DLQ, Rejections).
 * Maps to: {@code OperationsPage.tsx}
 * <p>
 * Tabs: rejections | reconciliation | payment_dlq | wallet_dlq
 * </p>
 */
public class AdminOperationsPage {

    private final Page page;

    public AdminOperationsPage(Page page) {
        this.page = page;
    }

    // Maps to pages/admin/money/OperationsPage.tsx ("Money Operations"): a tablist of four.

    public boolean isOperationsVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Money Operations").setExact(true)).isVisible();
    }

    private void tab(String name) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.TAB,
                new Page.GetByRoleOptions().setName(name).setExact(true)).click();
        page.waitForTimeout(500);
    }

    public void openRejectionsTab() { tab("Ledger Rejections"); }

    public void openReconciliationTab() { tab("Reconciliation Runs"); }

    public void openPaymentDlqTab() { tab("Payment DLQ"); }

    public void openWalletDlqTab() { tab("Wallet DLQ"); }

    public void resolveRejection(int index, String note) {
        page.locator("[data-testid='rejection-row'], tr").nth(index + 1).click();
        page.locator("input[placeholder*='note'], textarea").first().fill(note);
        page.locator("button:has-text('Resolve')").first().click();
        page.waitForTimeout(1000);
    }
}
