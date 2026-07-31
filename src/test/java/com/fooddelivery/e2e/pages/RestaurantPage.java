package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class RestaurantPage {
    private final Page page;

    public RestaurantPage(Page page) {
        this.page = page;
    }

    public void selectOutlet(String outletName) {
        // The select element has options with the outlet names
        page.locator("select").selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(outletName));
        page.waitForTimeout(500);
    }

    public void acceptOrder() {
        page.locator("button:has-text('Accept Order'), button:has-text('Accept')").first().click();
        page.waitForTimeout(500);
    }

    public void startPreparing() {
        page.locator("button:has-text('Start Cook')").first().click();
        page.waitForTimeout(500);
    }

    public void markReadyForPickup() {
        page.locator("button:has-text('Mark Prepared')").first().click();
        page.waitForTimeout(500);
    }

    public String getPickupOtp() {
        // Refresh orders to ensure we have the latest status in case SSE is slow/failed
        page.locator("button[title='Refresh Orders']").click();
        page.waitForTimeout(1000);
        return page.locator("text=Handover OTP:").locator("xpath=..").locator("span.font-mono").innerText().trim();
    }
}
