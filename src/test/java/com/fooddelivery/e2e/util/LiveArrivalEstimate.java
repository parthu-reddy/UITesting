package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * A5b: the customer's "Arriving in" comes from the assigned rider's current position.
 *
 * <p>Everything is read from the rendered tracker ({@code data-testid=arrival-estimate}: its
 * {@code data-eta-source} and {@code data-eta-ms}, and the visible "Live" chip). The rider moves only
 * through their browser's real geolocation, which the rider app streams over its WebSocket; no API
 * call, backend write or clock change is made here. Each observation is recorded for the evidence.
 */
public final class LiveArrivalEstimate {
    private final Page customer;
    private final Page rider;
    private final String orderId;
    private final List<Map<String, Object>> observations = new ArrayList<>();

    public LiveArrivalEstimate(Page customer, Page rider, String orderId) {
        this.customer = customer;
        this.rider = rider;
        this.orderId = orderId;
        // A resumed order keeps the observations its earlier invocations recorded.
        try {
            Path file = evidenceFile();
            if (Files.exists(file)) {
                Map<?, ?> earlier = new com.google.gson.Gson().fromJson(Files.readString(file), Map.class);
                for (Object row : (List<?>) earlier.get("observations")) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    ((Map<?, ?>) row).forEach((k, v) -> copy.put(String.valueOf(k), v));
                    observations.add(copy);
                }
            }
        } catch (java.io.IOException failure) {
            throw new AssertionError("Cannot read earlier live arrival evidence", failure);
        }
    }

    private Path evidenceFile() {
        return Path.of("target/lifecycle", orderId + "-live-eta.json");
    }

    private Locator estimate() {
        return customer.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "'] [data-testid='arrival-estimate']");
    }

    /**
     * Waits for the tracker to present the estimate as live. The first poll after a new leg
     * schedules the route off-thread and a later poll (every 10 s while a rider is assigned)
     * carries it, so this allows several polls.
     */
    public Map<String, Object> awaitLive(String stage, double riderLat, double riderLng) {
        Locator estimate = estimate();
        assertThat(estimate).hasAttribute("data-eta-source", "live",
                new LocatorAssertions.HasAttributeOptions().setTimeout(90_000));
        assertThat(estimate.getByText("Live", new Locator.GetByTextOptions().setExact(true))).isVisible();
        return observe(stage, riderLat, riderLng);
    }

    /** Moves the rider's GPS to {@code lat,lng}; waits until the live estimate is within {@code seconds} of now. */
    public Map<String, Object> moveRiderAndAwaitArrivalWithin(String stage, double lat, double lng, long seconds) {
        rider.context().setGeolocation(new com.microsoft.playwright.options.Geolocation(lat, lng));
        customer.waitForCondition(() -> {
            Map<String, Object> now = read();
            return "live".equals(now.get("source")) && ((Number) now.get("etaMinusNowSeconds")).longValue() <= seconds;
        }, new Page.WaitForConditionOptions().setTimeout(120_000));
        return observe(stage, lat, lng);
    }

    private Map<String, Object> read() {
        Locator estimate = estimate();
        Map<String, Object> row = new LinkedHashMap<>();
        if (estimate.count() != 1) {
            row.put("source", "absent");
            row.put("etaMinusNowSeconds", Long.MAX_VALUE);
            return row;
        }
        long etaMs = Long.parseLong(estimate.getAttribute("data-eta-ms"));
        long browserNow = ((Number) customer.evaluate("() => Date.now()")).longValue();
        row.put("source", estimate.getAttribute("data-eta-source"));
        row.put("etaMs", etaMs);
        row.put("browserNowMs", browserNow);
        row.put("etaMinusNowSeconds", (etaMs - browserNow) / 1000);
        return row;
    }

    private Map<String, Object> observe(String stage, double riderLat, double riderLng) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", orderId);
        row.put("stage", stage);
        row.put("recordedAt", java.time.Instant.now().toString());
        row.putAll(read());
        row.put("riderLat", riderLat);
        row.put("riderLng", riderLng);
        row.put("liveChipVisible", estimate().getByText("Live", new Locator.GetByTextOptions().setExact(true)).isVisible());
        observations.add(row);
        write();
        org.assertj.core.api.Assertions.assertThat(row.get("source")).as(stage + " estimate source").isEqualTo("live");
        org.assertj.core.api.Assertions.assertThat((Long) row.get("etaMinusNowSeconds"))
                .as(stage + " estimate is ahead of now and within two hours").isBetween(0L, 7_200L);
        return row;
    }

    private void write() {
        try {
            Path file = evidenceFile();
            Files.createDirectories(file.getParent());
            // In Java, not page.evaluate: Playwright cannot pass a Java long into the page.
            Files.writeString(file, new com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    .toJson(Map.of("observations", observations)));
        } catch (java.io.IOException failure) {
            throw new AssertionError("Cannot record live arrival evidence", failure);
        }
    }
}
