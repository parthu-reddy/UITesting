package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

public class CustomerDashboardPage {
    private final Page page;

    public CustomerDashboardPage(Page page) {
        this.page = page;
    }

    // --- Header & Global Navigation ---

    public void clickAddressHeader() {
        page.locator("span:has-text('Deliver to')").first().click();
    }

    public void clickSettingsIcon() {
        // Find the button inside the header that contains the User icon or is next to the address
        page.locator("header button").nth(1).click();
    }

    public void clickThemeToggle() {
        page.locator("button:has(svg.lucide-moon), button:has(svg.lucide-sun)").first().click();
    }
    
    public void clickViewCart() {
        page.locator("button:has-text('View Cart')").click();
    }

    // --- Address Selector Modal (Quick View) ---

    public void clickCloseAddressSelector() {
        page.locator("text=Select Delivery Location").locator("xpath=..").locator("button").click();
    }

    public void clickUseCurrentLocation() {
        page.locator("button:has-text('Use Current Location')").click();
    }

    public void clickAddNewAddress() {
        page.locator("button:has-text('Add New Address')").click();
    }

    public void clickSavedAddress(String addressLabel) {
        page.locator("div.fixed.inset-0").locator("p:has-text('" + addressLabel + "')").click();
    }

    public void clickCloseLocationPrompt() {
        page.locator("button:has-text('Understood')").click();
    }

    // --- Restaurant & Menu Interaction ---

    public void fillSearch(String query) {
        page.getByPlaceholder("Search restaurants...").fill(query);
    }

    public void clickCategory(String categoryName) {
        page.locator("button:has-text('" + categoryName + "')").click();
    }

    public void clickRestaurant(String restaurantName) {
        page.getByText(restaurantName, new Page.GetByTextOptions().setExact(true)).first().click();
    }

    public void clickBackFromRestaurant() {
        // Back arrow inside the restaurant menu
        page.locator("button:has(svg.lucide-arrow-left)").first().click();
    }

    public void clickAddToCart(String itemName) {
        Locator itemContainer = page.locator("div:has(> div > h4:has-text('" + itemName + "'))").first();
        Locator addButton = itemContainer.locator("button:has-text('Add')").first();
        
        if (addButton.isVisible()) {
            addButton.click();
        } else {
            itemContainer.locator("button:has(svg.lucide-plus)").first().click();
        }
    }

    public void clickRemoveFromCart(String itemName) {
        Locator itemContainer = page.locator("div:has(> div > h4:has-text('" + itemName + "'))").first();
        itemContainer.locator("button:has(svg.lucide-minus)").first().click();
    }

    // --- Order Tracking Overlay ---

    public void clickOrderTracking(String orderId) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*Order #" + orderId + ".*"))).click();
    }

    public void clickCloseTrackingOverlay() {
        page.locator("button:has(svg.lucide-x)").first().click();
    }

    public void clickApproveDelay() {
        page.locator("button:has-text('Approve Delay')").click();
    }

    public void clickCancelOrder() {
        page.locator("button:has-text('Cancel Order')").click();
    }

    public void clickDismissFailedOrder() {
        page.locator("button:has-text('Dismiss')").click();
    }
}
