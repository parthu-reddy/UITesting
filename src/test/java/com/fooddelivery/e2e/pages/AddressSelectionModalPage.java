package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AddressSelectionModalPage {
    private final Page page;

    public AddressSelectionModalPage(Page page) {
        this.page = page;
    }

    public void clickClose() {
        page.locator("button:has(svg.lucide-x)").first().click();
    }

    public void clickUseCurrentLocation() {
        page.locator("button", new Page.LocatorOptions().setHasText("Use current location")).click();
    }

    public void selectSavedAddress(String addressLabel) {
        page.locator("button", new Page.LocatorOptions().setHasText(addressLabel)).first().click();
    }

    public void clickAddNewAddress() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add New Address")).click();
    }
}
