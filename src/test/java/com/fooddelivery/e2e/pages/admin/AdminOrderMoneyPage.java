package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Order Money view.
 * Maps to: {@code AdminOrderMoney.tsx}
 * <p>
 * Read-only breakdown of order money: customer paid, restaurant payout, rider payout.
 * Used to verify financial integrity in E2E tests.
 * </p>
 */
public class AdminOrderMoneyPage {

    private final Page page;

    public AdminOrderMoneyPage(Page page) {
        this.page = page;
    }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isOrderMoneyVisible() {
        return page.locator("text=Customer Paid, text=Restaurant Payout, text=Rider Payout").first().isVisible();
    }

    // ── Customer section ─────────────────────────────────────────────────

    public String getCustomerTotal() {
        return page.locator("text=Customer Paid").locator("xpath=../../..").locator("text=Total").locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getFoodCost() {
        return page.locator("text=Food Cost").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getDeliveryFee() {
        return page.locator("text=Delivery Fee").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getPlatformFee() {
        return page.locator("text=Platform Fee").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getTaxes() {
        return page.locator("text=Taxes, text=SGST").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Restaurant section ───────────────────────────────────────────────

    public String getRestaurantNetPayout() {
        return page.locator("text=Net Payout").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Rider section ────────────────────────────────────────────────────

    public String getRiderNetPayout() {
        return page.locator("text=Net Rider, text=Final Rider").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Platform revenue ─────────────────────────────────────────────────

    public String getPlatformRevenue() {
        return page.locator("text=Platform Revenue, text=Platform Net").first().locator("xpath=..").locator("span").last().innerText().trim();
    }
}
