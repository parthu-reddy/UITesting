package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
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
        try {
            Locator btn = page.locator("button:has-text('Register New Outlet')").first();
            if (btn.isVisible()) {
                btn.click();
            }
        } catch (Exception ignored) { }

        Locator loc = page.locator("text=New Outlet Registration").first();
        try {
            loc.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void fillOutletName(String name) {
        page.locator("input[placeholder*='Bella']").first().fill(name);
    }

    public void searchAndSelectLocation(String query) {
        page.locator("input[aria-label='Search for a place']").first().fill(query);
        page.locator(".absolute.z-50 button").first().waitFor();
        page.locator(".absolute.z-50 button").first().click();
        page.waitForTimeout(1000);
    }
    
    public void fillFssai(String fssai) {
        page.locator("input[placeholder*='FSSAI']").first().fill(fssai);
    }

    public void submit() {
        page.locator("button:has-text('Register Outlet')").first().click();
        page.waitForTimeout(2000);
    }
}
