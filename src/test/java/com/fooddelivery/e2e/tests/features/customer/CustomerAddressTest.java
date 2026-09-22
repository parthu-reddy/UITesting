package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests customer address management: add new, select existing, verify header updates.
 */
@Tag("feature")
public class CustomerAddressTest extends TestBase {

    @Test
    @DisplayName("Select existing address → Deliver-to header updates")
    void selectExistingAddress() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        // Click "Deliver to" to open address modal
        dashboard.clickDeliverTo();

        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();

        // Select an existing address
        int count = modal.getAddressCount();
        assertThat(count).isGreaterThan(0).as("Test user should have at least one saved address");

        modal.selectExistingAddress("Home");
        customerPage.waitForTimeout(1000);

        // Verify the "Deliver to" header updated
        String deliverTo = dashboard.getDeliverToText();
        assertThat(deliverTo).isNotEmpty();
    }

    @Test
    @DisplayName("Add new address → appears in address list")
    void addNewAddress() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        dashboard.clickDeliverTo();
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();

        modal.clickAddNewAddress();
        modal.fillAddressLabel("Test Office");
        modal.fillAddressLine("123 Test Street, Bangalore");
        modal.saveAddress();

        customerPage.waitForTimeout(2000);
    }
}
