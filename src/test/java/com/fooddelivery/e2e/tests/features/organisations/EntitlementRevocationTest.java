package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.business.BusinessHubPage;
import com.fooddelivery.e2e.pages.admin.AdminPartnerApprovalsPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.util.BusinessPlatformFixture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("ui-only")
@Tag("feature-organisations")
public class EntitlementRevocationTest extends TestBase {
    @Test void acceptedStaffApprovalAndRemovalRefreshOnNormalUiRequests() {
        String phone = BusinessPlatformFixture.phone("entitlement.member", "8999");
        String applicant = BusinessPlatformFixture.phone("entitlement.owner", "9999");
        String brand = BusinessPlatformFixture.approvedRestaurant(restaurantPage, adminPage, applicant, testAdminPhone);
        String name = brand + " Team " + applicant;
        restaurantPage.navigate(TestConfig.APP_URL + "/business");
        BusinessHubPage owner = new BusinessHubPage(restaurantPage); owner.open(name); owner.members(); owner.invite(phone, "STAFF");
        BusinessPlatformFixture.fresh(customerPage, phone, "E2E O45 Revoked Staff").openPortal(Portal.BUSINESS);
        BusinessHubPage member = new BusinessHubPage(customerPage); member.accept(name);
        PortalLauncherPage launcher = new PortalLauncherPage(customerPage); launcher.open(); launcher.state(Portal.RESTAURANT, "Available"); launcher.choose(Portal.RESTAURANT);
        new RestaurantDashboardPage(customerPage).waitForDashboard();
        assertThat(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Restaurant management and menu settings").setExact(true))).isHidden();
        // The colleague accepted in a separate browser. Reopen the rendered organisation
        // so the owner reads its current members instead of the pre-acceptance snapshot.
        owner.allOrganisations(); owner.open(name); owner.members();
        assertThat(owner.member(phone)).isVisible();
        owner.remove(phone); AtomicInteger refreshes = new AtomicInteger(); customerPage.onRequest(request -> {
            if (request.method().equals("POST") && com.fooddelivery.e2e.util.UrlPaths.path(request.url()).equals("/api/v1/auth/session/refresh")) refreshes.incrementAndGet();
        });
        // The removed person's next ordinary request (here: opening the launcher) renews the token
        // without RESTAURANT, and the app moves them off the Restaurant dashboard to the portal
        // chooser at once. That navigation replaces the launcher dialog, so assert on the chooser page.
        if (!customerPage.url().contains("/portals")) {
            customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Switch portal").setExact(true)).click();
        }
        customerPage.waitForURL(java.util.regex.Pattern.compile(".*/portals(?:[?#].*)?$"));
        assertThat(customerPage.getByTestId("portal-restaurant")).containsText("Get started");
        org.assertj.core.api.Assertions.assertThat(refreshes.get()).isLessThanOrEqualTo(1);
        customerPage.navigate(TestConfig.APP_URL + "/restaurant"); assertThat(customerPage.getByTestId("portal-choices")).isVisible();
    }
    @Tag("slow")
    @Tag("feature-catalog")
    @Tag("feature-partner-onboarding")
    @Test void ownBrandSuspensionAndReinstatementChangeAccessAndDiscovery() {
        String phone = BusinessPlatformFixture.phone("suspension", "9999");
        String brand = BusinessPlatformFixture.approvedRestaurant(restaurantPage, adminPage, phone, testAdminPhone);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage); dashboard.waitForDashboard();
        CustomerHomePage home = new CustomerHomePage(customerPage); home.waitForRestaurantFeed(); home.searchRestaurant(brand);
        customerPage.waitForCondition(() -> home.isRestaurantVisible(brand));
        AdminPartnerApprovalsPage admin = new AdminPartnerApprovalsPage(adminPage); admin.open(); admin.restaurants();
        status("Approved"); admin.select(brand); admin.decide("Suspend", "E2E O45 review of owned dummy brand");
        PortalLauncherPage launcher = new PortalLauncherPage(restaurantPage); launcher.open(); launcher.awaitChangedState(Portal.RESTAURANT, "Suspended");
        assertThat(launcher.tile(Portal.RESTAURANT).getByRole(AriaRole.BUTTON)).hasCount(0); launcher.close();
        customerPage.reload(); dashboard.waitForDashboard(); home.waitForRestaurantFeed(); home.searchRestaurant(brand);
        org.assertj.core.api.Assertions.assertThat(home.isRestaurantVisible(brand)).isFalse();
        status("Suspended"); admin.select(brand); admin.decide("Reinstate", null);
        launcher.open(); launcher.awaitChangedState(Portal.RESTAURANT, "Available"); launcher.choose(Portal.RESTAURANT);
        customerPage.reload(); dashboard.waitForDashboard(); home.waitForRestaurantFeed(); home.searchRestaurant(brand); customerPage.waitForCondition(() -> home.isRestaurantVisible(brand));
    }    private void status(String value) {
        adminPage.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Application status").setExact(true)).click();
        adminPage.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(value).setExact(true)).click();
    }

}
