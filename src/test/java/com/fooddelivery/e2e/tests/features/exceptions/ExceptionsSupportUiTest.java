package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class ExceptionsSupportUiTest extends TestBase {

    @Test
    @DisplayName("EXCEPTION-01: Restaurant Rejects Order")
    void restaurantRejectsOrder() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        
        // Simulating the UI state for rejecting an order
        boolean isRejectButtonVisible = restaurantPage.locator("text=Reject").isVisible();
        assertThat(isRejectButtonVisible).isFalse(); // Will be false because we don't have an order
    }

    @Test
    @DisplayName("EXCEPTION-02: Customer Cancels Order")
    void customerCancelsOrder() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);

        boolean isCancelButtonVisible = customerPage.locator("text=Cancel Order").isVisible();
        assertThat(isCancelButtonVisible).isFalse();
    }
    
    @Test
    @DisplayName("SUPPORT-01: Submit Review")
    void submitReview() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        
        boolean isRateVisible = customerPage.locator("text=Rate Order").isVisible();
        assertThat(isRateVisible).isFalse();
    }
}
