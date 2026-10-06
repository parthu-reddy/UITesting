package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminAdCreativesPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.GatewayApi;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A3 gate: a campaign goes live end to end. The owner of Brand 1 (9000000001) creates a campaign promoting an
 * outlet that is in a seeded customer's nearby listing; activation is refused until an admin approves its
 * OUTLET_BANNER creative (rejected first, in the admin UI, then resubmitted and approved); the business wallet
 * covers a day (topped up through the W2 API if needed); the campaign goes ACTIVE at once; the customer's own
 * listing shows that outlet first with the Sponsored badge; its impression bills the business wallet, books a
 * balanced ledger line and counts in performance; pausing takes it out of the listing.
 * Real sign-ins, gateway calls from the signed-in pages, no routed responses. The campaign is left PAUSED.
 */
@Tag("business-platform") @Tag("bp-a3")
public class CampaignModerationActivationFlowTest extends TestBase {

    private static final String NEARBY = "/api/v1/restaurants/nearby";

    @Test void campaignGoesLiveAndBills() throws Exception {
        assertThat(System.getProperty("bp.a3.preflight")).as("Run only after the A3 release and fresh seeds").isEqualTo("true");
        Path retained = Path.of("target/business-platform/a3/campaign-" + System.currentTimeMillis() + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false); manifest.put("customerPhone", testCustomerPhone);

        // The customer's own nearby listing, as the app requests it.
        Response[] nearby = new Response[1];
        customerPage.onResponse(r -> {
            if ("GET".equals(r.request().method()) && NEARBY.equals(UrlPaths.path(r.url()))) nearby[0] = r;
        });
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.waitForCondition(() -> nearby[0] != null);
        String listingUrl = nearby[0].url();
        List<Map<String, Object>> before = listing(listingUrl);

        // An outlet of Brand 1 in that listing, as its owner sees it.
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login("9000000001").openPortal(Portal.RESTAURANT);
        String org = onlyOrganisation(restaurantPage);
        Map<?, ?> outlet = outlets(restaurantPage).stream()
                .filter(o -> before.stream().anyMatch(r -> o.get("id").equals(r.get("id"))))
                .findFirst().orElseThrow(() -> new AssertionError("No Brand 1 outlet is in customer " + testCustomerPhone
                        + "'s nearby listing; pass -Dcustomer.phone for a seeded customer whose Home is near Brand 1"));
        String outletId = (String) outlet.get("id");
        String account = "/api/v1/advertisers/" + org;
        assertThat(GatewayApi.put(restaurantPage, account, Map.of("displayName", "Brand 1 Ads", "timeZone", "Asia/Kolkata")).status()).isIn(200, 201);
        manifest.put("organisationId", org); manifest.put("outletId", outletId); save(retained, manifest);

        // DRAFT, refused without an approved creative.
        String today = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
        String name = "E2E A3 " + System.currentTimeMillis();
        var created = GatewayApi.post(restaurantPage, account + "/campaigns", Map.of("advertiserId", org, "name", name,
                "dailyBudget", 20, "lifetimeBudget", 100, "maxBid", 0.50, "startDate", today, "promotedOutletId", outletId));
        assertThat(created.status()).isEqualTo(201);
        assertThat(created.data().get("status")).isEqualTo("DRAFT");
        String campaignId = (String) created.data().get("id");
        String campaign = account + "/campaigns/" + campaignId;
        manifest.put("campaignId", campaignId); save(retained, manifest);
        var refused = GatewayApi.post(restaurantPage, campaign + "/activate", null);
        assertThat(refused.status()).isEqualTo(400);
        assertThat(String.valueOf(refused.object().get("message"))).contains("no approved creative");

        // An OUTLET_BANNER creative: PENDING, the outlet's own banner.
        var creative = GatewayApi.post(restaurantPage, campaign + "/creatives", Map.of("source", "OUTLET_BANNER"));
        assertThat(creative.status()).isEqualTo(201);
        assertThat(creative.data().get("auditStatus")).isEqualTo("PENDING");
        assertThat(creative.data().get("assetUrl")).isEqualTo(outlet.get("bannerUrl"));
        String creativeId = (String) creative.data().get("id");
        manifest.put("creativeId", creativeId); save(retained, manifest);

        // The admin rejects it in the UI, with a reason the advertiser reads; activation is still refused.
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
        AdminAdCreativesPage moderation = new AdminAdCreativesPage(adminPage);
        moderation.open();
        moderation.assertPending(creativeId, name, (String) outlet.get("name"));
        String reason = "Banner is too dark to read on the listing";
        moderation.reject(creativeId, reason);
        Map<?, ?> rejected = creativeOf(campaign, creativeId);
        assertThat(rejected.get("auditStatus")).isEqualTo("REJECTED");
        assertThat(rejected.get("rejectionReason")).isEqualTo(reason);
        assertThat(GatewayApi.post(restaurantPage, campaign + "/activate", null).status()).isEqualTo(400);

        // Resubmitted, approved in the UI.
        var resubmitted = GatewayApi.put(restaurantPage, campaign + "/creatives/" + creativeId, Map.of("source", "OUTLET_BANNER"));
        assertThat(resubmitted.status()).isEqualTo(200);
        assertThat(resubmitted.data().get("auditStatus")).isEqualTo("PENDING");
        moderation.refresh();
        moderation.assertPending(creativeId, name, (String) outlet.get("name"));
        moderation.approve(creativeId);
        assertThat(creativeOf(campaign, creativeId).get("auditStatus")).isEqualTo("APPROVED");

        // The business wallet covers a day; then ACTIVE at once (its first day has begun).
        String wallet = "/api/v1/money/business/" + org;
        BigDecimal balance = balance(wallet);
        manifest.put("balanceBeforeTopUp", balance.toPlainString());
        if (balance.compareTo(new BigDecimal("20")) < 0) {
            var topUp = topUp(wallet, UUID.randomUUID().toString(), 100);
            assertThat(topUp.status()).isIn(200, 201);
            waitUntil(() -> balance(wallet).compareTo(balance.add(new BigDecimal("100"))) == 0, 15000, "the ₹100 top-up to settle");
            manifest.put("toppedUp", 100);
        }
        save(retained, manifest);
        var activated = GatewayApi.post(restaurantPage, campaign + "/activate", null);
        assertThat(activated.status()).isEqualTo(200);
        assertThat(activated.data().get("status")).isEqualTo("ACTIVE");

        // The customer's listing: that outlet first, sponsored, with its real data.
        waitUntil(() -> {
            List<Map<String, Object>> now = listing(listingUrl);
            return !now.isEmpty() && Boolean.TRUE.equals(now.get(0).get("isSponsored")) && outletId.equals(now.get(0).get("id"));
        }, 30000, "the campaign to be indexed and served first in the customer's listing");
        Map<String, Object> card = listing(listingUrl).get(0);
        assertThat(card.get("name")).isEqualTo(outlet.get("name"));
        assertThat(((Map<?, ?>) card.get("adData")).get("campaignId")).isEqualTo(campaignId);

        // The customer sees it; the card's impression is recorded and bills the business wallet.
        BigDecimal beforeImpression = balance(wallet);
        manifest.put("balanceBeforeImpression", beforeImpression.toPlainString());
        Response impression = customerPage.waitForResponse(
                r -> UrlPaths.path(r.url()).equals("/api/v1/tracking/impression"),
                new Page.WaitForResponseOptions().setTimeout(30000),
                () -> {
                    customerPage.reload();
                    Locator dialog = customerPage.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG);
                    try {
                        dialog.waitFor(new Locator.WaitForOptions().setTimeout(5000));
                        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
                    } catch (com.microsoft.playwright.TimeoutError noAddressPrompt) {
                        // the saved address carried over the reload
                    }
                });
        assertThat(impression.status()).isBetween(200, 299);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                customerPage.getByText("Sponsored", new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        waitUntil(() -> balance(wallet).compareTo(beforeImpression) < 0, 15000, "the impression to be charged to the business wallet");
        BigDecimal charged = beforeImpression.subtract(balance(wallet));
        assertThat(charged).isPositive().isLessThanOrEqualTo(new BigDecimal("0.50"));
        manifest.put("charged", charged.toPlainString()); save(retained, manifest);

        // The ledger booked it: a debit of the organisation's prepaid account for exactly that amount.
        var debits = statementLines(org).stream()
                .filter(l -> "AD_IMPRESSION".equals(l.get("category")) && "DEBIT".equals(l.get("direction")))
                .filter(l -> new BigDecimal(l.get("amount").toString()).compareTo(charged) == 0).toList();
        assertThat(debits).as("BUSINESS_PREPAID debit for the impression").isNotEmpty();

        // Performance counts it for today.
        waitUntil(() -> {
            var perf = GatewayApi.get(restaurantPage, campaign + "/performance");
            if (perf.status() != 200) return false;
            List<?> days = (List<?>) perf.data().get("content");
            return days.stream().map(Map.class::cast).anyMatch(d -> today.equals(d.get("date"))
                    && ((Number) d.get("impressions")).longValue() >= 1);
        }, 15000, "performance to count the impression");

        // Paused: out of the listing.
        assertThat(GatewayApi.post(restaurantPage, campaign + "/pause", null).status()).isEqualTo(200);
        waitUntil(() -> listing(listingUrl).stream().noneMatch(r -> Boolean.TRUE.equals(r.get("isSponsored"))
                && r.get("adData") instanceof Map<?, ?> ad && campaignId.equals(ad.get("campaignId"))), 10000, "the paused campaign to leave the listing");

        manifest.put("completed", true); save(retained, manifest);
        System.out.println("A3 campaign moderated, activated, served, billed and paused; retained " + retained);
    }

