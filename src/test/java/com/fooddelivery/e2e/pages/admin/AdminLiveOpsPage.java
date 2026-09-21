package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminLiveOperations.tsx}
 * <p>
 * Admin live operations: order list with pagination, driver assignment panel,
 * partial/post-delivery refunds, map view.
 * </p>
 */
public class AdminLiveOpsPage {
    private final Page page;
    public AdminLiveOpsPage(Page page) { this.page = page; }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isLiveOpsVisible() {
        return page.locator("text=Live Operations, text=Active Orders, text=Live Map").first().isVisible();
    }

    // ── Order list ───────────────────────────────────────────────────────

    public int getActiveOrderCount() {
        return page.locator("[data-testid='live-order'], .live-order-card, button:has(text='#')").count();
    }

    public void selectOrder(int index) {
        page.locator("button:has(p:has-text('#'))").nth(index).click();
        page.waitForTimeout(500);
    }

    public void selectOrderById(String orderIdPrefix) {
        page.locator("button:has(p:has-text('#" + orderIdPrefix + "'))").first().click();
        page.waitForTimeout(500);
    }

    public boolean isOrderSelected() {
        return page.locator("text=Order #, text=Refund Actions").first().isVisible();
    }

    public void refresh() {
        page.locator("button:has-text('Refresh')").first().click();
        page.waitForTimeout(1000);
    }

    // ── Pagination ───────────────────────────────────────────────────────

    public void nextPage() {
        page.locator("button:has-text('Next')").first().click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        page.locator("button:has-text('Prev')").first().click();
        page.waitForTimeout(500);
    }

    public String getPageInfo() {
        return page.locator("text=Page").first().innerText().trim();
    }

    // ── Driver assignment ────────────────────────────────────────────────

    public int getAvailableDriverCount() {
        return page.locator("text=Available Drivers").first()
                .locator("xpath=..").locator("button:has-text('Assign')").count();
    }

    public void assignDriver(int driverIndex) {
        page.locator("button:has-text('Assign')").nth(driverIndex).click();
        page.waitForTimeout(2000);
    }

    // ── Refund actions ───────────────────────────────────────────────────

    public void fillRefundAmount(String amount) {
        page.locator("input[type='number'][placeholder*='Amount']").first().fill(amount);
    }

    public void clickPartialRefund() {
        page.locator("button:has-text('Partial Refund')").first().click();
        page.waitForTimeout(2000);
    }

    public void clickPostDeliveryRefund() {
        page.locator("button:has-text('Post-Delivery')").first().click();
        page.waitForTimeout(2000);
    }

    public void initiatePartialRefund(String amount) {
        fillRefundAmount(amount);
        clickPartialRefund();
    }

    public void initiatePostDeliveryRefund(String amount) {
        fillRefundAmount(amount);
        clickPostDeliveryRefund();
    }
}
