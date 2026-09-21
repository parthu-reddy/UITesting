package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;

/**
 * Page Object for the free delivery progress tracker bar.
 * Maps to: {@code CustomerFreeDeliveryTracker.tsx}
 */
public class CustomerFreeDeliveryTrackerPage {

    private final Page page;

    public CustomerFreeDeliveryTrackerPage(Page page) {
        this.page = page;
    }

    public boolean isTrackerVisible() {
        return page.locator("text=free delivery, text=Free Delivery, svg.lucide-bike").first().isVisible();
    }

    public boolean isFreeDeliveryUnlocked() {
        return page.locator("text=Free delivery unlocked, svg.lucide-check-circle").first().isVisible();
    }

    /**
     * Gets the remaining amount text, e.g. "Add ₹120 more for free delivery".
     */
    public String getRemainingAmountText() {
        return page.locator("text=more for free, text=Add ₹").first().innerText().trim();
    }
}
