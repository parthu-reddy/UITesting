package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RestaurantDashboardPage {
    private final Page page;

    public RestaurantDashboardPage(Page page) {
        this.page = page;
    }

    // --- Header & Global Actions ---
    public void toggleOutletStatus() {
        page.locator("button[title='Toggle Outlet Status']").click();
    }

    public void toggleTheme() {
        page.locator("button[title='Toggle Light/Dark Mode']").click();
    }

    public void selectOutlet(String outletId) {
        page.locator("select").first().selectOption(outletId);
    }

    public void clickProfileSettings() {
        page.locator("button[title='Profile Settings']").click();
    }

    public void clickSettingsConsole() {
        page.locator("button[title='Restaurant Registration & Menu Settings']").click();
    }

    // --- Tabs ---
    public void clickLiveKitchenFeedTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Live Kitchen Feed")).click();
    }

    public void clickMenuStockTogglesTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Menu Stock Toggles")).click();
    }

    // --- Live Kitchen Feed Actions (Kanban Board) ---
    private Locator getOrderCard(String orderId) {
        // Find the card container that contains the specific order ID text
        String shortId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
        return page.locator("div.rounded-2xl").filter(new Locator.FilterOptions().setHasText("#" + shortId));
    }

    public void clickAcceptOrder(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Accept Order")).first().click();
        // Or if it's the one in New Placed which just says 'Accept'
        // Let's use a broader regex or text pattern.
        // There's a button with text 'Accept' and one with 'Accept Order'
        // Playwright's hasText matches substrings, so "Accept" will match both.
    }
    
    public void clickAcceptOrderAnyText(String orderId) {
        getOrderCard(orderId).getByText("Accept", new Locator.GetByTextOptions().setExact(false)).first().click();
    }

    public void clickDelayOrder(String orderId) {
        getOrderCard(orderId).getByText("Delay", new Locator.GetByTextOptions().setExact(false)).click();
    }

    public void clickCancelOrder(String orderId) {
        // The cancel button is a circle with X (XCircle icon), doesn't always have text, but sometimes has "Cancel Order"
        // Let's look for button with class containing red or specific svg
        getOrderCard(orderId).locator("button.text-red-600, button.text-red-400").first().click();
    }

    public void clickStartCook(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Start Cook")).click();
    }

    public void clickMarkPrepared(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Mark Prepared")).click();
    }

    // --- Delay Drawer ---
    public void selectDelayMinutes(String orderId, int minutes) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("+" + minutes + " Min")).click();
    }

    public void fillDelayReason(String orderId, String reason) {
        getOrderCard(orderId).locator("input[placeholder*='e.g. High custom baking']").fill(reason);
    }

    public void submitDelay(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Submit Delay")).click();
    }

    // --- Cancel Drawer ---
    public void fillCancelReason(String orderId, String reason) {
        getOrderCard(orderId).locator("input[placeholder*='e.g. Out of stock']").or(
            getOrderCard(orderId).locator("input[placeholder*='e.g. Item dropped']")
        ).fill(reason);
    }

    public void clickCancelBack(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Back")).click();
    }

    public void clickConfirmCancel(String orderId) {
        getOrderCard(orderId).locator("button", new Locator.LocatorOptions().setHasText("Confirm Cancel")).click();
    }
    
    // --- Pickup OTP ---
    public String getPickupOtp(String orderId) {
        return getOrderCard(orderId).locator("span.font-mono.tracking-wider.text-indigo-500").innerText().trim();
    }

    // --- Menu Stock Toggles Tab ---
    private Locator getDishRow(String dishName) {
        return page.locator("div.rounded-2xl").filter(new Locator.FilterOptions().setHasText(dishName));
    }

    public void toggleDishStock(String dishName) {
        getDishRow(dishName).locator("button").click();
    }
    
    // --- Settings Console Actions ---
    public void clickBackToKitchenFeed() {
        page.locator("button", new Page.LocatorOptions().setHasText("Back to Kitchen Feed")).click();
    }

    public void clickOutletManagementTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Outlet Management")).click();
    }

    public void clickMenuCatalogEditorTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Menu Catalog Editor")).click();
    }

    public void clickOrderHistoryTab() {
        page.locator("button", new Page.LocatorOptions().setHasText("Order History")).click();
    }

    // --- Outlet Management ---
    private Locator getOutletRow(String outletName) {
        return page.locator("div.rounded-2xl").filter(new Locator.FilterOptions().setHasText(outletName));
    }

    public void clickEditOutletSettings(String outletName) {
        getOutletRow(outletName).locator("button[title='Edit Settings']").click();
    }

    public void clickEditOutletShifts(String outletName) {
        getOutletRow(outletName).locator("button[title='Edit Shifts']").click();
    }
}
