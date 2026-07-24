package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AdminPortalPage {
    private final Page page;

    public AdminPortalPage(Page page) {
        this.page = page;
    }

    // --- Sidebar Navigation ---
    public void clickLiveOperationsTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Live Operations")).click();
    }

    public void clickUserManagementTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("User Management")).click();
    }

    public void clickCategoriesTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Categories")).click();
    }

    public void clickFleetMapTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Fleet Map")).click();
    }

    // --- Live Operations Tab ---
    public void clickRefreshOrders() {
        page.locator("button", new Page.LocatorOptions().setHasText("Refresh")).click();
    }

    public void selectActiveOrder(String orderIdShort) {
        // orderIdShort should be like "#12345678"
        page.locator("button").filter(new Locator.FilterOptions().setHasText(orderIdShort)).click();
    }

    public void assignDriver(String driverName) {
        // Find the driver row in the Assignment Panel and click Assign
        Locator driverRow = page.locator("div.p-3").filter(new Locator.FilterOptions().setHasText(driverName)).first();
        driverRow.locator("button", new Locator.LocatorOptions().setHasText("Assign")).click();
    }

    // --- User Management Tab ---
    public void selectRoleFilter(String roleName) {
        page.locator("select").selectOption(roleName); // ADMIN, CUSTOMER, RESTAURANT, DELIVERY
    }

    public void fillUserSearch(String query) {
        page.locator("input[placeholder='User ID / Phone']").fill(query);
    }

    public void submitUserSearch() {
        // The search button is next to the input
        page.locator("input[placeholder='User ID / Phone']").locator("..").locator("button").click();
    }

    public void selectUser(String identifier) {
        // identifier can be name or phone
        page.locator("button").filter(new Locator.FilterOptions().setHasText(identifier)).click();
    }

    public void removeRole(String roleName) {
        page.locator("div").filter(new Locator.FilterOptions().setHasText(roleName)).locator("button").first().click();
    }

    public void fillNewRole(String roleName) {
        page.locator("input[placeholder='Add Role (e.g. ADMIN)']").fill(roleName);
    }

    public void clickAddRole() {
        page.locator("button", new Page.LocatorOptions().setHasText("Add")).click();
    }

    public void clickViewUserActiveOrderOnMap(String orderIdShort) {
        Locator orderRow = page.locator("div").filter(new Locator.FilterOptions().setHasText(orderIdShort)).first();
        orderRow.locator("button", new Locator.LocatorOptions().setHasText("View on Map")).click();
    }

    // --- Categories Tab ---
    public void fillCategoryName(String name) {
        page.locator("input[name='categoryName']").fill(name);
    }

    public void fillCategoryDescription(String description) {
        page.locator("textarea[name='categoryDesc']").fill(description);
    }

    public void clickCreateCategory() {
        page.locator("button", new Page.LocatorOptions().setHasText("Create Category")).click();
    }
}
