package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.fooddelivery.e2e.base.TestConfig;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Geolocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Opt-in visual checks at existing lifecycle boundaries, before either OTP is entered. */
public final class RiderVisualAudit {
    private RiderVisualAudit() {}

    public static void capture(Page page, String orderId, String stage) {
        capture(page, orderId, stage, TestConfig.GEO_LAT, TestConfig.GEO_LNG);
    }

    /** {@code lat}/{@code lng} is the rider's current emulated position (moved by LiveCustomerMap). */
    public static void capture(Page page, String orderId, String stage, double lat, double lng) {
        String directory = System.getProperty("visual.audit.dir", "").trim();
        if (directory.isEmpty()) return;
        // Playwright's emulated GPS is one static fix stamped when it is set, and it never yields
        // another. The map accepts a cached fix up to 60s old, then waits for a fresh one that never
        // comes (2026-10-07 probe: a 70s-old fix timed out with code 3; the same fix re-applied
        // placed the courier at once). A real device keeps producing fixes, so give the page a
        // current one and remount the map with it; the reload restores the server-backed job.
        // The phone size comes first: a map built at desktop width and shrunk while still loading
        // left every pin outside the 333px map (2026-10-07, f01c1e92). A phone builds it at size.
        page.setViewportSize(390, 844);
        page.context().setGeolocation(new Geolocation(lat, lng));
        page.reload();
        assertThat(page.getByText("#" + orderId, new Page.GetByTextOptions().setExact(true))).isVisible();
        Locator main = page.getByRole(AriaRole.MAIN,
                new Page.GetByRoleOptions().setName("Delivery").setExact(true));
        Locator shell = main.locator("xpath=../..");
        org.assertj.core.api.Assertions.assertThat(shell.evaluate(
                "element => getComputedStyle(element).backgroundColor"))
                .as("active rider canvas at %s", stage).isEqualTo("rgb(18, 22, 28)");
        assertThat(page.getByText("#" + orderId, new Page.GetByTextOptions().setExact(true))).isVisible();
        Locator code = page.getByPlaceholder(stage.equals("assigned")
                ? "Enter 6-digit pickup OTP" : "Ask customer for 6-digit OTP");
        assertThat(code).hasValue("");

        Locator calls = main.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Call ")));
        assertThat(calls).hasCount(2);
        for (int i = 0; i < 2; i++) {
            assertThat(calls.nth(i)).isVisible();
            var bounds = calls.nth(i).boundingBox();
            org.assertj.core.api.Assertions.assertThat(bounds.width).isGreaterThanOrEqualTo(48);
            org.assertj.core.api.Assertions.assertThat(bounds.height).isGreaterThanOrEqualTo(48);
        }
        Locator map = main.getByRole(AriaRole.REGION,
                new Locator.GetByRoleOptions().setName("Live order tracking").setExact(true));
        assertThat(map).isVisible();
        // The region mounts before asynchronous location/pin placement. A blank first frame
        // is not the completed map; wait for the real known points, without requesting SSE.
        try {
            assertThat(map.getByRole(AriaRole.IMG,
                    new Locator.GetByRoleOptions().setName("Courier location").setExact(true))).isVisible();
        } catch (AssertionError missing) {
            recordMissingPins(page, map, directory, "rider-" + stage + "-" + orderId);
            throw missing;
        }
        for (String destination : new String[]{"Customer", "Restaurant"}) {
            assertThat(map.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                    .setName("Open directions to " + destination).setExact(true))).isVisible();
        }
        // isVisible ignores clipping. Before MapLibre's first render the pins sit outside the map
        // box and the capture shows an empty map (2026-10-07, d3e0ebed). Wait until the courier's
        // centre and part of each directions popup are drawn inside the visible map.
        page.waitForCondition(() -> (Boolean) map.evaluate("region => {"
                + "const r = region.getBoundingClientRect();"
                + "const box = el => el && el.getBoundingClientRect();"
                + "const centred = b => !!b && b.width > 0 && b.left + b.width / 2 >= r.left"
                + "  && b.left + b.width / 2 <= r.right && b.top + b.height / 2 >= r.top && b.top + b.height / 2 <= r.bottom;"
                + "const overlaps = b => !!b && b.width > 0 && b.right > r.left && b.left < r.right"
                + "  && b.bottom > r.top && b.top < r.bottom;"
                + "return centred(box(region.querySelector('[aria-label=\"Courier location\"]')))"
                + "  && ['Customer', 'Restaurant'].every(d =>"
                + "    overlaps(box(region.querySelector('[aria-label=\"Open directions to ' + d + '\"]')))); }"),
                new Page.WaitForConditionOptions().setTimeout(15000));
        Locator swipe = main.getByRole(AriaRole.SLIDER,
                new Locator.GetByRoleOptions().setName(Pattern.compile(stage.equals("assigned")
                        ? "^Slide to confirm pickup$" : "^Slide to deliver")));
        assertThat(swipe).isVisible();
        org.assertj.core.api.Assertions.assertThat(swipe.boundingBox().height).isGreaterThanOrEqualTo(48);
        org.assertj.core.api.Assertions.assertThat((Boolean) page.evaluate(
                "() => document.documentElement.scrollWidth <= innerWidth")).isTrue();

