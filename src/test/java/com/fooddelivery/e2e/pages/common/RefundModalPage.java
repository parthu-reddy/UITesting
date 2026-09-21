package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Refund Modal (admin/shared).
 * Maps to: {@code RefundModal.tsx}
 * <p>
 * Full refund or partial refund with item selection, reason, and two-step flow
 * (quote request → final confirmation).
 * </p>
 */
public class RefundModalPage {

    private final Page page;

    public RefundModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Refund, text=Process Refund, text=Refund Type").first().isVisible();
    }

    public void selectFullRefund() {
        page.locator("button:has-text('Full'), label:has-text('Full')").first().click();
        page.waitForTimeout(300);
    }

    public void selectPartialRefund() {
        page.locator("button:has-text('Partial'), label:has-text('Partial')").first().click();
        page.waitForTimeout(300);
    }

    public void selectReason(String reason) {
        page.locator("select, [role='combobox']").first().selectOption(reason);
    }

    public void fillDescription(String desc) {
        page.locator("textarea").first().fill(desc);
    }

    public void requestQuote() {
        page.locator("button:has-text('Get Quote'), button:has-text('Request')").first().click();
        page.waitForTimeout(2000);
    }

    public void confirmRefund() {
        page.locator("button:has-text('Confirm'), button:has-text('Process')").first().click();
        page.waitForTimeout(2000);
    }

    public boolean isRefundSuccess() {
        return page.locator("text=success, text=processed, text=approved").first().isVisible();
    }
}
