package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Refund Queue.
 * Maps to: {@code RefundQueue.tsx, RefundTicketPanel.tsx}
 */
public class AdminRefundQueuePage {

    private final Page page;

    public AdminRefundQueuePage(Page page) {
        this.page = page;
    }

    public boolean isRefundQueueVisible() {
        return page.locator("text=Refund Queue, text=Refunds, text=Pending Refunds").first().isVisible();
    }

    public int getRefundCount() {
        return page.locator("[data-testid='refund-row'], .refund-card, tr").count() - 1; // subtract header
    }

    public void openRefundTicket(int index) {
        page.locator("[data-testid='refund-row'], tr").nth(index + 1).click();
        page.waitForTimeout(500);
    }

    public void approveRefund() {
        page.locator("button:has-text('Approve Refund'), button:has-text('Approve')").first().click();
        page.waitForTimeout(1000);
    }

    public void rejectRefund() {
        page.locator("button:has-text('Reject'), button:has-text('Deny')").first().click();
        page.waitForTimeout(1000);
    }
}
