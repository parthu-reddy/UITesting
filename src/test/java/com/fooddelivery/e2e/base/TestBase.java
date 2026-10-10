package com.fooddelivery.e2e.base;

import com.fooddelivery.e2e.pages.common.Portal;
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

/**
 * Base class for all E2E tests.
 * <p>
 * Provides up to 4 isolated browser contexts (customer, restaurant, rider, admin)
 * with geolocation, notifications, video recording, and console logging.
 * </p>
 */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class TestBase {


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
    /** The admin page holds this class's shared session: signing it out per method would end it for the rest. */
    private boolean adminUsesClassSession;

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
    public static void tearDownClass(org.junit.jupiter.api.TestInfo info) {
        com.fooddelivery.e2e.util.ClassAdminSession.signOut(info.getTestClass().orElse(null), browser);
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    public void setUpContexts() {
        testCustomerPhone = TestConfig.customerPhone();
        testRestaurantPhone = TestConfig.restaurantPhone();
        testRiderPhone = TestConfig.riderPhone();
        testAdminPhone = TestConfig.adminPhone();
        adminUsesClassSession = false;

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
            signOutAndClose("customer", customerPage, customerContext);
            signOutAndClose("restaurant", restaurantPage, restaurantContext);
            signOutAndClose("rider", riderPage, riderContext);
            if (!adminUsesClassSession) signOutQuietly("admin", adminPage);
            closeQuietly(adminContext);
        }
    }

    /**
     * Signs one page out and closes its context at once. Signing every page out before closing any left the other
     * pages running on revoked sessions, so their background calls (e.g. the storefront's debounced quote) drew
     * 401s from the gateway that read like product failures (checkpoint144).
     */
    private void signOutAndClose(String label, Page page, BrowserContext context) {
        signOutQuietly(label, page);
        closeQuietly(context);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private BrowserContext createContext(String label) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setPermissions(Arrays.asList("geolocation", "notifications"))
                .setGeolocation(TestConfig.GEO_LAT, TestConfig.GEO_LNG);


        if (TestConfig.RECORD_VIDEO) {
            options.setRecordVideoDir(Paths.get("target/videos/" + label));
            options.setRecordVideoSize(1280, 720);
        }
        return browser.newContext(options);
    }

    /**
     * Signs the admin in through the ordinary UI and administrator verification once per test class; later methods
     * reuse that session in their own fresh context (util/ClassAdminSession). A reused session that no longer opens
     * the portal is dropped and replaced by a real sign-in.
     */
    protected final void loginAsAdmin() {
        Class<?> testClass = getClass();
        if (com.fooddelivery.e2e.util.ClassAdminSession.has(testClass)) {
            com.fooddelivery.e2e.util.ClassAdminSession.inject(testClass, adminContext);
            adminPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + Portal.ADMIN.path);
            try {
                new AdminPortalPage(adminPage).waitForPortal();
                adminUsesClassSession = true;
                return;
            } catch (RuntimeException | AssertionError e) {
                System.out.println("[E2E SETUP] class admin session did not open the portal; signing in again");
                com.fooddelivery.e2e.util.ClassAdminSession.forget(testClass);
            }
        }
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL)
                .openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
        com.fooddelivery.e2e.util.ClassAdminSession.capture(testClass, adminContext, adminPage);
        adminUsesClassSession = true;
    }

    private Page createPage(BrowserContext context) {
        Page page = context.newPage();
        page.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);

        page.onConsoleMessage(msg ->
                System.out.println("[BROWSER " + msg.type().toUpperCase() + "] " + redactAuthValues(msg.text())));
        page.onDialog(dialog -> {
            System.out.println("[BROWSER DIALOG] " + redactAuthValues(dialog.message()));
            dialog.dismiss();
        });
        page.onResponse(response -> {
            if (response.status() >= 400) {
                System.out.println("[BROWSER NETWORK ERROR] " + response.status() + " " + response.request().method() + " " + redactAuthValues(response.url()));
            }
            // Every API write, so a run proves what it changed on Dev (P0-2 impact verdicts come from source).
            // A route.fulfill response has no server address: "fixture" never reached Dev.
            String method = response.request().method();
            if (response.url().contains("/api/v1/") && !method.equals("GET") && !method.equals("HEAD") && !method.equals("OPTIONS")) {
                System.out.println("[E2E WRITE] " + method + " " + response.status() + " "
                        + (response.serverAddr() == null ? "fixture" : "dev") + " " + redactAuthValues(response.url()));
            }
        });

        return page;
    }

    private static String redactAuthValues(String message) {
        return message.replaceAll("(?i)(https?://[^?\\s'\"]+)\\?[^\\s'\"]+", "$1?[redacted]")
                .replaceAll("(?i)([?&](?:token|otp)=)[^&#\\s'\"]+", "$1[redacted]");
    }

    /** Teardown must still close every context when a sign-out fails; the statuses are printed, never asserted. */
    protected final void signOutQuietly(String label, Page page) {
        try {
            var statuses = com.fooddelivery.e2e.util.SessionSignOut.signOut(page);
            if (!statuses.isEmpty()) System.out.println("[E2E TEARDOWN] " + label + " signed out " + statuses);
        } catch (Exception e) {
            System.out.println("[E2E TEARDOWN] " + label + " sign-out failed: " + e.getMessage());
        }
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
