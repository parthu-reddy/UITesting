package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Customer Home and Restaurant Browser.
 * Maps to: {@code CustomerHomeChrome.tsx, CustomerMainView.tsx, CustomerRestaurantBrowser.tsx}
 */
public class CustomerHomePage {

    private final Page page;

    public CustomerHomePage(Page page) {
        this.page = page;
    }

    // ── Address selection ────────────────────────────────────────────────

    public void selectAddress(String addressLabel) {
        try {
            // Wait briefly for the forced modal to animate in on first login
            page.waitForTimeout(1500);

            // Check if the modal is already open
            if (!page.locator("text=Delivery Location").isVisible()) {
                page.locator("text=Deliver to").first()
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(15000));

                page.locator("text=Deliver to").first().click(new Locator.ClickOptions().setForce(true));
            }

            // Wait for address modal
            page.locator("text=Delivery Location").waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            page.locator("p:has-text('" + addressLabel + "')").first().click();

            // Wait for modal to close
            page.locator("text=Delivery Location").waitFor(
                    new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            System.err.println("[CUSTOMER] Failed in selectAddress: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("target/screenshots/select-address-timeout.png")));
            throw e;
        }
    }

    // ── Restaurant browsing ──────────────────────────────────────────────

    public void openRestaurant(String brandName) {
        page.locator("h5")
                .filter(new Locator.FilterOptions()
                        .setHasText(java.util.regex.Pattern.compile("^" + brandName + "$")))
                .first().click();
        page.waitForTimeout(500);
    }

    public void searchRestaurant(String query) {
        Locator search = page.locator("input[placeholder*='Search'], input[type='search']").first();
        search.fill(query);
        page.waitForTimeout(500);
    }

    public int getVisibleRestaurantCount() {
        return page.locator("h5").count(); // Restaurant cards use h5 for brand name
    }

    public boolean isRestaurantVisible(String brandName) {
        return page.locator("h5:has-text('" + brandName + "')").isVisible();
    }
}
