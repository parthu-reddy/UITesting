package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RestaurantOutletSettingsPage {
    private final Page page;

    public RestaurantOutletSettingsPage(Page page) {
        this.page = page;
    }

    // --- Outlet Menu Editor (Overrides) ---
    private Locator getItemRow(String itemName) {
        return page.locator("div.border-slate-200, div.border-slate-800").filter(new Locator.FilterOptions().setHasText(itemName)).first();
    }

    public void clickEditOverride(String itemName) {
        getItemRow(itemName).locator("button").first().click();
    }

    public void fillOverridePrice(String price) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Price")).locator("input[type='number']").first().fill(price);
    }

    public void fillOverridePrepTime(String prepTime) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Prep")).locator("input[type='number']").first().fill(prepTime);
    }

    public void selectOverrideStatus(String value) {
        // value should be "true" or "false"
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Status")).locator("select").selectOption(value);
    }

    public void clickCancelOverride() {
        page.locator("button:has(svg.lucide-x)").click();
    }

    public void clickSaveOverride() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Override")).click();
    }

    public void clickSaveTimings() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save")).click();
    }

    // --- Outlet Shift Editor ---
    public void clickAddShift() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add Shift")).click();
    }

    public void fillShiftOpens(int index, String time) {
        page.locator("input[type='time']").nth(index * 2).fill(time);
    }

    public void fillShiftCloses(int index, String time) {
        page.locator("input[type='time']").nth(index * 2 + 1).fill(time);
    }

    public void clickRemoveShift(int index) {
        page.locator("button[title='Remove Shift']").nth(index).click();
    }

    public void clickSaveShiftChanges() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Changes")).click();
    }

    // --- Outlet Settings Editor ---
    public void fillDefaultPrepTime(String prepTimeSeconds) {
        page.locator("input[type='number']").fill(prepTimeSeconds);
    }

    public void clickSaveSettings() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save")).click();
    }
}
