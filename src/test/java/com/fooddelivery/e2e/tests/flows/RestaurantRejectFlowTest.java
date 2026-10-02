package com.fooddelivery.e2e.tests.flows;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.*;
import org.junit.jupiter.api.*;

/** Existing rejection flow uses the same quote/payment/readiness and owned-reason checks. */
@Tag("flow")
public class RestaurantRejectFlowTest extends TestBase {
    @Test @DisplayName("Restaurant rejects exact quoted order and customer sees retained cancellation")
    void restaurantCancelsOrder() throws java.io.IOException {
        String retained=System.getProperty("refund.resume.order.id", "").trim();
        if(!retained.isEmpty()) {
            customerPage.navigate(TestConfig.APP_URL);new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();loginAsAdmin();
            RefundRecoveryChecks.resume(customerPage,adminPage,retained,"CANCELLED_BY_RESTAURANT",testCustomerPhone,testRestaurantPhone,testRiderPhone);
            return;
        }
        try(SeededRiderDuty duty=SeededRiderDuty.ensureOnline(riderPage,testRiderPhone)){
            customerPage.navigate(TestConfig.APP_URL);new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            restaurantPage.navigate(TestConfig.APP_URL);new LoginPage(restaurantPage).loginAs("Restaurant Partner",testRestaurantPhone);
            new RestaurantDashboardPage(restaurantPage).waitForDashboard();
            String resume=System.getProperty("restaurant.resume.order.id", "").trim();
            LiveOrderFixture.Created order=resume.isEmpty()
                ? LiveOrderFixture.place(customerPage,restaurantPage,testCustomerPhone,testRestaurantPhone,testRiderPhone,duty)
                : LiveOrderFixture.resumePending(customerPage,restaurantPage,resume,testCustomerPhone,testRestaurantPhone,testRiderPhone).order();
            var original=RefundRecoveryChecks.order(customerPage,order.id());
            var originalPaid=OrderMoneyChecks.amount(original,"totalAmount");
            RestaurantRejectionScenario.reject(restaurantPage,customerPage,order.id());
            loginAsAdmin();RefundRecoveryChecks.verify(customerPage,adminPage,order.id(),originalPaid);
        }
    }
}
