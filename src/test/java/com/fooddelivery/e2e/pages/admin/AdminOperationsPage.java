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

    public boolean isOperationsVisible() {
        return page.locator("text=Rejections, text=Reconciliation, text=Operations").first().isVisible();
    }

    public void openRejectionsTab() {
        page.locator("button:has-text('Rejections')").first().click();
        page.waitForTimeout(500);
    }

    public void openReconciliationTab() {
        page.locator("button:has-text('Reconciliation')").first().click();
        page.waitForTimeout(500);
    }

    public void openPaymentDlqTab() {
        page.locator("button:has-text('Payment DLQ'), button:has-text('Payment')").first().click();
        page.waitForTimeout(500);
    }

    public void openWalletDlqTab() {
        page.locator("button:has-text('Wallet DLQ'), button:has-text('Wallet')").first().click();
        page.waitForTimeout(500);
    }

    public void resolveRejection(int index, String note) {
        page.locator("[data-testid='rejection-row'], tr").nth(index + 1).click();
        page.locator("input[placeholder*='note'], textarea").first().fill(note);
        page.locator("button:has-text('Resolve')").first().click();
        page.waitForTimeout(1000);
    }
}
