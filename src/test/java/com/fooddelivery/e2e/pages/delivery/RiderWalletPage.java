package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for Rider Wallet.
 * Maps to: {@code RiderWalletSection.tsx}
 */
public class RiderWalletPage {

    private final Page page;

    public RiderWalletPage(Page page) {
        this.page = page;
    }

    public boolean isWalletVisible() {
        return page.locator("text=Wallet, text=Balance").first().isVisible();
    }

    public String getWalletBalance() {
        return page.locator("text=Balance").locator("xpath=..").locator("span").last().innerText().trim();
    }
}
