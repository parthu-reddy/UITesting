package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SharedSettingsViewPage {
    private final Page page;

    public SharedSettingsViewPage(Page page) {
        this.page = page;
    }

    public void clickBack() {
        page.locator("button:has(svg.lucide-x)").first().click();
    }

    public void clickProfileTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Profile")).click();
    }

    public void clickHistoryTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("History")).click();
    }

    public void clickAddressesTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Addresses")).click();
    }

    // --- Profile Tab ---
    public void fillFullName(String name) {
        // Label precedes input, or use input type/placeholder
        page.locator("div.space-y-1\\.5").filter(new Locator.FilterOptions().setHasText("Full Name")).locator("input").fill(name);
    }

    public void fillEmailAddress(String email) {
        page.locator("div.space-y-1\\.5").filter(new Locator.FilterOptions().setHasText("Email Address")).locator("input").fill(email);
    }

    public void clickSaveProfileChanges() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Profile Changes")).click();
    }

    public void clickRemoveSession(int index) {
        page.locator("button", new Page.LocatorOptions().setHasText("Remove")).nth(index).click();
    }

    public void clickLogOut() {
        page.locator("button", new Page.LocatorOptions().setHasText("Log Out")).click();
    }

    // --- History Tab ---
    public void clickReorder(String orderId) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText(orderId)).locator("button", new Locator.LocatorOptions().setHasText("Reorder")).first().click();
    }
    
    public void clickPreviousPage() {
        page.locator("button", new Page.LocatorOptions().setHasText("Previous")).click();
    }
    
    public void clickNextPage() {
        page.locator("button", new Page.LocatorOptions().setHasText("Next")).click();
    }

    // --- Addresses Tab ---
    public void clickAddNewAddress() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add New Address")).click();
    }

    public void clickDeleteAddress(String label) {
        page.locator("div.p-4").filter(new Locator.FilterOptions().setHasText(label)).locator("button:has(svg.lucide-trash2)").click();
    }
}
