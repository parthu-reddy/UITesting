package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Restaurant Dashboard shell.
 * Maps to: {@code RestaurantDashboard.tsx, RestaurantPortal.tsx}
 * <p>
 * Also covers sub-components: {@code RestaurantHeaderBar.tsx}, {@code RestaurantTabPanels.tsx},
 * {@code RestaurantOrderDrawers.tsx}
 * </p>
 */
public class RestaurantDashboardPage {

    private final Page page;

    public RestaurantDashboardPage(Page page) {
        this.page = page;
    }

    public void waitForDashboard() {
        page.getByText("Menu Stock Toggles").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    // ── Outlet selection ─────────────────────────────────────────────────

    public void selectOutlet(String outletName) {
        page.locator("select").selectOption(
                new com.microsoft.playwright.options.SelectOption().setLabel(outletName));
        page.waitForTimeout(500);
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openOrdersTab() {
        page.locator("button:has-text('Live Kitchen Feed'), [role='tab']:has-text('Live Kitchen Feed')").first().click();
        page.waitForTimeout(300);
    }

    public void openMenuTab() {
        page.locator("button:has-text('Menu'), [role='tab']:has-text('Menu')").first().click();
        page.waitForTimeout(300);
    }

    public void openSettingsTab() {
        page.locator("button:has-text('Settings'), [role='tab']:has-text('Settings')").first().click();
        page.waitForTimeout(300);
    }

    public void openEarningsTab() {
        page.locator("button:has-text('Earnings'), [role='tab']:has-text('Earnings')").first().click();
        page.waitForTimeout(300);
    }

    public void openReviewsTab() {
        page.locator("button:has-text('Reviews'), [role='tab']:has-text('Reviews')").first().click();
        page.waitForTimeout(300);
    }
}
