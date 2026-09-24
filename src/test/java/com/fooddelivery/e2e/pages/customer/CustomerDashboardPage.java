package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Customer Dashboard shell.
 * Maps to: {@code CustomerDashboard.tsx + DashboardHeader.tsx}
 */
public class CustomerDashboardPage {

    private final Page page;

    public CustomerDashboardPage(Page page) {
        this.page = page;
        this.page.onConsoleMessage(msg -> System.out.println("[BROWSER CONSOLE] " + msg.text()));
    }

    public void waitForDashboard() {
        try {
            page.route("https://nominatim.openstreetmap.org/reverse*", route -> {
                route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("{\"display_name\":\"Mocked Current Location\"}"));
            });
        } catch (Exception ignored) { }

        Locator loc = page.locator("text=Use Current Location").first();
        try {
            loc.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            loc.click();
            page.waitForTimeout(1000);
        } catch (Exception ignored) { }

        // Wait for the header to appear
        page.locator("header").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openHomeTab() {
        page.locator("button:has-text('Home'), [role='tab']:has-text('Home'), nav button").first().click();
        page.waitForTimeout(300);
    }

    public void openOrdersTab() {
        page.locator("button:has-text('Orders'), [role='tab']:has-text('Orders')").first().click();
        page.waitForTimeout(300);
    }

    public void openSettingsTab() {
        page.locator("button[title='Profile Settings']").first().click();
        page.waitForTimeout(300);
    }

    // ── Deliver-to header ────────────────────────────────────────────────

    public String getDeliverToText() {
        return page.locator("header >> text=Deliver to").locator("..").innerText().trim();
    }

    public void clickDeliverTo() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON)
            .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Deliver to"))
            .first().click();
        page.waitForTimeout(500);
    }

    // ── Active orders ────────────────────────────────────────────────────

    public boolean hasActiveOrders() {
        return page.locator("[data-testid='order-tracker']").first().isVisible();
    }

}
