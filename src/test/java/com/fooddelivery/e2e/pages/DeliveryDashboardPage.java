package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DeliveryDashboardPage {
    private final Page page;

    public DeliveryDashboardPage(Page page) {
        this.page = page;
    }

    // --- Header & General ---
    public void toggleDutyStatus() {
        // Clicks either "Online Duty" or "Offline" depending on current state
        page.locator("button").filter(new Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("Online Duty|Offline"))).click();
    }

    public void clickProfileSettings() {
        page.locator("button[title='Profile Settings']").click();
    }

    public void clickToggleTheme() {
        page.locator("button[title='Toggle Light/Dark Mode']").click();
    }

    // --- History / Stats ---
    public void clickTripsCompleted() {
        page.locator("button").filter(new Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("(?i)Trips Completed"))).click();
    }

    public void clickBackFromHistory() {
        // Back arrow has an ancestor button with class containing rounded-full
        page.locator("button").filter(new Locator.FilterOptions().setHasText("Completed Deliveries")).locator("..").locator("button").first().click();
        // Or simpler, just finding the arrow button by its role/context, but using text is safer.
        // The back button doesn't have text. It's an ArrowLeft icon.
        // Let's use getByRole for it if possible, or just the first button in the history modal.
    }

    public void fillHistoryDateFilter(String date) {
        page.locator("input[type='date']").fill(date);
    }

    public void clickClearHistoryFilter() {
        page.locator("button", new Page.LocatorOptions().setHasText("Clear")).click();
    }

    public void clickHistoryPrevPage() {
        page.locator("button", new Page.LocatorOptions().setHasText("Prev")).click();
    }

    public void clickHistoryNextPage() {
        page.locator("button", new Page.LocatorOptions().setHasText("Next")).click();
    }

    // --- Available Jobs ---
    public void clickAcceptAndOpenMap() {
        page.locator("button", new Page.LocatorOptions().setHasText("Accept & Open Map")).click();
    }

    // --- Ping / Dispatch ---
    public void clickTimeoutDispatch() {
        page.locator("button", new Page.LocatorOptions().setHasText("Timeout")).click();
    }

    public void clickDeclineDispatch() {
        page.locator("button", new Page.LocatorOptions().setHasText("Decline")).click();
    }

    public void clickAcceptDispatchOrder() {
        page.locator("button", new Page.LocatorOptions().setHasText("Accept Order")).click();
    }

    // --- Active Job ---
    public void clickMarkArrivedAtRestaurant() {
        page.locator("button", new Page.LocatorOptions().setHasText("Mark Arrived at Restaurant")).click();
    }

    public void fillPickupOTP(String otp) {
        page.locator("input[placeholder='Enter 6-digit pickup OTP']").fill(otp);
    }

    public void clickConfirmPickupAndStartDriving() {
        page.locator("button", new Page.LocatorOptions().setHasText("Confirm Pickup & Start Driving")).click();
    }

    public void clickAbortDelivery() {
        page.locator("button", new Page.LocatorOptions().setHasText("Abort Delivery (Emergency)")).click();
    }

    public void fillCustomerOTP(String otp) {
        page.locator("input[placeholder='Ask customer for 6-digit OTP']").fill(otp);
    }

    public void checkGoOfflineAfterDelivery(boolean check) {
        Locator checkbox = page.locator("input#goOfflineAfter");
        if (check) {
            checkbox.check();
        } else {
            checkbox.uncheck();
        }
    }

    public void clickConfirmDelivery() {
        page.locator("button").filter(new Locator.FilterOptions().setHasText(java.util.regex.Pattern.compile("Confirm Delivery & Credit"))).click();
    }

    public void clickCustomerUnavailable() {
        page.locator("button", new Page.LocatorOptions().setHasText("Customer Unavailable (Mark Failed)")).click();
    }

    // --- Prompts / Modals ---
    public void clickEnablePermissions() {
        page.locator("button", new Page.LocatorOptions().setHasText("Enable Permissions")).click();
    }
}
