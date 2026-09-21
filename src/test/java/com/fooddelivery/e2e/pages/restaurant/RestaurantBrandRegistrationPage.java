package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
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
        try {
            Locator btn = page.locator("button:has-text('Register New Brand')").first();
            if (btn.isVisible()) {
                btn.click();
            }
        } catch (Exception ignored) { }

        Locator loc = page.locator("text=New Brand Registration").first();
        try {
            loc.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void fillBrandName(String name) {
        page.locator("input[placeholder*='e.g. KFC']").first().fill(name);
    }

    public void fillDescription(String desc) {
        // Obsolete in new UI, ignored
    }

    public void submit() {
        // Step 1
        Locator inputs = page.locator("input[type='text']");
        String randomStr = String.valueOf(System.currentTimeMillis() % 100000);
        inputs.nth(1).fill("29ABCDE" + randomStr + "1Z5"); // GSTIN
        inputs.nth(2).fill("ABCDE" + randomStr); // PAN
        inputs.nth(3).fill("U12345KA2023PTC" + randomStr + "6"); // CIN
        
        page.locator("button:has-text('Next')").first().click();
        page.waitForTimeout(1000);

        // Step 2
        Locator bankInputs = page.locator("input[type='text']");
        bankInputs.nth(0).fill("1234567890");
        bankInputs.nth(1).fill("HDFC0000001");
        
        page.locator("button:has-text('Complete Registration')").first().click();
        page.waitForTimeout(2000);
    }
}
