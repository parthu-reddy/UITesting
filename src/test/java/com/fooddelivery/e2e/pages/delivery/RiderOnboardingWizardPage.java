package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for rider onboarding wizard.
 * Maps to: {@code RiderOnboardingWizard.tsx}
 * <p>
 * Multi-step wizard for new riders: vehicle info, ID upload, bank details.
 * </p>
 */
public class RiderOnboardingWizardPage {

    private final Page page;

    public RiderOnboardingWizardPage(Page page) {
        this.page = page;
    }

    public boolean isWizardVisible() {
        return page.locator("text=Partner Onboarding, text=Complete your KYC").first().isVisible();
    }

    public void completeOnboarding() {
        // Step 0: Basic Profile
        page.locator("input[type='text']").first().fill("E2E Test Rider");
        page.locator("select, [role='combobox']").first().selectOption("MCWG");
        page.locator("input[type='text']").nth(1).fill("KA01AB1234");
        uploadDummyFile();
        clickContinue();

        // Step 1: Driving License
        page.locator("input[type='text']").first().fill("KA1234567890123");
        page.locator("input[type='date']").first().fill("1990-01-01");
        uploadDummyFile();
        clickContinue();
        clickContinue(); // Dismiss approval screen

        // Step 2: Vehicle RC
        page.locator("input[type='text']").first().fill("KA01AB1234");
        uploadDummyFile();
        clickContinue();
        clickContinue(); // Dismiss approval screen

        // Step 3: Bank Account
        page.locator("input[type='text']").first().fill("1234567890");
        page.locator("input[type='text']").nth(1).fill("HDFC0000001");
        clickContinue();
        clickContinue(); // Dismiss approval screen

        // Step 4: Face Match (Selfie)
        uploadDummyFile();
        page.locator("button:has-text('Complete Verification')").first().click();
        page.waitForTimeout(2000);
    }

    private void uploadDummyFile() {
        page.setInputFiles("input[type='file']", java.nio.file.Paths.get("src/test/resources/dummy.png"));
    }

    private void clickContinue() {
        page.locator("button:has-text('Save & Continue'), button:has-text('Verify License'), button:has-text('Verify Vehicle'), button:has-text('Initiate Penny Drop'), button:has-text('Continue')").first().click();
        page.waitForTimeout(1000);
    }

    // Compatibility methods for RiderOnboardingFullTest
    public int getCurrentStep() {
        String text = page.locator("text=Step").first().innerText();
        return Integer.parseInt(text.replaceAll("[^0-9]", "").substring(0, 1));
    }

    public void fillVehicleNumber(String number) {
        page.locator("input[type='text']").nth(1).fill(number);
    }

    public void selectVehicleType(String type) {
        page.locator("select, [role='combobox']").first().selectOption(type);
    }

    public void clickNext() {
        clickContinue();
    }

    public void clickSubmit() {
        page.locator("button:has-text('Complete Verification')").first().click();
        page.waitForTimeout(2000);
    }
}
