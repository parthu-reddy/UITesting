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

    /** The order list's "Active Orders" header is always rendered (AdminLiveOperations.tsx). */
    public boolean isLiveOpsVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Orders").setExact(true)).isVisible();
    }

    // ── Order list ───────────────────────────────────────────────────────

    /** One button per active order, in the list under the "Active Orders" header. */
    private com.microsoft.playwright.Locator orders() {
        return page.locator("div:has(> h3:text-is('Active Orders')) + div > button");
    }

    public int getActiveOrderCount() {
        return orders().count();
    }

    public void selectOrder(int index) {
        orders().nth(index).click();
        page.waitForTimeout(500);
    }

    public void selectOrderById(String orderIdPrefix) {
        page.locator("button:has(p:has-text('#" + orderIdPrefix + "'))").first().click();
        page.waitForTimeout(500);
    }

    /** A selected order opens its panel, which carries "Refund Actions". */
    public boolean isOrderSelected() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Refund Actions").setExact(true)).isVisible();
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
