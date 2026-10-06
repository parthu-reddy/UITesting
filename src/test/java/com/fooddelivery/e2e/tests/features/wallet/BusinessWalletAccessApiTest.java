package com.fooddelivery.e2e.tests.features.wallet;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.GatewayApi;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * W1 gate (runs after the W1 + A1 release and a fresh Dev seed): an organisation's business wallet is
 * readable by members with WALLET_VIEW (OWNER, ADMIN, MANAGER) and by nobody else. Real Dev sign-in
 * through the UI; calls go through the gateway from the signed-in page. The disposable member and
 * the manifest are retained; nothing is cleaned up.
 */
@Tag("business-platform") @Tag("bp-w1")
public class BusinessWalletAccessApiTest extends TestBase {

    @Test void businessWalletAccess() throws Exception {
        assertThat(System.getProperty("bp.w1.preflight")).as("Run only after the W1+A1 release and fresh seeds").isEqualTo("true");
        String phone = System.getProperty("bp.w1.phone");
        assertThat(phone).matches("9999[0-9]{6}");
        Path retained = Path.of("target/business-platform/w1/member-" + phone + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("phone", phone); manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false);

        login(restaurantPage, "9000000001");
        String org = onlyOrganisation(restaurantPage);
        manifest.put("organisationId", org); saveManifest(retained, manifest);
        String wallet = "/api/v1/money/business/" + org;

        var owner = GatewayApi.get(restaurantPage, wallet);
        assertThat(owner.status()).isEqualTo(200);
        assertThat(owner.data().get("entityType")).isEqualTo("BUSINESS");
        assertThat(owner.data().get("entityId")).isEqualTo(org);
        assertThat(owner.data().get("currency")).isEqualTo("INR");
        assertThat(new BigDecimal(owner.data().get("balance").toString())).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        manifest.put("ownerBalance", owner.data().get("balance"));
        assertThat(GatewayApi.get(restaurantPage, wallet + "/transactions?page=0&size=20").status()).isEqualTo(200);
        assertThat(GatewayApi.get(restaurantPage, wallet + "/transactions?size=51").status()).isEqualTo(400);

        // A disposable STAFF member of the same organisation is refused; promoted to MANAGER, admitted.
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(phone, "E2E W1 Staff", "w1_" + phone + "@test.com");
        String orgPath = "/api/v1/organisations/" + org;
        var invited = GatewayApi.post(restaurantPage, orgPath + "/invitations", Map.of("phoneNumber", phone, "role", "STAFF"));
        assertThat(invited.status()).isEqualTo(201);
        assertThat(GatewayApi.post(customerPage, "/api/v1/organisation-invitations/" + invited.data().get("id") + "/accept", null).status()).isEqualTo(200);
        String userId = memberId(restaurantPage, orgPath, phone);
        manifest.put("userId", userId); saveManifest(retained, manifest);
        customerPage.reload(); // the new membership grants the BUSINESS role on the next token
        assertThat(GatewayApi.get(customerPage, wallet).status()).isEqualTo(403);

        assertThat(GatewayApi.patch(restaurantPage, orgPath + "/members/" + userId, Map.of("role", "MANAGER")).status()).isEqualTo(200);
        customerPage.waitForTimeout(5100); // a STAFF decision may stay fresh for five seconds
        var manager = GatewayApi.get(customerPage, wallet);
        assertThat(manager.status()).isEqualTo(200);
        assertThat(manager.data().get("balance").toString()).isEqualTo(owner.data().get("balance").toString());

        // The owner of another organisation is refused.
        login(adminPage, "9000000002");
        assertThat(GatewayApi.get(adminPage, wallet).status()).isEqualTo(403);

        // A person without an approved business has no BUSINESS role, so the gateway refuses even
        // their own organisation's wallet until it is approved: by design (W1 validation.md).
        login(customerPage, "9000000011");
        String noBrandOrg = onlyOrganisation(customerPage);
        assertThat(GatewayApi.get(customerPage, "/api/v1/money/business/" + noBrandOrg).status()).isEqualTo(403);

        manifest.put("finalRole", "MANAGER"); manifest.put("completed", true); saveManifest(retained, manifest);
        System.out.println("W1 business wallet access verified; retained fixture " + retained);
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
