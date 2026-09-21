package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Simulates the {@code <SwipeAction>} gesture used in the delivery UI.
 * <p>
 * The component uses {@code role="slider"} and supports keyboard shortcuts:
 * pressing {@code End} or {@code Enter} confirms the action immediately.
 * This is more reliable than simulating pointer drag in headless mode.
 * </p>
 */
public final class SwipeHelper {

    private SwipeHelper() {}

    /**
     * Confirms a SwipeAction by its aria-label text.
     * Uses keyboard shortcut (End key) which the component explicitly supports,
     * making this approach resilient to viewport size and headless rendering differences.
     *
     * @param page  the Playwright page containing the SwipeAction
     * @param label the aria-label text (or partial match) of the SwipeAction slider
     */
    public static void swipeToConfirm(Page page, String label) {
        Locator slider = page.locator("div[role='slider']")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first();

        slider.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

        // Focus the slider and press End to trigger confirmation
        slider.focus();
        slider.press("End");

        // Wait for the confirming state to appear
        page.waitForTimeout(500);
    }

    /**
     * Confirms a SwipeAction by simulating a full pointer drag across the track.
     * Use this as a fallback if keyboard confirmation doesn't trigger correctly.
     *
     * @param page  the Playwright page
     * @param label the aria-label text of the SwipeAction slider
     */
    public static void swipeToConfirmByDrag(Page page, String label) {
        Locator slider = page.locator("div[role='slider']")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first();

        slider.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

        var box = slider.boundingBox();
        if (box == null) {
            throw new IllegalStateException("SwipeAction slider not visible: " + label);
        }

        double startX = box.x + 20;
        double endX = box.x + box.width * 0.90;
        double midY = box.y + box.height / 2;

        page.mouse().move(startX, midY);
        page.mouse().down();
        // Simulate gradual drag with multiple steps for realism
        for (int step = 1; step <= 20; step++) {
            double x = startX + (endX - startX) * step / 20.0;
            page.mouse().move(x, midY);
        }
        page.mouse().up();

        page.waitForTimeout(500);
    }
}
