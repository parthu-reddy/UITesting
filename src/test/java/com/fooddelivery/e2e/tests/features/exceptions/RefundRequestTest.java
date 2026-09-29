package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.common.ChatWidgetPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises refund-quote chat from the real delivered-order summary using an order created by
 * this test. The request records a quote event; this test never approves or pays a refund.
 */
@Tag("feature")
@Tag("chat")
public class RefundRequestTest extends TestBase {

    @Test
    @DisplayName("CHAT-REFUND-01..04: Refund quote modal validates and sends a chat request")
    void requestRefundQuoteForDeliveredTestOrder() {
        ChatTelemetry telemetry = observeChatTraffic(customerPage);
        AtomicBoolean allowChatReconnect = new AtomicBoolean(false);
        AtomicInteger blockedChatSockets = new AtomicInteger();
        customerPage.routeWebSocket(url -> url.contains("/ws/chat"), socket -> {
            if (allowChatReconnect.get()) {
                socket.connectToServer();
            } else {
                blockedChatSockets.incrementAndGet();
                socket.close();
            }
        });
        CompletedDeliveryFixture.Result order = CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone);

        ChatWidgetPage refund = new ChatWidgetPage(customerPage);
        Response sessionResponse = customerPage.waitForResponse(
                response -> response.request().method().equals("POST")
                        && response.url().contains("/api/v1/chat/sessions"),
                new Page.WaitForResponseOptions().setTimeout(20000),
                () -> refund.openRefundRequest(order.orderId()));

        Locator orderTracker = customerPage.locator(
                "[data-testid='order-tracker'][data-order-id='" + order.orderId() + "']");
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        orderTracker.getByText("Order delivered",
                                new Locator.GetByTextOptions().setExact(true)))
                .isVisible();
        assertThat(sessionResponse.status()).as("refund chat session is created").isBetween(200, 299);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(refund.refundModal()).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                refund.refundModal().getByText("Request Refund Quote",
                        new Locator.GetByTextOptions().setExact(true))).isVisible();

        customerPage.waitForCondition(() -> refund.refundItemCheckboxes().count() > 0,
                new Page.WaitForConditionOptions().setTimeout(15000));
        Locator submitButton = refund.requestQuoteButton();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(submitButton).isDisabled();

        refund.refundItemCheckboxes().first().check();
        assertThat(submitButton.isDisabled())
                .as("selecting an item without a reason does not enable the request")
                .isTrue();

        String reason = "E2E quote request for order " + order.shortOrderId();
        refund.fillRefundReason(reason);

        Locator connectingStatus = refund.refundModal().getByRole(
                com.microsoft.playwright.options.AriaRole.STATUS);
        customerPage.waitForCondition(() -> blockedChatSockets.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(15000));
        customerPage.waitForCondition(
                connectingStatus::isVisible,
                new Page.WaitForConditionOptions().setTimeout(15000));
        assertThat(submitButton.isDisabled())
                .as("a valid quote cannot be discarded while the chat WebSocket is disconnected")
                .isTrue();

        allowChatReconnect.set(true);
        waitForStompConnected(customerPage, telemetry);
        customerPage.waitForCondition(submitButton::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(15000));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(submitButton).isEnabled();

        refund.fillRefundReason("");
        assertThat(submitButton.isDisabled())
                .as("a connected chat still requires a refund reason")
                .isTrue();
        assertThat(customerPage.getByText("Requesting quote...",
                new Page.GetByTextOptions().setExact(true)).count())
                .as("no quote message is created while the connected form has no reason")
                .isZero();

        refund.fillRefundReason(reason);
        customerPage.waitForCondition(submitButton::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(10000));
        refund.submitRefundRequest();
        refund.refundModal().waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                customerPage.getByText("Requesting quote...",
                        new Page.GetByTextOptions().setExact(true)))
                .isVisible();
    }

    private static ChatTelemetry observeChatTraffic(Page page) {
        ChatTelemetry telemetry = new ChatTelemetry();
        page.onWebSocket(webSocket -> {
            if (!webSocket.url().contains("/ws/chat")) return;
            webSocket.onFrameReceived(frame -> {
                String text = frame.text();
                if (text != null && text.stripLeading().startsWith("CONNECTED")) {
                    telemetry.stompState().set("CONNECTED");
                }
            });
        });
        return telemetry;
    }

    private static void waitForStompConnected(Page page, ChatTelemetry telemetry) {
        page.waitForCondition(() -> telemetry.stompState().get().equals("CONNECTED"),
                new Page.WaitForConditionOptions().setTimeout(20000));
    }

    private record ChatTelemetry(AtomicReference<String> stompState) {
        private ChatTelemetry() {
            this(new AtomicReference<>("not connected"));
        }
    }
}
