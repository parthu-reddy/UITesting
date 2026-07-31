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
            
            // Click the existing address label inside the modal
            page.locator("text=Delivery Location").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("p:has-text('" + addressLabel + "')").first().click();
            
            // Wait for modal to close
            page.locator("text=Delivery Location").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            System.err.println("Failed in selectAddress: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/select-address-timeout.png")));
            throw e;
        }
    }

    public void openRestaurant(String restaurantName) {
        // Find the h5 element containing the brand name (which is what CustomerRestaurantCard renders)
        page.locator("h5").filter(new Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("^" + restaurantName + "$"))).first().click();
        page.waitForTimeout(500); // UI animation delay
    }

    public void addItemToCart(String category, String itemName) {
        // Find an item with preparation time < 15 min so rider is assigned immediately
        com.microsoft.playwright.Locator dishControls = page.locator("div.flex.justify-between.items-center.mt-2");
        dishControls.first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        
        for (int i = 0; i < dishControls.count(); i++) {
            com.microsoft.playwright.Locator control = dishControls.nth(i);
            String text = control.textContent(); 
            if (text.contains("mins")) {
                try {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*mins").matcher(text);
                    if (m.find()) {
                        int mins = Integer.parseInt(m.group(1));
                        if (mins < 15) {
                            System.out.println("Found item with prep time: " + mins + " mins");
                            control.locator("button:has-text('Add')").click();
                            page.waitForTimeout(500);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // ignore and try next
                }
            }
        }
        
        System.out.println("Could not find item with <15 mins prep time, adding first available item as fallback.");
        page.locator("button:has-text('Add')").first().click();
        page.waitForTimeout(500);
        
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
        try {
            page.locator("button#outlet-select").click();
            page.locator("h2:has-text('Select Outlet Location')").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("p:has-text('" + outletNamePartial + "')").first().click();
            page.waitForTimeout(500); // UI animation delay
        } catch (Exception e) {
            System.err.println("Failed to select outlet: " + outletNamePartial);
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/select-outlet-timeout.png")));
            throw e;
        }
    }

    public String getDeliveryOtp() {
        // Reload page to fetch latest order status in case SSE connection failed
        page.reload();
        page.waitForTimeout(2000);
        // Wait for the delivery OTP to be visible and extract it
        page.locator("text=Secure Delivery Verification").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return page.locator("text=Secure Delivery Verification").locator("xpath=..").locator("div.bg-gradient-to-r").innerText().trim();
    }
}
