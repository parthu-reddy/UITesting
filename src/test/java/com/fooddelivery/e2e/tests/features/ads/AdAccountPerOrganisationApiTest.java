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
 * A1 gate (runs after the W1 + A1 release and a fresh Dev seed): the ad account IS the organisation.
 * Started once (repeat returns it unchanged); STAFF refused, MANAGER manages, non-members cannot tell
 * it exists, an organisation without an approved business cannot start one. Real Dev sign-in through
 * the UI; calls go through the gateway from the signed-in page. Fixtures are retained.
 */
@Tag("business-platform") @Tag("bp-a1")
public class AdAccountPerOrganisationApiTest extends TestBase {

    @Test void adAccountPerOrganisation() throws Exception {
        assertThat(System.getProperty("bp.a1.preflight")).as("Run only after the W1+A1 release and fresh seeds").isEqualTo("true");
        String phone = System.getProperty("bp.a1.phone");
        assertThat(phone).matches("9999[0-9]{6}");
        Path retained = Path.of("target/business-platform/a1/member-" + phone + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("phone", phone); manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false);

        login(restaurantPage, "9000000001");
        String org = onlyOrganisation(restaurantPage);
        String account = "/api/v1/advertisers/" + org;
        manifest.put("organisationId", org); saveManifest(retained, manifest);

        int before = GatewayApi.get(restaurantPage, account).status();
        assertThat(before).as("404 on a fresh seed; 200 when an earlier run already started it").isIn(200, 404);
        Map<String, Object> body = Map.of("displayName", "Brand 1 Ads", "timeZone", "Asia/Kolkata");
        var started = GatewayApi.put(restaurantPage, account, body);
        assertThat(started.status()).isEqualTo(before == 404 ? 201 : 200);
        assertThat(started.data().get("id")).isEqualTo(org);
        var again = GatewayApi.put(restaurantPage, account, Map.of("displayName", "Renamed by a repeat", "timeZone", "Asia/Dubai"));
        assertThat(again.status()).isEqualTo(200);
        assertThat(again.data().get("displayName")).isEqualTo(started.data().get("displayName"));
        assertThat(again.data().get("timeZone")).isEqualTo(started.data().get("timeZone"));
        manifest.put("adAccountStartedByThisRun", before == 404); saveManifest(retained, manifest);

        // A disposable STAFF member is refused; promoted to MANAGER, reads and creates a DRAFT campaign.
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(phone, "E2E A1 Staff", "a1_" + phone + "@test.com");
        String orgPath = "/api/v1/organisations/" + org;
        var invited = GatewayApi.post(restaurantPage, orgPath + "/invitations", Map.of("phoneNumber", phone, "role", "STAFF"));
        assertThat(invited.status()).isEqualTo(201);
        assertThat(GatewayApi.post(customerPage, "/api/v1/organisation-invitations/" + invited.data().get("id") + "/accept", null).status()).isEqualTo(200);
        String userId = (String) content(GatewayApi.get(restaurantPage, orgPath + "/members")).stream()
                .filter(m -> phone.equals(m.get("phoneNumber"))).findFirst().orElseThrow().get("userId");
        manifest.put("userId", userId); saveManifest(retained, manifest);
        customerPage.reload();
        assertThat(GatewayApi.get(customerPage, account).status()).isEqualTo(403);

        assertThat(GatewayApi.patch(restaurantPage, orgPath + "/members/" + userId, Map.of("role", "MANAGER")).status()).isEqualTo(200);
        customerPage.waitForTimeout(5100);
        assertThat(GatewayApi.get(customerPage, account).status()).isEqualTo(200);
        String tomorrow = LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1).toString();
        var campaign = GatewayApi.post(customerPage, account + "/campaigns", Map.of("advertiserId", org, "name", "E2E A1 " + phone,
                "dailyBudget", 100, "lifetimeBudget", 1000, "maxBid", 5, "startDate", tomorrow));
        assertThat(campaign.status()).isEqualTo(201);
        assertThat(campaign.data().get("status")).isEqualTo("DRAFT");
        manifest.put("campaignId", campaign.data().get("id")); saveManifest(retained, manifest);

        // The owner of another organisation cannot tell the account exists.
        login(adminPage, "9000000002");
        assertThat(GatewayApi.get(adminPage, account).status()).isEqualTo(404);

        // No approved business, no BUSINESS role: the gateway refuses. The service's own 409 for the
        // same rule is proven by AdAccountServiceTest.
        login(customerPage, "9000000011");
        String noBrandOrg = onlyOrganisation(customerPage);
        assertThat(GatewayApi.put(customerPage, "/api/v1/advertisers/" + noBrandOrg, body).status()).isEqualTo(403);

        // The campaign wallet is the organisation's business wallet (W1).
        assertThat(GatewayApi.get(restaurantPage, "/api/v1/money/business/" + org).status()).isEqualTo(200);
        manifest.put("completed", true); saveManifest(retained, manifest);
        System.out.println("A1 ad account per organisation verified; retained fixture " + retained);
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
