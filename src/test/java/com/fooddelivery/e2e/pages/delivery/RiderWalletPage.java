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

    /** "Earnings Wallet" with the balance beside it (RiderWalletSection.tsx). */
    private com.microsoft.playwright.Locator heading() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Earnings Wallet").setExact(true));
    }

    public boolean isWalletVisible() {
        return heading().isVisible();
    }

    public String getWalletBalance() {
        return heading().locator("xpath=..").locator("span").last().innerText().trim();
    }
}
