package com.fooddelivery.e2e.base;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Centralized configuration for all E2E tests.
 * All values are overridable via system properties for CI flexibility.
 */
public final class TestConfig {

    private TestConfig() {}

    /** Base URL of the application under test. */
    static final String DEFAULT_APP_URL = "https://gulf-strike-dark-extras.trycloudflare.com";
    public static final String APP_URL = resolveAppUrl(System.getProperty("app.url"), System.getenv("E2E_APP_URL"));

    static String resolveAppUrl(String property, String environment) {
        String selected = property != null ? property : environment != null ? environment : DEFAULT_APP_URL;
        return selected.replaceAll("/+$", "");
    }

    public static String customerPhone() { return seededPhone("customer.phone", "8000000", 500); }
    public static String restaurantPhone() { return seededPhone("restaurant.phone", "9000000", 10); }
    public static String riderPhone() { return seededPhone("rider.phone", "7000000", 30); }
    public static String adminPhone() { return System.getProperty("admin.phone", "1000000001"); }

    private static String seededPhone(String property, String prefix, int upperBound) {
        String override = System.getProperty(property);
        return override != null ? override
                : prefix + String.format("%03d", ThreadLocalRandom.current().nextInt(1, upperBound + 1));
    }

    /** Default timeout for locator waits (ms). */
    public static final int DEFAULT_TIMEOUT = Integer.parseInt(
            System.getProperty("default.timeout", "60000"));

    /** Artificial delay after every Playwright action (ms). Runs are fast by default; the runner's --debug sets 400. */
    public static final int SLOW_MO = Integer.parseInt(
            System.getProperty("slow.mo", "0"));

    /** Headless by default; the runner's --debug opens a visible window. */
    public static final boolean HEADLESS = Boolean.parseBoolean(
            System.getProperty("headless", "true"));

    /** Video recording of browser sessions; off by default, the runner's --debug turns it on. */
    public static final boolean RECORD_VIDEO = Boolean.parseBoolean(
            System.getProperty("record.video", "false"));

    /** The retained runner-secret OTP harness is disabled until explicitly re-enabled. */
    public static boolean e2eOtpEnabled() {
        return Boolean.parseBoolean(System.getProperty("e2e.otp.enabled",
                System.getenv().getOrDefault("E2E_OTP_ENABLED", "false")));
    }

    /**
     * Reads the credential used by the non-browser E2E OTP harness. It is intentionally not a
     * constant so test output and browser state cannot accidentally expose it.
     */
    public static String e2eRunnerSecret() {
        if (!e2eOtpEnabled()) {
            throw new IllegalStateException(
                    "The E2E runner-secret OTP feature is disabled");
        }
        String configured = System.getProperty("e2e.runner.secret");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("E2E_RUNNER_SECRET");
        }
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                    "Set e2e.runner.secret or E2E_RUNNER_SECRET before running browser E2E tests");
        }
        return configured;
    }

    // ── Test User Profiles ──────────────────────────────────────────

    /** Used only when the selected admin reaches first-login profile completion. */
    public static final String ADMIN_PROFILE_NAME = System.getProperty("admin.profile.name", "E2E Admin");
    public static final String ADMIN_PROFILE_EMAIL = System.getProperty(
            "admin.profile.email", "e2e-admin-dynamic@example.com");

    // ── Geolocation (Bangalore) ──────────────────────────────────────────

    public static final double GEO_LAT = 12.9808;
    public static final double GEO_LNG = 77.6467;
}
