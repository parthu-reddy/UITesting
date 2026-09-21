package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Map tracking components.
 * Maps to: {@code MapPanel.tsx}, {@code OrderTrackingMap.tsx}, {@code PlaceSearchField.tsx},
 * {@code CoordinateFields.tsx}
 * <p>
 * Map rendering is not directly E2E testable in headless mode, but the containers,
 * search inputs, and coordinate field interactions can be verified.
 * </p>
 */
public class MapTrackingPage {

    private final Page page;

    public MapTrackingPage(Page page) {
        this.page = page;
    }

    public boolean isMapContainerVisible() {
        return page.locator("[data-testid='map-container'], .map-panel, [class*='map']").first().isVisible();
    }

    public void searchPlace(String query) {
        page.locator("input[placeholder*='Search'], input[placeholder*='search'], input[placeholder*='Place']").first().fill(query);
        page.waitForTimeout(1000);
    }

    public boolean hasSearchResults() {
        return page.locator("[data-testid='place-result'], .place-suggestion").first().isVisible();
    }

    public void selectFirstResult() {
        page.locator("[data-testid='place-result'], .place-suggestion").first().click();
        page.waitForTimeout(500);
    }

    public void fillCoordinates(String lat, String lng) {
        page.locator("input[placeholder*='lat'], input[name*='lat']").first().fill(lat);
        page.locator("input[placeholder*='lng'], input[name*='lng']").first().fill(lng);
    }
}
