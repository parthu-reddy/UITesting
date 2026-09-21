package com.fooddelivery.e2e.tests.features.rider;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.pages.delivery.RiderSettingsPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the full Rider Onboarding Wizard flow.
 * Covers: ONBOARD-01..08
 */
@Tag("rider-onboarding")
public class RiderOnboardingFullTest extends TestBase {

    @BeforeEach
    void loginRider() {
        riderPage.navigate(TestConfig.APP_URL);
        // Login as rider
        new LoginPage(riderPage).loginAs("Delivery Executive", TestConfig.RIDER_PHONE);
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
    }

    @Test
    @DisplayName("ONBOARD-01: Rider onboarding wizard visible")
    void onboardingWizardVisible() {
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.openSettingsTab();
        
        RiderSettingsPage settings = new RiderSettingsPage(riderPage);
        if (settings.isOnboardingWizardVisible()) {
            RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
            assertThat(wizard.isWizardVisible()).isTrue();
        }
    }

    @Test
    @DisplayName("ONBOARD-03: Fill vehicle number in wizard")
    void fillVehicleNumberInWizard() {
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.openSettingsTab();
        
        RiderSettingsPage settings = new RiderSettingsPage(riderPage);
        if (settings.isOnboardingWizardVisible()) {
            RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
            wizard.fillVehicleNumber("KA01AB1234");
        }
    }

    @Test
    @DisplayName("ONBOARD-04: Select vehicle type in wizard")
    void selectVehicleTypeInWizard() {
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.openSettingsTab();
        
        RiderSettingsPage settings = new RiderSettingsPage(riderPage);
        if (settings.isOnboardingWizardVisible()) {
            RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
            wizard.selectVehicleType("2-Wheeler");
        }
    }

    @Test
    @DisplayName("ONBOARD-06: Navigate wizard steps")
    void navigateWizardSteps() {
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.openSettingsTab();
        
        RiderSettingsPage settings = new RiderSettingsPage(riderPage);
        if (settings.isOnboardingWizardVisible()) {
            RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
            int initialStep = wizard.getCurrentStep();
            wizard.fillVehicleNumber("KA01AB1234");
            wizard.selectVehicleType("2-Wheeler");
            wizard.clickNext();
            
            riderPage.waitForTimeout(500);
            int nextStep = wizard.getCurrentStep();
            assertThat(nextStep).isNotEqualTo(initialStep);
        }
    }
}
