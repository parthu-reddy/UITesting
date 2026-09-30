package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminLiveOperations.tsx}
 * <p>
 * Admin live operations: order list with pagination, read-only nearby rider telemetry,
 * routes to the audited intervention and refund workflows, and map view.
 * </p>
 */
public class AdminLiveOpsPage {
    private final Page page;
    public AdminLiveOpsPage(Page page) { this.page = page; }

    // ── Visibility ───────────────────────────────────────────────────────

    /** The order list's "Active Orders" header is always rendered (AdminLiveOperations.tsx). */
    public boolean isLiveOpsVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Orders").setExact(true)).isVisible();
    }

    // ── Order list ───────────────────────────────────────────────────────

    /** One button per active order, in the list under the "Active Orders" header. */
    private com.microsoft.playwright.Locator orders() {
        return page.locator("div:has(> h3:text-is('Active Orders')) + div > button");
    }

    public int getActiveOrderCount() {
        return orders().count();
    }

    public void waitForOrdersLoaded() {
        orders().first().or(page.getByText("No active orders right now.",
                        new Page.GetByTextOptions().setExact(true))).first()
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(30000));
    }

    public boolean isEmptyStateVisible() {
        return page.getByText("No active orders right now.",
                new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    public void selectOrder(int index) {
        orders().nth(index).click();
        page.waitForTimeout(500);
    }

    public void selectOrderById(String orderIdPrefix) {
        page.locator("button:has(p:has-text('#" + orderIdPrefix + "'))").first().click();
        page.waitForTimeout(500);
    }

    /** A selected order opens its panel, which carries "Refund Actions". */
    public boolean isOrderSelected() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Refund Actions").setExact(true)).isVisible();
    }

    public String getNearbyReadyDriverHeading() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Nearby Ready Drivers \\(\\d+\\)$")))
                .innerText().trim();
    }

    public boolean isNoAvailableDriverMessageVisible() {
        return page.getByText("No available drivers nearby.",
                new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    public void refresh() {
        page.locator("button:has-text('Refresh')").first().click();
        page.waitForTimeout(1000);
    }

    // ── Pagination ───────────────────────────────────────────────────────

    public void nextPage() {
        com.microsoft.playwright.Locator button = page.locator("button:has-text('Next')").first();
        if (button.count() == 0 || !button.isEnabled()) return;
        button.click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        com.microsoft.playwright.Locator button = page.locator("button:has-text('Prev')").first();
        if (button.count() == 0 || !button.isEnabled()) return;
        button.click();
        page.waitForTimeout(500);
    }

    public String getPageInfo() {
        return page.getByText(java.util.regex.Pattern.compile("^Page \\d+ of \\d+$")).first().innerText().trim();
    }

    public boolean canGoNextPage() {
        com.microsoft.playwright.Locator next = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Next").setExact(true));
        return next.count() > 0 && next.isEnabled();
    }

    public boolean canGoPreviousPage() {
        com.microsoft.playwright.Locator previous = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Prev").setExact(true));
        return previous.count() > 0 && previous.isEnabled();
    }

    // ── Nearby rider telemetry and manual-intervention handoff ────────────

    public int getNearbyReadyDriverCount() {
        return page.getByTestId("nearby-ready-driver").count();
    }

    /** The selected order's candidate request has completed with cards, an empty state, or an error. */
    public void waitForNearbyDriverResult() {
        page.getByTestId("nearby-ready-drivers")
                .locator("[data-testid='nearby-ready-driver'], p:has-text('No available drivers nearby.'), p[role='alert']")
                .first()
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    public boolean hasDirectAssignmentAction() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Assign").setExact(true)).count() > 0;
    }

    public boolean isManualInterventionHandoffVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Open Manual Interventions").setExact(true)).isVisible();
    }

    public void openManualInterventions() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Open Manual Interventions").setExact(true)).click();
    }

}
