package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderQueuePage;
import com.fooddelivery.e2e.util.StateSetupHelper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Cross-role coverage for a restaurant delay that requires a customer decision. */
@Tag("ui-only")
@Tag("feature-order-tracking")
@Tag("feature-restaurant-orders")
public class DelayApprovalFlowTest extends TestBase {

    private String orderId;
    private String shortOrderId;
    private String outletName;
    private RestaurantOrderQueuePage restaurantQueue;
    private RestaurantOrderActionsPage restaurantActions;

    @BeforeEach
    void placeOrderAndRequestDelay() {
        StateSetupHelper.ensureRiderIsOnline(riderPage, testRiderPhone);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        restaurantQueue = new RestaurantOrderQueuePage(restaurantPage);
        restaurantQueue.waitForQueueLoad();

        StateSetupHelper.OrderSetupResult order =
                StateSetupHelper.placeOrder(customerPage, testCustomerPhone, testRestaurantPhone);
        orderId = order.orderId;
        shortOrderId = order.orderId.substring(0, Math.min(8, order.orderId.length()));
        outletName = order.outletName;

        restaurantQueue.selectOutlet(outletName);
        restaurantQueue.refreshOrders();
        restaurantActions = new RestaurantOrderActionsPage(restaurantPage);

        Locator incomingCard = restaurantActions.orderCard(shortOrderId);
        incomingCard.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        // "Accept · 25 min" -- the promised prep time follows the verb.
        assertThat(incomingCard.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Accept\\b")))).isVisible();

        restaurantActions.requestDelay(shortOrderId, 15, DELAY_REASON);
        assertThat(restaurantActions.orderCard(shortOrderId))
                .hasAttribute("data-status", "AWAITING_DELAY_APPROVAL");

        customerPage.reload();
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        selectTrackedOrder(shortOrderId);
    }

    private static final String DELAY_REASON = "Kitchen needs fifteen more minutes";

    /** "I’ll wait" -- a typographic apostrophe, so match the end of the name. */
    private Locator waitButton(CustomerOrderTrackerPage tracker) {
        return tracker.tracker().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("ll wait$")));
    }

    @Tag("auto-cancel")
    @Tag("slow")
    @Test
    @DisplayName("DELAY-01-04: Restaurant requests 15 minutes and customer approves")
    void customerApprovesRestaurantDelay() {
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage, orderId);
        tracker.waitForStatus("AWAITING_DELAY_APPROVAL");
        // The kitchen's own request, minutes and reason (OrderTrackerLive headlineFor, UI 13bdd17
        // over CustomerApplication 408e582).
        assertThat(tracker.tracker()).containsText("The kitchen asked for 15 more minutes");
        assertThat(tracker.tracker()).containsText(DELAY_REASON);
        assertThat(waitButton(tracker)).isVisible();

        com.microsoft.playwright.Response answer = customerPage.waitForResponse(r -> r.request().method().equals("POST")
                && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/orders/" + orderId + "/delay-approval"),
                tracker::approveDelay);
        org.assertj.core.api.Assertions.assertThat(answer.status()).isBetween(200, 299);
        assertThat(waitButton(tracker)).isHidden();

        // Approval does not move the order by itself: the restaurant re-accepts on ORDER_DELAY_APPROVED (Kafka), then
        // the customer order follows (CustomerOrderService.handleDelayApproval). The screen shows ACCEPTED at once
        // (useLiveOrderActions), so the server's state is read after that event: the restaurant queue refreshes every
        // 5 s, and a customer reload re-reads the order.
        restaurantPage.reload();
        restaurantQueue.waitForQueueLoad();
        assertThat(restaurantActions.orderCard(shortOrderId)).hasAttribute("data-status",
                Pattern.compile("^(ACCEPTED|PREPARING)$"),
                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
        customerPage.reload();
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        selectTrackedOrder(shortOrderId);
        assertThat(tracker.tracker()).hasAttribute("data-status", Pattern.compile("^(ACCEPTED|PREPARING)$"),
                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
    }

    @Tag("slow")
    @Tag("feature-cancellation")
    @Test
    @DisplayName("DELAY-05-07: Customer rejects restaurant delay and order is cancelled")
    void customerRejectsRestaurantDelay() {
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage, orderId);
        tracker.waitForStatus("AWAITING_DELAY_APPROVAL");
        assertThat(waitButton(tracker)).isVisible();

        // The answer must reach the server before the reload below, and it must be the delay answer, not a cancel.
        com.microsoft.playwright.Response answer = customerPage.waitForResponse(r -> r.request().method().equals("POST")
                && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/orders/" + orderId + "/delay-approval"),
                tracker::rejectDelay);
        org.assertj.core.api.Assertions.assertThat(answer.status()).isBetween(200, 299);

        // The server's answer: declining publishes ORDER_DELAY_REJECTED, which settles the order
        // as CANCELLED_BY_RESTAURANT with the reason "Customer rejected delay"
        // (AwaitingDelayApprovalState.handleDelayRejected). Since UI useLiveOrderActions A9 the
        // screen shows that status at once.
        assertThat(tracker.tracker()).hasAttribute("data-status", "CANCELLED_BY_RESTAURANT");
        assertThat(waitButton(tracker)).isHidden();
        // After a reload a settled order is no longer active, so no tracker opens by itself; History reads the
        // server's record, which proves the status is the server's, not the screen's.
        customerPage.reload();
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        com.fooddelivery.e2e.pages.customer.CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator history = new com.fooddelivery.e2e.pages.customer.CustomerOrderHistoryPage(customerPage).pageToOrder(orderId, 20);
        assertThat(history).containsText("Cancelled by Restaurant");
        history.click();
        assertThat(tracker.tracker()).hasAttribute("data-status", "CANCELLED_BY_RESTAURANT");
        assertThat(tracker.tracker().getByTestId("terminal-headline")).hasText("The restaurant could not fulfil this order.");
        // The customer's own answer, carried on ORDER_DELAY_REJECTED (CustomerOrderService.handleDelayApproval);
        // a timeout would read the sweeper's reason instead.
        assertThat(tracker.tracker().getByTestId("cancellation-reason"))
                .containsText("Customer manually rejected additional prep time request");

        restaurantPage.reload();
        restaurantQueue.waitForQueueLoad();
        restaurantActions.orderCard(shortOrderId).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                .setTimeout(30000));
    }

    private void selectTrackedOrder(String shortId) {
        Locator selector = customerPage.locator("[role='combobox'][aria-label='Which order to track']");
        if (selector.count() == 0 || !selector.isVisible()) return;
        selector.click();
        customerPage.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("#" + shortId)))
                .click();
    }
}
