package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

public class CustomerPage {
    private final Page page;

    public CustomerPage(Page page) {
        this.page = page;
    }

    public void selectAddress(String addressLabel) {
        try {
            // Wait for dashboard header
            page.locator("span:has-text('Deliver to')").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            
            // Open Location modal
            page.locator("span:has-text('Deliver to')").click();
            
            // Click the specific address inside the modal
            page.locator("div.fixed.inset-0").locator("p:has-text('" + addressLabel + "')").click();
            
            // Wait for modal to close (Deliver To is visible again)
            page.locator("text=Select Delivery Location").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            System.err.println("Failed in selectAddress: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/select-address-timeout.png")));
            throw e;
        }
    }

    public void openRestaurant(String restaurantName) {
        // Search and exact match the restaurant title, click the first one if multiple
        page.getByText(restaurantName, new Page.GetByTextOptions().setExact(true)).first().click();
        page.waitForTimeout(500); // UI animation delay
    }

    public void addItemToCart(String category, String itemName) {
        // Wait for the restaurant menu to load
        page.locator("text=" + category).first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        
        // Ensure the item exists
        page.locator("text=" + itemName).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        
        // Assuming there is an "Add" button adjacent/inside the item container
        // We will click the first exact "Add" button on the screen for simplicity, 
        // as the Happy Path assumes empty cart and straight adding.
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^Add$"))).first().click();
        
        // Wait for cart popup to appear
        page.locator("text=Items Added").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void placeOrder() {
        // View Cart
        page.locator("text=View Cart").click();
        
        // Wait for Cart drawer to animate up
        page.waitForTimeout(1000);
        
        // Address is already set from the deliver-to modal in selectAddress(),
        // so no need to fill it again here in the cart.
        
        // Place Order button in Cart
        page.locator("button", new Page.LocatorOptions().setHasText("Place Cash-on-Delivery Order")).click();
        
        page.waitForTimeout(1000); // wait for checkout modal
        page.locator("button", new Page.LocatorOptions().setHasText("Pay Securely")).click();
        
        // Wait for cart to close (or UI to switch to orders tab)
        page.waitForTimeout(2000);
    }

    public void verifyOrderStatus(String expectedStatus) {
        try {
            page.locator("text=" + expectedStatus).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        } catch (Exception e) {
            System.err.println("Failed to see expected status: " + expectedStatus);
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/status-timeout.png")));
            throw e;
        }
    }

    public void selectOutlet(String outletNamePartial) {
        String value = page.locator("select#outlet-select option", new Page.LocatorOptions().setHasText(outletNamePartial)).first().getAttribute("value");
        page.locator("select#outlet-select").selectOption(value);
        page.waitForTimeout(500); // UI animation delay
    }

    public String getDeliveryOtp() {
        // Wait for the delivery OTP to be visible and extract it
        page.locator("text=Secure Delivery Verification").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return page.locator("text=Secure Delivery Verification").locator("xpath=..").locator("div.bg-gradient-to-r").innerText().trim();
    }
}
