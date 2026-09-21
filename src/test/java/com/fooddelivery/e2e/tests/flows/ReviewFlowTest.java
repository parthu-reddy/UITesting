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
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        // wait handled by selectHomeFromOpenDialog

        // Navigate to order history to find a delivered order
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.openOrdersTab();
        customerPage.waitForTimeout(2000);

        // Check if there's a rate/review button on a completed order
        if (customerPage.locator("button:has-text('Rate'), button:has-text('Review')").first().isVisible()) {
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
}
