package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderDetailsModalPage;
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

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flow")
@Tag("ui-only")
public class RestaurantFulfillmentTest extends TestBase {

    private String fullOrderId;
    private String shortOrderId;
    private String outletName;
    private RestaurantOrderQueuePage orderQueue;

    @BeforeEach
    void setupOrder() {
        // Log in to the restaurant first so the SSE connection is active
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        orderQueue = new RestaurantOrderQueuePage(restaurantPage);
        orderQueue.waitForQueueLoad();
        StateSetupHelper.ensureRiderIsOnline(riderPage, testRiderPhone);

        String resumeOrderId = System.getProperty("resume.order.id", "").trim();
        if (!resumeOrderId.isEmpty()) {
            outletName = System.getProperty("resume.outlet", "").trim();
            assertThat(outletName).as("-Dresume.outlet is required with -Dresume.order.id").isNotBlank();
            fullOrderId = resumeOrderId;
            shortOrderId = fullOrderId.substring(0, Math.min(8, fullOrderId.length()));
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            orderQueue.selectOutlet(outletName);
            orderQueue.waitForQueueLoad();
            orderQueue.refreshOrders();
            return;
        }

        StateSetupHelper.OrderSetupResult result = StateSetupHelper.placeOrder(customerPage, testCustomerPhone, testRestaurantPhone);
        fullOrderId = result.orderId;
        shortOrderId = fullOrderId.substring(0, 8);
        outletName = result.outletName;

        if (outletName != null) {
            orderQueue.selectOutlet(outletName);
            orderQueue.waitForQueueLoad();
        }
        orderQueue.refreshOrders();
    }

    /** The card's status, from its data-status attribute (the backend enum), not its pill copy. */
    private static void assertCardStatus(Locator card, String status) {
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(card)
                .hasAttribute("data-status", status);
    }

    @Test
    @DisplayName("REST-ACCEPT-01-08/12-16/18: Exact order moves from incoming to prepared and survives reload")
    void restaurantCanAcceptAndPrepareOrder() {
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        Locator incoming = actions.orderCard(shortOrderId);
        incoming.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        // The card prints the short id in capitals: "#1A2B3C4D" (RestaurantOrderCard.tsx).
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(incoming)
                .containsText("#" + shortOrderId.toUpperCase());
        // "Accept · 25 min": the promised prep time follows the verb.
        Locator accept = incoming.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Accept\\b")));
        if (accept.isVisible()) {
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(incoming
                    .getByRole(AriaRole.LIST, new Locator.GetByRoleOptions().setName("Dishes"))
                    .getByRole(AriaRole.LISTITEM).first()).isVisible();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(incoming)
                    .containsText("Order value");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(incoming)
                    .containsText("Your payout");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(accept).isEnabled();
            // An incoming order's secondary action is "Reject"; it reads "Cancel" once accepted.
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(incoming.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Reject").setExact(true))).isEnabled();

            actions.openOrderDetails(shortOrderId);
            RestaurantOrderDetailsModalPage details = new RestaurantOrderDetailsModalPage(restaurantPage);
            assertThat(details.isOpen()).isTrue();
            assertThat(details.getOrderId()).contains(shortOrderId);
            restaurantPage.getByText("Order Items", new Page.GetByTextOptions().setExact(true))
                    .waitFor(new Locator.WaitForOptions().setTimeout(15000));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByText(
                    "Transparent Financial Breakdown", new Page.GetByTextOptions().setExact(true))).isVisible();
            details.close();

            actions.acceptOrder(shortOrderId);
            assertCardStatus(actions.orderCard(shortOrderId), "ACCEPTED");

            // Once accepted, the customer can no longer cancel from the tracker.
            customerPage.reload();
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
            tracker.waitForTracker();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(tracker.tracker().getByRole(
                    AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))).isHidden();

            // The outlet choice survives a reload (localStorage), and the card must not duplicate.
            restaurantPage.reload();
            orderQueue.waitForQueueLoad();
            assertCardStatus(actions.orderCard(shortOrderId), "ACCEPTED");
            assertThat(restaurantPage.locator("[data-testid='restaurant-order-card'][data-order-id='"
                    + fullOrderId + "']").count()).isEqualTo(1);
        }

        Locator startCook = actions.orderCard(shortOrderId).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Start cooking").setExact(true));
        if (startCook.isVisible()) actions.startCooking(shortOrderId);
        assertCardStatus(actions.orderCard(shortOrderId), "PREPARING");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(actions.orderCard(shortOrderId)
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Mark ready").setExact(true))).isVisible();

        new CustomerOrderTrackerPage(customerPage).waitForStatus("PREPARING");

        actions.markPrepared(shortOrderId);
        assertCardStatus(actions.orderCard(shortOrderId), "READY_FOR_PICKUP");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(actions.orderCard(shortOrderId)
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Show pickup code").setExact(true))).isVisible();
        String pickupOtp = actions.getPickupOtp(shortOrderId);
        assertThat(pickupOtp).matches("[0-9]{6}");
    }

    @Test
    @DisplayName("REST-ACCEPT-09: Restaurant rejects an incoming order")
    void restaurantRejectsIncomingOrder() {
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(actions.orderCard(shortOrderId)
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Reject").setExact(true))).isEnabled();
        actions.cancelOrder(shortOrderId);
        actions.orderCard(shortOrderId).waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                .setTimeout(20000));

        customerPage.reload();
        Locator terminalHeadline = customerPage.locator("[data-testid='terminal-headline']");
        terminalHeadline.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(terminalHeadline.innerText()).isEqualTo("The restaurant could not fulfil this order.");
        assertThat(customerPage.locator("[data-testid='cancellation-reason']").innerText())
                .contains("Item out of stock");
    }

    @Test
    @DisplayName("REST-NAV-01: Restaurant Queue Tab States")
    void restaurantQueueTabStates() {
        // The six KanbanColumn titles (RestaurantOrderQueue.tsx). Each column is a <section>
        // named "<title>, <n> orders", so check the region, not a loose text match.
        for (String column : new String[]{"Incoming", "In the kitchen", "Waiting on customer",
                "Ready for pickup", "Out for delivery", "Refund requests"}) {
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(
                    AriaRole.REGION, new Page.GetByRoleOptions()
                            .setName(Pattern.compile("^" + Pattern.quote(column) + ", \\d+ orders?$"))))
                    .isVisible();
        }
    }
}
