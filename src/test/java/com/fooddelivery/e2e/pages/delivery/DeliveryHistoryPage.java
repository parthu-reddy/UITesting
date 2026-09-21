package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for Delivery History.
 * Maps to: {@code DeliveryHistoryPanel.tsx}
 */
public class DeliveryHistoryPage {

    private final Page page;

    public DeliveryHistoryPage(Page page) {
        this.page = page;
    }

    public boolean isHistoryVisible() {
        return page.locator("text=Delivery History, text=Past Deliveries, text=Completed").first().isVisible();
    }

    public int getDeliveryCount() {
        return page.locator("[data-testid='delivery-history-item'], .delivery-card").count();
    }
}
