package com.fooddelivery.e2e.tests.features.wallet;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.business.BusinessHubPage;
import com.fooddelivery.e2e.pages.business.BusinessWalletPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.GatewayApi;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * W3 gate (after the W3 release; W2 must be live-green): the business wallet page in the Business hub.
 * A seeded restaurant owner (TestConfig.restaurantPhone(): random 9000000001–9000000010, or -Drestaurant.phone)
 * opens their organisation's Wallet tab, sees the balance the API reports, adds ₹100
 * through the real dialog and payment modal (the Dev gateway captures a card), and sees the balance rise by
 * ₹100.00 with the top-up as the newest statement line. A disposable MANAGER, invited and accepted through
 * the hub, sees the balance and no Add money button. Fixtures are retained; nothing is cleaned up.
 */
@Tag("feature-wallet")
public class BusinessWalletUiTest extends TestBase {

    @Test void businessWalletPage() throws Exception {
        assertThat(System.getProperty("bp.w3.preflight")).as("Run only after the W3 release").isEqualTo("true");
        String managerPhone = System.getProperty("bp.w3.phone");
        assertThat(managerPhone).matches("9999[0-9]{6}");
        Path retained = Path.of("target/business-platform/w3/run-" + managerPhone + ".json");
        var manifest = new LinkedHashMap<String, Object>();
        manifest.put("phone", managerPhone); manifest.put("dataPolicy", "retain"); manifest.put("cleanupPerformed", false);

        // The owner: Business hub -> their organisation -> Wallet.
        restaurantPage.navigate(TestConfig.APP_URL);
        String owner = TestConfig.restaurantPhone();
        manifest.put("ownerPhone", owner);
        new LoginPage(restaurantPage).login(owner).openPortal(Portal.BUSINESS);
        BusinessHubPage hub = new BusinessHubPage(restaurantPage);
        String organisationName = hub.openApprovedOrganisationAfterLaunch();
        BusinessWalletPage wallet = new BusinessWalletPage(restaurantPage);
        wallet.open();
        String org = wallet.organisationId();
        manifest.put("organisationId", org); manifest.put("organisationName", organisationName); save(retained, manifest);

        BigDecimal before = apiBalance(restaurantPage, org);
        manifest.put("balanceBefore", before.toPlainString()); save(retained, manifest);
        assertThat(wallet.balance()).hasText(rupees(before));

        // Add ₹100: the payment dialog asks for exactly ₹100.00, never ₹10,000.00.
        var pay = wallet.startAddMoney("100");
        assertThat(pay).hasText("Add " + rupees(new BigDecimal("100")) + " to the wallet");
        assertThat(restaurantPage.getByText(rupees(new BigDecimal("10000")))).hasCount(0);
        var started = wallet.payAndWaitForConfirmation(pay);
        assertThat(started.status()).isEqualTo(200);
        String topupId = (String) restaurantPage.evaluate("t => JSON.parse(t).data.topupId", started.text());
        assertThat(topupId).matches("[a-f0-9-]{36}");
        manifest.put("topupId", topupId); save(retained, manifest);

        BigDecimal expected = before.add(new BigDecimal("100.00"));
        assertThat(wallet.balance()).hasText(rupees(expected));
        assertThat(apiBalance(restaurantPage, org)).isEqualByComparingTo(expected);
        assertThat(wallet.newestLineCategory()).hasText("Top-up");
        assertThat(wallet.newestLineAmount()).hasText("+" + rupees(new BigDecimal("100")));
        // The newest line is this exact top-up, not merely a ₹100 credit.
        var newest = GatewayApi.get(restaurantPage, "/api/v1/money/business/" + org + "/transactions?page=0&size=1");
        assertThat(newest.status()).isEqualTo(200);
        Map<?, ?> line = (Map<?, ?>) ((java.util.List<?>) newest.data().get("content")).get(0);
        assertThat(line.get("referenceId")).isEqualTo(topupId);
        assertThat(line.get("category")).isEqualTo("BUSINESS_WALLET_TOPUP");
        manifest.put("balanceAfter", expected.toPlainString()); save(retained, manifest);

        // A disposable MANAGER, invited and accepted through the hub: balance, but no Add money.
        hub.allOrganisations(); hub.open(organisationName); hub.members(); hub.invite(managerPhone, "MANAGER");
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginNewPerson(managerPhone, "E2E W3 Manager", "w3_" + managerPhone + "@test.com")
                .openPortal(Portal.BUSINESS);
        BusinessHubPage colleague = new BusinessHubPage(customerPage);
        colleague.accept(organisationName); colleague.open(organisationName);
        BusinessWalletPage managerWallet = new BusinessWalletPage(customerPage);
        managerWallet.open();
        assertThat(managerWallet.balance()).hasText(rupees(expected));
        assertThat(managerWallet.addMoneyButton()).hasCount(0);
        assertThat(managerWallet.unavailableReason()).containsText("Owners and admins can add money");

        manifest.put("completed", true); save(retained, manifest);
        System.out.println("W3 business wallet page verified; retained fixture " + retained);
    }

    /** Formatted by the browser exactly as the UI's formatINR does (en-IN grouping: "₹1,00,000.00"). */
    private String rupees(BigDecimal amount) {
        return (String) restaurantPage.evaluate(
                "v => new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR', currencyDisplay: 'symbol'}).format(v)",
                amount.doubleValue());
    }

    private static BigDecimal apiBalance(Page page, String org) {
        var response = GatewayApi.get(page, "/api/v1/money/business/" + org);
        assertThat(response.status()).isEqualTo(200);
        return new BigDecimal(response.data().get("balance").toString());
    }

    private void save(Path path, Map<String, Object> manifest) throws Exception {
        Files.createDirectories(path.getParent());
        Files.writeString(path, (String) restaurantPage.evaluate("value => JSON.stringify(value,null,2)", manifest) + "\n");
    }
}
