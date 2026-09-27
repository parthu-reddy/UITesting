package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class CustomerRoutingUiTest extends TestBase {

    @Test
    @DisplayName("NAV-02: Customer settings deep link opens directly and survives reload")
    void verifyCustomerRoutingPersistence() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone, "Test Customer", "customer@example.com");

        // Dismiss any dashboard overlays (like Location Required).
        customerPage.keyboard().press("Escape");
        customerPage.waitForTimeout(500);

        // Exercise the route itself instead of reaching settings through in-memory UI state.
        String settingsUrl = TestConfig.APP_URL.replaceAll("/$", "") + "/customer/settings/profile";
        customerPage.navigate(settingsUrl);
        customerPage.waitForURL("**/customer/settings/profile");
        customerPage.getByText("Account Settings", new Page.GetByTextOptions().setExact(true)).waitFor();
        assertThat(customerPage.getByText("Account Settings", new Page.GetByTextOptions().setExact(true)).isVisible())
                .isTrue();

        customerPage.reload();
        customerPage.waitForURL("**/customer/settings/profile");

        assertThat(customerPage.url()).endsWith("/customer/settings/profile");
        customerPage.getByText("Account Settings", new Page.GetByTextOptions().setExact(true)).waitFor();
        assertThat(customerPage.getByText("Account Settings", new Page.GetByTextOptions().setExact(true)).isVisible())
                .isTrue();
    }
}
