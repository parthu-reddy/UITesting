package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Pointer gestures for the UI SwipeAction slider; keyboard confirmation is explicit. */
public final class SwipeHelper {

    private SwipeHelper() {}

    private static Locator slider(Page page, String label) {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.SLIDER,
                new Page.GetByRoleOptions().setName(label));
    }

    public static void swipeToConfirm(Page page, String label) {
        page.bringToFront();
        swipeToConfirmByDrag(page, label);
    }

    /**
     * Confirms a SwipeAction by its aria-label text.
     * Uses keyboard shortcut (End key) which the component explicitly supports,
     * making this approach resilient to viewport size and headless rendering differences.
     *
     * @param page  the Playwright page containing the SwipeAction
     * @param label the aria-label text (or partial match) of the SwipeAction slider
     */
    public static void confirmWithKeyboard(Page page, String label) {
        Locator slider = slider(page, label);

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
     *
     * <p>SwipeAction confirms on release iff the pointer is past 85% of the track, and it reports
     * its progress in {@code aria-valuenow} while the pointer is down. So the outcome is decided
     * before release: a drag observed at 85%+ confirms when released; a drag that fell short (the
     * panel moved under the pointer, a re-render) springs back to 0 without confirming and is
     * retried. There is no sleep: callers wait for the status request the swipe sends.
     *
     * @param page  the Playwright page
     * @param label the aria-label text of the SwipeAction slider
     */
    public static void swipeToConfirmByDrag(Page page, String label) {
        Locator slider = slider(page, label);
        java.util.regex.Pattern pastThreshold = java.util.regex.Pattern.compile("(?:8[5-9]|9[0-9]|100)");
        com.microsoft.playwright.PlaywrightException lastShortfall = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            slider.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
            slider.scrollIntoViewIfNeeded();
            // Locked while a previous confirmation is in flight; it re-arms at 0 when that settles.
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(slider).isEnabled();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(slider)
                    .hasAttribute("aria-valuenow", "0");
            // Unlike raw mouse coordinates, hover waits for animation stability and hit testing.
            slider.hover(new Locator.HoverOptions().setPosition(20, 28));
            var box = slider.boundingBox();
            if (box == null) {
                throw new IllegalStateException("SwipeAction slider not visible: " + label);
            }
            double endX = box.x + box.width * 0.95;
            double midY = box.y + box.height / 2;

            page.mouse().down();
            boolean reached = false;
            try {
                // One continuous movement. Separate move calls each inherit slowMo.
                page.mouse().move(endX, midY, new com.microsoft.playwright.Mouse.MoveOptions().setSteps(30));
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(slider)
                        .hasAttribute("aria-valuenow", pastThreshold,
                                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions()
                                        .setTimeout(3000));
                reached = true;
            } catch (AssertionError | com.microsoft.playwright.PlaywrightException shortfall) {
                lastShortfall = new com.microsoft.playwright.PlaywrightException(
                        "attempt " + attempt + ": drag reached " + slider.getAttribute("aria-valuenow") + "%", shortfall);
                System.out.printf("[SWIPE] %s attempt %d fell short (%s%%); retrying%n",
                        label, attempt, slider.getAttribute("aria-valuenow"));
            } finally {
                page.mouse().up();
            }
            if (reached) {
                System.out.printf("[SWIPE] %s confirmed on attempt %d%n", label, attempt);
                return;
            }
        }
        throw new IllegalStateException("SwipeAction '" + label + "' never reached the confirm threshold in "
                + MAX_ATTEMPTS + " drags", lastShortfall);
    }

    private static final int MAX_ATTEMPTS = 3;
}
