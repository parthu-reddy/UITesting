package com.fooddelivery.e2e.tests.features.ads;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminAdCreativesPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A3 measurement (not a gate): p95 of the admin creative queue (budget 300 ms). Read-only: the admin opens
 * Ad creatives through the UI, and the exact queue request the page made is replayed from the signed-in page —
 * 5 warm-ups, then {@code -Dmeasure.samples} (default 40) sequential requests timed with performance.now() in the
 * browser, so the figures include the tunnel round trip (an upper bound on server time).
 * Opt-in: {@code -Dmeasure.creatives=true}. Writes target/business-platform/a3/.
 */
@Tag("measurement")
@Tag("feature-ads")
public class AdCreativeQueueLatencyMeasurement extends TestBase {

    private static final String QUEUE = "/api/v1/internal/admin/ad-creatives";

    @Test void adCreativeQueueLatency() throws Exception {
        assertThat(System.getProperty("measure.creatives")).as("opt-in measurement").isEqualTo("true");
        int samples = Integer.getInteger("measure.samples", 40);

        Response[] queue = new Response[1];
        adminPage.onResponse(r -> {
            if ("GET".equals(r.request().method()) && QUEUE.equals(UrlPaths.path(r.url()))) queue[0] = r;
        });
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
        new AdminAdCreativesPage(adminPage).open();
        adminPage.waitForCondition(() -> queue[0] != null);
        assertThat(queue[0].status()).isEqualTo(200);
        String url = queue[0].url();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) adminPage.evaluate("""
            async ({url, samples}) => {
              // The admin portal's own step-up session (tokenStore.ts keeps it in sessionStorage), as the page sends it.
              const token = sessionStorage.getItem('admin_auth_token');
              if (!token) throw new Error('A verified admin session is required');
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
        report.put("path", UrlPaths.path(url));
        report.put("method", "browser performance.now() through the tunnel; 5 warm-ups; sequential");
        Path out = Path.of("target/business-platform/a3/creative-queue-latency-" + System.currentTimeMillis() + ".json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, (String) adminPage.evaluate("v => JSON.stringify(v, null, 2)", report) + "\n");
        assertThat(((Map<?, ?>) result.get("statuses")).keySet()).as("every sample answered 200").isEqualTo(java.util.Set.of("200"));
        System.out.println("A3 creative queue latency: " + report + " -> " + out);
    }
}
