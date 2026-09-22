package com.fooddelivery.e2e.base;

import com.microsoft.playwright.*;
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

    @BeforeAll
    public static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(TestConfig.HEADLESS)
                        .setSlowMo(TestConfig.SLOW_MO)
                        .setArgs(List.of(
                                "--unsafely-treat-insecure-origin-as-secure=" + TestConfig.APP_URL.replaceAll("/$", "")
                        ))
        );
    }

    @AfterAll
    public static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    public void setUpContexts() {
        testCustomerPhone = String.format("8000000%03d", (int)(Math.random() * 500) + 1);
        testRestaurantPhone = String.format("9000000%03d", (int)(Math.random() * 500) + 1);
        testRiderPhone = String.format("7000000%03d", (int)(Math.random() * 500) + 1);
        testAdminPhone = String.format("1000000%03d", (int)(Math.random() * 500) + 1);

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
        closeQuietly(customerContext);
        closeQuietly(restaurantContext);
        closeQuietly(riderContext);
        closeQuietly(adminContext);
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

    private Page createPage(BrowserContext context) {
        Page page = context.newPage();
        page.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);

        page.onConsoleMessage(msg ->
                System.out.println("[BROWSER " + msg.type().toUpperCase() + "] " + msg.text()));
        page.onDialog(dialog -> {
            System.out.println("[BROWSER DIALOG] " + dialog.message());
            dialog.dismiss();
        });
        page.onResponse(response -> {
            if (response.status() >= 400) {
                System.out.println("[BROWSER NETWORK ERROR] " + response.status() + " " + response.request().method() + " " + response.url());
            }
        });

        return page;
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
