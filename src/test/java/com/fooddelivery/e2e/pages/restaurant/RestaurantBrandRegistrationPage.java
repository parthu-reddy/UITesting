package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for restaurant brand registration.
 * Maps to: {@code BrandRegistration.tsx}
 */
public class RestaurantBrandRegistrationPage {

    private final Page page;

    public RestaurantBrandRegistrationPage(Page page) {
        this.page = page;
    }

    public boolean isRegistrationVisible() {
        return page.locator("text=Register Brand, text=Brand Name, text=Create Brand").first().isVisible();
    }

    public void fillBrandName(String name) {
        page.locator("input[placeholder*='Brand'], input[placeholder*='brand'], input[placeholder*='name']").first().fill(name);
    }

    public void fillDescription(String desc) {
        page.locator("textarea").first().fill(desc);
    }

    public void submit() {
        page.locator("button:has-text('Register'), button:has-text('Create'), button:has-text('Submit')").first().click();
        page.waitForTimeout(2000);
    }
}
