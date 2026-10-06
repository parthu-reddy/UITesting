package com.fooddelivery.e2e.tests.features.ads;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A2 measurement (not a gate): latency of the customer listing's own nearby request, before and after A2
 * (A2 adds a server-side ad call capped at 150 ms). Read-only: a seeded customer opens Home through the UI,
 * the exact nearby URL the app requested is replayed from the signed-in page — 5 warm-ups, then
 * {@code -Dmeasure.samples} (default 40) timed requests, sequential, measured with performance.now() in the
 * browser, so the figures include the tunnel round trip (an upper bound on server time).
 * Opt-in: {@code -Dmeasure.nearby=true -Dmeasure.label=before|after}. Writes target/business-platform/a2/.
 */
@Tag("business-platform") @Tag("measurement")
public class NearbyListingLatencyMeasurement extends TestBase {

    @Test void nearbyListingLatency() throws Exception {
        assertThat(System.getProperty("measure.nearby")).as("opt-in measurement").isEqualTo("true");
        String label = System.getProperty("measure.label", "");
        assertThat(label).as("-Dmeasure.label=before|after").isIn("before", "after");
        int samples = Integer.getInteger("measure.samples", 40);

        Response[] nearby = new Response[1];
        customerPage.onResponse(r -> {
            if ("GET".equals(r.request().method()) && "/api/v1/restaurants/nearby".equals(UrlPaths.path(r.url()))) nearby[0] = r;
        });
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.waitForCondition(() -> nearby[0] != null);
        assertThat(nearby[0].status()).isEqualTo(200);
        String url = nearby[0].url();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) customerPage.evaluate("""
            async ({url, samples}) => {
              const token = localStorage.getItem('auth_token');
              if (!token) throw new Error('A signed-in page is required');
              const call = async () => {
                const t0 = performance.now();
                const r = await fetch(url, {headers: {Authorization: 'Bearer ' + token}, credentials: 'same-origin'});
                await r.text();
                return {ms: performance.now() - t0, status: r.status};
              };
              for (let i = 0; i < 5; i++) await call();
              const ms = [], statuses = {};
              for (let i = 0; i < samples; i++) { const c = await call(); ms.push(c.ms); statuses[c.status] = (statuses[c.status] || 0) + 1; }
              ms.sort((a, b) => a - b);
              const at = q => ms[Math.min(ms.length - 1, Math.ceil(q * ms.length) - 1)];
              return {n: ms.length, p50: at(0.5), p95: at(0.95), max: ms[ms.length - 1], statuses};
            }
            """, Map.of("url", url, "samples", samples));

        Map<String, Object> report = new java.util.LinkedHashMap<>(result);
        report.put("label", label);
        report.put("customerPhone", testCustomerPhone);
        report.put("path", UrlPaths.path(url));
        report.put("method", "browser performance.now() through the tunnel; 5 warm-ups; sequential");
        Path out = Path.of("target/business-platform/a2/nearby-latency-" + label + "-" + System.currentTimeMillis() + ".json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, (String) customerPage.evaluate("v => JSON.stringify(v, null, 2)", report) + "\n");
        assertThat(((Map<?, ?>) result.get("statuses")).keySet()).as("every sample answered 200").isEqualTo(java.util.Set.of("200"));
        System.out.println("A2 nearby latency " + label + ": " + report + " -> " + out);
    }
}
