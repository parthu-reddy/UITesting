package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.pages.delivery.RiderSettingsPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests the rider application state that is rendered after normal browser login. */
@Tag("feature")
@Tag("ui-only")
public class RiderOnboardingTest extends TestBase {

    @Test
    void checkOnboardingWizard() {
        riderPage.navigate(TestConfig.APP_URL);

        if (Boolean.getBoolean("scenario.rider.enabled")) {
            assertThat(testRiderPhone).isIn("7000000031", "7000000032", "7000000034");
            new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);

            RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
            assertThat(wizard.isWizardVisible()).isTrue();

            String statusLabel = switch (testRiderPhone) {
                case "7000000031" -> "Awaiting admin review";
                case "7000000032" -> "Changes requested";
                case "7000000034" -> "Suspended";
                default -> throw new AssertionError("Unexpected scenario rider phone");
            };
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                    riderPage.getByText(statusLabel, new Page.GetByTextOptions().setExact(true))).isVisible();
            if (!testRiderPhone.endsWith("31")) {
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(riderPage.getByText(
                        "Seeded application requires corrected documents.",
                        new Page.GetByTextOptions().setExact(true))).isVisible();
            }
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(riderPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Offline").setExact(true))).isHidden();
        } else {
            new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
            DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
            dashboard.waitForDashboard();
            dashboard.openSettingsTab();

            RiderSettingsPage settings = new RiderSettingsPage(riderPage);
            assertThat(settings.isSettingsVisible() || settings.isOnboardingWizardVisible()).isTrue();
        }
    }
}
