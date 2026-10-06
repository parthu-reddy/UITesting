package com.fooddelivery.e2e.tests.features.organisations;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
@Tag("business-platform") @Tag("bp-o4") @Tag("ui-only")
public class AdminStepUpTest extends TestBase {
    @Test void administratorVerifiesSeparatelyAndKeepsEverydayAccess() {
        new LoginPage(adminPage).login(testAdminPhone).openPortal(Portal.CUSTOMER);
        PortalLauncherPage launcher = new PortalLauncherPage(adminPage); launcher.open(); launcher.state(Portal.ADMIN, "Verification required"); launcher.choose(Portal.ADMIN);
        assertThat(adminPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Administrator verification").setExact(true))).isVisible();
        new LoginPage(adminPage).stepUpAdmin(); launcher.open(); launcher.choose(Portal.CUSTOMER);
        assertThat(adminPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Switch portal").setExact(true))).isVisible();
        assertThat(adminPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Administrator verification").setExact(true))).isHidden();
    }
    @Test void nonStaffCannotStartAdministratorVerification() {
        new LoginPage(customerPage).login(testCustomerPhone); customerPage.navigate(TestConfig.APP_URL + "/admin");
        var response = customerPage.waitForResponse(r -> com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/auth/admin-session/otp")
                && r.request().method().equals("POST"), () -> customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Send administrator code").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(403);
        assertThat(customerPage.getByRole(AriaRole.ALERT)).isVisible();
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back to portals").setExact(true)).click();
        assertThat(customerPage.getByTestId("portal-customer")).containsText("Available");
    }
}
