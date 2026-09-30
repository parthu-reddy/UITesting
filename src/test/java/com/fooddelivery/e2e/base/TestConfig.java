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
