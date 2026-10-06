package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * CAMPAIGN-01/ONBOARD/WALLET/05-07/14: the restaurant's Campaigns tab, against the organisation's real ad
 * account (A1: the ad account IS the organisation that owns the selected outlet's brand).
 *
 * <p>{@code -Dcampaign.outlet}: the outlet to select (its brand and time zone seed the start step).
 * Writes nothing unless asked: {@code -Dcampaign.onboard=true} lets it press "Start advertising" when
 * the organisation has no ad account (one account, permanently; the business wallet already exists, W1); {@code -Dcampaign.create=true}
 * lets it launch one DRAFT campaign. A DRAFT never serves or spends: activation needs an approved creative.
 */
@Tag("campaigns")
public class RestaurantCampaignsLiveTest extends TestBase {
    private static final String OUTLET = System.getProperty("campaign.outlet", "").trim();
    private final List<String> advertiserWrites = new ArrayList<>();

    @BeforeEach
    void ownerOnCampaigns() {
        Assumptions.assumeFalse(OUTLET.isEmpty(), "needs -Dcampaign.outlet");
        restaurantPage.onRequest(request -> {
            if (request.url().contains("/api/v1/advertisers") && !"GET".equals(request.method())) {
                advertiserWrites.add(request.method() + " " + request.url().replaceFirst("^https?://[^/]+", ""));
            }
        });
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        dashboard.selectOutlet(OUTLET);
        dashboard.openCampaignsTab();
    }

    @Test
    @DisplayName("CAMPAIGN-01/ONBOARD/WALLET: the tab shows the organisation's ad account, or starts one from the outlet's brand and zone")
    void tabMatchesTheOwnersAdvertiser() {
        Map<?, ?> outlet = selectedOutlet();
        Map<?, ?> brand = brand((String) outlet.get("brandId"));
        String brandName = (String) brand.get("name");
        String organisationId = (String) brand.get("organisationId");
        String zone = (String) outlet.get("timeZone");
        Map<?, ?> me = get("/api/v1/advertisers/" + organisationId);

        if (Integer.valueOf(404).equals(me.get("status"))) {
            // CAMPAIGN-01 (not advertising yet): the start step, prefilled, and nothing written by opening it.
            Locator start = restaurantPage.getByTestId("start-advertising");
            assertThat(start).isVisible();
            assertThat(start.getByLabel("Business name")).hasValue(brandName);
            assertThat(start).containsText(zone + ", this outlet's time zone");
            assertThat(restaurantPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ad Spending History"))).isHidden();
            org.assertj.core.api.Assertions.assertThat(advertiserWrites).as("opening the tab creates nothing").isEmpty();
            Assumptions.assumeTrue(Boolean.getBoolean("campaign.onboard"),
                    "start step verified; pass -Dcampaign.onboard=true to create this owner's advertiser");

            // CAMPAIGN-ONBOARD: one press starts exactly one ad account, keyed by the organisation, under the brand's name and the outlet's zone.
            start.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Start advertising").setExact(true)).click();
            assertThat(restaurantPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ad Spending History"))).isVisible();
            org.assertj.core.api.Assertions.assertThat(advertiserWrites).containsExactly("PUT /api/v1/advertisers/" + organisationId);
            me = get("/api/v1/advertisers/" + organisationId);
            Map<?, ?> created = (Map<?, ?>) ((Map<?, ?>) me.get("body")).get("data");
            org.assertj.core.api.Assertions.assertThat(created.get("id")).as("the ad account is the organisation").isEqualTo(organisationId);
            org.assertj.core.api.Assertions.assertThat(created.get("displayName")).isEqualTo(brandName);
            org.assertj.core.api.Assertions.assertThat(created.get("timeZone")).isEqualTo(zone);
        }

        // CAMPAIGN-01 (advertising): the campaign screen, reloading it finds the same advertiser and creates nothing.
        org.assertj.core.api.Assertions.assertThat(me.get("status")).isEqualTo(200);
        String advertiserId = (String) ((Map<?, ?>) ((Map<?, ?>) me.get("body")).get("data")).get("id");
        assertThat(restaurantPage.getByTestId("start-advertising")).isHidden();
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Campaign").setExact(true))).isVisible();

        // CAMPAIGN-WALLET: campaigns spend from the organisation's business wallet (W1) and the card shows it.
        Map<?, ?> wallet = get("/api/v1/money/business/" + advertiserId);
        org.assertj.core.api.Assertions.assertThat(wallet.get("status")).as("owner reads the business wallet").isEqualTo(200);
        BigDecimal balance = new BigDecimal(String.valueOf(((Map<?, ?>) ((Map<?, ?>) wallet.get("body")).get("data")).get("balance")));
        Locator balanceCard = restaurantPage.getByText("Ad Wallet Balance", new Page.GetByTextOptions().setExact(true)).locator("..");
        assertThat(balanceCard).containsText(inr(balance));

        // The list is the server's list.
        Map<?, ?> page = (Map<?, ?>) ((Map<?, ?>) get("/api/v1/advertisers/" + advertiserId + "/campaigns").get("body")).get("data");
        List<?> campaigns = (List<?>) page.get("content");
        if (campaigns.isEmpty()) {
            assertThat(restaurantPage.getByText("No campaigns found. Create your first ad campaign!")).isVisible();
        } else {
            for (Object value : campaigns) {
                Map<?, ?> c = (Map<?, ?>) value;
                Locator card = restaurantPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName((String) c.get("name")).setExact(true)).locator("../../..");
                assertThat(card).containsText(String.valueOf(c.get("status")));
                assertThat(card).containsText(inr(new BigDecimal(String.valueOf(c.get("dailyBudget")))));
                assertThat(card).containsText(inr(new BigDecimal(String.valueOf(c.get("lifetimeBudget")))));
            }
        }
    }

    @Test
    @DisplayName("CAMPAIGN-05..07: an unsaved draft is discarded on Cancel")
    void draftIsDiscardedOnCancel() {
        requireAdvertiser();
        restaurantPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Campaign").setExact(true)).click();
        Locator dialog = restaurantPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog.getByText("New Ad Campaign", new Locator.GetByTextOptions().setExact(true))).isVisible();
        dialog.getByPlaceholder("e.g. Summer Special Boost").fill("Unsaved E2E draft");
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        assertThat(dialog).isHidden();
        assertThat(restaurantPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Unsaved E2E draft").setExact(true))).hasCount(0);
        org.assertj.core.api.Assertions.assertThat(advertiserWrites).as("a cancelled draft writes nothing").isEmpty();
    }

    @Test
    @DisplayName("CAMPAIGN-14: a launched campaign is stored in rupees, as typed, and listed as DRAFT")
    void launchedCampaignIsStoredInRupees() {
        Assumptions.assumeTrue(Boolean.getBoolean("campaign.create"), "creates a DRAFT campaign; pass -Dcampaign.create=true");
        String advertiserId = requireAdvertiser();
        String name = "E2E draft " + java.util.UUID.randomUUID().toString().substring(0, 8);

        restaurantPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Campaign").setExact(true)).click();
        Locator dialog = restaurantPage.getByRole(AriaRole.DIALOG);
        dialog.getByLabel("Campaign Name").fill(name);
        dialog.getByLabel("Daily Budget (₹)").fill("50");
        dialog.getByLabel("Total Budget (₹)").fill("500");
        dialog.getByLabel("Bid per Impression (₹)").fill("1.5");
        // The app's shared transport re-wraps each request, and Playwright reports no body for it, so
        // "rupees, as typed" is proven by the stored values below (paise would read 5000/50000/150).
        Response launched = restaurantPage.waitForResponse(r -> r.url().endsWith("/api/v1/advertisers/" + advertiserId + "/campaigns") && "POST".equals(r.request().method()),
                () -> dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Launch Campaign").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(launched.status()).isEqualTo(201);

        Locator card = restaurantPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(name).setExact(true)).locator("../../..");
        assertThat(card).containsText("DRAFT");
        assertThat(card).containsText(inr(new BigDecimal("50")));
        assertThat(card).containsText(inr(new BigDecimal("500")));
        Map<?, ?> page = (Map<?, ?>) ((Map<?, ?>) get("/api/v1/advertisers/" + advertiserId + "/campaigns").get("body")).get("data");
        Map<?, ?> stored = ((List<?>) page.get("content")).stream().map(Map.class::cast)
                .filter(c -> name.equals(c.get("name"))).findFirst().orElseThrow();
        org.assertj.core.api.Assertions.assertThat(new BigDecimal(String.valueOf(stored.get("dailyBudget")))).isEqualByComparingTo("50");
        org.assertj.core.api.Assertions.assertThat(new BigDecimal(String.valueOf(stored.get("lifetimeBudget")))).isEqualByComparingTo("500");
        org.assertj.core.api.Assertions.assertThat(new BigDecimal(String.valueOf(stored.get("maxBid")))).isEqualByComparingTo("1.5");
        org.assertj.core.api.Assertions.assertThat(stored.get("status")).isEqualTo("DRAFT");
    }

    /** The ad account (organisation) id; the campaign screen is only reachable once one exists. */
    private String requireAdvertiser() {
        Map<?, ?> me = get("/api/v1/advertisers/" + brand((String) selectedOutlet().get("brandId")).get("organisationId"));
        Assumptions.assumeTrue(Integer.valueOf(200).equals(me.get("status")),
                "this organisation has no ad account yet; run tabMatchesTheOwnersAdvertiser with -Dcampaign.onboard=true first");
        assertThat(restaurantPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("New Campaign").setExact(true))).isVisible();
        return (String) ((Map<?, ?>) ((Map<?, ?>) me.get("body")).get("data")).get("id");
    }

    private Map<?, ?> selectedOutlet() {
        Object body = get("/api/v1/outlets").get("body");
        List<?> outlets = (List<?>) (body instanceof Map<?, ?> envelope ? envelope.get("data") : body);
        return outlets.stream().map(Map.class::cast).filter(o -> OUTLET.equals(o.get("name"))).findFirst()
                .orElseThrow(() -> new AssertionError("outlet " + OUTLET + " is not this owner's"));
    }

    private Map<?, ?> brand(String brandId) {
        Object body = get("/api/v1/brands").get("body");
        List<?> brands = (List<?>) (body instanceof Map<?, ?> envelope ? envelope.get("data") : body);
        return brands.stream().map(Map.class::cast).filter(b -> brandId.equals(b.get("id"))).findFirst().orElseThrow();
    }

    /** A signed-in read as the restaurant owner: {status, body}. */
    private Map<?, ?> get(String path) {
        return (Map<?, ?>) restaurantPage.evaluate("""
            async path => {
              const response = await fetch(path, {headers:{Authorization:'Bearer '+localStorage.getItem('auth_token')}});
              const text = await response.text();
              return {status:response.status, body:text ? JSON.parse(text) : null};
            }
            """, path);
    }

    private static String inr(BigDecimal amount) {
        return new java.text.DecimalFormat("₹#,##0.00").format(amount);
    }
}
