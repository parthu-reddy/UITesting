package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for Restaurant Earnings Tab.
 * Maps to: {@code RestaurantEarningsTab.tsx}
 */
public class RestaurantEarningsPage {

    private final Page page;

    public RestaurantEarningsPage(Page page) {
        this.page = page;
    }

    public boolean isEarningsVisible() {
        return page.locator("text=Earnings, text=Revenue, text=Payout").first().isVisible();
    }

    public String getTotalEarnings() {
        return page.locator("text=Total Earnings, text=Net Payout").locator("xpath=..").locator("span").last().innerText().trim();
    }
}
