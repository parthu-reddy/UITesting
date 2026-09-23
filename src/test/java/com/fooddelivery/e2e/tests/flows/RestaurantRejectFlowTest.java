package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests restaurant rejecting/cancelling an order.
 */
@Tag("flow")
public class RestaurantRejectFlowTest extends TestBase {

    @Test
    @DisplayName("Restaurant cancels order → Customer sees cancelled")
    void restaurantCancelsOrder() {
        // Keep a seeded rider available so the outlet remains orderable.
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        DeliveryOnlineTogglePage riderToggle = new DeliveryOnlineTogglePage(riderPage);
        riderToggle.goOnline();
        assertThat(riderToggle.isOnline()).isTrue();

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();

        int brandNumber = Integer.parseInt(testRestaurantPhone.substring(7));
        String selectedOutlet = new NearbyOutletPage(customerPage)
                .openBrandAndSelectNearby("Brand " + brandNumber);
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();

        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String orderId = tracker.getOrderId();
        assertThat(orderId).as("New order must appear in the customer tracker").isNotBlank();
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));

        dashboard.selectOutlet(selectedOutlet);
        restaurantPage.reload();
        dashboard.waitForDashboard();
        dashboard.openOrdersTab();
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        actions.cancelOrder(shortOrderId);

        customerPage.reload();
        Locator exactOrderId = customerPage.locator("span.font-mono", new com.microsoft.playwright.Page.LocatorOptions()
                .setHasText("#" + shortOrderId)).first();
        exactOrderId.waitFor(new Locator.WaitForOptions().setTimeout(20000));

        Locator terminalHeadline = customerPage.locator("[data-testid='terminal-headline']");
        terminalHeadline.waitFor(new Locator.WaitForOptions().setTimeout(20000));
        assertThat(terminalHeadline.innerText())
                .isEqualTo("The restaurant could not fulfil this order.");
        assertThat(customerPage.locator("[data-testid='cancellation-reason']").innerText())
                .contains("Item out of stock");
    }
}
