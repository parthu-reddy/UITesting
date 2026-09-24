package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.nio.file.Paths;
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
        Locator loc = page.locator("text=Partner Onboarding").first();
        try {
            loc.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE).setTimeout(15000));
            return true;
        } catch (Exception e) {
            System.err.println("Wizard not visible. Current page HTML:");
            System.err.println(page.content());
            return false;
        }
    }

    public void completeOnboarding() {
        // Step 0: Basic Profile
        page.locator("input[type='text']").first().fill("Test Rider");
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Vehicle Type")).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION, new Page.GetByRoleOptions().setName("Motorcycle / Scooter")).click();
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
        page.locator("input[type='file']").setInputFiles(Paths.get("src/test/resources/dummy.png"));
        page.getByText("Click to replace").waitFor();
    }

    private void clickContinue() {
        page.locator("button:has-text('Save & Continue'), button:has-text('Verify License'), button:has-text('Verify Vehicle'), button:has-text('Initiate Penny Drop'), button:has-text('Continue')").first().click();
        page.waitForTimeout(1000);
    }

    public void completeDevModeOnboarding() {
        // Step 0: Basic Profile
        page.locator("input[type='text']").first().fill("Test Rider");
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Vehicle Type")).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION, new Page.GetByRoleOptions().setName("Motorcycle / Scooter")).click();
        page.locator("input[type='text']").nth(1).fill("KA01AB1234");
        uploadDummyFile();
        clickContinue();

        // Step 1: Driving License (Auto-Approved in Dev)
        page.getByText("Driving License Approved").waitFor();
        clickContinue();

        // Step 2: Vehicle RC (Auto-Approved in Dev)
        page.getByText("Vehicle RC Approved").waitFor();
        clickContinue();

        // Step 3: Bank Account (Auto-Approved in Dev)
        page.getByText("Bank Verified").waitFor();
        clickContinue();

        // Step 4: Face Match (Selfie - never auto-approved)
        page.getByText("Please upload a clear selfie").waitFor();
        uploadDummyFile();
        page.locator("button:has-text('Complete Verification')").first().click();
        page.waitForTimeout(2000);
    }

    // Compatibility methods for RiderOnboardingFullTest
    public int getCurrentStep() {
        String text = page.locator("text=Step").first().innerText();
        return Integer.parseInt(text.replaceAll("[^0-9]", "").substring(0, 1));
    }

    public void fillVehicleNumber(String number) {
        page.locator("input[type='text']").nth(1).fill(number);
    }

    /** Wizard options: "Bicycle", "EV Two-Wheeler", "Motorcycle / Scooter", "Car / LMV". */
    public void selectVehicleType(String type) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Vehicle Type")).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(type).setExact(true)).click();
    }

    public void clickNext() {
        clickContinue();
    }

    public void clickSubmit() {
        page.locator("button:has-text('Complete Verification')").first().click();
        page.waitForTimeout(2000);
    }
}