        try {
            Path evidence = Path.of(directory);
            Files.createDirectories(evidence);
            String name = "rider-" + stage + "-" + orderId;
            map.scrollIntoViewIfNeeded();
            page.screenshot(new Page.ScreenshotOptions().setPath(evidence.resolve(name + "-map.png")));
            swipe.scrollIntoViewIfNeeded();
            page.screenshot(new Page.ScreenshotOptions().setPath(evidence.resolve(name + "-contacts-swipe.png")));
            org.assertj.core.api.Assertions.assertThat((Boolean) swipe.evaluate("element => {"
                    + "const r = element.getBoundingClientRect();"
                    + "return [0.1, 0.5, 0.9].every(f => element.contains(document.elementFromPoint("
                    + "r.left + r.width * f, r.top + r.height / 2))); }"))
                    .as("pickup/delivery swipe must remain uncovered by floating controls").isTrue();
            Locator chat = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                    .setName(Pattern.compile("^Open chat for order " + orderId.substring(0, 6))));
            assertThat(chat).isVisible();
            org.assertj.core.api.Assertions.assertThat((Boolean) chat.evaluate("element => !!element.closest('header')"))
                    .as("rider chat belongs to the persistent header").isTrue();
            org.assertj.core.api.Assertions.assertThat(chat.boundingBox().height).isGreaterThanOrEqualTo(48);
            org.assertj.core.api.Assertions.assertThat(chat.boundingBox().width).isGreaterThanOrEqualTo(48);
            if (stage.equals("assigned")) {
                org.assertj.core.api.Assertions.assertThat((Boolean) code.evaluate("element => {"
                        + "const s = getComputedStyle(element, '::placeholder');"
                        + "const c = document.createElement('canvas').getContext('2d');"
                        + "c.font = s.fontSize + ' ' + s.fontFamily;"
                        + "const own = getComputedStyle(element);"
                        + "return c.measureText(element.placeholder).width <= element.clientWidth"
                        + " - parseFloat(own.paddingLeft) - parseFloat(own.paddingRight); }"))
                        .as("the complete pickup hint fits inside the mobile input").isTrue();
                org.assertj.core.api.Assertions.assertThat(code.evaluate(
                        "element => getComputedStyle(element, '::placeholder').letterSpacing"))
                        .isIn("normal", "0px");
            }
            // Only rendered text: input OTPs were proved empty, no response bodies or auth state.
            Files.writeString(evidence.resolve(name + ".txt"), "orderId=" + orderId + "\nstage=" + stage
                    + "\nviewport=390x844\ncallTargets=2\nminTargetPx=48\n" + main.innerText());
        } catch (IOException failure) {
            throw new AssertionError("Cannot retain active rider visual evidence", failure);
        }
    }

    /**
     * Pins are placed only after both the restaurant lookup and the rider's geolocation settle.
     * Record which one was outstanding, so a missing pin is diagnosed from the run, not guessed.
     * The probe repeats the map's own getCurrentPosition options; it records only timing/codes.
     */
    private static void recordMissingPins(Page page, Locator map, String directory, String name) {
        try {
            Object state = page.evaluate("async () => {"
                    + "const lookups = performance.getEntriesByType('resource')"
                    + "  .filter(e => /\\/api\\/v1\\/restaurants\\/[^/?]+$/.test(new URL(e.name).pathname))"
                    + "  .map(e => ({ startMs: Math.round(e.startTime), endMs: Math.round(e.responseEnd),"
                    + "               status: e.responseStatus ?? null }));"
                    + "let permission = 'unsupported';"
                    + "try { permission = (await navigator.permissions.query({ name: 'geolocation' })).state; } catch (e) {}"
                    + "const started = performance.now();"
                    + "const probe = await new Promise(done => navigator.geolocation.getCurrentPosition("
                    + "  () => done({ outcome: 'position' }), error => done({ outcome: 'error', code: error.code }),"
                    + "  { enableHighAccuracy: false, timeout: 15000, maximumAge: 60000 }));"
                    + "probe.elapsedMs = Math.round(performance.now() - started);"
                    + "return { pageAgeMs: Math.round(started), visibility: document.visibilityState,"
                    + "  focused: document.hasFocus(), permission, restaurantLookups: lookups, geolocationProbe: probe };"
                    + "}");
            Object markers = map.evaluate("region => [...region.querySelectorAll('.maplibregl-marker')]"
                    + ".map(m => m.getAttribute('aria-label') || 'unlabelled')");
            Path evidence = Path.of(directory);
            Files.createDirectories(evidence);
            Files.writeString(evidence.resolve(name + "-missing-pins.txt"),
                    "page=" + state + "\nmarkersAfterProbe=" + markers + "\n");
        } catch (RuntimeException | IOException diagnosticFailure) {
            System.out.println("[VISUAL] missing-pin diagnostics unavailable: " + diagnosticFailure.getMessage());
        }
    }
}
