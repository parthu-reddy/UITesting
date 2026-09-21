package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Refund Request Modal.
 * Maps to: {@code RefundRequestModal.tsx}
 */
public class RefundRequestModalPage {

    private final Page page;

    public RefundRequestModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Request Refund, text=Refund Request").first().isVisible();
    }

    public void selectReason(String reason) {
        page.locator("button:has-text('" + reason + "')").first().click();
        page.waitForTimeout(300);
    }

    public void fillDetails(String details) {
        page.locator("textarea").first().fill(details);
    }

    public void submit() {
        page.locator("button:has-text('Submit'), button:has-text('Request Refund')").first().click();
        page.waitForTimeout(2000);
    }

    public boolean isSuccess() {
        return page.locator("text=submitted, text=received, text=under review").first().isVisible();
    }
}
