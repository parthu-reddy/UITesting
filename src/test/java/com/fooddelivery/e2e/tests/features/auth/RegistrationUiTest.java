package com.fooddelivery.e2e.tests.features.auth;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerAddressModalPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.pages.restaurant.OutletRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantBrandRegistrationPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.AfterEach;
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
        customerPage.navigate(TestConfig.APP_URL);
        
        // Use a new unique phone number to trigger the registration flow
        String newCustomerPhone = disposablePhone("customer", "8999");
        
        LoginPage loginPage = new LoginPage(customerPage);
        loginPage.registerAs("Order Food", newCustomerPhone, "E2E Test Customer", "customer_" + newCustomerPhone + "@test.com");
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        // New users have no seeded Home. Start from their automatic location selector.
        customerPage.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Select Delivery Location").setExact(true)).waitFor();
        customerPage.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Add New Address")).click();
        
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();
        
        modal.searchAndSelectLocation("Keerthi Rendezvous");
        modal.fillAddressLabel("Test Registration Home");
        modal.fillAddressLine("Keerthi Rendezvous, E2E registration address");
        modal.fillCity("Bangalore");
        modal.fillState("Karnataka");
        modal.fillZipCode("560001");
        
        modal.saveAddress();
        
        // Wait for the modal to close and the address to be auto-selected
        customerPage.waitForTimeout(2000);
        
        dashboard.waitForDashboard();
        
        // The header should now display the label of our newly created and selected address
        customerPage.locator("header >> text=Test Registration Home").waitFor();
        assertThat(customerPage.locator("header >> text=Test Registration Home").isVisible()).isTrue();
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
        wizard.completeDevModeOnboarding();
        
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
                .as("Use run_registration_e2e.py to allocate unused accounts and retire them afterwards")
                .isEqualTo("true");
        String phone = System.getProperty("registration." + persona + ".phone");
        assertThat(phone).matches(prefix + "[0-9]{6}");
        return phone;
    }

    @AfterEach
    void logOutDisposableSessions() {
        for (var page : new com.microsoft.playwright.Page[] {customerPage, riderPage, restaurantPage}) {
            if (page == null || page.isClosed() || !page.url().startsWith(TestConfig.APP_URL)) continue;
            Object status = page.evaluate("""
                    async () => {
                        const token = localStorage.getItem('auth_token');
                        if (!token) return 200;
                        const response = await fetch('/api/v1/internal/auth/logout', {
                            method: 'POST', headers: {Authorization: `Bearer ${token}`}, credentials: 'omit'
                        });
                        if (response.ok) localStorage.removeItem('auth_token');
                        return response.status;
                    }
                    """);
            assertThat(((Number) status).intValue()).isEqualTo(200);
        }
    }

}
