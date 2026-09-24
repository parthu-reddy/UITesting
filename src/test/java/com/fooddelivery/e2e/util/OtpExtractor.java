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
     * Read from {@code [data-testid='delivery-code']} ("DELIVERY CODE" card). The previous
     * locator keyed on the heading "Secure Delivery Verification" and a gradient class, both of
     * which the redesign removed.
     * </p>
     *
     * @param page the customer's Playwright page
     * @return the OTP string
     */
    public static String getCustomerDeliveryOtp(Page page) {
        // Tracker is already open from StateSetupHelper, do not reload as it drops the React state
        Locator otpValue = page.locator("[data-testid='delivery-code']").first();
        otpValue.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        return otpValue.innerText().trim();
    }

    /**
     * Extracts the restaurant pickup/handover OTP from the RestaurantOrderActions component.
     * <p>
     * The OTP is behind a toggle button "Show pickup code" that reveals the value
     * for 6 seconds before auto-hiding. Extraction must be immediate after clicking.
     * </p>
     *
     * @param page the restaurant's Playwright page
     * @return the 6-digit OTP string
     */
    public static String getRestaurantPickupOtp(Page page, String shortOrderId) {
        Locator container = page.locator("div:has(span:has-text('#" + shortOrderId + "'))").first();
        Locator otpButton = container.locator("button:has-text('Show pickup code')").first();
        otpButton.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        otpButton.click();

        // We cannot reuse otpButton here because the text changes, making :has-text('Show pickup code') false!
        // We use span.tracking-widest to uniquely identify the OTP span (since the left pane buttons also have span.font-mono)
        Locator otpSpan = container.locator("span.tracking-widest").first();
        otpSpan.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));

        String otp = otpSpan.innerText().trim();
        return otp;
    }
}
