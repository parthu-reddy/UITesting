package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.function.BooleanSupplier;

/**
 * Reusable wait utilities for E2E tests.
 * <p>
 * Playwright's built-in auto-waiting handles most cases, but SSE-driven
 * status updates and animations sometimes need explicit polling.
 * </p>
 */
public final class WaitHelpers {

    private WaitHelpers() {}

    /**
     * Waits until a locator containing the expected text becomes visible.
     * Useful for SSE-driven order status updates that may arrive asynchronously.
     *
     * @param page           the Playwright page
     * @param expectedText   the text content to wait for
     * @param timeoutMs      maximum wait time in milliseconds
     */
    public static void waitForText(Page page, String expectedText, int timeoutMs) {
        page.locator("text=" + expectedText).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(timeoutMs));
    }

    /**
     * Waits for an order status update by polling the page for visible text.
     * Reloads the page once midway through if the text hasn't appeared,
     * to recover from stale SSE connections.
     *
     * @param page           the Playwright page
     * @param expectedStatus the expected status text (e.g., "Accepted by Kitchen")
     * @param timeoutMs      maximum wait time in milliseconds
     */
    public static void waitForOrderStatus(Page page, String expectedStatus, int timeoutMs) {
        long start = System.currentTimeMillis();
        boolean reloaded = false;

        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                Locator status = page.locator("text=" + expectedStatus).first();
                if (status.isVisible()) return;
            } catch (Exception ignored) { }

            // Reload once midway to recover from dead SSE
            if (!reloaded && System.currentTimeMillis() - start > timeoutMs / 2) {
                System.out.println("[WAIT] Reloading page to recover SSE for status: " + expectedStatus);
                page.reload();
                page.waitForTimeout(2000);
                reloaded = true;
            }

            page.waitForTimeout(500);
        }

        // Final assertion — let Playwright throw with its standard timeout message
        page.locator("text=" + expectedStatus).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
    }

    /**
     * Polls a condition at regular intervals until it returns true or times out.
     *
     * @param condition   the condition to evaluate
     * @param timeoutMs   maximum wait time
     * @param intervalMs  polling interval
     * @param description human-readable description for error messages
     */
    public static void pollUntil(BooleanSupplier condition, int timeoutMs, int intervalMs, String description) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (condition.getAsBoolean()) return;
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for: " + description, e);
            }
        }
        throw new AssertionError("Timed out after " + timeoutMs + "ms waiting for: " + description);
    }

    /**
     * Waits for a button to become visible and then clicks it.
     * Captures a screenshot on failure for debugging.
     *
     * @param page       the Playwright page
     * @param buttonText the button text to find and click
     * @param timeoutMs  maximum wait time
     */
    public static void clickWhenReady(Page page, String buttonText, int timeoutMs) {
        Locator button = page.locator("button:has-text('" + buttonText + "')").first();
        button.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
        button.click();
    }
}
