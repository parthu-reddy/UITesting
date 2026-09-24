package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderQueuePage;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests customer cancellation while order is in PENDING_ACCEPTANCE or CREATED status.
 */
@Tag("flow")
public class OrderCancellationFlowTest extends TestBase {

    @Test
    @DisplayName("Customer cancels order before restaurant accepts")
    void customerCancelsBeforeAcceptance() {
        // Checkout availability is calculated from an online nearby rider, even though the
        // rider is not otherwise part of a pre-acceptance cancellation.
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        DeliveryOnlineTogglePage riderToggle = new DeliveryOnlineTogglePage(riderPage);
        riderToggle.goOnline();
        assertThat(riderToggle.isOnline()).isTrue();

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        int brandNumber = Integer.parseInt(testRestaurantPhone.substring(7));
        String selectedOutlet = new NearbyOutletPage(customerPage)
                .openBrandAndSelectNearby("Brand " + brandNumber);
        assertThat(selectedOutlet).as("The customer must select the randomized restaurant's nearby outlet")
                .isNotBlank();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantOrderQueuePage restaurantQueue = new RestaurantOrderQueuePage(restaurantPage);
        restaurantQueue.waitForQueueLoad();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();

        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();

        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String orderId = tracker.getOrderId();
        assertThat(orderId).as("The newly placed order must appear in the tracker").isNotBlank();
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));

        restaurantQueue.selectOutlet(selectedOutlet);
        restaurantQueue.refreshOrders();
        RestaurantOrderActionsPage restaurantActions = new RestaurantOrderActionsPage(restaurantPage);
        restaurantActions.orderCard(shortOrderId)
                .waitFor(new Locator.WaitForOptions().setTimeout(30000));
        customerPage.locator("button:has-text('Cancel Order')").first()
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));

        tracker.cancelOrder();

        Locator exactOrder = customerPage.locator("span.font-mono", new com.microsoft.playwright.Page.LocatorOptions()
                .setHasText("#" + orderId)).first();
        exactOrder.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        Locator terminalHeadline = customerPage.locator("[data-testid='terminal-headline']");
        terminalHeadline.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        assertThat(terminalHeadline.innerText()).isEqualTo("This order was cancelled.");
        assertThat(customerPage.locator("button:has-text('Cancel Order')").count()).isZero();

        restaurantPage.reload();
        restaurantQueue.selectOutlet(selectedOutlet);
        restaurantActions.orderCard(shortOrderId).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                .setTimeout(30000));
    }
}
