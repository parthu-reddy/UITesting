package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Map tracking components.
 * Maps to: {@code MapPanel.tsx}, {@code OrderTrackingMap.tsx}, {@code PlaceSearchField.tsx},
 * {@code CoordinateFields.tsx}
 * <p>
 * Live map canvas and rendered courier movement are verified in HappyDeliveryFlowTest.
 * Place search and coordinate controls are separate contracts.
 * </p>
 */
public class MapTrackingPage {

    private final Page page;

    public MapTrackingPage(Page page) {
        this.page = page;
    }

    public boolean isMapContainerVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.REGION,
                new Page.GetByRoleOptions().setName("Live order tracking").setExact(true)).isVisible();
    }

    /**
     * PlaceSearchField.tsx: the input is named "Search for a place". Matching on a "Search"
     * placeholder instead picked the customer home's restaurant search, which comes first.
     */
    public void searchPlace(String query) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search for a place").setExact(true)).fill(query);
        page.waitForTimeout(1000);
    }

    /** Suggestions render in the field's own wrapper (the div whose direct child is the input), one map-pin button per place. */
    private com.microsoft.playwright.Locator results() {
        return page.locator("div.relative:has(> input[aria-label='Search for a place']) button:has(svg.lucide-map-pin)");
    }

    public boolean hasSearchResults() {
        return results().first().isVisible();
    }

    public void selectFirstResult() {
        results().first().click();
        page.waitForTimeout(500);
    }

    public void fillCoordinates(String lat, String lng) {
        page.locator("input[placeholder*='lat'], input[name*='lat']").first().fill(lat);
        page.locator("input[placeholder*='lng'], input[name*='lng']").first().fill(lng);
    }
}
