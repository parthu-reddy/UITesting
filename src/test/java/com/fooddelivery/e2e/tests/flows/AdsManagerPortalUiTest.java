package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminAdCreativesPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.ads.AdsManagerPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.GatewayApi;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A4 gate: Ads Manager end to end, in the UI. Brand 2's owner (9000000002, a different organisation from A3's run) opens
 * Ads Manager from the launcher, starts advertising if needed, and creates a campaign in the wizard with an UPLOADED
 * image (D-A4-UPLOAD): it stays private ("In review", a signed preview) until an admin approves it in the A3 admin page;
 * approval publishes it, and the customer's listing then shows that outlet first, Sponsored, with that image. The
 * campaign is activated and paused from its page, its impression appears in its performance, and the restaurant
 * portal has no Campaigns tab. Real sign-ins, no routed responses; the campaign is left PAUSED.
 */
@Tag("feature-ads")
public class AdsManagerPortalUiTest extends TestBase {

    private static final String NEARBY = "/api/v1/restaurants/nearby";

    @Test void ownerRunsACampaignInAdsManager() throws Exception {
        assertThat(System.getProperty("bp.a4.preflight")).as("Run only after the A4 release and fresh seeds").isEqualTo("true");
        String owner = System.getProperty("bp.a4.owner", "9000000002");
        Path retained = Path.of("target/business-platform/a4/campaign-" + System.currentTimeMillis() + ".json");
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

        // The owner opens Ads Manager from the launcher: Brand 2's organisation.
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(owner).openPortal(Portal.ADS);
        AdsManagerPage ads = new AdsManagerPage(restaurantPage);
        ads.waitForPortal();
        Map<?, ?> org = onlyOrganisation(restaurantPage);
        String orgId = (String) org.get("id");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(ads.organisation()).hasText((String) org.get("displayName"));
        manifest.put("organisationId", orgId); manifest.put("startedAccount", ads.startAdvertisingIfOffered()); save(retained, manifest);

        // An outlet of this organisation in the customer's listing.
        Map<?, ?> outlet = outlets(restaurantPage).stream()
                .filter(o -> before.stream().anyMatch(r -> o.get("id").equals(r.get("id"))))
                .findFirst().orElseThrow(() -> new AssertionError("No outlet of " + owner + "'s organisation is in customer " + testCustomerPhone
                        + "'s nearby listing; pass -Dcustomer.phone for a seeded customer whose Home is near it"));
        String outletId = (String) outlet.get("id"), outletName = (String) outlet.get("name");
        manifest.put("outletId", outletId); save(retained, manifest);

        // The wizard: Restaurant, the outlet, ₹20/day, ₹100 total, ₹0.50 bid, an uploaded PNG, submit.
        String name = "E2E A4 " + System.currentTimeMillis();
        Path image = pngCreative();
        String campaignId = ads.newCampaign().business("Restaurant").outlet(outletName).budget(name, "20", "100", "0.50").uploadImage(image).submit(name);
        String campaign = "/api/v1/advertisers/" + orgId + "/campaigns/" + campaignId;
        manifest.put("campaignId", campaignId); save(retained, manifest);
        ads.assertStatusLabel("In review");

        // Private until approved: the creative is an UPLOAD, previewed through a short signed link, not a public URL.
        Map<?, ?> pending = onlyCreative(campaign);
        String creativeId = (String) pending.get("id");
        assertThat(pending.get("source")).isEqualTo("UPLOAD");
        assertThat(pending.get("auditStatus")).isEqualTo("PENDING");
        String signed = (String) pending.get("previewUrl");
        assertThat(signed).contains("X-Amz-Signature").contains("/ad-creatives/" + orgId + "/" + campaignId + "/");
        manifest.put("creativeId", creativeId); save(retained, manifest);
        assertThat(GatewayApi.post(restaurantPage, campaign + "/activate", null).status()).isEqualTo(400);

        // The admin approves it in the A3 page; approval publishes the file.
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
        AdminAdCreativesPage moderation = new AdminAdCreativesPage(adminPage);
        moderation.open();
        moderation.assertPending(creativeId, name, outletName);
        moderation.approve(creativeId);
        Map<?, ?> approved = onlyCreative(campaign);
        assertThat(approved.get("auditStatus")).isEqualTo("APPROVED");
        String published = (String) approved.get("previewUrl");
        assertThat(published).doesNotContain("X-Amz-Signature").doesNotContain("documents").endsWith(".png");
        var publicRead = restaurantPage.context().request().get(published);
        assertThat(publicRead.status()).as("the published creative is publicly readable").isEqualTo(200);
        assertThat(publicRead.body()).isEqualTo(Files.readAllBytes(image));
        manifest.put("publishedUrl", published); save(retained, manifest);

        // The wallet covers a day (topped up through W2 if not); then Activate from the campaign's page.
        String wallet = "/api/v1/money/business/" + orgId;
        BigDecimal balance = balance(wallet);
        manifest.put("balanceBeforeTopUp", balance.toPlainString());
        if (balance.compareTo(new BigDecimal("20")) < 0) {
            assertThat(topUp(wallet, UUID.randomUUID().toString(), 100).status()).isIn(200, 201);
            waitUntil(() -> balance(wallet).compareTo(balance.add(new BigDecimal("100"))) == 0, 15000, "the ₹100 top-up to settle");
            manifest.put("toppedUp", 100);
        }
        save(retained, manifest);
        boolean paused = false;
        try {
            ads.reloadCampaign();
            ads.activate();
            ads.waitForStatus("ACTIVE");
            ads.assertStatusLabel("Live");

            // The customer's listing: that outlet first, Sponsored, showing the published creative.
            waitUntil(() -> {
                List<Map<String, Object>> now = listing(listingUrl);
                return !now.isEmpty() && Boolean.TRUE.equals(now.get(0).get("isSponsored")) && outletId.equals(now.get(0).get("id"));
            }, 30000, "the campaign to be indexed and served first in the customer's listing");
            Map<?, ?> ad = (Map<?, ?>) listing(listingUrl).get(0).get("adData");
            assertThat(ad.get("campaignId")).isEqualTo(campaignId);
            assertThat(ad.get("adm")).isEqualTo(published);
            Response impression = customerPage.waitForResponse(
                    r -> UrlPaths.path(r.url()).equals("/api/v1/tracking/impression"),
                    new Page.WaitForResponseOptions().setTimeout(30000),
                    () -> {
                        customerPage.reload();
                        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
                        try {
                            dialog.waitFor(new Locator.WaitForOptions().setTimeout(5000));
                            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
                        } catch (com.microsoft.playwright.TimeoutError noAddressPrompt) {
                            // the saved address carried over the reload
                        }
                    });
            assertThat(impression.status()).isBetween(200, 299);
            Locator card = customerPage.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("Sponsored")).first();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(card.getByRole(AriaRole.IMG, new Locator.GetByRoleOptions().setName(outletName)))
                    .hasAttribute("src", published);

            // Its performance counts the impression (KafkaAnalyticsConsumer), and the campaign's page shows it.
            waitUntil(() -> {
                var perf = GatewayApi.get(restaurantPage, campaign + "/performance");
                return perf.status() == 200 && ((List<?>) perf.data().get("content")).stream().map(Map.class::cast)
                        .anyMatch(d -> ((Number) d.get("impressions")).longValue() >= 1);
            }, 30000, "performance to count the impression");
            ads.reloadCampaign();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(ads.performance().getByRole(AriaRole.TABLE)).isVisible();

            // Pause from the page; it leaves the listing.
            ads.pause();
            ads.waitForStatus("PAUSED");
            paused = true;
            waitUntil(() -> listing(listingUrl).stream().noneMatch(r -> Boolean.TRUE.equals(r.get("isSponsored"))
                    && r.get("adData") instanceof Map<?, ?> a && campaignId.equals(a.get("campaignId"))), 10000, "the paused campaign to leave the listing");
        } finally {
            if (!paused) System.out.println("A4 cleanup pause after failure: " + GatewayApi.post(restaurantPage, campaign + "/pause", null).status());
        }

