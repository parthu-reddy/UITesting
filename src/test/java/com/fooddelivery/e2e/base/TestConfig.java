package com.fooddelivery.e2e.base;

/**
 * Centralized configuration for all E2E tests.
 * All values are overridable via system properties for CI flexibility.
 */
public final class TestConfig {

    private TestConfig() {}

    /** Base URL of the application under test. */
    public static final String APP_URL = System.getProperty("app.url", System.getenv().getOrDefault("E2E_APP_URL",
            "https://gulf-strike-dark-extras.trycloudflare.com/"));

    /** Default timeout for locator waits (ms). */
    public static final int DEFAULT_TIMEOUT = Integer.parseInt(
            System.getProperty("default.timeout", "60000"));

    /** Artificial delay between Playwright actions (ms). 0 for CI, 300–500 for local debugging. */
    public static final int SLOW_MO = Integer.parseInt(
            System.getProperty("slow.mo", "400"));

    /** Run browsers headless (true for CI, false for local). */
    public static final boolean HEADLESS = Boolean.parseBoolean(
            System.getProperty("headless", "false"));

    /** Enable video recording of browser sessions. */
    public static final boolean RECORD_VIDEO = Boolean.parseBoolean(
            System.getProperty("record.video", "true"));

    // ── Test User Phone Numbers ──────────────────────────────────────────

    public static final String CUSTOMER_PHONE = System.getProperty("customer.phone", "8000000001");
    public static final String RESTAURANT_PHONE = System.getProperty("restaurant.phone", "9000000001");
    public static final String RIDER_PHONE = System.getProperty("rider.phone", "7000000001");
    public static final String ADMIN_PHONE = System.getProperty("admin.phone", "1000000001");

    /** Used only when the selected admin reaches first-login profile completion. */
    public static final String ADMIN_PROFILE_NAME = System.getProperty("admin.profile.name", "E2E Admin");
    public static final String ADMIN_PROFILE_EMAIL = System.getProperty(
            "admin.profile.email", "e2e-admin-" + ADMIN_PHONE + "@example.com");

    // ── Geolocation (Bangalore) ──────────────────────────────────────────

    public static final double GEO_LAT = 12.9808;
    public static final double GEO_LNG = 77.6467;
}
