package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.Map;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Prove rendered marker motion from the rider browser's normal GPS/WebSocket pipeline. */
public final class LiveCustomerMap {
    private LiveCustomerMap() {}
    public static List<Map<String, Object>> assertMovement(Page customer, Page rider, String orderId,
            String riderPhone, double initialLat, double initialLng) {
        customer.bringToFront();
        org.assertj.core.api.Assertions.assertThat(customer.evaluate("() => document.visibilityState")).isEqualTo("visible");
        Locator tracker = customer.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        assertThat(tracker).isVisible();
        Locator riderCard = tracker.getByTestId("rider-card");
        assertThat(riderCard).isVisible();
        assertThat(riderCard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Call ")))).isVisible();
        Locator map = customer.getByRole(AriaRole.REGION,
                new Page.GetByRoleOptions().setName("Live order tracking").setExact(true));
        Object layout = map.evaluate("el => { const r=el.getBoundingClientRect(), p=el.parentElement; return {height:r.height,width:r.width,parentHeight:p.getBoundingClientRect().height,flexShrink:getComputedStyle(p).flexShrink,viewportHeight:innerHeight,viewportWidth:innerWidth,visibility:document.visibilityState}; }");
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("target/lifecycle", orderId + "-map-layout.json"),
                    (String) customer.evaluate("data => JSON.stringify(data,null,2)", layout));
        } catch (java.io.IOException failure) { throw new AssertionError("Cannot retain map layout proof", failure); }
        assertThat(map).isVisible();
        org.assertj.core.api.Assertions.assertThat(map.boundingBox().height).as("Map retains its height when the live bill is expanded").isGreaterThanOrEqualTo(160);
        org.assertj.core.api.Assertions.assertThat(map.boundingBox().width).isPositive();
        assertThat(map.locator("canvas.maplibregl-canvas")).isVisible();
        Locator marker = map.getByRole(AriaRole.IMG,
                new Locator.GetByRoleOptions().setName("Courier location").setExact(true));
        marker.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        customer.waitForCondition(() -> at(marker, initialLat, initialLng),
                new Page.WaitForConditionOptions().setTimeout(30000));
        Map<String, Object> before = point(marker, orderId, riderPhone);
        // A nearby 1m GPS change triggers the real watch callback. No synthetic stream,
        // backend write, time advance or intentional sleep is used.
        double movedLat = initialLat + 0.00001;
        double movedLng = initialLng + 0.00001;
        rider.context().setGeolocation(new com.microsoft.playwright.options.Geolocation(movedLat, movedLng));
        customer.waitForCondition(() -> at(marker, movedLat, movedLng),
                new Page.WaitForConditionOptions().setTimeout(30000));
        Map<String, Object> after = point(marker, orderId, riderPhone);
        assertThat(map.getByRole(AriaRole.IMG,
                new Locator.GetByRoleOptions().setName("Courier location").setExact(true))).hasCount(1);
        map.screenshot(new Locator.ScreenshotOptions().setPath(java.nio.file.Path.of("target/lifecycle", orderId + "-customer-map.png")));
        return List.of(before, after);
    }
    private static boolean at(Locator marker, double lat, double lng) {
        String observedLat = marker.getAttribute("data-lat"), observedLng = marker.getAttribute("data-lng");
        if (observedLat == null || observedLng == null) return false;
        return Math.abs(Double.parseDouble(observedLat) - lat) < 0.0000005
                && Math.abs(Double.parseDouble(observedLng) - lng) < 0.0000005;
    }
    private static Map<String, Object> point(Locator marker, String orderId, String riderPhone) {
        return Map.of("orderId", orderId, "riderPhone", riderPhone,
                "lat", Double.parseDouble(marker.getAttribute("data-lat")),
                "lng", Double.parseDouble(marker.getAttribute("data-lng")), "source", "rendered-marker");
    }
}
