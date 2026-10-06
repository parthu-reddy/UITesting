package com.fooddelivery.e2e.tests.features.wallet;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.GatewayApi;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * W2 gate (runs after the W2 release and a fresh Dev seed): an OWNER tops up the organisation's business
 * wallet directly, idempotently, in rupees; a declined payment ends FAILED and credits nothing; the ledger
 * books the top-up as cash from the gateway; a MANAGER cannot top up. Real Dev sign-in through the UI;
 * calls go through the gateway from the signed-in page. Fixtures (the top-ups, the disposable member) are
 * retained; nothing is cleaned up.
 *
 * <p>The ₹10.13 top-up is declined by the Dev payment mock on purpose (MockTopupDeclineSeam, never in prod).
 */
@Tag("business-platform") @Tag("bp-w2")
public class BusinessWalletTopUpFlowTest extends TestBase {

    private static final String DECLINED_BY_DEV_SEAM = "10.13";

    @Test void businessWalletTopUpFlow() throws Exception {
        assertThat(System.getProperty("bp.w2.preflight")).as("Run only after the W2 release and fresh seeds").isEqualTo("true");
        String phone = System.getProperty("bp.w2.phone");
        assertThat(phone).matches("9999[0-9]{6}");
        Path retained = Path.of("target/business-platform/w2/run-" + phone + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("phone", phone); manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false);

        login(restaurantPage, "9000000001");
        String org = onlyOrganisation(restaurantPage);
        String wallet = "/api/v1/money/business/" + org;
        manifest.put("organisationId", org); saveManifest(retained, manifest);
        BigDecimal before = balance(restaurantPage, wallet);
        manifest.put("balanceBefore", before.toPlainString());

        // ₹100 by CARD with key K: SUCCESS within 10 s, balance + 100.00, newest statement line is this top-up.
        String key = UUID.randomUUID().toString();
        var first = topUp(restaurantPage, wallet, key, "100");
        assertThat(first.status()).as("top-up response %s", first.body()).isEqualTo(200);
        String topupId = (String) first.data().get("topupId");
        assertThat(topupId).isNotBlank();
        manifest.put("topupId", topupId); saveManifest(retained, manifest);
        Map<?, ?> settled = waitForSettlement(restaurantPage, wallet, topupId);
        assertThat(settled.get("status")).isEqualTo("SUCCESS");
        assertThat(balance(restaurantPage, wallet)).isEqualByComparingTo(before.add(new BigDecimal("100.00")));
        Map<?, ?> newest = content(GatewayApi.get(restaurantPage, wallet + "/transactions?page=0&size=1")).get(0);
        assertThat(new BigDecimal(newest.get("amount").toString())).isEqualByComparingTo("100.00");
        assertThat(newest.get("transactionType")).isEqualTo("CREDIT");
        assertThat(newest.get("referenceId")).isEqualTo(topupId);

        // The same key again: the same top-up, no second payment, balance unchanged.
        var again = topUp(restaurantPage, wallet, key, "100");
        assertThat(again.status()).as("repeat response %s", again.body()).isEqualTo(200);
        assertThat(again.data().get("topupId")).isEqualTo(topupId);
        assertThat(balance(restaurantPage, wallet)).isEqualByComparingTo(before.add(new BigDecimal("100.00")));
        // The same key with another amount: a conflict.
        assertThat(topUp(restaurantPage, wallet, key, "200").status()).isEqualTo(409);

        // Declined by the Dev seam: FAILED with a reason, nothing credited.
        var declined = topUp(restaurantPage, wallet, UUID.randomUUID().toString(), DECLINED_BY_DEV_SEAM);
        assertThat(declined.status()).as("declined top-up response %s", declined.body()).isEqualTo(200);
        String declinedId = (String) declined.data().get("topupId");
        manifest.put("declinedTopupId", declinedId); saveManifest(retained, manifest);
        Map<?, ?> failed = waitForSettlement(restaurantPage, wallet, declinedId);
        assertThat(failed.get("status")).isEqualTo("FAILED");
        assertThat((String) failed.get("failureReason")).isNotBlank();
        assertThat(balance(restaurantPage, wallet)).isEqualByComparingTo(before.add(new BigDecimal("100.00")));

        // Outside the limits: 400 before any payment.
        assertThat(topUp(restaurantPage, wallet, UUID.randomUUID().toString(), "5").status()).isEqualTo(400);
        assertThat(topUp(restaurantPage, wallet, UUID.randomUUID().toString(), "100001").status()).isEqualTo(400);

        // The ledger: the top-up is cash from the gateway into the organisation's prepaid account, exactly
        // once; the declined one is not booked. Read through the owner's own statement (WALLET_VIEW).
        List<Map<?, ?>> ledger = statementLines(restaurantPage, org);
        var booked = ledger.stream().filter(l -> topupId.equals(l.get("referenceId"))).toList();
        assertThat(booked).hasSize(1);
        assertThat(booked.get(0).get("category")).isEqualTo("BUSINESS_WALLET_TOPUP");
        assertThat(booked.get(0).get("direction")).isEqualTo("CREDIT");
        assertThat(new BigDecimal(booked.get(0).get("amount").toString())).isEqualByComparingTo("100.00");
        assertThat(ledger.stream().noneMatch(l -> declinedId.equals(l.get("referenceId")))).isTrue();

        // A MANAGER can see the wallet but not top it up.
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(phone, "E2E W2 Manager", "w2_" + phone + "@test.com");
        String orgPath = "/api/v1/organisations/" + org;
        var invited = GatewayApi.post(restaurantPage, orgPath + "/invitations", Map.of("phoneNumber", phone, "role", "MANAGER"));
        assertThat(invited.status()).isEqualTo(201);
        assertThat(GatewayApi.post(customerPage, "/api/v1/organisation-invitations/" + invited.data().get("id") + "/accept", null).status()).isEqualTo(200);
        manifest.put("managerUserId", memberId(restaurantPage, orgPath, phone)); saveManifest(retained, manifest);
        reloadWithRenewedSession(customerPage);
        assertThat(GatewayApi.get(customerPage, wallet).status()).isEqualTo(200);
        assertThat(topUp(customerPage, wallet, UUID.randomUUID().toString(), "100").status()).isEqualTo(403);

        manifest.put("balanceAfter", balance(restaurantPage, wallet).toPlainString());
        manifest.put("completed", true); saveManifest(retained, manifest);
        System.out.println("W2 business wallet top-up flow verified; retained fixture " + retained);
    }

