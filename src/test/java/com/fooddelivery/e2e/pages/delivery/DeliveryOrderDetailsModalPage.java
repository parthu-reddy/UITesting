package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for the delivery order details modal.
 * Maps to: {@code DeliveryOrderDetailsModal.tsx}
 */
public class DeliveryOrderDetailsModalPage {

    private final Page page;

    public DeliveryOrderDetailsModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Order #, text=Order Details, svg.lucide-receipt").first().isVisible();
    }

    public String getOrderId() {
        return page.locator("text=Order #").first().innerText().trim();
    }

    public void close() {
        page.locator("button:has(svg.lucide-x), button:has-text('Close')").first().click();
        page.waitForTimeout(300);
    }
}
