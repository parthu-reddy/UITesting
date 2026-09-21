package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the restaurant order details modal.
 * Maps to: {@code RestaurantOrderDetailsModal.tsx}
 */
public class RestaurantOrderDetailsModalPage {

    private final Page page;

    public RestaurantOrderDetailsModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Order #, text=Order Details").first().isVisible();
    }

    public String getOrderId() {
        return page.locator("text=Order #").first().innerText().trim();
    }

    public String getCustomerName() {
        return page.locator("text=Customer").locator("xpath=..").locator("span, p").last().innerText().trim();
    }

    public void close() {
        page.locator("button:has(svg.lucide-x), button:has-text('Close')").first().click();
        page.waitForTimeout(300);
    }
}
