package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.*;
import org.junit.jupiter.api.*;

/**
 * Tests rider onboarding wizard for new riders.
 */
@Tag("feature")
public class RiderOnboardingTest extends TestBase {

    @Test
    @DisplayName("Login as new rider → check onboarding wizard")
    void checkOnboardingWizard() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();

        dashboard.openSettingsTab();
        riderPage.waitForTimeout(2000);

        RiderSettingsPage settings = new RiderSettingsPage(riderPage);
        if (settings.isOnboardingWizardVisible()) {
            System.out.println("[TEST] Onboarding wizard detected — rider profile incomplete.");
        } else if (settings.isSettingsVisible()) {
            System.out.println("[TEST] Settings visible — rider already onboarded.");
        }
    }
}
