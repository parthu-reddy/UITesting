package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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
        return legend().isVisible();
    }

    /** The legend, then the rider layer: a rider pin or the explicit no-location note (the legend renders first). */
    public void waitForFleetMap() {
        legend().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        riderMarkers().or(page.getByTestId("fleet-riders-empty")).first()
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    private com.microsoft.playwright.Locator legend() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Fleet Map Legend").setExact(true));
    }

    private com.microsoft.playwright.Locator riderMarkers() {
        return page.locator(".fleet-marker[data-pin-tone^='rider']");
    }

    public int getDriverMarkerCount() {
        return riderMarkers().count();
    }

    public boolean isNoRiderLocationStateVisible() {
        return page.getByTestId("fleet-riders-empty").isVisible();
    }

    public double getMapViewportHeight() {
        return ((Number) page.locator(".maplibregl-map")
                .evaluate("map => map.getBoundingClientRect().height")).doubleValue();
    }

    /**
     * Captures the map's ancestor dimensions and computed layout values when a viewport check
     * fails. This makes a collapsed map distinguishable from a tile or marker-data failure.
     */
    public String getMapLayoutDiagnostics() {
        return (String) page.locator(".maplibregl-map").evaluate("""
                map => {
                  const nodes = [];
                  for (let node = map; node && nodes.length < 7; node = node.parentElement) {
                    const rect = node.getBoundingClientRect();
                    const style = getComputedStyle(node);
                    nodes.push({
                      tag: node.tagName,
                      className: node.className,
                      width: Math.round(rect.width),
                      height: Math.round(rect.height),
                      computedHeight: style.height,
                      minHeight: style.minHeight,
                      display: style.display,
                      flex: style.flex,
                      position: style.position,
                      overflow: style.overflow
                    });
                  }
                  return JSON.stringify(nodes);
                }
                """);
    }

    public java.util.List<com.microsoft.playwright.Locator> getRiderMarkers() {
        return riderMarkers().all();
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

    private com.microsoft.playwright.Locator refreshControl() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Refresh fleet map").setExact(true));
    }

    public boolean isRefreshControlVisible() {
        return refreshControl().isVisible();
    }

    /** Uses the in-page refresh control so the check exercises the polling view without a reload. */
    public void refreshMap() {
        refreshControl().click();
    }
}
