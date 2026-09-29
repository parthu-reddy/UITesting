package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Read-only rider history checks used by the cross-role review journey. */
@Tag("review")
public class RiderReviewHistoryApiTest extends TestBase {

    @Test
    @DisplayName("REVIEW-05: Rider can read a completed order from history while off duty")
    void riderCanReadCompletedHistoryWhileOffline() {
        String orderId = System.getProperty("review.order.id");
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Set -Dreview.order.id to an existing delivered order");
        }

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        assertThat(new DeliveryOnlineTogglePage(riderPage).isOffline())
                .as("this read-only check must leave the rider's existing duty state untouched")
                .isTrue();

        String token = (String) riderPage.evaluate("() => localStorage.getItem('auth_token')");
        assertThat(token).isNotBlank();

        String riderTimeZone = (String) riderPage.evaluate(
                "() => Intl.DateTimeFormat().resolvedOptions().timeZone");
        ZoneId riderZone = ZoneId.of(riderTimeZone);
        LocalDate riderDate = LocalDate.now(riderZone);
        String from = URLEncoder.encode(riderDate.atStartOfDay(riderZone).toInstant().toString(),
                StandardCharsets.UTF_8);
        String to = URLEncoder.encode(riderDate.plusDays(1).atStartOfDay(riderZone).toInstant().toString(),
                StandardCharsets.UTF_8);
        String historyUrl = "/api/v1/delivery/orders/history?from=" + from + "&to=" + to;

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) riderPage.evaluate(
                """
                async ({ url, token }) => {
                    const response = await fetch(url, {
                      headers: { Authorization: `Bearer ${token}` }
                    });
                    return { status: response.status, body: await response.text() };
                }
                """,
                Map.of("url", historyUrl, "token", token));

        assertThat(((Number) response.get("status")).intValue())
                .as("delivery history API response")
                .isBetween(200, 299);
        assertThat((String) response.get("body"))
                .as("the order assigned to this rider appears in the rider's local-day history")
                .contains(orderId);
    }
}
