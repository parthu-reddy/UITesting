package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;

public class CustomerAddressModal {
    private final Page page;

    public CustomerAddressModal(Page page) {
        this.page = page;
    }

    public void clickCloseAddressModal() {
        page.locator(".bg-white\\/20.dark\\:bg-slate-900\\/20 > div > button:has(svg.lucide-x)").first().click();
    }

    public void fillSearchArea(String query) {
        page.getByPlaceholder("Search for area, street name...").fill(query);
    }

    public void clickUseCurrentLocation() {
        page.locator("button:has-text('Locate Me')").click();
    }

    public void clickSearchResult(String descriptionPrefix) {
        page.locator("button:has-text('" + descriptionPrefix + "')").first().click();
    }

    public void clickSavedAddress(String label) {
        page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile(".*" + label + ".*"))).first().click();
    }

    public void fillLabel(String label) {
        page.getByPlaceholder("Label (e.g. Home, Work)").fill(label);
    }

    public void fillAddressLine1(String address1) {
        page.getByPlaceholder("Address Line 1").fill(address1);
    }

    public void fillAddressLine2(String address2) {
        page.getByPlaceholder("Address Line 2 (Optional)").fill(address2);
    }

    public void fillCity(String city) {
        page.getByPlaceholder("City").fill(city);
    }

    public void fillState(String state) {
        page.getByPlaceholder("State").fill(state);
    }

    public void fillZipCode(String zip) {
        page.getByPlaceholder("ZIP Code").fill(zip);
    }

    public void clickSaveAddress() {
        page.locator("button:has-text('Save Address')").click();
    }

    public void clickDeliverToThisLocationTemporary() {
        page.locator("button:has-text('Deliver to this location (Temporary)')").click();
    }
}
