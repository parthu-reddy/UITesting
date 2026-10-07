package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;

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

    /** Read the signed-in person's available outlets through the visible selector. */
    public List<String> availableOutletLabels() {
        Locator outlet = page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Outlet").setExact(true));
        outlet.click();
        try {
            Locator options = page.getByRole(com.microsoft.playwright.options.AriaRole.LISTBOX,
                    new Page.GetByRoleOptions().setName("Outlet").setExact(true))
                    .getByRole(com.microsoft.playwright.options.AriaRole.OPTION)
                    .filter(new Locator.FilterOptions().setVisible(true));
            options.first().waitFor();
            return options.allInnerTexts().stream().map(String::trim).sorted().toList();
        } finally {
            outlet.press("Escape");
        }
    }

    /** A named fixture must be visible for the same owner; defaults use that owner's own options. */
    public String availableOutlet(String requested, int fallback) {
        List<String> available = availableOutletLabels();
        if (requested == null || requested.isBlank()) {
            if (available.size() <= fallback) {
                throw new AssertionError("Signed-in restaurant needs " + (fallback + 1)
                        + " outlets, but selector shows " + available);
            }
            return available.get(fallback);
        }
        List<String> matching = available.stream().filter(label -> matchesOutlet(label, requested)).toList();
        if (matching.size() != 1) {
            throw new AssertionError("Named outlet " + requested + " must match exactly one visible outlet of the"
                    + " signed-in restaurant; pass -Drestaurant.phone for its owner. Available: " + available);
        }
        return matching.get(0);
    }

    public void selectOutlet(String outletName) {
        Locator outlet = page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Outlet"));
        if (!matchesOutlet(outlet.innerText(), outletName)) {
            outlet.click(); Locator options = page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION); options.first().waitFor();
            boolean selected = false;
            for (int i = 0; i < options.count(); i++) {
                if (matchesOutlet(options.nth(i).innerText(), outletName)) { options.nth(i).click(); selected = true; break; }
            }
            if (!selected) throw new AssertionError("Outlet not found in the rendered grouped selector: " + outletName);
        }
        page.waitForCondition(() -> matchesOutlet(outlet.innerText(), outletName), new Page.WaitForConditionOptions().setTimeout(5000));
    }

    private static boolean matchesOutlet(String value, String outletName) { return value.trim().equals(outletName) || value.trim().endsWith(" / " + outletName); }

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
        page.locator("button[aria-label='Restaurant management and menu settings']").first().click();
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
