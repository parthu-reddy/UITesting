package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;

/**
 * Page Object for Customer Order History.
 * Maps to: {@code CustomerOrderHistory.tsx, OrderCard.tsx}
 */
public class CustomerOrderHistoryPage {

    private final Page page;

    public CustomerOrderHistoryPage(Page page) {
        this.page = page;
    }

    public void waitForHistoryLoad() {
        page.waitForTimeout(2000); // Wait for API to load order history
    }

    public int getOrderCount() {
        return page.locator("[data-testid='order-card'], .order-card, div:has(> text='Order #')").count();
    }

    public boolean hasOrderWithStatus(String status) {
        return page.locator("text=" + status).isVisible();
    }

    public void clickOrderCard(int index) {
        page.locator("[data-testid='order-card'], .order-card").nth(index).click();
        page.waitForTimeout(500);
    }

    public boolean hasReorderButton() {
        return page.locator("button:has-text('Reorder')").first().isVisible();
    }

    public void clickReorder() {
        page.locator("button:has-text('Reorder')").first().click();
        page.waitForTimeout(500);
    }
}
