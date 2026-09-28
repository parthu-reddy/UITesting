package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import org.junit.jupiter.api.*;

/**
 * Tests the post-delivery review submission flow.
 */
@Tag("flow")
public class ReviewFlowTest extends TestBase {

    @Test
    @DisplayName("Submit star rating and comment after delivery")
    void submitReview() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        // Navigate to settings → history to find delivered orders
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openSettingsTab();
        customerPage.getByRole(com.microsoft.playwright.options.AriaRole.TAB,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.waitForTimeout(2000);

        // Check if there's a rate/review button on a completed order
        boolean hasRateButton = customerPage.locator("button:has-text('Rate'), button:has-text('Review')").first().isVisible();
        Assumptions.assumeTrue(hasRateButton,
                "No delivered orders with rate/review button found — skipping");
        customerPage.locator("button:has-text('Rate'), button:has-text('Review')").first().click();
        customerPage.waitForTimeout(1000);

        // Fill in review
        if (customerPage.locator("textarea, input[placeholder*='review']").first().isVisible()) {
            customerPage.locator("textarea, input[placeholder*='review']").first().fill("Great food and fast delivery!");
        }

        // Click submit
        customerPage.locator("button:has-text('Submit'), button:has-text('Send')").first().click();
        customerPage.waitForTimeout(2000);
    }
}

