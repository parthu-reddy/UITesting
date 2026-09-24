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

    // Maps to AdminFleetMap.tsx. Markers are maplibre elements built by createMapPin, which tags
    // each with data-pin-tone: restaurant, customer, rider or rider-offline.

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isFleetMapVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Fleet Map Legend").setExact(true)).isVisible();
    }

    private com.microsoft.playwright.Locator riderMarkers() {
        return page.locator(".fleet-marker[data-pin-tone^='rider']");
    }

    public int getDriverMarkerCount() {
        return riderMarkers().count();
    }

    // ── Driver selection ─────────────────────────────────────────────────

    /** A click opens the marker's popup ("Rider: <name>, Status: ...") and copies the rider id. */
    public void selectDriver(int index) {
        riderMarkers().nth(index).click();
        page.waitForTimeout(500);
    }

    private com.microsoft.playwright.Locator riderPopup() {
        return page.locator(".maplibregl-popup:has-text('Rider:')");
    }

    public boolean isDriverDetailVisible() {
        return riderPopup().isVisible();
    }

    public String getDriverName() {
        String text = riderPopup().innerText();
        return text.substring(text.indexOf("Rider:") + "Rider:".length()).split("\n")[0].trim();
    }

    // ── Map controls ─────────────────────────────────────────────────────

    /** The fleet map has no refresh control; a reload refetches every layer. */
    public void refreshMap() {
        page.reload();
        page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Fleet Map Legend").setExact(true))
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(15000));
    }
}
