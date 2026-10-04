package com.fooddelivery.e2e.tests.features.organisations;

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
    @Test
    void privateReviewAndDecisions() {
        String restaurantPhone = phone("restaurant.admin", "9999");
        String deliveryPhone = phone("delivery.admin", "7999");
        String restaurantName = applicantName("restaurant-admin", restaurantPhone);
        String deliveryName = applicantName("delivery-admin", deliveryPhone);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).registerAs("Restaurant Partner", restaurantPhone, restaurantName,
                "bp3_" + restaurantPhone + "@test.com");
        RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
        restaurantDashboard.waitForDashboard();
        restaurantDashboard.openSettingsTab();
        RestaurantBrandRegistrationPage restaurantApplication = new RestaurantBrandRegistrationPage(restaurantPage);
        restaurantApplication.submitNewApplication(restaurantName, restaurantPhone);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).registerAs("Delivery Executive", deliveryPhone, deliveryName,
                "bp3_" + deliveryPhone + "@test.com");
        new RiderOnboardingWizardPage(riderPage).completeDevModeOnboarding(deliveryName, "KA" + deliveryPhone);

        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        approvals.open();
        approvals.restaurants();
        assertThat(approvals.row("E2E pending-brand")).hasCount(1);
        approvals.select(restaurantName);
        approvals.assertChecks("GST check", "APPROVED");
        approvals.assertChecks("Bank check", "APPROVED");
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
        new DeliveryDashboardPage(riderPage).waitForDashboard();
    }

    private void loginAsAdminThroughVisibleControls() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone,
                TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        new AdminPortalPage(adminPage).waitForPortal();
    }
}
