package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Page;

public class CustomerAddressPage {
    private final Page page;

    public CustomerAddressPage(Page page) {
        this.page = page;
    }

    public void clickBackToSettings() {
        page.locator("button:has(svg.lucide-x)").first().click();
    }

    public void fillSearchArea(String query) {
        page.getByPlaceholder("Search for area, street name...").fill(query);
    }

    public void clickSearchResult(String suggestion) {
        page.locator("div.cursor-pointer:has-text('" + suggestion + "')").first().click();
    }

    public void fillLabel(String label) {
        page.getByPlaceholder("e.g. Home, Work").fill(label);
    }

    public void fillAddressLine1(String address1) {
        page.locator("textarea").first().fill(address1);
    }

    public void fillCity(String city) {
        page.locator("input[placeholder='']").nth(1).fill(city); // Actually city doesn't have a placeholder, but it has a label. Let's use more specific locators.
    }
    
    // Better way to do it: by preceding label
    public void fillCityByLabel(String city) {
        page.locator("div.space-y-1\\.5:has(label:has-text('City')) > input").fill(city);
    }

    public void fillStateByLabel(String state) {
        page.locator("div.space-y-1\\.5:has(label:has-text('State')) > input").fill(state);
    }

    public void fillZipByLabel(String zip) {
        page.locator("div.space-y-1\\.5:has(label:has-text('Zip')) > input").fill(zip);
    }

    public void clickConfirmLocation() {
        page.locator("button:has-text('Confirm Location')").click();
    }
}
