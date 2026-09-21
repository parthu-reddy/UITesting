package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for Restaurant Settings.
 * Maps to: {@code RestaurantSettingsShell.tsx, OutletSettingsEditor.tsx, BrandRegistration.tsx}
 * <p>
 * Also covers sub-components: {@code OutletShiftEditor.tsx}, {@code OutletTimingsField.tsx},
 * {@code RestaurantBrandSelector.tsx}, {@code CategorySelector.tsx}, {@code PartnerAccountModal.tsx}
 * </p>
 */
public class RestaurantSettingsPage {

    private final Page page;

    public RestaurantSettingsPage(Page page) {
        this.page = page;
    }

    public boolean isSettingsVisible() {
        return page.locator("text=Restaurant Settings, text=Brand Settings, text=Outlet Settings").first().isVisible();
    }

    public void openBrandSettings() {
        page.locator("button:has-text('Brand'), text=Brand Settings").first().click();
        page.waitForTimeout(300);
    }

    public void openOutletSettings() {
        page.locator("button:has-text('Outlet'), text=Outlet Settings").first().click();
        page.waitForTimeout(300);
    }

    public void openTimings() {
        page.locator("button:has-text('Timings'), text=Operating Hours").first().click();
        page.waitForTimeout(300);
    }

    public void openKyc() {
        page.locator("button:has-text('KYC'), text=Verification").first().click();
        page.waitForTimeout(300);
    }
}

