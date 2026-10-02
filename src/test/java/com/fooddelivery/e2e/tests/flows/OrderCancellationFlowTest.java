package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.util.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.util.Map;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** One owned paid pending order covers customer cancellation, retained views and refund recovery. */
@Tag("flow") @Tag("cancellation")
public class OrderCancellationFlowTest extends TestBase {
    @Test @DisplayName("Customer cancels exact paid pending order; original payment and ledger recover")
    void customerCancelsBeforeAcceptance() throws java.io.IOException {
        String retained=System.getProperty("refund.resume.order.id", "").trim();
        if(!retained.isEmpty()) {
            customerPage.navigate(TestConfig.APP_URL);new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();loginAsAdmin();
            RefundRecoveryChecks.resume(customerPage,adminPage,retained,"CANCELLED",testCustomerPhone,testRestaurantPhone,testRiderPhone);
            return;
        }
        try(SeededRiderDuty duty=SeededRiderDuty.ensureOnline(riderPage,testRiderPhone)){
            customerPage.navigate(TestConfig.APP_URL);new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            restaurantPage.navigate(TestConfig.APP_URL);new LoginPage(restaurantPage).loginAs("Restaurant Partner",testRestaurantPhone);
            RestaurantDashboardPage dashboard=new RestaurantDashboardPage(restaurantPage);dashboard.waitForDashboard();
            LiveOrderFixture.Created created=LiveOrderFixture.place(customerPage,restaurantPage,testCustomerPhone,testRestaurantPhone,testRiderPhone,duty);
            String id=created.id();Map<?,?> original=RefundRecoveryChecks.order(customerPage,id);
            var originalPaid=OrderMoneyChecks.amount(original,"totalAmount");
            Locator card=new RestaurantOrderActionsPage(restaurantPage).orderCard(id);assertThat(card).hasAttribute("data-status","CREATED");
            CustomerOrderTrackerPage tracker=new CustomerOrderTrackerPage(customerPage,id);
            Response cancel=customerPage.waitForResponse(r->r.request().method().equals("POST")&&r.url().endsWith("/api/v1/orders/"+id+"/cancel"),tracker::cancelOrder);
            org.assertj.core.api.Assertions.assertThat(cancel.status()).isBetween(200,299);
            assertThat(tracker.tracker()).hasAttribute("data-status","CANCELLED");
            assertThat(tracker.tracker().getByTestId("terminal-headline")).hasText("This order was cancelled.");
            assertThat(tracker.tracker().getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))).isHidden();
            restaurantPage.reload();dashboard.waitForDashboard();dashboard.selectOutlet(created.outlet());assertThat(card).isHidden();
            loginAsAdmin();RefundRecoveryChecks.verify(customerPage,adminPage,id,originalPaid);
        }
    }
}
