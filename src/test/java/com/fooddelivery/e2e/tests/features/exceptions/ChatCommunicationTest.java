package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI-only regression coverage for customer messaging while an order is active and during the
 * delivered-order grace period. Requires seeded customer, restaurant, and rider accounts plus a
 * nearby Brand 1 outlet.
 */
@Tag("feature")
public class ChatCommunicationTest extends TestBase {

    @Test
    @DisplayName("Customer can send a message while the order is active and restaurant receives it")
    void customerCanSendMessageDuringActiveOrder() {
        ChatTelemetry customerTelemetry = observeChatTraffic(customerPage);
        ChatTelemetry restaurantTelemetry = observeChatTraffic(restaurantPage);
        AtomicReference<ChatAttempt> activeAttempt = new AtomicReference<>();

        CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone,
                order -> activeAttempt.set(trySendMessage(customerPage, order,
                        "E2E active-order check " + order.shortOrderId(), customerTelemetry,
                        restaurantPage, restaurantTelemetry)));

        assertSuccessfulChatAttempt(activeAttempt.get(), "while the order is active", true);
    }

    @Test
    @DisplayName("Customer can send a message during the delivered-order grace period")
    void customerCanSendMessageAfterDelivery() {
        ChatTelemetry telemetry = observeChatTraffic(customerPage);
        CompletedDeliveryFixture.Result order = CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone);

        ChatAttempt attempt = trySendMessage(customerPage, order,
                "E2E delivery-window check " + order.shortOrderId(), telemetry, null, null);
        assertSuccessfulChatAttempt(attempt, "after delivery", false);
    }

    private static ChatTelemetry observeChatTraffic(Page page) {
        ChatTelemetry telemetry = new ChatTelemetry();
        page.onResponse(response -> {
            if (response.request().method().equals("POST")
                    && response.url().contains("/api/v1/chat/sessions")) {
                telemetry.sessionResponseStatus().set(response.status());
            }
            if (response.url().contains("/ws/chat")) {
                telemetry.websocketResponseStatus().set(response.status());
            }
        });
        page.onWebSocket(webSocket -> {
            if (!webSocket.url().contains("/ws/chat")) return;
            telemetry.websocketState().set("websocket open; awaiting STOMP CONNECTED");
            webSocket.onFrameReceived(frame -> {
                String text = frame.text();
                if (text == null) return;
                String trimmed = text.stripLeading();
                if (trimmed.startsWith("CONNECTED")) telemetry.websocketState().set("STOMP CONNECTED");
                else if (trimmed.startsWith("ERROR")) telemetry.websocketState().set("STOMP ERROR");
            });
        });
        return telemetry;
    }

    private static ChatAttempt trySendMessage(Page customerPage, CompletedDeliveryFixture.Result order,
                                               String message, ChatTelemetry customerTelemetry,
                                               Page recipientPage, ChatTelemetry recipientTelemetry) {
        CustomerOrderChatPage customerChat = new CustomerOrderChatPage(customerPage);
        CustomerOrderChatPage recipientChat = null;
        Integer customerSessionStatus = null;

        try {
            Response sessionResponse = customerPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().contains("/api/v1/chat/sessions"),
                    new Page.WaitForResponseOptions().setTimeout(20000),
                    () -> customerChat.openChat(order.orderId()));
            customerSessionStatus = sessionResponse.status();
            if (!sessionResponse.ok()) {
                closeChatIfOpen(customerChat);
                return attempt(customerSessionStatus, customerTelemetry, recipientTelemetry,
                        false, false, "customer session request was rejected");
            }

            waitForConnected(customerPage, customerChat, customerTelemetry);

            if (recipientPage != null) {
                CustomerOrderChatPage peerChat = new CustomerOrderChatPage(recipientPage);
                Response peerSessionResponse = recipientPage.waitForResponse(
                        response -> response.request().method().equals("POST")
                                && response.url().contains("/api/v1/chat/sessions"),
                        new Page.WaitForResponseOptions().setTimeout(20000),
                        () -> {
                            new RestaurantOrderActionsPage(recipientPage).openChat(order.shortOrderId());
                            peerChat.openChat(order.orderId());
                        });
                recipientChat = peerChat;
                if (!peerSessionResponse.ok()) {
                    closeChatIfOpen(customerChat);
                    closeChatIfOpen(recipientChat);
                    return attempt(customerSessionStatus, customerTelemetry, recipientTelemetry,
                            false, false, "restaurant session request was rejected");
                }
                waitForConnected(recipientPage, recipientChat, recipientTelemetry);
            }

            customerChat.sendMessage(message);
            boolean recipientReceivedMessage = recipientPage == null;
            if (recipientPage != null) {
                com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                        recipientPage.getByText(message,
                                new Page.GetByTextOptions().setExact(true))).isVisible();
                recipientReceivedMessage = true;
            }
            closeChatIfOpen(customerChat);
            if (recipientChat != null) closeChatIfOpen(recipientChat);
            return attempt(customerSessionStatus, customerTelemetry, recipientTelemetry,
                    true, recipientReceivedMessage, "");
        } catch (RuntimeException | AssertionError failure) {
            closeChatIfOpen(customerChat);
            if (recipientChat != null) closeChatIfOpen(recipientChat);
            return attempt(customerSessionStatus, customerTelemetry, recipientTelemetry,
                    false, false, failure.getClass().getSimpleName());
        }
    }

    private static void waitForConnected(Page page, CustomerOrderChatPage chat, ChatTelemetry telemetry) {
        page.waitForCondition(
                () -> telemetry.websocketState().get().startsWith("STOMP "),
                new Page.WaitForConditionOptions().setTimeout(20000));
        assertThat(telemetry.websocketState().get()).isEqualTo("STOMP CONNECTED");
        page.waitForCondition(chat.composer()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(15000));
    }

    private static ChatAttempt attempt(Integer customerSessionStatus, ChatTelemetry customerTelemetry,
                                       ChatTelemetry recipientTelemetry, boolean customerMessageVisible,
                                       boolean recipientMessageVisible, String failure) {
        return new ChatAttempt(
                customerSessionStatus,
                customerTelemetry.websocketResponseStatus().get(),
                customerTelemetry.websocketState().get(),
                recipientTelemetry == null ? null : recipientTelemetry.sessionResponseStatus().get(),
                recipientTelemetry == null ? null : recipientTelemetry.websocketResponseStatus().get(),
                recipientTelemetry == null ? null : recipientTelemetry.websocketState().get(),
                customerMessageVisible, recipientMessageVisible, failure);
    }

    private static void closeChatIfOpen(CustomerOrderChatPage chat) {
        try {
            if (chat.isChatOpen()) chat.closeChat();
        } catch (RuntimeException ignored) {
            // Preserve the original chat failure; the order fixture still needs to finish safely.
        }
    }

    private static void assertSuccessfulChatAttempt(ChatAttempt attempt, String orderState,
                                                     boolean expectRecipientMessage) {
        assertThat(attempt)
                .as("chat attempt %s; customer session=%s, customer WebSocket=%s (%s), "
                                + "recipient session=%s, recipient WebSocket=%s (%s), failure=%s",
                        orderState,
                        attempt == null ? null : attempt.customerSessionStatus(),
                        attempt == null ? null : attempt.customerWebsocketResponseStatus(),
                        attempt == null ? "not started" : attempt.customerWebsocketState(),
                        attempt == null ? null : attempt.recipientSessionStatus(),
                        attempt == null ? null : attempt.recipientWebsocketResponseStatus(),
                        attempt == null ? "not started" : attempt.recipientWebsocketState(),
                        attempt == null ? "no result" : attempt.failure())
                .isNotNull();
        assertThat(attempt.customerSessionStatus())
                .as("creating the customer chat session %s", orderState)
                .isBetween(200, 299);
        assertThat(attempt.customerWebsocketState())
                .as("authenticated customer chat WebSocket handshake %s", orderState)
                .isEqualTo("STOMP CONNECTED");
        assertThat(attempt.customerMessageVisible())
                .as("customer message echoed in chat %s", orderState)
                .isTrue();

        if (expectRecipientMessage) {
            assertThat(attempt.recipientSessionStatus())
                    .as("creating the restaurant chat session %s", orderState)
                    .isBetween(200, 299);
            assertThat(attempt.recipientWebsocketState())
                    .as("authenticated restaurant chat WebSocket handshake %s", orderState)
                    .isEqualTo("STOMP CONNECTED");
            assertThat(attempt.recipientMessageVisible())
                    .as("restaurant receives the customer message %s", orderState)
                    .isTrue();
        }
    }

    private record ChatTelemetry(AtomicReference<Integer> sessionResponseStatus,
                                 AtomicReference<Integer> websocketResponseStatus,
                                 AtomicReference<String> websocketState) {
        private ChatTelemetry() {
            this(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>("not opened"));
        }
    }

    private record ChatAttempt(Integer customerSessionStatus, Integer customerWebsocketResponseStatus,
                               String customerWebsocketState, Integer recipientSessionStatus,
                               Integer recipientWebsocketResponseStatus, String recipientWebsocketState,
                               boolean customerMessageVisible, boolean recipientMessageVisible,
                               String failure) { }
}
