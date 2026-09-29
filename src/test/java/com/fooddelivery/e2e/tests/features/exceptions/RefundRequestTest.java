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
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(submitButton).isEnabled();

        waitForStompConnected(customerPage, telemetry);
        refund.submitRefundRequest();
        refund.refundModal().waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        customerPage.waitForCondition(telemetry.refundQuoteFrameSent()::get,
                new Page.WaitForConditionOptions().setTimeout(10000));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                customerPage.getByText("Requesting quote...",
                        new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(telemetry.refundQuoteFrameSent().get())
                .as("client sends a REFUND_QUOTE_REQUEST STOMP frame")
                .isTrue();
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
            webSocket.onFrameSent(frame -> {
                String text = frame.text();
                if (text != null && text.contains("/app/chat.send/")
                        && text.contains("REFUND_QUOTE_REQUEST")) {
                    telemetry.refundQuoteFrameSent().set(true);
                }
            });
        });
        return telemetry;
    }

    private static void waitForStompConnected(Page page, ChatTelemetry telemetry) {
        page.waitForCondition(() -> telemetry.stompState().get().equals("CONNECTED"),
                new Page.WaitForConditionOptions().setTimeout(20000));
    }

    private record ChatTelemetry(AtomicReference<String> stompState,
                                 AtomicReference<Boolean> refundQuoteFrameSent) {
        private ChatTelemetry() {
            this(new AtomicReference<>("not connected"), new AtomicReference<>(false));
        }
    }
}
