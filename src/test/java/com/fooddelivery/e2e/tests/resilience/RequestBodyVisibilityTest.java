package com.fooddelivery.e2e.tests.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Why request.postData() was null for the app's own POSTs (2026-10-08: orders, rider status).
 *
 * FoodDeliveryAppUI's authFetch sends every request as {@code fetch(new Request(request.clone(), {...}))}. A
 * cloned Request carries its body as a stream, and Chromium reports no post data for a streamed upload, so
 * Playwright's postData() is null unless a route intercepts the request. This pins that behaviour: tests that
 * read an un-routed request body must register a pass-through route first.
 *
 * No Dev writes: unauthenticated POSTs to a path that does not exist (the gateway refuses them).
 */
@Tag("feature-shell")
public class RequestBodyVisibilityTest extends TestBase {
    private static final String PROBE_PATH = "/api/v1/e2e-request-body-probe";

    @Test
    @DisplayName("HARNESS: an authFetch-shaped POST hides its body from postData() unless a route intercepts it")
    void clonedRequestBodyIsVisibleOnlyThroughARoute() {
        customerPage.navigate(TestConfig.APP_URL);
        Map<String, String> bodies = new ConcurrentHashMap<>();
        customerPage.onRequest(request -> {
            if (request.url().endsWith(PROBE_PATH)) bodies.put(request.headerValue("x-probe"), String.valueOf(request.postData()));
        });

        send("plain", "init");
        send("request", "request");
        send("cloned", "cloned");
        customerPage.route("**" + PROBE_PATH, route -> route.resume());
        send("cloned-routed", "cloned");

        System.out.println("[POSTDATA PROBE] " + new java.util.TreeMap<>(bodies));
        assertThat(bodies.get("plain")).contains("\"probe\":\"plain\"");
        assertThat(bodies.get("request")).contains("\"probe\":\"request\"");
        assertThat(bodies.get("cloned")).isEqualTo("null");
        assertThat(bodies.get("cloned-routed")).contains("\"probe\":\"cloned-routed\"");
    }

    /** shape: "init" = fetch(url, init); "request" = fetch(new Request(url, init)); "cloned" = authFetch's
     *  fetch(new Request(request.clone(), {...})) (FoodDeliveryAppUI/src/lib/authFetch.ts). */
    private void send(String probe, String shape) {
        customerPage.evaluate("async ([path, probe, shape]) => {\n"
                + "  const init = { method: 'POST', body: JSON.stringify({ probe }),\n"
                + "    headers: { 'Content-Type': 'application/json', 'x-probe': probe } };\n"
                + "  const base = new Request(new URL(path, window.location.origin), init);\n"
                + "  const sent = shape === 'init' ? fetch(new URL(path, window.location.origin), init)\n"
                + "    : fetch(shape === 'cloned' ? new Request(base.clone(), { headers: base.headers }) : base);\n"
                + "  await sent"
                + "    .catch(() => undefined);\n"
                + "}", java.util.List.of(PROBE_PATH, probe, shape));
    }
}
