package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.fooddelivery.e2e.util.BrowserTestData.applicantName;
import static com.fooddelivery.e2e.util.BrowserTestData.phone;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * O3 browser gate. The historical filename remains for plan compatibility, but this class uses
 * only visible applicant, customer, and admin controls.
 */
@Tag("business-platform") @Tag("bp-o3")
public class RestaurantApplicationApiTest extends PartnerApplicationsUiTestBase {
    @Test
    void restaurantApplicationLifecycle() {
        String phone = phone("restaurant.lifecycle", "9999");
        String submittedName = applicantName("restaurant-lifecycle", phone);
        String correctedName = submittedName + " corrected";

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).registerAs("Restaurant Partner", phone, submittedName, "bp3_" + phone + "@test.com");
        RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
        restaurantDashboard.waitForDashboard();
        restaurantDashboard.openSettingsTab();
        RestaurantBrandRegistrationPage application = new RestaurantBrandRegistrationPage(restaurantPage);
        application.submitNewApplication(submittedName, phone);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        CustomerHomePage customerHome = new CustomerHomePage(customerPage);
        customerHome.searchRestaurant(submittedName);
        assertThat(customerHome.isRestaurantVisible(submittedName))
                .as("An application awaiting review is absent from the customer discovery UI")
                .isFalse();

        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        approvals.open();
        approvals.restaurants();
        approvals.select(submittedName);
        approvals.assertChecks("GST check", "APPROVED");
        approvals.assertChecks("Bank check", "APPROVED");
        approvals.rejectWithoutReason();
        String reason = "E2E review requires a clearer document";
        approvals.decide("Reject", reason);
        approvals.assertLeftQueue(submittedName);

        application.refreshAndAssertStatus("Changes requested");
        application.assertReviewReason(reason);
        application.reviseBrandNameAndResubmit(correctedName);

        approvals.refreshApplications();
        approvals.select(correctedName);
        approvals.decide("Approve", null);
        approvals.assertLeftQueue(correctedName);
        application.refreshAndAssertStatus("Approved");

        customerHome.searchRestaurant(correctedName);
        customerPage.waitForCondition(() -> customerHome.isRestaurantVisible(correctedName),
                new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(20_000));
        new NearbyOutletPage(customerPage).openBrandAndSelectNearby(correctedName);
    }

    private void loginAsAdminThroughVisibleControls() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).loginAs("System Admin", testAdminPhone,
                TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL);
        new AdminPortalPage(adminPage).waitForPortal();
    }
}
