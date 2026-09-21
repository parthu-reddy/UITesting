package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for Rider Earnings.
 * Maps to: {@code RiderEarnings.tsx}
 */
public class RiderEarningsPage {

    private final Page page;

    public RiderEarningsPage(Page page) {
        this.page = page;
    }

    public boolean isEarningsVisible() {
        return page.locator("text=Earnings, text=Today, text=Total").first().isVisible();
    }

    public String getTodayEarnings() {
        return page.locator("text=Today").locator("xpath=..").locator("span").last().innerText().trim();
    }
}
