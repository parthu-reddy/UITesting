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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End Registration flow tests for Customer, Rider, and Restaurant personas.
 */
@Tag("e2e-registration")
@Disabled("Creates permanent accounts, addresses, brands, and outlets; requires authorized disposable test data")
public class RegistrationUiTest extends TestBase {

    @Test
    @DisplayName("REG-01: Customer Registration Flow")
    void customerRegistrationFlow() {
        customerPage.onResponse(response -> {
            if (!response.ok()) {
                System.out.println("API FAILED: " + response.url() + " " + response.status() + " " + response.statusText());
                try {
                    System.out.println("API RESPONSE BODY: " + new String(response.body()));
                } catch (Exception e) {}
            }
        });
        customerPage.onConsoleMessage(msg -> {
            System.out.println("BROWSER CONSOLE: " + msg.type() + " " + msg.text());
        });
        customerPage.navigate(TestConfig.APP_URL);
        
        // Use a new unique phone number to trigger the registration flow
        String newCustomerPhone = "8999" + (int)(Math.random() * 900000 + 100000);
        
        LoginPage loginPage = new LoginPage(customerPage);
        loginPage.loginAs("Order Food", newCustomerPhone, "E2E Test Customer", "customer_" + newCustomerPhone + "@test.com");
        
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
        dashboard.clickDeliverTo();
        customerPage.locator("text=Add New Address").first().click();
        
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();
        
        modal.searchAndSelectLocation("Keerthi Rendezvous");
        modal.fillAddressLabel("Test Registration Home");
        
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
        
        String newRiderPhone = "7999" + (int)(Math.random() * 900000 + 100000);
        
        LoginPage loginPage = new LoginPage(riderPage);
        loginPage.loginAs("Delivery Executive", newRiderPhone, "E2E Test Rider", "rider_" + newRiderPhone + "@test.com");
        
        // For riders, first login should lead to the onboarding wizard
        RiderOnboardingWizardPage wizard = new RiderOnboardingWizardPage(riderPage);
        assertThat(wizard.isWizardVisible()).isTrue();
        
        // Complete the dev mode onboarding which includes clicking through the auto-approved
        // sections and uploading a selfie at the end.
        wizard.completeDevModeOnboarding();
        
        // Post-onboarding, should reach the dashboard.
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();
        
        dashboard.goOnline();
        
        riderPage.waitForCondition(() ->
            riderPage.locator("text=Trips Completed").isVisible() ||
            riderPage.locator("text=Offline").isVisible() ||
            riderPage.locator("text=Online Duty").isVisible(),
            new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(10000));
    }

    @Test
    @DisplayName("REG-03: Restaurant Registration Flow")
    void restaurantRegistrationFlow() {
        restaurantPage.navigate(TestConfig.APP_URL);
        
        String newRestPhone = "9999" + (int)(Math.random() * 900000 + 100000);
        
        LoginPage loginPage = new LoginPage(restaurantPage);
        loginPage.loginAs("Restaurant Partner", newRestPhone, "E2E Test Restaurant Owner", "restaurant_" + newRestPhone + "@test.com");
        
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
        
        outletPage.fillOutletName("Tin Factory");
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
}
