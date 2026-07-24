package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SharedModals {
    private final Page page;

    public SharedModals(Page page) {
        this.page = page;
    }

    // --- Complete Profile Modal ---
    
    public void fillCompleteProfileName(String name) {
        page.getByPlaceholder("Enter your full name").fill(name);
    }
    
    public void fillCompleteProfileEmail(String email) {
        page.getByPlaceholder("Enter your email address").fill(email);
    }
    
    public void clickSaveProfileAndContinue() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save Profile & Continue")).click();
    }
    
    // --- Session Management Modal ---
    
    public void clickCloseSessionManagementModal() {
        // First button inside the modal header
        page.locator(".relative.w-full.max-w-lg button").first().click();
    }
    
    public void clickRemoveSessionDevice(int sessionIndex) {
        // Assuming sessionIndex starts from 0 for the first listed session
        page.locator("button[title='Log out from this device']").nth(sessionIndex).click();
    }
    
    // --- Name Prompt Modal ---
    
    public void fillNamePromptName(String name) {
        page.getByPlaceholder("E.g. John Doe").fill(name);
    }
    
    public void fillNamePromptEmail(String email) {
        page.getByPlaceholder("john@example.com").fill(email);
    }
    
    public void clickNamePromptContinue() {
        page.locator("button:has-text('Continue')").click();
    }
}
