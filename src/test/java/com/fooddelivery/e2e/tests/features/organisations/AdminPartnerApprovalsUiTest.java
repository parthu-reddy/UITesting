package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.fooddelivery.e2e.util.BrowserTestData.applicantName;
import static com.fooddelivery.e2e.util.BrowserTestData.phone;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Admin review is driven entirely by the rendered partner-approvals interface. */
@Tag("business-platform") @Tag("bp-o3")
public class AdminPartnerApprovalsUiTest extends PartnerApplicationsUiTestBase {
    /** Read-only queue navigation remains testable while private storage setup is unavailable. */
    @Test
    void reviewQueuesNavigation() {
        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        observeQueue("restaurant", approvals::open);
        assertThat(approvals.row("E2E pending-brand")).hasCount(1);
        observeQueue("restaurant", approvals::refreshApplications);
        assertThat(approvals.row("E2E pending-brand")).hasCount(1);
        var status = adminPage.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Application status").setExact(true));
        status.click();
        observeQueue("restaurant", () -> adminPage.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Changes requested").setExact(true)).click());
        assertThat(status).hasAttribute("aria-expanded", "false");
        assertThat(approvals.row("E2E rejected-brand")).hasCount(1);
        assertThat(approvals.row("E2E pending-brand")).hasCount(0);
        status.click();
        observeQueue("restaurant", () -> adminPage.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Awaiting admin review").setExact(true)).click());
        assertThat(status).hasAttribute("aria-expanded", "false");
        assertThat(approvals.row("E2E pending-brand")).hasCount(1);
        observeQueue("delivery", approvals::deliveryPartners);
        assertThat(approvals.row("E2E pending-kyc")).hasCount(1);
        observeQueue("delivery", approvals::refreshApplications);
        assertThat(approvals.row("E2E pending-kyc")).hasCount(1);
    }

    private void observeQueue(String type, Runnable action) {
        var response = adminPage.waitForResponse(candidate ->
                java.net.URI.create(candidate.url()).getPath().equals(
                        "/api/v1/internal/admin/" + type + "-applications")
                        && candidate.request().method().equals("GET"), action);
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("Queue response caused by the visible admin control").isEqualTo(200);
    }

    @Test
    void privateReviewAndDecisions() {
        String restaurantPhone = phone("restaurant.admin", "9999");
        String deliveryPhone = phone("delivery.admin", "7999");
        String restaurantName = applicantName("restaurant-admin", restaurantPhone);
        String deliveryName = applicantName("delivery-admin", deliveryPhone);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginNewPerson(restaurantPhone, restaurantName, "bp3_" + restaurantPhone + "@test.com").openOnboarding(Portal.RESTAURANT);
        RestaurantBrandRegistrationPage restaurantApplication = new RestaurantBrandRegistrationPage(restaurantPage);
        restaurantApplication.submitNewApplication(restaurantName, restaurantPhone);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginNewPerson(deliveryPhone, deliveryName, "bp3_" + deliveryPhone + "@test.com").openOnboarding(Portal.DELIVERY);
        new RiderOnboardingWizardPage(riderPage).completeDevModeOnboarding(deliveryName, RiderOnboardingWizardPage.plateFor(deliveryPhone));

        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        approvals.open();
        approvals.restaurants();
        assertThat(approvals.row("E2E pending-brand")).hasCount(1);
        approvals.select(restaurantName);
        approvals.assertChecks("GST check", "Passed");
        approvals.assertChecks("Bank check", "Passed");
        approvals.requestPrivateView("gstin");
        approvals.rejectWithoutReason();
        String reason = "E2E review requires a clearer document";
        approvals.decide("Reject", reason);
        approvals.assertLeftQueue(restaurantName);
        restaurantApplication.refreshAndAssertStatus("Changes requested");
        restaurantApplication.assertReviewReason(reason);

        approvals.deliveryPartners();
        approvals.select(deliveryName);
        approvals.assertChecks("Documents and selfie", "Passed");
        approvals.requestPrivateView("selfie");
        approvals.decide("Approve", null);
        approvals.assertLeftQueue(deliveryName);
        riderPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Refresh status").setExact(true)).click();
        com.fooddelivery.e2e.pages.common.PortalLauncherPage launcher = new com.fooddelivery.e2e.pages.common.PortalLauncherPage(riderPage);
        launcher.open(); launcher.state(Portal.DELIVERY, "Available"); launcher.choose(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
    }

    private void loginAsAdminThroughVisibleControls() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
    }
}
