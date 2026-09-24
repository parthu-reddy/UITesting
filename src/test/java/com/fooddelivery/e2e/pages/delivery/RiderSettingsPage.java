package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for Rider Settings and Onboarding Wizard.
 * Maps to: {@code RiderSettingsView.tsx, RiderOnboardingWizard.tsx}
 */
public class RiderSettingsPage {

    private final Page page;

    public RiderSettingsPage(Page page) {
        this.page = page;
    }

    /** A verified rider's settings: the "Rider Settings" heading (RiderSettingsView.tsx). */
    public boolean isSettingsVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Rider Settings").setExact(true)).isVisible();
    }

    /** An unverified rider sees the takeover wizard instead: "Partner Onboarding" (RiderOnboardingWizard.tsx). */
    public boolean isOnboardingWizardVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Partner Onboarding").setExact(true)).isVisible();
    }

    public void fillVehicleNumber(String number) {
        page.locator("input[placeholder*='vehicle'], input[placeholder*='Vehicle']").first().fill(number);
    }

    /** The custom Select (a combobox + listbox), not a native select. Options read e.g. "EV Two-Wheeler". */
    public void selectVehicleType(String type) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Vehicle Type").setExact(true)).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(type).setExact(true)).click();
    }

    public void completeOnboardingStep() {
        page.locator("button:has-text('Next'), button:has-text('Continue'), button:has-text('Save')").first().click();
        page.waitForTimeout(500);
    }
}
