package com.fooddelivery.e2e.base;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * O3 retains each applicant and closes browser contexts without invoking legacy state helpers.
 * Duty changes belong to the visible scenario itself; teardown never reads or changes backend state.
 */
public abstract class PartnerApplicationsUiTestBase extends TestBase {
    private Path measurements;

    @Override
    @BeforeEach
    public void setUpContexts() {
        super.setUpContexts();
        measurements = Path.of("target/business-platform/o3/browser-timings/",
                getClass().getSimpleName() + "-" + UUID.randomUUID() + ".csv");
        try {
            Files.createDirectories(measurements.getParent());
            Files.writeString(measurements,
                    "recordedAt,scenario,family,status,networkTotalMs,requestToHeadersMs,targetMs,method\n",
                    StandardOpenOption.CREATE_NEW);
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException("Could not create O3 browser timing evidence", failure);
        }
        for (Page page : Arrays.asList(customerPage, restaurantPage, riderPage, adminPage)) {
            page.onRequestFinished(this::recordUiResponseTiming);
        }
    }

    /** Observe requests caused by the normal UI; never issue a request or read response bodies. */
    private void recordUiResponseTiming(Request request) {
        if (!"GET".equals(request.method())) return;
        String path = java.net.URI.create(request.url()).getPath();
        String family = switch (path) {
            case "/api/v1/internal/admin/restaurant-applications",
                    "/api/v1/internal/admin/delivery-applications" -> "admin-review-queue";
            case "/api/v1/delivery-onboarding/application", "/api/v1/verification/status/me" -> "onboarding-status";
            default -> path.matches("/api/v1/restaurant-onboarding/organisations/[^/]+/application")
                    ? "onboarding-status" : null;
        };
        if (family == null) return;
        var timing = request.timing();
        var response = request.response();
        var row = List.of(java.time.Instant.now().toString(), getClass().getSimpleName(), family,
                Integer.toString(response == null ? 0 : response.status()), Double.toString(timing.responseEnd),
                Double.toString(timing.responseStart - timing.requestStart),
                family.equals("admin-review-queue") ? "300" : "150",
                "Passive completed browser request from visible UI; includes tunnel/network latency; excludes rendering");
        String encoded = row.stream().map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "\n";
        try {
            Files.writeString(measurements, encoded, StandardOpenOption.APPEND);
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException("Could not retain O3 browser timing evidence", failure);
        }
    }

    @Override
    @AfterEach
    public void tearDownContexts() {
        RuntimeException closeFailure = null;
        for (BrowserContext context : Arrays.asList(
                customerContext, restaurantContext, riderContext, adminContext)) {
            if (context == null) continue;
            try {
                context.close();
            } catch (RuntimeException failure) {
                if (closeFailure == null) closeFailure = failure;
                else closeFailure.addSuppressed(failure);
            }
        }
        if (closeFailure != null) throw closeFailure;
    }
}
