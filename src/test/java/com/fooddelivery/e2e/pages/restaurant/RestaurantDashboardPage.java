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
        page.waitForCondition(() ->
                        anyVisible(page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, new Page.GetByRoleOptions().setName("Menu").setExact(true))) ||
                        anyVisible(page.getByText("Updates every 5 s", new Page.GetByTextOptions().setExact(true))),
                new Page.WaitForConditionOptions().setTimeout(15000));
    }

    private boolean anyVisible(Locator candidates) {
        for (int i = 0; i < candidates.count(); i++) {
            if (candidates.nth(i).isVisible()) return true;
        }
        return false;
    }

    // ── Outlet selection ─────────────────────────────────────────────────

    public void selectOutlet(String outletName) {
        Locator outlet = page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Outlet"));
        if (!outlet.innerText().trim().equals(outletName)) {
            outlet.click();
            page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                    new Page.GetByRoleOptions().setName(outletName).setExact(true)).click();
        }
        page.waitForCondition(() -> outlet.innerText().trim().equals(outletName),
                new Page.WaitForConditionOptions().setTimeout(5000));
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openOrdersTab() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.TAB, 
            new com.microsoft.playwright.Page.GetByRoleOptions()
                .setName(java.util.regex.Pattern.compile("^Orders.*")))
            .first().click();
        page.waitForTimeout(300);
    }

    public void openMenuTab() {
        page.locator("button:has-text('Menu'), [role='tab']:has-text('Menu')").first().click();
        page.waitForTimeout(300);
    }

    public void openSettingsTab() {
        page.locator("button[aria-label='Restaurant registration and menu settings']").first().click();
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
