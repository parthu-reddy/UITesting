package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminFleetMap.tsx, AdminAssignmentMap.tsx}
 * <p>
 * Fleet monitoring: map view, driver selection, driver details, filter controls.
 * </p>
 */
public class AdminFleetMapPage {
    private final Page page;
    public AdminFleetMapPage(Page page) { this.page = page; }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isFleetMapVisible() {
        return page.locator("text=Fleet Map, text=Fleet, text=Drivers").first().isVisible();
    }

    public int getDriverMarkerCount() {
        return page.locator("[data-testid='driver-marker'], .driver-pin").count();
    }

    // ── Driver selection ─────────────────────────────────────────────────

    public void selectDriver(int index) {
        page.locator("[data-testid='driver-marker'], .driver-pin, button:has(svg.lucide-truck)").nth(index).click();
        page.waitForTimeout(500);
    }

    public boolean isDriverDetailVisible() {
        return page.locator("text=Driver Details, text=Driver Info, text=Active Delivery").first().isVisible();
    }

    public String getDriverName() {
        return page.locator("text=Driver Details, text=Driver Info").locator("xpath=..").locator("p, span").first().innerText().trim();
    }

    // ── Map controls ─────────────────────────────────────────────────────

    public void refreshMap() {
        page.locator("button:has-text('Refresh'), button:has(svg.lucide-refresh-cw)").first().click();
        page.waitForTimeout(1000);
    }
}
