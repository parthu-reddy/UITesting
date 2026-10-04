package com.fooddelivery.e2e.base;

import com.microsoft.playwright.*;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all E2E tests.
 * <p>
 * Provides up to 4 isolated browser contexts (customer, restaurant, rider, admin)
 * with geolocation, notifications, video recording, and console logging.
 * </p>
 */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class TestBase {

    private static final Map<String, String> ADMIN_STORAGE_STATES = new ConcurrentHashMap<>();

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext customerContext;
    protected BrowserContext restaurantContext;
    protected BrowserContext riderContext;
    protected BrowserContext adminContext;

    protected Page customerPage;
    protected Page restaurantPage;
    protected Page riderPage;
    protected Page adminPage;

    protected String testCustomerPhone;
    protected String testRestaurantPhone;
    protected String testRiderPhone;
    protected String testAdminPhone;

    @BeforeAll
    public static void setUpClass() {
        System.out.println("[E2E STARTUP] Creating Playwright driver");
        playwright = Playwright.create();
        System.out.println("[E2E STARTUP] Launching Chromium (headless=" + TestConfig.HEADLESS + ")");
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(TestConfig.HEADLESS)
                        .setSlowMo(TestConfig.SLOW_MO)
                        .setArgs(List.of(
                                "--unsafely-treat-insecure-origin-as-secure=" + TestConfig.APP_URL.replaceAll("/$", ""),
                                "--disable-background-timer-throttling",
                                "--disable-backgrounding-occluded-windows",
                                "--disable-renderer-backgrounding"
                        ))
        );
        System.out.println("[E2E STARTUP] Chromium ready");
    }

    @AfterAll
    public static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    public void setUpContexts() {
        testCustomerPhone = TestConfig.customerPhone();
        testRestaurantPhone = TestConfig.restaurantPhone();
        testRiderPhone = TestConfig.riderPhone();
        testAdminPhone = TestConfig.adminPhone();

        customerContext = createContext("customer");
        restaurantContext = createContext("restaurant");
        riderContext = createContext("rider");
        adminContext = createContext("admin");

        customerPage = createPage(customerContext);
        restaurantPage = createPage(restaurantContext);
        riderPage = createPage(riderContext);
        adminPage = createPage(adminContext);
    }

    @AfterEach
    public void tearDownContexts() {
        try {
            com.fooddelivery.e2e.util.SeededRiderDuty.finishOfflineIfIdle(riderPage);
        } finally {
            closeQuietly(customerContext);
            closeQuietly(restaurantContext);
            closeQuietly(riderContext);
            closeQuietly(adminContext);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private BrowserContext createContext(String label) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setPermissions(Arrays.asList("geolocation", "notifications"))
                .setGeolocation(TestConfig.GEO_LAT, TestConfig.GEO_LNG);

        if ("admin".equals(label)) {
            String storageState = ADMIN_STORAGE_STATES.get(adminStorageStateKey());
            if (storageState != null) options.setStorageState(storageState);
        }

        if (TestConfig.RECORD_VIDEO) {
            options.setRecordVideoDir(Paths.get("target/videos/" + label));
            options.setRecordVideoSize(1280, 720);
        }
        return browser.newContext(options);
    }

    /**
     * Admin E2E tests share the one seeded admin account. Reuse its authenticated browser state
     * across isolated contexts so a suite run does not exceed the server's per-phone OTP limits.
     */
    protected final void loginAsAdmin() {
        adminPage.navigate(TestConfig.APP_URL);
        AdminPortalPage portal = new AdminPortalPage(adminPage);
        String cacheKey = adminStorageStateKey();

        if (ADMIN_STORAGE_STATES.containsKey(cacheKey)) {
            try {
                portal.waitForPortal();
                ADMIN_STORAGE_STATES.put(cacheKey, adminContext.storageState());
                return;
            } catch (TimeoutError expiredOrInvalidState) {
                boolean loginScreen = adminPage.locator("button:has-text('System Admin'):visible").count() > 0;
                if (!loginScreen) throw expiredOrInvalidState;
                ADMIN_STORAGE_STATES.remove(cacheKey);
            }
        }

        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone,
                TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        portal.waitForPortal();
        ADMIN_STORAGE_STATES.put(cacheKey, adminContext.storageState());
    }

    private String adminStorageStateKey() {
        return TestConfig.APP_URL + "|" + testAdminPhone;
    }

    private Page createPage(BrowserContext context) {
        Page page = context.newPage();
        page.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);

        page.onConsoleMessage(msg ->
                System.out.println("[BROWSER " + msg.type().toUpperCase() + "] " + redactAuthValues(msg.text())));
        page.onDialog(dialog -> {
            System.out.println("[BROWSER DIALOG] " + dialog.message());
            dialog.dismiss();
        });
        page.onResponse(response -> {
            if (response.status() >= 400) {
                System.out.println("[BROWSER NETWORK ERROR] " + response.status() + " " + response.request().method() + " " + redactAuthValues(response.url()));
            }
        });

        return page;
    }

    private static String redactAuthValues(String message) {
        return message.replaceAll("(?i)(https?://[^?\\s'\"]+)\\?[^\\s'\"]+", "$1?[redacted]")
                .replaceAll("(?i)([?&](?:token|otp)=)[^&#\\s'\"]+", "$1[redacted]");
    }

    private void closeQuietly(BrowserContext ctx) {
        try {
            if (ctx != null) ctx.close();
        } catch (Exception ignored) { }
    }

    /**
     * Returns all active pages so the ScreenshotOnFailure extension can capture them.
     */
    public List<Page> getAllPages() {
        return Arrays.asList(customerPage, restaurantPage, riderPage, adminPage);
    }
}
