package com.fooddelivery.e2e.tests.features.organisations;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.PartnerApplicationsUiTestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.regex.Pattern;

import static com.fooddelivery.e2e.util.BrowserTestData.applicantName;
import static com.fooddelivery.e2e.util.BrowserTestData.phone;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Read-only regression against the retained, previously approved O3 applicant. */
@Tag("feature-catalog")
@Tag("feature-partner-onboarding")
public class ApprovedRestaurantBrandSearchUiTest extends PartnerApplicationsUiTestBase {
    @Test
    void findsRenamedBrandAndKeepsOutletSearch() {
        String submittedName = applicantName("restaurant-lifecycle", phone("restaurant.search", "9999"));
        String brand = submittedName + " corrected";
        String outlet = submittedName + " Outlet";

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        var search = customerPage.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Search restaurants or cuisines").setExact(true));
        // Prove a loaded, diverse feed first, then the filtered count. A heading that was
        // already visible before the 300 ms debounce cannot prove the query matched.
        var nearbyCount = customerPage.getByText(Pattern.compile("^[0-9]+ nearby$"));
        assertThat(nearbyCount).hasText(Pattern.compile("^(?:[2-9]|[1-9][0-9]+) nearby$"));
        search.fill("  " + brand.toUpperCase(Locale.ROOT) + "  ");
        assertThat(nearbyCount).hasText("1 nearby");
        var brandHeading = customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(brand).setExact(true));
        assertThat(brandHeading).isVisible();
        brandHeading.click();
        assertThat(customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(outlet).setExact(true))).isVisible();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Back to restaurants").setExact(true)).click();
        assertThat(nearbyCount).hasText(Pattern.compile("^(?:[2-9]|[1-9][0-9]+) nearby$"));
        search.fill(outlet);
        assertThat(nearbyCount).hasText("1 nearby");
        assertThat(brandHeading).isVisible();
    }
}
