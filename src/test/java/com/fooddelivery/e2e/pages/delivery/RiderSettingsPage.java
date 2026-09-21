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

    public boolean isSettingsVisible() {
        return page.locator("text=Profile, text=Settings, text=Vehicle").first().isVisible();
    }

    public boolean isOnboardingWizardVisible() {
        return page.locator("text=Complete Your Profile, text=Onboarding, text=Step").first().isVisible();
    }

    public void fillVehicleNumber(String number) {
        page.locator("input[placeholder*='vehicle'], input[placeholder*='Vehicle']").first().fill(number);
    }

    public void selectVehicleType(String type) {
        page.locator("select").selectOption(type);
    }

    public void completeOnboardingStep() {
        page.locator("button:has-text('Next'), button:has-text('Continue'), button:has-text('Save')").first().click();
        page.waitForTimeout(500);
    }
}
