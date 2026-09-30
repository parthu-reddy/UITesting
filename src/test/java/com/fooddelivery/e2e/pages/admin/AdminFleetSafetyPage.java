package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Narrow page object for the browser-routed Fleet Map fixture.
 *
 * <p>The public map view has no mutation controls. These locators deliberately cover the
 * accessible legend, refresh control, map region, and the DOM pins produced by MapLibre so the
 * test can prove every read layer reaches the rendered map.</p>
 */
public final class AdminFleetSafetyPage {

    private static final String PARTIAL_REFRESH_ERROR =
            "Some fleet data could not be refreshed. Showing the latest available data.";

    private final Page page;

    public AdminFleetSafetyPage(Page page) {
        this.page = page;
    }

    public void waitForFleetMap() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Fleet Map Legend").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        mapRegion().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForLayerCounts(int restaurants, int riders, int customers) {
        layerCount("Restaurants", restaurants).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        layerCount("Riders", riders).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        layerCount("Customers", customers).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void selectFleetCity(String cityId) {
        fleetCitySelector().selectOption(cityId);
    }

    public void waitForFleetCity(String cityId) {
        fleetCitySelector().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForCondition(() -> cityId.equals(fleetCitySelector().inputValue()),
                new Page.WaitForConditionOptions().setTimeout(10_000));
    }

    public void waitForFixtureMarkerCount(int expectedCount) {
        page.waitForCondition(() -> fixtureMarkers().count() == expectedCount,
                new Page.WaitForConditionOptions().setTimeout(10_000));
    }

    public int fixtureMarkerCount() {
        return fixtureMarkers().count();
    }

    public void openOnlineRiderPopup() {
        onlineRiderMarker().click();
    }

    public Locator riderPopup() {
        return page.locator(".maplibregl-popup:has-text('Rider:')").first();
    }

    public void waitForRiderPopup() {
        riderPopup().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void refresh() {
        refreshButton().click();
    }

    public void waitForPartialRefreshError() {
        partialRefreshError().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForRefreshErrorToClear() {
        partialRefreshError().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public double mapViewportHeight() {
        return ((Number) page.locator(".maplibregl-map")
                .evaluate("map => map.getBoundingClientRect().height")).doubleValue();
    }

    private Locator mapRegion() {
        return page.getByRole(AriaRole.REGION,
                new Page.GetByRoleOptions().setName("Live fleet map").setExact(true));
    }

    private Locator layerCount(String label, int count) {
        return page.getByText(label + " (" + count + ")",
                new Page.GetByTextOptions().setExact(true));
    }

    private Locator fixtureMarkers() {
        return page.locator(".fleet-marker");
    }

    private Locator onlineRiderMarker() {
        return page.locator(".fleet-marker[data-pin-tone='rider']").first();
    }

    private Locator refreshButton() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Refresh fleet map").setExact(true));
    }

    private Locator fleetCitySelector() {
        return page.getByRole(AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Fleet city").setExact(true));
    }

    private Locator partialRefreshError() {
        return page.getByText(PARTIAL_REFRESH_ERROR,
                new Page.GetByTextOptions().setExact(true));
    }
}
