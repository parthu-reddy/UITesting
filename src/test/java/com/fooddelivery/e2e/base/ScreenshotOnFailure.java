package com.fooddelivery.e2e.base;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 extension that captures a screenshot and page HTML
 * for every active page when a test fails.
 */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    private static final Path SCREENSHOT_DIR = Paths.get("target/screenshots");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) return;
        Object testInstance = context.getTestInstance().orElse(null);
        if (!(testInstance instanceof TestBase)) return;

        TestBase base = (TestBase) testInstance;
        String testName = context.getDisplayName().replaceAll("[^a-zA-Z0-9]", "_");

        try {
            Files.createDirectories(SCREENSHOT_DIR);
        } catch (Exception ignored) { }

        for (Page page : base.getAllPages()) {
            if (page == null || page.isClosed()) continue;

            String label = identifyPage(page, base);
            String prefix = testName + "_" + label;

            try {
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(SCREENSHOT_DIR.resolve(prefix + ".png"))
                        .setFullPage(true));
            } catch (Exception e) {
                System.err.println("Failed to capture screenshot for " + label + ": " + e.getMessage());
            }

            try {
                Path htmlPath = SCREENSHOT_DIR.resolve(prefix + ".html");
                Files.writeString(htmlPath, page.content());
            } catch (Exception e) {
                System.err.println("Failed to capture HTML for " + label + ": " + e.getMessage());
            }
        }
    }

    private String identifyPage(Page page, TestBase base) {
        if (page == base.customerPage) return "customer";
        if (page == base.restaurantPage) return "restaurant";
        if (page == base.riderPage) return "rider";
        if (page == base.adminPage) return "admin";
        return "unknown";
    }
}
