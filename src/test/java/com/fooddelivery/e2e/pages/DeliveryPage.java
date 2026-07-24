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
            if (page.locator("button:has-text('Online Duty')").isVisible()) {
                System.out.println("Rider is already online.");
                return;
            }
            // Find the "Offline" button to toggle online status
            page.locator("button:has-text('Offline')").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            page.locator("button:has-text('Offline')").click();
            page.waitForTimeout(1000);
            
            // Check if Grant Permissions is shown and click it
            if (page.locator("button:has-text('Grant Permissions & Go Online')").isVisible()) {
                page.locator("button:has-text('Grant Permissions & Go Online')").click();
                page.waitForTimeout(1000);
            }
        } catch (Exception e) {
            System.err.println("Could not go online: " + e.getMessage());
        }
    }

    public void acceptDelivery() {
        // Find the "Accept Delivery" button (or similar text)
        page.locator("text=Accept Delivery").first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("text=Accept Delivery").first().click();
        page.waitForTimeout(500);
    }

    public void markPickedUp(String otp) {
        // Find "Confirm Pickup & Start Driving" button
        // Need to fill the OTP first
        page.locator("input[placeholder='Enter 6-digit pickup OTP']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("input[placeholder='Enter 6-digit pickup OTP']").fill(otp);
        
        page.locator("button", new Page.LocatorOptions().setHasText("Confirm Pickup")).first().click();
        page.waitForTimeout(500);
    }

    public void markDelivered(String otp) {
        // Need to fill the delivery OTP
        page.locator("input[placeholder='Ask customer for 6-digit OTP']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.locator("input[placeholder='Ask customer for 6-digit OTP']").fill(otp);
        
        page.locator("button", new Page.LocatorOptions().setHasText("Confirm Delivery")).first().click();
        page.waitForTimeout(500);
    }
}
