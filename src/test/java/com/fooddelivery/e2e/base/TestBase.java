package com.fooddelivery.e2e.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class TestBase {
    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext customerContext;
    protected BrowserContext restaurantContext;
    protected BrowserContext riderContext;

    protected Page customerPage;
    protected Page restaurantPage;
    protected Page riderPage;

    protected static final String APP_URL = "http://140.245.225.221/";

    @BeforeAll
    public static void setUpClass() {
        playwright = Playwright.create();
        // Headless = true for stability in CI/Remote environments
        browser = playwright.chromium().launch(new com.microsoft.playwright.BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(java.util.Arrays.asList("--unsafely-treat-insecure-origin-as-secure=http://140.245.225.221")));
    }

    @AfterAll
    public static void tearDownClass() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    public void setUpContexts() {
        // Create 3 isolated incognito browser contexts
        customerContext = browser.newContext(new com.microsoft.playwright.Browser.NewContextOptions()
                .setPermissions(java.util.Arrays.asList("geolocation", "notifications"))
                .setGeolocation(12.9808, 77.6467));
        restaurantContext = browser.newContext(new com.microsoft.playwright.Browser.NewContextOptions()
                .setPermissions(java.util.Arrays.asList("geolocation", "notifications"))
                .setGeolocation(12.9808, 77.6467));
        riderContext = browser.newContext(new com.microsoft.playwright.Browser.NewContextOptions()
                .setPermissions(java.util.Arrays.asList("geolocation", "notifications"))
                .setGeolocation(12.9808, 77.6467));

        // Create a new tab/page in each context
        customerPage = customerContext.newPage();
        restaurantPage = restaurantContext.newPage();
        riderPage = riderContext.newPage();
        
        // Log console messages and handle dialogs for each page
        for (Page page : java.util.Arrays.asList(customerPage, restaurantPage, riderPage)) {
            page.onConsoleMessage(msg -> System.out.println("BROWSER CONSOLE: " + msg.type() + " " + msg.text()));
            page.onDialog(dialog -> {
                System.out.println("BROWSER DIALOG: " + dialog.message());
                dialog.dismiss();
            });
        }
        
        customerPage.setDefaultTimeout(60000);
        restaurantPage.setDefaultTimeout(60000);
        riderPage.setDefaultTimeout(60000);
    }

    @AfterEach
    public void tearDownContexts() {
        if (customerContext != null) customerContext.close();
        if (restaurantContext != null) restaurantContext.close();
        if (riderContext != null) riderContext.close();
    }
}
