package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.customer.PaymentModalPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.util.CheckoutAvailability;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("cancellation")
public class OrderCancellationTest extends TestBase {

    @Test
    @DisplayName("CANCEL-01 to CANCEL-02: Restaurant rejects order")
    void restaurantRejectsOrder() {
        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

            int brandNumber = Integer.parseInt(testRestaurantPhone.substring(7));
            String selectedOutlet = new NearbyOutletPage(customerPage)
                    .openBrandAndSelectNearby("Brand " + brandNumber);
            new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
            new CustomerMenuViewPage(customerPage).clickViewCart();
            CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
            cart.waitForCartOpen();
            CheckoutAvailability.requireDeliveryAvailable(
                    CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));

            PaymentModalPage payment = new PaymentModalPage(customerPage);
            payment.waitForOpen();
            payment.placeOrder("Credit or debit card");

            CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
            String orderId = tracker.getOrderId();
            assertThat(orderId).as("A real customer order must be created before restaurant rejection")
                    .isNotBlank();
            String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));

            restaurantPage.navigate(TestConfig.APP_URL);
            new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
            RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
            dashboard.waitForDashboard();
            dashboard.selectOutlet(selectedOutlet);
            dashboard.openOrdersTab();

            RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
            Locator orderCard = actions.orderCard(shortOrderId);
            orderCard.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(20000));
            assertThat(orderCard.locator("button:has-text('Reject'), button:has-text('Cancel')")
                    .first().isVisible()).isTrue();
            actions.cancelOrder(shortOrderId);
            orderCard.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(20000));

            // Terminal orders are removed from the active-order endpoint after a reload.
            // Find the rejected order in History, then open its detail tracker to verify the
            // customer can still see who cancelled it and why.
            customerPage.reload();
            CustomerDashboardPage.openProfileSettings(customerPage);
            Locator historyTab = customerPage.getByRole(AriaRole.TAB,
                    new com.microsoft.playwright.Page.GetByRoleOptions()
                            .setName("History").setExact(true));
            historyTab.click();

            Locator historyOrder = customerPage.locator("[data-testid='customer-history-order']")
                    .filter(new Locator.FilterOptions().setHasText(shortOrderId));
            historyOrder.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(20000));
            assertThat(historyOrder.innerText()).containsIgnoringCase("Cancelled by Restaurant");
            historyOrder.click();

            Locator cancelledOrder = customerPage.locator(
                    "[data-testid='order-tracker'][data-order-id='" + orderId + "']");
            cancelledOrder.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(20000));
            Locator terminalHeadline = cancelledOrder.locator("[data-testid='terminal-headline']");
            terminalHeadline.waitFor(new Locator.WaitForOptions().setTimeout(20000));
            assertThat(terminalHeadline.innerText())
                    .isEqualTo("The restaurant could not fulfil this order.");
            assertThat(cancelledOrder.locator("[data-testid='cancellation-reason']").innerText())
                    .contains("Item out of stock");
        }
    }

    @Test
    @DisplayName("CANCEL-07 to CANCEL-09: Customer cancels order before restaurant accepts")
    void customerCancelsOrderBeforeAccept() {
        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

            int brandNumber = Integer.parseInt(testRestaurantPhone.substring(7));
            new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand " + brandNumber);
            new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
            new CustomerMenuViewPage(customerPage).clickViewCart();
            new CustomerCartDrawerPage(customerPage).waitForCartOpen();
            CheckoutAvailability.requireDeliveryAvailable(
                    CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));

            PaymentModalPage payment = new PaymentModalPage(customerPage);
            payment.waitForOpen();
            payment.placeOrder("Credit or debit card");

            CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
            String orderId = tracker.getOrderId();
            assertThat(orderId).as("A real customer order must exist before cancellation").isNotBlank();
            Locator ownOrder = customerPage.locator(
                    "[data-testid='order-tracker'][data-order-id='" + orderId + "']");
            Locator cancelButton = ownOrder.locator("button:has-text('Cancel order')");
            cancelButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));

            Response cancellationResponse = customerPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().endsWith("/api/v1/orders/" + orderId + "/cancel"),
                    new com.microsoft.playwright.Page.WaitForResponseOptions().setTimeout(20000),
                    tracker::cancelOrder);
            assertThat(cancellationResponse.status())
                    .as("customer cancellation API response body: %s", cancellationResponse.text())
                    .isBetween(200, 299);

            Locator cancelledOrder = customerPage.locator(
                    "[data-testid='order-tracker'][data-order-id='" + orderId + "'][data-status='CANCELLED']");
            cancelledOrder.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(20000));
            assertThat(cancelledOrder.getAttribute("data-status")).isEqualTo("CANCELLED");
        }
    }
}
