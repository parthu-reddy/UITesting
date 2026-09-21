package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the outlet selector modal.
 * Maps to: {@code CustomerOutletSelectorModal.tsx}
 */
public class CustomerOutletSelectorModalPage {

    private final Page page;

    public CustomerOutletSelectorModalPage(Page page) {
        this.page = page;
    }

    public boolean isModalOpen() {
        return page.locator("text=Select Outlet, text=Choose Outlet, text=outlet").first().isVisible();
    }

    public int getOutletCount() {
        return page.locator("[data-testid='outlet-option'], .outlet-card").count();
    }

    public void selectOutlet(String outletName) {
        page.locator("button:has-text('" + outletName + "'), div:has-text('" + outletName + "')").first().click();
        page.waitForTimeout(500);
    }

    public void selectOutletByIndex(int index) {
        page.locator("[data-testid='outlet-option'], .outlet-card").nth(index).click();
        page.waitForTimeout(500);
    }
}
