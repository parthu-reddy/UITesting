package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.concurrent.atomic.AtomicInteger;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Non-staff cannot pass the rendered admin step-up. Direct endpoint probes are deferred. */
@Tag("feature-admin-ops")
@Tag("feature-auth")
public class AdminAuthorizationLiveE2ETest extends TestBase {
    @ParameterizedTest(name="{0}: admin step-up denies non-staff") @ValueSource(strings={"CUSTOMER","RESTAURANT"})
    void nonStaffCannotPassAdministratorStepUp(String selected) {
        Page page=selected.equals("CUSTOMER")?customerPage:restaurantPage;
        String phone=selected.equals("CUSTOMER")?testCustomerPhone:testRestaurantPhone;
        new LoginPage(page).login(phone).openPortal(Portal.valueOf(selected));
        AtomicInteger productRequests=new AtomicInteger();
        page.onRequest(request -> { if(request.url().contains("/api/v1/admin/")||request.url().contains("/api/v1/internal/admin/")) productRequests.incrementAndGet(); });
        page.navigate(TestConfig.APP_URL.replaceAll("/$","")+"/admin/users");
        var dialog=page.getByRole(AriaRole.DIALOG,new Page.GetByRoleOptions().setName("Administrator verification").setExact(true));
        assertThat(dialog).isVisible();
        var response=page.waitForResponse(r -> r.url().endsWith("/api/v1/auth/admin-session/otp")&&r.request().method().equals("POST"),
            () -> dialog.getByRole(AriaRole.BUTTON,new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Send administrator code").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(403);
        assertThat(dialog.getByRole(AriaRole.ALERT)).isVisible();
        assertThat(page.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Admin").setExact(true))).hasCount(0);
        org.assertj.core.api.Assertions.assertThat(productRequests.get()).isZero();
        dialog.getByRole(AriaRole.BUTTON,new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Back to portals").setExact(true)).click();
        assertThat(page.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Your portals").setExact(true))).isVisible();
    }
}