    /** The customer listing's own request, replayed from the signed-in customer page. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listing(String url) {
        Object data = customerPage.evaluate("""
            async (url) => {
              const token = localStorage.getItem('auth_token');
              const r = await fetch(url, {headers: {Authorization: 'Bearer ' + token}, credentials: 'same-origin'});
              if (!r.ok) throw new Error('nearby ' + r.status);
              return (await r.json()).data;
            }
            """, url);
        return (List<Map<String, Object>>) data;
    }

    private Map<?, ?> creativeOf(String campaign, String creativeId) {
        var list = GatewayApi.get(restaurantPage, campaign + "/creatives");
        assertThat(list.status()).isEqualTo(200);
        return ((List<?>) list.object().get("data")).stream().map(Map.class::cast)
                .filter(c -> creativeId.equals(c.get("id"))).findFirst().orElseThrow();
    }

    private BigDecimal balance(String wallet) {
        var response = GatewayApi.get(restaurantPage, wallet);
        assertThat(response.status()).isEqualTo(200);
        return new BigDecimal(response.data().get("balance").toString());
    }

    private GatewayApi.Response topUp(String wallet, String key, int amount) {
        var result = (Map<?, ?>) restaurantPage.evaluate("""
            async ({path, key, amount}) => {
              const token = localStorage.getItem('auth_token');
              const response = await fetch(path, {method: 'POST', credentials: 'same-origin', redirect: 'error',
                headers: {Authorization: 'Bearer ' + token, 'Content-Type': 'application/json', 'Idempotency-Key': key},
                body: JSON.stringify({amount, paymentMethod: 'CARD'})});
              const text = await response.text();
              let body = null;
              try { body = text ? JSON.parse(text) : null; } catch (e) { body = text; }
              return {status: response.status, body};
            }
            """, Map.of("path", wallet + "/topups", "key", key, "amount", amount));
        return new GatewayApi.Response(((Number) result.get("status")).intValue(), result.get("body"));
    }

    private List<Map<?, ?>> statementLines(String org) {
        var response = GatewayApi.get(restaurantPage, "/api/v1/ledger/statements/BUSINESS_PREPAID/" + org + "?page=0&size=50");
        assertThat(response.status()).isEqualTo(200);
        var lines = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.object().get("content")) lines.add((Map<?, ?>) row);
        return lines;
    }

    private List<Map<?, ?>> outlets(Page page) {
        var response = GatewayApi.get(page, "/api/v1/outlets");
        assertThat(response.status()).isEqualTo(200);
        var result = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.object().get("data")) result.add((Map<?, ?>) row);
        return result;
    }

    private String onlyOrganisation(Page page) {
        var response = GatewayApi.get(page, "/api/v1/organisations");
        assertThat(response.status()).isEqualTo(200);
        List<?> rows = (List<?>) response.data().get("content");
        assertThat(rows).hasSize(1);
        return (String) ((Map<?, ?>) rows.get(0)).get("id");
    }

    /** Polls a condition the system reaches asynchronously (outbox → Kafka → consumer); fails with what it waited for. */
    private void waitUntil(java.util.function.BooleanSupplier condition, long timeoutMs, String what) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) throw new AssertionError("Timed out after " + timeoutMs + " ms waiting for " + what);
            restaurantPage.waitForTimeout(500);
        }
    }

    private void save(Path path, Map<String, Object> manifest) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, (String) restaurantPage.evaluate("value => JSON.stringify(value,null,2)", manifest) + "\n");
    }
}
