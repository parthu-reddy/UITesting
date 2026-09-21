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
    }

    public void waitForDashboard() {
        // Wait for the "Deliver to" header or the restaurant browser to appear
        page.locator("text=Deliver to, text=Good Morning, text=What are you craving").first()
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
        page.locator("button:has-text('Settings'), button:has(svg.lucide-settings), [role='tab']:has-text('Settings')").first().click();
        page.waitForTimeout(300);
    }

    // ── Deliver-to header ────────────────────────────────────────────────

    public String getDeliverToText() {
        return page.locator("text=Deliver to").locator("xpath=..").innerText().trim();
    }

    public void clickDeliverTo() {
        page.locator("text=Deliver to").first().click();
        page.waitForTimeout(500);
    }

    // ── Active orders ────────────────────────────────────────────────────

    public boolean hasActiveOrders() {
        return page.locator("text=Active Orders, text=Track Order, text=Order Received").first().isVisible();
    }

    public int getActiveOrderCount() {
        return page.locator("[data-testid='active-order'], .active-order-card").count();
    }
}
