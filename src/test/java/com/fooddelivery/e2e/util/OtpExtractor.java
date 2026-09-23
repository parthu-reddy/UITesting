package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Extracts OTP values from various UI patterns across the food delivery app.
 */
public final class OtpExtractor {

    private OtpExtractor() {}

    /**
     * Extracts the customer delivery OTP from the OrderTrackerLive component.
     * <p>
     * The OTP is displayed in a gradient div next to "Secure Delivery Verification".
     * May require a page reload if SSE connection has dropped.
     * </p>
     *
     * @param page the customer's Playwright page
     * @return the 6-digit OTP string
     */
    public static String getCustomerDeliveryOtp(Page page) {
        // Tracker is already open from StateSetupHelper, do not reload as it drops the React state


        Locator otpSection = page.locator("text=Secure Delivery Verification");
        otpSection.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));

        // The OTP is in a sibling div with bg-gradient styling
        Locator otpValue = page.locator("text=Secure Delivery Verification")
                .locator("xpath=../..")
                .locator("div.bg-gradient-to-r");
        otpValue.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));

        String otp = otpValue.innerText().trim();
        return otp;
    }

    /**
     * Extracts the restaurant pickup/handover OTP from the RestaurantOrderActions component.
     * <p>
     * The OTP is behind a toggle button "Show Handover OTP" that reveals the value
     * for 6 seconds before auto-hiding. Extraction must be immediate after clicking.
     * </p>
     *
     * @param page the restaurant's Playwright page
     * @return the 6-digit OTP string
     */
    public static String getRestaurantPickupOtp(Page page, String shortOrderId) {
        Locator container = page.locator("div:has(span:has-text('#" + shortOrderId + "'))").first();
        Locator otpButton = container.locator("button:has-text('Show Handover OTP')").first();
        otpButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        otpButton.click();

        // We cannot reuse otpButton here because the text changes, making :has-text('Show Handover OTP') false!
        // We use span.tracking-widest to uniquely identify the OTP span (since the left pane buttons also have span.font-mono)
        Locator otpSpan = container.locator("span.tracking-widest").first();
        otpSpan.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));

        String otp = otpSpan.innerText().trim();
        return otp;
    }
}
