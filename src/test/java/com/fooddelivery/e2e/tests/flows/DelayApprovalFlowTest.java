package com.fooddelivery.e2e.tests.flows;

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
@Tag("flow")
@Tag("ui-only")
public class DelayApprovalFlowTest extends TestBase {

    private String shortOrderId;
    private String outletName;
    private RestaurantOrderQueuePage restaurantQueue;
    private RestaurantOrderActionsPage restaurantActions;

    @BeforeEach
    void placeOrderAndRequestDelay() {
        StateSetupHelper.ensureRiderIsOnline(riderPage, testRiderPhone);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        restaurantQueue = new RestaurantOrderQueuePage(restaurantPage);
        restaurantQueue.waitForQueueLoad();

        StateSetupHelper.OrderSetupResult order =
                StateSetupHelper.placeOrder(customerPage, testCustomerPhone, testRestaurantPhone);
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

    @Test
    @DisplayName("DELAY-01-04: Restaurant requests 15 minutes and customer approves")
    void customerApprovesRestaurantDelay() {
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        tracker.waitForStatus("AWAITING_DELAY_APPROVAL");
        // The kitchen's own request, minutes and reason (OrderTrackerLive headlineFor, UI 13bdd17
        // over CustomerApplication 408e582).
        assertThat(tracker.tracker()).containsText("The kitchen asked for 15 more minutes");
        assertThat(tracker.tracker()).containsText(DELAY_REASON);
        assertThat(waitButton(tracker)).isVisible();

        tracker.approveDelay();

        assertThat(waitButton(tracker)).isHidden();
        // Approval does not move the order by itself: it stays AWAITING_DELAY_APPROVAL until the
        // restaurant re-accepts (CustomerOrderService.handleDelayApproval), then it is ACCEPTED.
        tracker.waitForStatus("ACCEPTED", "PREPARING");

        restaurantPage.reload();
        restaurantQueue.waitForQueueLoad();
        assertThat(restaurantActions.orderCard(shortOrderId))
                .hasAttribute("data-status", Pattern.compile("^(ACCEPTED|PREPARING)$"));
    }

    @Test
    @DisplayName("DELAY-05-07: Customer rejects restaurant delay and order is cancelled")
    void customerRejectsRestaurantDelay() {
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        assertThat(tracker.tracker().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))).isVisible();

        tracker.rejectDelay();

        // The server's answer: declining publishes ORDER_DELAY_REJECTED, which settles the order
        // as CANCELLED_BY_RESTAURANT with the reason "Customer rejected delay"
        // (AwaitingDelayApprovalState.handleDelayRejected). Since UI useLiveOrderActions A9 the
        // screen shows that status at once; the reload proves it is the server's, not the screen's.
        customerPage.reload();
        tracker.waitForStatus("CANCELLED_BY_RESTAURANT");
        Locator terminalHeadline = customerPage.locator("[data-testid='terminal-headline']");
        terminalHeadline.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        org.assertj.core.api.Assertions.assertThat(terminalHeadline.innerText())
                .as("Rejected delay should end the exact tracked order")
                .isEqualTo("The restaurant could not fulfil this order.");
        assertThat(waitButton(tracker)).isHidden();

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
