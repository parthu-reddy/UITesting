package com.fooddelivery.e2e.tests.features.ads;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.GatewayApi;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A2 gate (after the A2 release and a fresh Dev seed): a campaign promotes exactly one approved, active
 * outlet of its own organisation, and its region follows that outlet's city. Outlets are read from each
 * owner's own outlet list, not hard-coded. Real Dev sign-in through the UI; calls go through the gateway
 * from the signed-in page. The created DRAFT campaign is retained (manifest under target/).
 */
@Tag("feature-ads")
public class PromotedOutletApiTest extends TestBase {

    @Test void campaignPromotesOneOfItsOwnOutlets() throws Exception {
        assertThat(System.getProperty("bp.a2.preflight")).as("Run only after the A2 release and fresh seeds").isEqualTo("true");
        Path retained = Path.of("target/business-platform/a2/promoted-outlet-" + System.currentTimeMillis() + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false);

        // Other organisations' outlets, as their owners see them.
        login(adminPage, "9000000002");
        Map<?, ?> brand2Outlet = outlets(adminPage).stream().filter(o -> Boolean.TRUE.equals(o.get("isActive"))).findFirst().orElseThrow();
        login(adminPage, "9000000014");
        Map<?, ?> inactiveOutlet = outlets(adminPage).stream().filter(o -> Boolean.FALSE.equals(o.get("isActive"))).findFirst()
                .orElseThrow(() -> new AssertionError("seeded 9000000014 has one inactive outlet"));

        login(restaurantPage, "9000000001");
        String org = onlyOrganisation(restaurantPage);
        String account = "/api/v1/advertisers/" + org;
        var started = GatewayApi.put(restaurantPage, account, Map.of("displayName", "Brand 1 Ads", "timeZone", "Asia/Kolkata"));
        assertThat(started.status()).as("started here (201) or by an earlier run (200)").isIn(200, 201);
        Map<?, ?> own = outlets(restaurantPage).stream().filter(o -> Boolean.TRUE.equals(o.get("isActive"))).findFirst().orElseThrow();
        manifest.put("organisationId", org); manifest.put("promotedOutletId", own.get("id")); saveManifest(retained, manifest);

        var created = GatewayApi.post(restaurantPage, account + "/campaigns", campaign(org, "E2E A2 own outlet", own.get("id")));
        assertThat(created.status()).isEqualTo(201);
        assertThat(created.data().get("status")).isEqualTo("DRAFT");
        assertThat(created.data().get("promotedBusinessType")).isEqualTo("RESTAURANT");
        assertThat(created.data().get("promotedEntityId")).isEqualTo(own.get("id"));
        assertThat(created.data().get("promotedEntityName")).isEqualTo(own.get("name"));
        String campaignPath = account + "/campaigns/" + created.data().get("id");
        manifest.put("campaignId", created.data().get("id")); saveManifest(retained, manifest);

        // The server-managed default ad group targets the outlet's city, and nothing else.
        var groups = GatewayApi.get(restaurantPage, campaignPath + "/ad-groups");
        assertThat(groups.status()).isEqualTo(200);
        List<?> content = (List<?>) groups.data().get("content");
        assertThat(content).hasSize(1);
        Map<?, ?> group = (Map<?, ?>) content.get(0);
        assertThat(group.get("name")).isEqualTo("Default");
        assertThat(((Map<?, ?>) group.get("geoTargeting")).get("regions")).isEqualTo(List.of(own.get("cityId")));

        // Regions are not an advertiser input.
        var geo = GatewayApi.put(restaurantPage, campaignPath + "/ad-groups/" + group.get("id"),
                Map.of("name", "Default", "geoTargeting", Map.of("regions", List.of("HYD"))));
        assertThat(geo.status()).isEqualTo(400);

        // Another organisation's outlet, active or not, is refused as another organisation's.
        assertThat(GatewayApi.post(restaurantPage, account + "/campaigns", campaign(org, "E2E A2 Brand 2 outlet", brand2Outlet.get("id"))).status())
                .isEqualTo(403);
        assertThat(GatewayApi.post(restaurantPage, account + "/campaigns", campaign(org, "E2E A2 inactive other outlet", inactiveOutlet.get("id"))).status())
                .as("the inactive-outlet rule for one's own outlet is PromotedOutletValidationTest").isEqualTo(403);

        manifest.put("completed", true); saveManifest(retained, manifest);
        System.out.println("A2 promoted outlet verified; retained fixture " + retained);
    }

    private Map<String, Object> campaign(String org, String name, Object outletId) {
        String tomorrow = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).toString();
        return Map.of("advertiserId", org, "name", name, "dailyBudget", 100, "lifetimeBudget", 1000, "maxBid", 5,
                "startDate", tomorrow, "promotedOutletId", outletId);
    }

    private void login(Page page, String phone) {
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).login(phone).openPortal(Portal.RESTAURANT);
    }

    private List<Map<?, ?>> outlets(Page page) {
        var response = GatewayApi.get(page, "/api/v1/outlets");
        assertThat(response.status()).isEqualTo(200);
        var result = new ArrayList<Map<?, ?>>();
        for (Object row : (List<?>) response.object().get("data")) result.add((Map<?, ?>) row);
        assertThat(result).isNotEmpty();
        return result;
    }

    private String onlyOrganisation(Page page) {
        var response = GatewayApi.get(page, "/api/v1/organisations");
        assertThat(response.status()).isEqualTo(200);
        List<?> rows = (List<?>) response.data().get("content");
        assertThat(rows).hasSize(1);
        return (String) ((Map<?, ?>) rows.get(0)).get("id");
    }

    private void saveManifest(Path path, Map<String, Object> manifest) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, (String) restaurantPage.evaluate("value => JSON.stringify(value,null,2)", manifest) + "\n");
    }
}
