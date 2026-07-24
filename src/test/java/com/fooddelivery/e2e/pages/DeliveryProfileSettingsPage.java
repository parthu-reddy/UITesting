package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DeliveryProfileSettingsPage {
    private final Page page;

    public DeliveryProfileSettingsPage(Page page) {
        this.page = page;
    }

    // --- Rider Settings View ---
    public void clickBackFromSettings() {
        page.locator("button").filter(new Locator.FilterOptions().setHasText("")).first().click(); // Needs better selector if multiple empty buttons, assuming it's the first X button
        // Or finding by adjacent text
        // page.locator("div").filter(new Locator.FilterOptions().setHasText("Rider Settings")).locator("button").click();
    }

    public void fillFullName(String name) {
        page.locator("input[placeholder='e.g. John Doe']").fill(name);
    }

    public void fillEmail(String email) {
        page.locator("input[placeholder='rider@example.com']").fill(email);
    }

    public void fillVehicleRegistration(String vehicle) {
        page.locator("input[placeholder='e.g. KA01AB1234']").fill(vehicle);
    }

    public void fillProfilePhotoUrl(String url) {
        page.locator("input[placeholder='https://example.com/photo.jpg']").fill(url);
    }

    public void clickSaveProfileChanges() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Profile Changes")).click();
    }

    public void clickSignOutDevice(int index) {
        page.locator("button[title='Sign out this device']").nth(index).click();
    }

    public void clickSignOut() {
        page.locator("button", new Page.LocatorOptions().setHasText("Sign Out")).click();
    }

    // --- Complete Profile Modal ---
    public void fillModalFullName(String name) {
        page.locator("input[placeholder='Enter your full name']").fill(name);
    }

    public void fillModalEmail(String email) {
        page.locator("input[placeholder='Enter your email address']").fill(email);
    }

    public void clickSaveProfileAndContinue() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Profile & Continue")).click();
    }
}
