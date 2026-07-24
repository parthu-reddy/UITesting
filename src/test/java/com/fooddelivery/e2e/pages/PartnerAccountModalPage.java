package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PartnerAccountModalPage {
    private final Page page;

    public PartnerAccountModalPage(Page page) {
        this.page = page;
    }

    public void clickClose() {
        // X button in the header
        page.locator("button").filter(new Locator.FilterOptions().setHasText("Account Settings")).locator("..").locator("button").first().click();
    }

    public void fillFullName(String name) {
        page.locator("input").first().fill(name); // Assume first input is Full Name
    }

    public void clickSaveChanges() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Changes")).click();
    }

    public void clickLogOutAll() {
        page.locator("button", new Page.LocatorOptions().setHasText("Log out all")).click();
    }

    public void clickRemoveDevice(int index) {
        page.locator("button", new Page.LocatorOptions().setHasText("Remove")).nth(index).click();
    }

    public void clickLogOut() {
        page.locator("button", new Page.LocatorOptions().setHasText("Log Out")).click();
    }
}
