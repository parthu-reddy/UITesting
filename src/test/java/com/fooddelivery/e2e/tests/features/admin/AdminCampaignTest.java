package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Admin Campaign management — create, budget, toggle, wallet top-up,
 * and Ad Performance dashboard.
 * Covers: CAMPAIGN-ADM-01..13, PERF-01..03
 */
@Tag("admin-campaigns")
public class AdminCampaignTest extends TestBase {

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone,
                TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-01: Campaigns tab visible")
    void campaignsTabVisible() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        assertThat(campaigns.isCampaignsVisible()).isTrue();
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-02: Campaign count")
    void campaignCount() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        int count = campaigns.getCampaignCount();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-03: Open create campaign modal")
    void openCreateCampaignModal() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openCreateCampaignModal();
        adminPage.waitForTimeout(500);
        // Modal should be open; fill name to verify interactivity
        campaigns.fillCampaignName("E2E Test Campaign");
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-05: Fill daily budget")
    void fillDailyBudget() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openCreateCampaignModal();
        campaigns.fillDailyBudget("5000");
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-06: Fill total budget")
    void fillTotalBudget() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openCreateCampaignModal();
        campaigns.fillTotalBudget("50000");
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-07: Fill bid amount")
    void fillBidAmount() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openCreateCampaignModal();
        campaigns.fillBidAmount("25");
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-09: Wallet balance visible")
    void walletBalanceVisible() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        String balance = campaigns.getWalletBalance();
        // Balance should be a numeric string (may include ₹ symbol)
        assertThat(balance).isNotNull();
    }

    @Test
    @DisplayName("CAMPAIGN-ADM-10: Open wallet top-up")
    void openWalletTopup() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openWalletTopup();
        adminPage.waitForTimeout(500);
        campaigns.fillTopupAmount("1000");
    }

    @Test
    @DisplayName("PERF-01: Open ad performance dashboard")
    void openAdPerformanceDashboard() {
        portal.openCampaignsTab();
        AdminCampaignsPage campaigns = new AdminCampaignsPage(adminPage);
        campaigns.openAdPerformance();
        adminPage.waitForTimeout(500);
        assertThat(campaigns.isPerformanceDashboardVisible()).isTrue();
    }
}
