package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RestaurantSettingsConsolePage {
    private final Page page;

    public RestaurantSettingsConsolePage(Page page) {
        this.page = page;
    }

    // --- Brand Registration ---
    public void clickRegisterNewBrand() {
        page.locator("button", new Page.LocatorOptions().setHasText("Register New Brand")).click();
    }

    public void fillBrandName(String name) {
        page.locator("input[placeholder='e.g. KFC']").fill(name);
    }

    public void fillGSTIN(String gstin) {
        // Find input related to GSTIN label
        page.locator("div").filter(new Locator.FilterOptions().setHasText("GSTIN")).locator("input").first().fill(gstin);
    }

    public void fillPAN(String pan) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("PAN")).locator("input").first().fill(pan);
    }

    public void fillCIN(String cin) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("CIN")).locator("input").first().fill(cin);
    }

    public void fillBankAccount(String acc) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Bank Account #")).locator("input").first().fill(acc);
    }

    public void fillIFSC(String ifsc) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("IFSC Code")).locator("input").first().fill(ifsc);
    }

    public void clickCancelBrandRegistration() {
        // The modal cancel button
        page.locator("button", new Page.LocatorOptions().setHasText("Cancel")).first().click();
    }

    public void clickSubmitBrandRegistration() {
        page.locator("button", new Page.LocatorOptions().setHasText("Register Brand")).click();
    }

    // --- Outlet Registration ---
    public void clickRegisterNewOutlet() {
        page.locator("button", new Page.LocatorOptions().setHasText("Register New Outlet")).click();
    }

    public void fillOutletName(String name) {
        page.locator("input[placeholder='e.g. Bella Italia (Downtown)']").fill(name);
    }

    public void fillFSSAI(String fssai) {
        page.locator("input[placeholder='14-digit FSSAI number']").fill(fssai);
    }

    public void fillBannerUrl(String url) {
        page.locator("input[type='url']").fill(url);
    }

    public void fillLocationSearch(String search) {
        page.locator("input[placeholder='Search for an address or landmark...']").fill(search);
    }

    public void clickUseCurrentLocation() {
        page.locator("button[title='Use Current Location']").click();
    }

    public void selectAddressResult(String text) {
        page.locator("button", new Page.LocatorOptions().setHasText(text)).click();
    }

    public void fillLatitude(String lat) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Latitude")).locator("input[type='number']").first().fill(lat);
    }

    public void fillLongitude(String lng) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText("Longitude")).locator("input[type='number']").first().fill(lng);
    }

    public void clickAddShift() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add Shift")).click();
    }

    public void fillShiftOpen(int index, String time) {
        page.locator("input[type='time']").nth(index * 2).fill(time);
    }

    public void fillShiftClose(int index, String time) {
        page.locator("input[type='time']").nth(index * 2 + 1).fill(time);
    }

    public void clickRemoveShift(int index) {
        page.locator("button[title='Remove Shift']").nth(index).click();
    }

    public void clickSubmitOutletRegistration() {
        page.locator("button", new Page.LocatorOptions().setHasText("Register Outlet")).click();
    }

    // --- Brand Master Menu ---
    public void clickAddCategory() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add Category")).click();
    }

    public void clickAddItem() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add Item")).click();
    }

    public void fillCategoryName(String name) {
        page.locator("input[placeholder='Category Name (e.g. Appetizers)']").fill(name);
    }

    public void clickSaveCategory() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Category")).click();
    }

    public void fillItemName(String name) {
        page.locator("input[placeholder='Item Name']").fill(name);
    }

    public void fillItemPrice(String price) {
        page.locator("input[placeholder='Base Price']").fill(price);
    }

    public void fillItemPrepTime(String time) {
        page.locator("input[placeholder='Prep (mins)']").fill(time);
    }

    public void fillItemDescription(String desc) {
        page.locator("input[placeholder='Description']").fill(desc);
    }

    public void clickSaveItem() {
        page.locator("button", new Page.LocatorOptions().setHasText("Save Item")).click();
    }

    private Locator getCategoryRow(String catName) {
        return page.locator("div.rounded-2xl").filter(new Locator.FilterOptions().setHasText(catName)).first();
    }

    public void clickSetupTimings(String catName) {
        getCategoryRow(catName).locator("button", new Locator.LocatorOptions().setHasText("Setup Timings")).click();
    }

    public void clickEditCategory(String catName) {
        getCategoryRow(catName).locator("button[title='Edit']").first().click(); // Assuming standard icons used for edit
    }

    public void clickDeleteCategory(String catName) {
        getCategoryRow(catName).locator("button.text-rose-500").first().click();
    }

    private Locator getItemRow(String itemName) {
        return page.locator("div.border-slate-100").filter(new Locator.FilterOptions().setHasText(itemName)).first();
    }

    public void clickEditItem(String itemName) {
        getItemRow(itemName).locator("button").filter(new Locator.FilterOptions().setHasText("Edit")).click();
    }
}
