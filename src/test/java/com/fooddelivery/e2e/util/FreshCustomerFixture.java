package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerAddressModalPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static org.assertj.core.api.Assertions.assertThat;

/** Explicit fresh Dev signup, retained after execution; no server-side cleanup. */
public final class FreshCustomerFixture {
    private FreshCustomerFixture() {}

    public static void registerWithAddress(Page page, String phone, String label) {
        assertThat(System.getProperty("registration.preflight"))
                .as("Allocate an unused Dev phone through run_registration_e2e.py").isEqualTo("true");
        assertThat(phone).matches("8999[0-9]{6}");
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).registerAs("Order Food", phone, "E2E Test Customer", "customer_" + phone + "@test.com");
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName("Select Delivery Location").setExact(true)).waitFor();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add New Address")).click();
        CustomerAddressModalPage modal = new CustomerAddressModalPage(page);
        modal.waitForModalOpen();
        modal.searchAndSelectLocation("Keerthi Rendezvous");
        modal.fillAddressLabel(label);
        modal.fillAddressLine("Keerthi Rendezvous, E2E registration address");
        modal.fillCity("Bangalore");
        modal.fillState("Karnataka");
        modal.fillZipCode("560001");
        modal.saveAddress();
        new CustomerDashboardPage(page).waitForDashboard();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("header")).containsText(label);
    }
}
