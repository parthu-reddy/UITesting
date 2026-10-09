package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

/**
 * Waits for the screen to render an API response before a test reads it.
 *
 * <p>This replaces {@code waitForLoadState(LoadState.NETWORKIDLE)}. Network idle needs 500 ms with no request at all,
 * which never came within 60 s on Dev for some admin screens (2026-10-09, AdminSupportUserReviewTest ×2, and the same
 * timeouts across that class on 2026-09-27). Unrelated requests had nothing to do with the data under test. What a
 * test reads is the DOM, so this waits for the response body, then for the DOM to stop changing.
 */
public final class UiSettle {

    /** The render of a response lands within a frame or two; 300 ms without a DOM change means it has landed. */
    static final int QUIET_MS = 300;
    /** A screen that keeps changing for this long is a defect worth failing on, not something to wait out. */
    static final int MAX_MS = 15_000;

    private static final String DOM_QUIET = """
            ([quietMs, maxMs]) => new Promise(resolve => {
              let quiet;
              const observer = new MutationObserver(() => { clearTimeout(quiet); quiet = setTimeout(() => finish(true), quietMs); });
              const cap = setTimeout(() => finish(false), maxMs);
              function finish(settled) { observer.disconnect(); clearTimeout(quiet); clearTimeout(cap); resolve(settled); }
              observer.observe(document, { subtree: true, childList: true, attributes: true, characterData: true });
              quiet = setTimeout(() => finish(true), quietMs);
            })""";

    private UiSettle() { }

    /** The response has been read in full and the DOM has been quiet for {@link #QUIET_MS}. */
    public static void after(Page page, Response response) {
        response.finished();
        Object settled = page.evaluate(DOM_QUIET, java.util.List.of(QUIET_MS, MAX_MS));
        if (!Boolean.TRUE.equals(settled)) {
            throw new AssertionError("The page kept changing for " + MAX_MS + " ms after " + response.request().method()
                    + " " + response.url().replaceAll("\\?.*", "") + "; a test cannot read a stable screen");
        }
    }
}
