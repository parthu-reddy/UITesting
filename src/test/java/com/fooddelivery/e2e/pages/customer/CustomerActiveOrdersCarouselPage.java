package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;

/**
 * Page Object for the active orders carousel strip.
 * Maps to: {@code CustomerActiveOrdersCarousel.tsx}
 * <p>
 * Shows live in-flight orders as a horizontal strip above the bottom navigation.
 * </p>
 */
public class CustomerActiveOrdersCarouselPage {

    private final Page page;

    public CustomerActiveOrdersCarouselPage(Page page) {
        this.page = page;
    }

    public boolean isCarouselVisible() {
        return page.locator("[data-testid='active-orders-carousel']").first().isVisible();
    }

    public int getActiveOrderCount() {
        // Each order card in the carousel is a Surface with status pill
        return page.locator("[data-testid='active-order-card']").count();
    }

    /**
     * Clicks on the nth active order card to open the order tracker.
     */
    public void openOrder(String orderId) {
        page.locator("[data-testid='active-order-card'][data-order-id='" + orderId + "']").click();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                page.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']")).isVisible();
    }

    public void openOrder(int index) {
        page.locator("[data-testid='active-order-card']").nth(index).click();

    }
}
