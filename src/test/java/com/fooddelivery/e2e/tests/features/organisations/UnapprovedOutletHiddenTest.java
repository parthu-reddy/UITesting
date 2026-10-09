package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Visible discovery proof for seeded pending and rejected restaurant applications. */
@Tag("feature-catalog")
@Tag("feature-partner-onboarding")
public class UnapprovedOutletHiddenTest extends PartnerApplicationsUiTestBase {
    @Test
    void pendingAndRejectedOutletsAreNotDiscoverableThroughTheCustomerUI() {
        assertSeededRestaurantStatus("9000000012", "Awaiting admin review");
        assertSeededRestaurantStatus("9000000013", "Changes requested");

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        CustomerHomePage customerHome = new CustomerHomePage(customerPage);
        for (String hiddenBrand : new String[]{"E2E pending-brand", "E2E rejected-brand"}) {
            customerHome.searchRestaurant(hiddenBrand);
            assertThat(customerHome.isRestaurantVisible(hiddenBrand))
                    .as("%s must not be rendered in customer discovery before approval", hiddenBrand)
                    .isFalse();
        }
    }

    private void assertSeededRestaurantStatus(String phone, String expectedStatus) {
        // Each account signs in through the UI in an empty session; retain server sessions/data.
        restaurantContext.close();
        restaurantContext = browser.newContext(new Browser.NewContextOptions()
                .setGeolocation(TestConfig.GEO_LAT, TestConfig.GEO_LNG)
                .setPermissions(java.util.List.of("geolocation")));
        restaurantPage = restaurantContext.newPage();
        restaurantPage.setDefaultTimeout(TestConfig.DEFAULT_TIMEOUT);
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(phone);
        restaurantPage.navigate(TestConfig.APP_URL + "/portals");
        restaurantPage.getByTestId("portal-restaurant").getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Restaurant").setExact(true)).click();
        new RestaurantBrandRegistrationPage(restaurantPage).assertStatus(expectedStatus);
    }
}
