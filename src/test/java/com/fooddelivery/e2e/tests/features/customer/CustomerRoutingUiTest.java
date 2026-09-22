package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class CustomerRoutingUiTest extends TestBase {

    @Test
    @DisplayName("CUST-01: Verify Customer Routing persists on page reload")
    void verifyCustomerRoutingPersistence() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone, "Test Customer", "customer@example.com");
        
        // Dismiss any dashboard overlays (like Location Required)
        customerPage.keyboard().press("Escape");
        customerPage.waitForTimeout(500);
        
        // Navigate to the Profile/Settings using the profile menu/button
        customerPage.locator("button[title='Profile Settings'], button[aria-label='Profile Settings'], button:has-text('Profile')").first()
                    .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        
        // Wait for the URL to reflect the React Router path
        customerPage.waitForURL("**/customer/settings*");
        assertThat(customerPage.url()).contains("/customer/settings");
        
        // Reload the page
        customerPage.reload();
        customerPage.waitForTimeout(2000);
        
        // Verify URL is STILL /customer/settings after reload
        assertThat(customerPage.url()).contains("/customer/settings");
        
        // Verify the settings page is still active (e.g. Profile details)
        assertThat(customerPage.locator("text=Profile Settings").first().isVisible() ||
                   customerPage.locator("text=Account").first().isVisible()).isTrue();
    }
}
