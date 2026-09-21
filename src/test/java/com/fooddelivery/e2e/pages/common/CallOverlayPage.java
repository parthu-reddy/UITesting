package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Call Overlay.
 * Maps to: {@code CallOverlay.tsx}
 * <p>
 * Displayed when a VoIP/phone call is initiated between customer/rider/restaurant.
 * </p>
 */
public class CallOverlayPage {

    private final Page page;

    public CallOverlayPage(Page page) {
        this.page = page;
    }

    public boolean isCallActive() {
        return page.locator("text=Calling, text=In Call, text=Connected, text=Ringing").first().isVisible();
    }

    public void endCall() {
        page.locator("button:has-text('End'), button:has(svg.lucide-phone-off)").first().click();
        page.waitForTimeout(500);
    }

    public void muteCall() {
        page.locator("button:has(svg.lucide-mic-off), button:has-text('Mute')").first().click();
    }
}
