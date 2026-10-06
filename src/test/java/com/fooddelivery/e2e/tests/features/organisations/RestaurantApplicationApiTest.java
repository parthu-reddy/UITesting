package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
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
        String outletName = submittedName + " Outlet";

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginNewPerson(phone, submittedName, "bp3_" + phone + "@test.com").openOnboarding(Portal.RESTAURANT);
        RestaurantBrandRegistrationPage application = new RestaurantBrandRegistrationPage(restaurantPage);
        application.submitNewApplication(submittedName, phone);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        CustomerHomePage customerHome = new CustomerHomePage(customerPage);
        customerHome.searchRestaurant(outletName);
        assertThat(customerHome.isRestaurantVisible(submittedName))
                .as("An application awaiting review is absent from the customer discovery UI")
                .isFalse();

        loginAsAdminThroughVisibleControls();
        AdminPartnerApprovalsPage approvals = new AdminPartnerApprovalsPage(adminPage);
        approvals.open();
        approvals.restaurants();
        approvals.select(submittedName);
        approvals.assertChecks("GST check", "Passed");
        approvals.assertChecks("Bank check", "Passed");
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
        restaurantPage.navigate(TestConfig.APP_URL + "/business");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByTestId("organisation-card")
                .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText(submittedName + " Team " + phone))).containsText("Approved",
                // Identity projects the decision ~2 s after it is made; the hub re-checks every 10 s while an application is pending.
                new com.microsoft.playwright.assertions.LocatorAssertions.ContainsTextOptions().setTimeout(25_000));

        // Customer search filters the loaded feed. Reload through the browser to observe a fresh
        // public listing after another session approves the application.
        customerPage.reload();
        new CustomerDashboardPage(customerPage).waitForDashboard();
        // Discovery now matches the displayed renamed brand independently of the saved outlet.
        // The retained-fixture search regression also checks the settled count and outlet alias.
        customerHome.searchRestaurant(correctedName);
        customerPage.waitForCondition(() -> customerHome.isRestaurantVisible(correctedName),
                new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(20_000));
        customerHome.openRestaurant(correctedName);
        // This applicant owns one outlet, so the multi-outlet chooser is not part of its UI.
        customerPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName(outletName).setExact(true)).waitFor();
        assertThat(customerPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Back to restaurants").setExact(true)).isVisible())
                .as("The approved restaurant opens its customer storefront through the visible card")
                .isTrue();
    }

    private void loginAsAdminThroughVisibleControls() {
        adminPage.navigate(TestConfig.APP_URL);
        new LoginPage(adminPage).login(testAdminPhone, TestConfig.ADMIN_PROFILE_NAME, TestConfig.ADMIN_PROFILE_EMAIL).openPortal(Portal.ADMIN);
        new AdminPortalPage(adminPage).waitForPortal();
    }
}