    private static GatewayApi.Response topUp(Page page, String wallet, String key, String amount) {
        // GatewayApi sends no custom headers, so the Idempotency-Key request is made here, the same way:
        // same origin, the page's own session token, never returned or logged.
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("path", wallet + "/topups"); args.put("key", key); args.put("amount", Double.parseDouble(amount)); // a JSON number in rupees
        var result = (Map<?, ?>) page.evaluate("""
            async ({path, key, amount}) => {
              const token = localStorage.getItem('auth_token');
              if (!token) throw new Error('A signed-in page is required');
              const response = await fetch(path, {method: 'POST', credentials: 'same-origin', redirect: 'error',
                signal: AbortSignal.timeout(15000),
                headers: {Authorization: 'Bearer ' + token, 'Content-Type': 'application/json', 'Idempotency-Key': key},
                body: JSON.stringify({amount, paymentMethod: 'CARD'})});
              const text = await response.text();
              return {status: response.status, body: text ? JSON.parse(text) : null};
            }
            """, args);
        return new GatewayApi.Response(((Number) result.get("status")).intValue(), result.get("body"));
    }

    private static Map<?, ?> waitForSettlement(Page page, String wallet, String topupId) {
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (true) {
            var status = GatewayApi.get(page, wallet + "/topups/" + topupId);
            assertThat(status.status()).isEqualTo(200);
            if (!"PENDING".equals(status.data().get("status")) || System.nanoTime() > deadline) {
                return status.data();
            }
            page.waitForTimeout(500);
        }
    }

    private static BigDecimal balance(Page page, String wallet) {
        var response = GatewayApi.get(page, wallet);
        assertThat(response.status()).isEqualTo(200);
        return new BigDecimal(response.data().get("balance").toString());
    }

    private static List<Map<?, ?>> statementLines(Page page, String org) {
        var response = GatewayApi.get(page, "/api/v1/ledger/statements/BUSINESS_PREPAID/" + org + "?page=0&size=50");
        assertThat(response.status()).isEqualTo(200);
        var lines = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.object().get("content")) lines.add((Map<?, ?>) row);
        return lines;
    }

    /**
     * Accepting an invitation bumps the member's entitlements version, so the gateway answers their old
     * token with 401 ENTITLEMENTS_CHANGED. Reload and let the app's own transport renew it; the landing
     * page's retried profile request returning 200 means the renewed token is the stored one.
     */
    private void reloadWithRenewedSession(Page page) {
        var renewed = new com.microsoft.playwright.Response[1];
        page.waitForResponse(r -> r.status() == 200 && UrlPaths.path(r.url()).equals("/api/v1/users/profile"),
                () -> renewed[0] = page.waitForResponse(r -> r.request().method().equals("POST")
                        && UrlPaths.path(r.url()).equals("/api/v1/auth/session/refresh"), page::reload));
        assertThat(renewed[0].status()).isEqualTo(200);
    }

    private void login(Page page, String phone) {
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).login(phone).openPortal(Portal.RESTAURANT);
    }

    private String onlyOrganisation(Page page) {
        var rows = content(GatewayApi.get(page, "/api/v1/organisations"));
        assertThat(rows).hasSize(1);
        return (String) rows.get(0).get("id");
    }

    private String memberId(Page page, String orgPath, String phone) {
        return (String) content(GatewayApi.get(page, orgPath + "/members")).stream()
                .filter(m -> phone.equals(m.get("phoneNumber"))).findFirst().orElseThrow().get("userId");
    }

    private List<Map<?, ?>> content(GatewayApi.Response response) {
        assertThat(response.status()).isEqualTo(200);
        var result = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.data().get("content")) result.add((Map<?, ?>) row);
        return result;
    }

    private void saveManifest(Path path, Map<String, Object> manifest) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, (String) restaurantPage.evaluate("value => JSON.stringify(value,null,2)", manifest) + "\n");
    }
}
