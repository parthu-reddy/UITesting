package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.*;
import org.junit.jupiter.api.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests rider onboarding wizard for new riders.
 */
@Tag("feature")
public class RiderOnboardingTest extends TestBase {

    @Test
    @DisplayName("Login as new rider → check onboarding wizard")
    void checkOnboardingWizard() {
        riderPage.navigate(TestConfig.APP_URL);
        if (Boolean.getBoolean("scenario.rider.enabled")) {
            assertThat(testRiderPhone).isIn("7000000031", "7000000032", "7000000034");
            var verification = riderPage.waitForResponse(
                    r -> java.net.URI.create(r.url()).getPath().equals("/api/delivery/verification/status")
                            && r.request().method().equals("GET"),
                    () -> new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone));
            assertThat(verification.status()).isEqualTo(200);
            assertThat(riderPage.evaluate("body => JSON.parse(body).success", verification.text())).isEqualTo(true);
            boolean inactive = testRiderPhone.endsWith("34");
            assertThat(riderPage.evaluate("body => JSON.parse(body).data.fullyVerified", verification.text())).isEqualTo(inactive);
            assertThat(riderPage.evaluate("body => JSON.parse(body).data.bankStatus", verification.text()))
                    .isEqualTo(inactive ? "VERIFIED" : "PENDING");
            if (inactive) {
                new DeliveryDashboardPage(riderPage).waitForDashboard();
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        riderPage.getByRole(AriaRole.BUTTON,
                                new Page.GetByRoleOptions().setName("Offline").setExact(true)))
                        .hasAttribute("aria-pressed", "false");
            } else {
                assertThat(new RiderOnboardingWizardPage(riderPage).isWizardVisible()).isTrue();
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        riderPage.getByRole(AriaRole.BUTTON,
                                new Page.GetByRoleOptions().setName("Offline").setExact(true))).isHidden();
            }
            // No onboarding submissions, activation or duty toggles. Verify inactive/state
            // preservation independently in the database after the browser run.
        } else {
            new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
            DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
            dashboard.waitForDashboard();
            dashboard.openSettingsTab();
            RiderSettingsPage settings = new RiderSettingsPage(riderPage);
            assertThat(settings.isOnboardingWizardVisible() || settings.isSettingsVisible()).isTrue();
        }
    }

}
