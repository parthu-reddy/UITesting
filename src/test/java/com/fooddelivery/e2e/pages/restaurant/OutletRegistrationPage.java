package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for outlet registration.
 * Maps to: {@code OutletRegistration.tsx}
 */
public class OutletRegistrationPage {

    private final Page page;

    public OutletRegistrationPage(Page page) {
        this.page = page;
    }

    public boolean isRegistrationVisible() {
        return page.locator("text=Register Outlet, text=Add Outlet, text=Outlet Name").first().isVisible();
    }

    public void fillOutletName(String name) {
        page.locator("input[placeholder*='Outlet'], input[placeholder*='outlet']").first().fill(name);
    }

    public void fillAddress(String address) {
        page.locator("input[placeholder*='Address'], input[placeholder*='address'], textarea").first().fill(address);
    }

    public void submit() {
        page.locator("button:has-text('Register'), button:has-text('Create'), button:has-text('Add')").first().click();
        page.waitForTimeout(2000);
    }
}
