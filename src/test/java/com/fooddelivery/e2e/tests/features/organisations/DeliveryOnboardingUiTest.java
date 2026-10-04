package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.fooddelivery.e2e.util.BusinessPlatformFixture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("business-platform") @Tag("bp-o5") @Tag("ui-only")
public class DeliveryOnboardingUiTest extends TestBase {
    @Test void approvedRiderEntersDeliveryOfflineWithoutAnotherPersonLogin() {
        String phone = BusinessPlatformFixture.phone("delivery", "7999"), name = "E2E O45 Rider " + phone;
        BusinessPlatformFixture.fresh(riderPage, phone, name).openOnboarding(Portal.DELIVERY);
        DeliveryOnboardingPage onboarding = new DeliveryOnboardingPage(riderPage); onboarding.submit(name, phone);
        BusinessPlatformFixture.approve(adminPage, testAdminPhone, name, true); onboarding.refresh("Approved");
        PortalLauncherPage launcher = new PortalLauncherPage(riderPage); launcher.open(); launcher.state(Portal.DELIVERY, "Available"); launcher.choose(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        assertThat(riderPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Offline").setExact(true))).isVisible();
    }
}
