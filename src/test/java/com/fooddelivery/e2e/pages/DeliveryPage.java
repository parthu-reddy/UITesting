package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

public class DeliveryPage {
    private final Page page;

    public DeliveryPage(Page page) {
        this.page = page;
    }

    public void goOnline() {
        // If already online, return
        try {
            page.waitForTimeout(2000); // wait for state to load from API
            if (page.locator("button:has-text('Online / Finding Orders')").isVisible()) {
                System.out.println("Rider is already online.");
                return;
            }
            // Find the "Offline" button to toggle online status
            page.locator("button:has-text('Offline')").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            page.locator("button:has-text('Offline')").click();
            page.waitForTimeout(4000);
            
            if (page.locator("button:has-text('Enable Permissions')").isVisible()) {
                page.locator("button:has-text('Enable Permissions')").click();
                page.waitForTimeout(6000);
            }
            
            // Permissions are auto-granted by Playwright context
        } catch (Exception e) {
            System.err.println("Could not go online: " + e.getMessage());
        }
    }

    public void acceptDelivery() {
        // Find the "Accept Order" or "Accept & Open Map" button
        page.locator("button:has-text('Accept Order'), button:has-text('Accept & Open Map')").first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("button:has-text('Accept Order'), button:has-text('Accept & Open Map')").first().click();
        page.waitForTimeout(500);
    }

    public void markPickedUp(String pickupOtp) {
        page.locator("input[placeholder='Enter 6-digit pickup OTP']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("input[placeholder='Enter 6-digit pickup OTP']").fill(pickupOtp);
        page.locator("button", new Page.LocatorOptions().setHasText("Verify & Pick Up")).first().click();
        page.waitForTimeout(500);
    }

    public void markDelivered(String deliveryOtp) {
        page.locator("input[placeholder='Ask customer for 6-digit OTP']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("input[placeholder='Ask customer for 6-digit OTP']").fill(deliveryOtp);
        page.locator("button", new Page.LocatorOptions().setHasText("Verify & Deliver")).first().click();
        page.waitForTimeout(500);
    }
}
