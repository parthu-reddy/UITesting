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
     * Grants browser-level notification and geolocation permissions via CDP so the
     * rider app can go online in headless mode without a native prompt blocking it.
     */
    public static void grantBrowserPermissions(Page page) {
        try {
            page.context().grantPermissions(
                    java.util.List.of("notifications", "geolocation"),
                    new com.microsoft.playwright.BrowserContext.GrantPermissionsOptions()
                            .setOrigin(page.url().replaceAll("(https?://[^/]+).*", "$1")));
        } catch (Exception e) {
            System.out.println("[RIDER] grantPermissions failed (may be already granted): " + e.getMessage());
        }
    }

    /** Go online only when offline; never cycle an already-online rider. */
    public void goOnline() {
        grantBrowserPermissions(page);
        new DeliveryDashboardPage(page).waitForDashboard();
        Locator offlineBtn = page.locator("button:has-text('Offline')").first();
        if (offlineBtn.isVisible()) {
            offlineBtn.click();

            // The "Enable Permissions" prompt may appear asynchronously after clicking
            // Offline. Poll for it with a short timeout instead of a single isVisible().
            Locator enablePermissions = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Enable Permissions"));
            try {
                enablePermissions.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
                enablePermissions.click();
            } catch (com.microsoft.playwright.TimeoutError ignored) {
                // Permissions already granted — prompt never appeared, which is fine.
            }

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