        // The restaurant portal for the same owner has no Campaigns tab.
        new LoginPage(restaurantPage).openPortal(Portal.RESTAURANT);
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Menu").setExact(true)).waitFor();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Campaigns").setExact(true))).hasCount(0);

        manifest.put("completed", true); save(retained, manifest);
        System.out.println("A4 campaign created in Ads Manager, uploaded creative published on approval, served, paused; retained " + retained);
    }

    /** A small real PNG (well under 2 MB): what an advertiser would upload. */
    private static Path pngCreative() throws Exception {
        BufferedImage img = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(new Color(0xF5, 0x9E, 0x0B)); g.fillRect(0, 0, 320, 180);
        g.setColor(Color.BLACK); g.drawString("E2E A4 " + System.currentTimeMillis(), 20, 90);
        g.dispose();
        Path path = Path.of("target/business-platform/a4/creative-" + System.currentTimeMillis() + ".png");
        Files.createDirectories(path.getParent());
        ImageIO.write(img, "png", path.toFile());
        return path;
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

    private Map<?, ?> onlyCreative(String campaign) {
        var list = GatewayApi.get(restaurantPage, campaign + "/creatives");
        assertThat(list.status()).isEqualTo(200);
        List<?> rows = (List<?>) list.object().get("data");
        assertThat(rows).hasSize(1);
        return (Map<?, ?>) rows.get(0);
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

    private List<Map<?, ?>> outlets(Page page) {
        var response = GatewayApi.get(page, "/api/v1/outlets");
        assertThat(response.status()).isEqualTo(200);
        var result = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.object().get("data")) result.add((Map<?, ?>) row);
        return result;
    }

    private Map<?, ?> onlyOrganisation(Page page) {
        var response = GatewayApi.get(page, "/api/v1/organisations");
        assertThat(response.status()).isEqualTo(200);
        List<?> rows = (List<?>) response.data().get("content");
        assertThat(rows).hasSize(1);
        return (Map<?, ?>) rows.get(0);
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
