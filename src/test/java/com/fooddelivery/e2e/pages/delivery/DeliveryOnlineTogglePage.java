package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Delivery Online Toggle.
 * Maps to: {@code DeliveryOnlineToggle.tsx} — uses {@code <Button>} with {@code aria-pressed}.
 */
public class DeliveryOnlineTogglePage {

    private final Page page;

    public DeliveryOnlineTogglePage(Page page) {
        this.page = page;
    }

    /**
     * Goes online and registers this browser's current location.
     *
     * <p>A seeded rider may still be rendered as online from an earlier session even though the
     * current browser has not registered its geolocation with dispatch. Cycle that stale online
     * state through offline before going online again so availability checks use this session.
     */
    public void goOnline() {
        Locator onlineBtn = page.locator("button:has-text('Online Duty')").first();
        if (onlineBtn.isVisible()) {
            onlineBtn.click();
            page.locator("button:has-text('Offline')")
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));
        }

        Locator offlineBtn = page.locator("button:has-text('Offline')").first();
        if (offlineBtn.isVisible()) {
            offlineBtn.click();
            Locator enablePermissions = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Enable Permissions"));
            if (enablePermissions.isVisible()) enablePermissions.click();
            page.locator("button:has-text('Online Duty')")
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(15000));
        }
    }

    /**
     * Goes offline by clicking the "Online Duty" button.
     */
    public void goOffline() {
        Locator onlineBtn = page.locator("button:has-text('Online Duty')").first();
        if (onlineBtn.isVisible()) {
            onlineBtn.click();
            page.locator("button:has-text('Offline')")
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(5000));
        }
    }

    public boolean isOnline() {
        return page.locator("button[aria-pressed='true']:has-text('Online Duty')").isVisible();
    }

    public boolean isOffline() {
        return page.locator("button:has-text('Offline')").isVisible();
    }

    public boolean isBlocked() {
        Locator btn = page.locator("button:has-text('Offline'), button:has-text('Online Duty')").first();
        return btn.isDisabled();
    }
}
