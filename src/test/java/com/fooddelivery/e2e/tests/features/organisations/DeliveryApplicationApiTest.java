package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.fooddelivery.e2e.util.BrowserTestData.applicantName;
import static com.fooddelivery.e2e.util.BrowserTestData.phone;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Historical filename retained for the plan; the test is a browser-only O3 flow. */
@Tag("business-platform") @Tag("bp-o3")
public class DeliveryApplicationApiTest extends PartnerApplicationsUiTestBase {
    @Test
    void deliveryApplicationLifecycle() {
        String phone = phone("delivery.lifecycle", "7999");
        String name = applicantName("delivery-lifecycle", phone);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).registerAs("Delivery Executive", phone, name, "bp3_" + phone + "@test.com");
        riderPage.setViewportSize(390, 844);
        new RiderOnboardingWizardPage(riderPage).completeDevModeOnboarding(name, "KA" + phone);

        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        approvals.open();
        approvals.deliveryPartners();
        approvals.select(name);
        approvals.assertChecks("Documents and selfie", "Passed");
        approvals.requestPrivateView("selfie");
        approvals.decide("Approve", null);
        approvals.assertLeftQueue(name);

        riderPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Refresh status").setExact(true)).click();
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        DeliveryOnlineTogglePage duty = new DeliveryOnlineTogglePage(riderPage);
        duty.goOnline();
        assertThat(riderPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Online Duty").setExact(true))).isVisible();
        duty.goOffline();
        assertThat(riderPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Offline").setExact(true))).isVisible();
    }

    private void loginAsAdminThroughVisibleControls() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone,
                TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        new AdminPortalPage(adminPage).waitForPortal();
    }
}
