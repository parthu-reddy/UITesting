package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    // --- High-Level Compound Actions (Backward Compatibility) ---

    public void loginAs(String role, String phone) {
        page.locator("text=" + role).first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForTimeout(500); 

        com.microsoft.playwright.Locator input = page.getByPlaceholder("9876543210");
        boolean found = false;
        for (int i = 0; i < 10; i++) {
            try {
                Locator locators = page.locator("text=" + role);
                for (int j = 0; j < locators.count(); j++) {
                    try {
                        locators.nth(j).click(new com.microsoft.playwright.Locator.ClickOptions().setTimeout(500));
                        input.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(1000));
                        found = true;
                        break;
                    } catch (Exception e2) {}
                }
                if (found) break;
            } catch (Exception e) {
                page.waitForTimeout(500);
            }
        }
        if (!found) {
            throw new RuntimeException("Could not click role card and see input");
        }
        
        fillPhoneNumber(phone);
        clickSendOneTimeOtp();
        
        page.waitForTimeout(2000);
        
        page.getByPlaceholder("- - - - - -").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        
        clickAutofillCode();
        page.waitForTimeout(500);
        
        clickVerifyAndSecureLogIn();
        
        page.getByPlaceholder("9876543210").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    // --- Input Fields ---

    public void fillPhoneNumber(String phone) {
        page.getByPlaceholder("9876543210").fill(phone);
    }

    public void fillOtp(String otp) {
        page.getByPlaceholder("- - - - - -").fill(otp);
    }

    // --- Individual Clickable Elements ---

    public void clickToggleTheme() {
        page.locator("button:has(svg.lucide-moon), button:has(svg.lucide-sun)").first().click();
    }

    public void clickSmsNotification() {
        page.locator("text=SMS GATEWAY").first().click();
    }

    public void clickBackButton() {
        page.locator("button:has(svg.lucide-arrow-left)").first().click();
    }

    // Desktop Cards
    public void clickOrderFoodCardDesktop() {
        page.locator("button:has-text('Order Food')").first().click();
    }

    public void clickRestaurantPartnerCardDesktop() {
        page.locator("button:has-text('Restaurant Partner')").first().click();
    }

    public void clickDeliveryExecutiveCardDesktop() {
        page.locator("button:has-text('Delivery Executive')").first().click();
    }

    public void clickSystemAdminCardDesktop() {
        page.locator("button:has-text('System Admin')").first().click();
    }

    // Mobile Carousel Navigation
    public void clickCarouselPrevious() {
        page.locator("button.lucide-chevron-left").click(); // Adjust as needed
    }

    public void clickCarouselNext() {
        page.locator("button.lucide-chevron-right").click(); // Adjust as needed
    }

    // Mobile Carousel Cards
    public void clickOrderFoodCarousel() {
        page.locator("button:has-text('Order Food')").nth(1).click();
    }

    public void clickRestaurantPartnerCarousel() {
        page.locator("button:has-text('Restaurant Partner')").nth(1).click();
    }

    public void clickDeliveryExecutiveCarousel() {
        page.locator("button:has-text('Delivery Executive')").nth(1).click();
    }

    public void clickSystemAdminCarousel() {
        page.locator("button:has-text('System Admin')").nth(1).click();
    }

    public void clickCarouselDot(int index) {
        // Find buttons under the dots div
        page.locator(".flex.justify-center.gap-2.mt-6 > button").nth(index).click();
    }

    // OTP Form Actions
    public void clickSendOneTimeOtp() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send One-Time OTP")).click();
    }

    public void clickResendSmsCode() {
        page.locator("button:has-text('Resend SMS Code')").click();
    }

    public void clickAutofillCode() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Autofill Code")).click();
    }

    public void clickVerifyAndSecureLogIn() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Verify & Secure Log In")).click();
    }
}
