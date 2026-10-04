package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.util.BusinessPlatformFixture;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("business-platform") @Tag("bp-o4") @Tag("ui-only")
public class OneLoginEntitlementsTest extends TestBase {
    @Test void ownerUsesCustomerRestaurantAndBusinessAfterOneOtp() {
        AtomicInteger sessions = new AtomicInteger(); customerPage.onRequest(request -> {
            if (request.method().equals("POST") && java.net.URI.create(request.url()).getPath().equals("/api/v1/auth/session")) sessions.incrementAndGet();
        });
        new LoginPage(customerPage).login("9000000001").openPortal(Portal.CUSTOMER);
        PortalLauncherPage launcher = new PortalLauncherPage(customerPage); launcher.open(); launcher.state(Portal.CUSTOMER, "Available");
        launcher.state(Portal.RESTAURANT, "Available"); launcher.state(Portal.BUSINESS, "Available"); launcher.choose(Portal.RESTAURANT);
        new RestaurantDashboardPage(customerPage).waitForDashboard(); launcher.open(); launcher.choose(Portal.BUSINESS);
        assertThat(customerPage.getByTestId("organisation-role")).containsText("OWNER");
        org.assertj.core.api.Assertions.assertThat(sessions.get()).isEqualTo(1);
    }
    @Test void approvedRiderUsesCustomerAndDeliveryButCannotOpenRestaurantOperations() {
        new LoginPage(riderPage).login("7000000001").openPortal(Portal.CUSTOMER);
        PortalLauncherPage launcher = new PortalLauncherPage(riderPage); launcher.open(); launcher.state(Portal.DELIVERY, "Available");
        launcher.state(Portal.RESTAURANT, "Get started"); launcher.choose(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        riderPage.navigate(TestConfig.APP_URL + "/restaurant");
        assertThat(riderPage.getByTestId("portal-choices")).isVisible();
    }
    @Test void freshPersonHasCustomerAndApplicationEntryPoints() {
        String phone = BusinessPlatformFixture.phone("fresh.customer", "8999");
        BusinessPlatformFixture.fresh(customerPage, phone, "E2E O45 Customer");
        PortalLauncherPage launcher = new PortalLauncherPage(customerPage); launcher.open(); launcher.state(Portal.CUSTOMER, "Available");
        launcher.state(Portal.RESTAURANT, "Get started"); launcher.state(Portal.DELIVERY, "Get started");
        launcher.state(Portal.ADMIN, "Unavailable");
        assertThat(launcher.tile(Portal.ADMIN).getByRole(AriaRole.BUTTON)).hasCount(0); launcher.choose(Portal.CUSTOMER);
        assertThat(customerPage.getByRole(AriaRole.HEADING, new com.microsoft.playwright.Page.GetByRoleOptions().setName("Select Delivery Location").setExact(true))).isVisible();
    }
    @Test void pendingRiderSeesApplicationWithoutOperationalAccess() {
        new LoginPage(riderPage).login("7000000031"); PortalLauncherPage launcher = new PortalLauncherPage(riderPage);
        launcher.open(); launcher.state(Portal.DELIVERY, "In review");
        launcher.tile(Portal.DELIVERY).getByRole(AriaRole.BUTTON).click();
        assertThat(riderPage.getByTestId("delivery-application")).isVisible();
        assertThat(riderPage.getByText("Awaiting admin review", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByRole(AriaRole.BUTTON, new com.microsoft.playwright.Page.GetByRoleOptions().setName("Offline").setExact(true))).isHidden();
    }
}
