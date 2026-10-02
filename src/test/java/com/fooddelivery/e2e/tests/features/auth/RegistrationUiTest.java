package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.pages.restaurant.OutletRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End Registration flow tests for Customer, Rider, and Restaurant personas.
 */
@Tag("e2e-registration")
@EnabledIfSystemProperty(named = "registration.enabled", matches = "true")
public class RegistrationUiTest extends TestBase {

    @Test
    @DisplayName("REG-01: Customer Registration Flow")
    void customerRegistrationFlow() {
        com.fooddelivery.e2e.util.FreshCustomerFixture.registerWithAddress(customerPage,
                disposablePhone("customer", "8999"), "Test Registration Home");
    }

    @Test
    @DisplayName("REG-02: Rider Registration Flow")
    void riderRegistrationFlow() {
        riderPage.navigate(TestConfig.APP_URL);
        
        String newRiderPhone = disposablePhone("rider", "7999");
        
        LoginPage loginPage = new LoginPage(riderPage);
        loginPage.registerAs("Delivery Executive", newRiderPhone, "E2E Test Rider", "rider_" + newRiderPhone + "@test.com");
        
        // For riders, first login should lead to the onboarding wizard
        RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
        assertThat(wizard.isWizardVisible()).isTrue();
        
        // Complete the dev mode onboarding which includes clicking through the auto-approved
        // sections and uploading a selfie at the end.
        var selfie = new java.util.concurrent.atomic.AtomicReference<com.microsoft.playwright.Response>();
        var refreshedStatus = riderPage.waitForResponse(response -> {
            String path = java.net.URI.create(response.url()).getPath();
            if ("/api/delivery/verification/biometric".equals(path) && response.request().method().equals("POST")) {
                selfie.set(response);
            }
            return selfie.get() != null && "/api/delivery/verification/status".equals(path)
                    && response.request().method().equals("GET");
        }, wizard::completeDevModeOnboarding);
        assertThat(selfie.get().status()).isEqualTo(200);
        assertThat(riderPage.evaluate("body => JSON.parse(body).success", selfie.get().text())).isEqualTo(true);
        assertThat(refreshedStatus.status()).isEqualTo(200);
        assertThat(riderPage.evaluate("body => JSON.parse(body).data.biometricStatus", refreshedStatus.text()))
                .as("The submitted selfie must be persisted for the current rider")
                .isEqualTo("VERIFIED");
        
        // Post-onboarding, should reach the dashboard.
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        
        // Registration ends at verified onboarding. Dispatch/duty checks need their own preflight.
        assertThat(riderPage.url()).contains("/delivery");
    }

    @Test
    @DisplayName("REG-03: Restaurant Registration Flow")
    void restaurantRegistrationFlow() {
        restaurantPage.navigate(TestConfig.APP_URL);
        
        String newRestPhone = disposablePhone("restaurant", "9999");
        
        LoginPage loginPage = new LoginPage(restaurantPage);
        loginPage.registerAs("Restaurant Partner", newRestPhone, "E2E Test Restaurant Owner", "restaurant_" + newRestPhone + "@test.com");
        
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        dashboard.openSettingsTab();
        
        // The first login for a restaurant should lead to the brand registration screen
        RestaurantBrandRegistrationPage brandPage = new RestaurantBrandRegistrationPage(restaurantPage);
        assertThat(brandPage.isRegistrationVisible()).isTrue();
        
        String randomBrandName = "E2E Brand " + (System.currentTimeMillis() % 1000000);
        brandPage.fillBrandName(randomBrandName);
        brandPage.submit();
        
        // Next, the outlet registration screen should appear
        OutletRegistrationPage outletPage = new OutletRegistrationPage(restaurantPage);
        assertThat(outletPage.isRegistrationVisible()).isTrue();
        
        outletPage.fillOutletName("E2E Registration " + newRestPhone);
        outletPage.searchAndSelectLocation("Keerthi Rendezvous");
        String randomFssai = String.format("1234%010d", (System.currentTimeMillis() % 10000000000L));
        outletPage.fillFssai(randomFssai);
        outletPage.submit();
        
        // Navigate back to home dashboard by toggling settings off
        dashboard.openSettingsTab();
        dashboard.openOrdersTab();
        dashboard.waitForDashboard();
        
        // The orders board: its "Orders" heading (RestaurantOrderQueue.tsx).
        restaurantPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Orders").setExact(true))
                .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000));
    }

    private String disposablePhone(String persona, String prefix) {
        assertThat(System.getProperty("registration.preflight"))
                .as("Use run_registration_e2e.py to allocate unused accounts; created data is retained")
                .isEqualTo("true");
        String phone = System.getProperty("registration." + persona + ".phone");
        assertThat(phone).matches(prefix + "[0-9]{6}");
        return phone;
    }



}
