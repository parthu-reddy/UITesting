package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI-only coverage for order chat while active, after dispatch, and during the delivered-order
 * grace period. The order fixture always finishes delivery before a captured chat failure is
 * asserted, so a chat regression cannot strand an order or rider.
 */
@Tag("feature")
@Tag("chat")
public class ChatCommunicationTest extends TestBase {

    @Test
    @DisplayName("Customer, restaurant, and rider exchange messages through the order lifecycle")
    void orderParticipantsCanChatThroughDelivery() {
        ChatTelemetry customerTelemetry = observeChatTraffic(customerPage);
        ChatTelemetry restaurantTelemetry = observeChatTraffic(restaurantPage);
        ChatTelemetry riderTelemetry = observeChatTraffic(riderPage);
        AtomicReference<ChatAttempt> restaurantConversation = new AtomicReference<>();
        AtomicReference<ChatAttempt> riderConversation = new AtomicReference<>();

        CompletedDeliveryFixture.Result order = CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone,
                activeOrder -> restaurantConversation.set(tryRestaurantRoundTrip(
                        customerPage, restaurantPage, activeOrder, customerTelemetry, restaurantTelemetry)),
                dispatchedOrder -> riderConversation.set(tryRiderRoundTrip(
                        customerPage, riderPage, dispatchedOrder, customerTelemetry, riderTelemetry)));

        ChatAttempt restaurantAttempt = restaurantConversation.get();
        assertSuccessfulRoundTrip(restaurantAttempt, "while the order is active");
        assertThat(restaurantAttempt.emptyMessageBlocked())
                .as("empty messages are disabled while the order is active")
                .isTrue();
        assertThat(restaurantAttempt.whitespaceMessageBlocked())
                .as("whitespace-only messages are disabled while the order is active")
                .isTrue();
        assertSuccessfulRoundTrip(riderConversation.get(), "after rider dispatch");

        ChatAttempt attempt = trySendMessage(customerPage, order,
                "E2E delivery-window check " + order.shortOrderId(), customerTelemetry);
        assertSuccessfulChatAttempt(attempt, "after delivery", false);
    }

    private static ChatAttempt tryRestaurantRoundTrip(Page customerPage, Page restaurantPage,
                                                      CompletedDeliveryFixture.Result order,
                                                      ChatTelemetry customerTelemetry,
                                                      ChatTelemetry restaurantTelemetry) {
        CustomerOrderChatPage customerChat = new CustomerOrderChatPage(customerPage);
        CustomerOrderChatPage restaurantChat = new CustomerOrderChatPage(restaurantPage);
        Integer customerSessionStatus = null;
        Integer restaurantSessionStatus = null;
        boolean customerChatOpened = false;
        boolean emptyMessageBlocked = false;
        boolean whitespaceMessageBlocked = false;
        boolean customerMessageVisible = false;
        boolean restaurantMessageVisible = false;
        boolean restaurantReplyVisible = false;
        boolean customerReplyVisible = false;

        try {
            customerSessionStatus = openParticipantChat(
                    customerPage, customerChat, order.orderId(), customerTelemetry);
            customerChatOpened = true;
            if (!isSuccessfulStatus(customerSessionStatus)) {
                return snapshot(customerTelemetry, restaurantTelemetry, customerSessionStatus,
                        restaurantSessionStatus, customerChatOpened, emptyMessageBlocked,
                        whitespaceMessageBlocked, customerMessageVisible, restaurantMessageVisible,
                        false, false, "customer session request was rejected");
            }
            waitForConnected(customerPage, customerChat, customerTelemetry);

            emptyMessageBlocked = customerChat.sendButton().isDisabled();
            customerChat.composer().fill("   \t");
            whitespaceMessageBlocked = customerChat.sendButton().isDisabled();
            customerChat.composer().fill("");

            Response restaurantSession = restaurantPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().contains("/api/v1/chat/sessions"),
                    new Page.WaitForResponseOptions().setTimeout(20000),
                    () -> {
                        new RestaurantOrderActionsPage(restaurantPage).openChat(order.shortOrderId());
                        restaurantChat.openChat(order.orderId());
                    });
            restaurantSessionStatus = restaurantSession.status();
            if (!restaurantSession.ok()) {
                return snapshot(customerTelemetry, restaurantTelemetry, customerSessionStatus,
                        restaurantSessionStatus, customerChatOpened, emptyMessageBlocked,
                        whitespaceMessageBlocked, customerMessageVisible, restaurantMessageVisible,
                        false, false, "restaurant session request was rejected");
            }
            waitForConnected(restaurantPage, restaurantChat, restaurantTelemetry);

            String longMessage = "E2E long customer message " + "message-part ".repeat(20)
                    + order.shortOrderId();
            assertThat(longMessage.length()).as("long-message fixture length").isGreaterThan(200);
            customerChat.sendMessage(longMessage);
            assertMessageVisible(customerPage, longMessage);
            customerMessageVisible = true;
            assertMessageVisible(restaurantPage, longMessage);
            restaurantMessageVisible = true;

            String reply = "E2E restaurant reply " + order.shortOrderId();
            restaurantChat.sendMessage(reply);
            assertMessageVisible(restaurantPage, reply);
            restaurantReplyVisible = true;
            assertMessageVisible(customerPage, reply);
            customerReplyVisible = true;

            return snapshot(customerTelemetry, restaurantTelemetry, customerSessionStatus,
                    restaurantSessionStatus, customerChatOpened, emptyMessageBlocked,
                    whitespaceMessageBlocked, customerMessageVisible, restaurantMessageVisible,
                    restaurantReplyVisible, customerReplyVisible, "");
        } catch (RuntimeException | AssertionError failure) {
            return snapshot(customerTelemetry, restaurantTelemetry, customerSessionStatus,
                    restaurantSessionStatus, customerChatOpened, emptyMessageBlocked,
                    whitespaceMessageBlocked, customerMessageVisible, restaurantMessageVisible,
                    restaurantReplyVisible, customerReplyVisible, failure.getClass().getSimpleName());
        } finally {
            closeChatIfOpen(customerChat);
            closeChatIfOpen(restaurantChat);
        }
    }

    private static ChatAttempt tryRiderRoundTrip(Page customerPage, Page riderPage,
                                                 CompletedDeliveryFixture.Result order,
                                                 ChatTelemetry customerTelemetry,
                                                 ChatTelemetry riderTelemetry) {
        CustomerOrderChatPage customerChat = new CustomerOrderChatPage(customerPage);
        CustomerOrderChatPage riderChat = new CustomerOrderChatPage(riderPage);
        Integer customerSessionStatus = null;
        Integer riderSessionStatus = null;
        boolean customerChatOpened = false;
        boolean customerMessageVisible = false;
        boolean riderMessageVisible = false;
        boolean riderReplyVisible = false;
        boolean customerReplyVisible = false;
        String stage = "waiting for customer tracker to show rider assignment";

        try {
            waitForCustomerOrderRiderAssignment(customerPage, order.orderId());

            stage = "opening customer chat session";
            customerSessionStatus = openParticipantChat(
                    customerPage, customerChat, order.orderId(), customerTelemetry);
            customerChatOpened = true;
            if (!isSuccessfulStatus(customerSessionStatus)) {
                return snapshot(customerTelemetry, riderTelemetry, customerSessionStatus,
                        riderSessionStatus, customerChatOpened, false, false,
                        customerMessageVisible, riderMessageVisible, false, false,
                        "customer session request was rejected");
            }
            stage = "waiting for customer chat connection";
            waitForConnected(customerPage, customerChat, customerTelemetry);

            stage = "opening rider chat session";
            riderSessionStatus = openParticipantChat(
                    riderPage, riderChat, order.orderId(), riderTelemetry);
            if (!isSuccessfulStatus(riderSessionStatus)) {
                return snapshot(customerTelemetry, riderTelemetry, customerSessionStatus,
                        riderSessionStatus, customerChatOpened, false, false,
                        customerMessageVisible, riderMessageVisible, false, false,
                        "rider session request was rejected after customer tracking showed assignment; HTTP "
                                + riderSessionStatus);
            }
            stage = "waiting for rider chat connection";
            waitForConnected(riderPage, riderChat, riderTelemetry);

            stage = "sending customer message to rider";
            String message = "E2E customer-to-rider message " + order.shortOrderId();
            customerChat.sendMessage(message);
            assertMessageVisible(customerPage, message);
            customerMessageVisible = true;
            assertMessageVisible(riderPage, message);
            riderMessageVisible = true;

            stage = "sending rider reply";
            String reply = "E2E rider reply " + order.shortOrderId();
            riderChat.sendMessage(reply);
            assertMessageVisible(riderPage, reply);
            riderReplyVisible = true;
            assertMessageVisible(customerPage, reply);
            customerReplyVisible = true;

            return snapshot(customerTelemetry, riderTelemetry, customerSessionStatus,
                    riderSessionStatus, customerChatOpened, false, false,
                    customerMessageVisible, riderMessageVisible, riderReplyVisible,
                    customerReplyVisible, "");
        } catch (RuntimeException | AssertionError failure) {
            return snapshot(customerTelemetry, riderTelemetry, customerSessionStatus,
                    riderSessionStatus, customerChatOpened, false, false,
                    customerMessageVisible, riderMessageVisible, riderReplyVisible,
                    customerReplyVisible, failure.getClass().getSimpleName() + " at " + stage
                            + (failure.getMessage() == null ? "" : ": " + failure.getMessage()));
        } finally {
            closeChatIfOpen(customerChat);
            closeChatIfOpen(riderChat);
        }
    }

    private static ChatAttempt trySendMessage(Page customerPage, CompletedDeliveryFixture.Result order,
                                              String message, ChatTelemetry telemetry) {
        CustomerOrderChatPage customerChat = new CustomerOrderChatPage(customerPage);
        Integer customerSessionStatus = null;
        boolean customerChatOpened = false;
        boolean customerMessageVisible = false;

        try {
            customerSessionStatus = openParticipantChat(
                    customerPage, customerChat, order.orderId(), telemetry);
            customerChatOpened = true;
            if (!isSuccessfulStatus(customerSessionStatus)) {
                return snapshot(telemetry, null, customerSessionStatus, null,
                        customerChatOpened, false, false, false, false,
                        false, false, "customer session request was rejected");
            }

            waitForConnected(customerPage, customerChat, telemetry);
            customerChat.sendMessage(message);
            assertMessageVisible(customerPage, message);
            customerMessageVisible = true;
            return snapshot(telemetry, null, customerSessionStatus, null,
                    customerChatOpened, false, false, customerMessageVisible, false,
                    false, false, "");
        } catch (RuntimeException | AssertionError failure) {
            return snapshot(telemetry, null, customerSessionStatus, null,
                    customerChatOpened, false, false, customerMessageVisible, false,
                    false, false, failure.getClass().getSimpleName());
        } finally {
            closeChatIfOpen(customerChat);
        }
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

    private static void waitForConnected(Page page, CustomerOrderChatPage chat, ChatTelemetry telemetry) {
        page.waitForCondition(
                () -> telemetry.websocketState().get().startsWith("STOMP "),
                new Page.WaitForConditionOptions().setTimeout(20000));
        assertThat(telemetry.websocketState().get()).isEqualTo("STOMP CONNECTED");
        page.waitForCondition(chat.composer()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(15000));
    }

    private static void waitForCustomerOrderRiderAssignment(Page customerPage, String orderId) {
        Locator riderCard = customerPage.locator(
                "[data-testid='order-tracker'][data-order-id='" + orderId + "'] [data-testid='rider-card']");
        // The customer order hook polls accepted/preparing orders every 30 seconds; allow one
        // complete poll while preserving the selected tracker used by the delivery fixture.
        customerPage.waitForCondition(riderCard::isVisible,
                new Page.WaitForConditionOptions().setTimeout(35000));
        assertThat(riderCard.isVisible())
                .as("the customer live tracker shows the rider assigned to order %s before rider chat opens", orderId)
                .isTrue();
    }

    private static Integer openParticipantChat(Page page, CustomerOrderChatPage chat,
                                               String orderId, ChatTelemetry telemetry) {
        Integer existingSessionStatus = telemetry.sessionResponseStatus().get();
        if (isSuccessfulStatus(existingSessionStatus)) {
            chat.openChat(orderId);
            return existingSessionStatus;
        }

        Response session = page.waitForResponse(
                response -> response.request().method().equals("POST")
                        && response.url().contains("/api/v1/chat/sessions"),
                new Page.WaitForResponseOptions().setTimeout(20000),
                () -> chat.openChatLauncher(orderId));
        return session.status();
    }

    private static boolean isSuccessfulStatus(Integer status) {
        return status != null && status >= 200 && status < 300;
    }

    private static ChatAttempt snapshot(ChatTelemetry customerTelemetry, ChatTelemetry peerTelemetry,
                                       Integer customerSessionStatus, Integer peerSessionStatus,
                                       boolean customerChatOpened, boolean emptyMessageBlocked,
                                       boolean whitespaceMessageBlocked, boolean customerMessageVisible,
                                       boolean peerMessageVisible, boolean peerReplyVisible,
                                       boolean customerReplyVisible, String failure) {
        return new ChatAttempt(
                customerSessionStatus,
                customerTelemetry.websocketResponseStatus().get(),
                customerTelemetry.websocketState().get(),
                peerSessionStatus == null && peerTelemetry == null ? null : peerSessionStatus,
                peerTelemetry == null ? null : peerTelemetry.websocketResponseStatus().get(),
                peerTelemetry == null ? null : peerTelemetry.websocketState().get(),
                customerChatOpened, emptyMessageBlocked, whitespaceMessageBlocked,
                customerMessageVisible, peerMessageVisible, peerReplyVisible,
                customerReplyVisible, failure);
    }

    private static void closeChatIfOpen(CustomerOrderChatPage chat) {
        try {
            if (chat.isChatOpen()) chat.closeChat();
        } catch (RuntimeException ignored) {
            // Preserve the original chat failure; the order fixture still needs to finish safely.
        }
    }

    private static void assertMessageVisible(Page page, String message) {
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                page.getByText(message, new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    private static void assertSuccessfulRoundTrip(ChatAttempt attempt, String orderState) {
        assertSuccessfulChatAttempt(attempt, orderState, true);
        assertThat(attempt.peerReplyVisible())
                .as("peer reply is visible in the sender chat %s", orderState)
                .isTrue();
        assertThat(attempt.customerReplyVisible())
                .as("customer receives the peer reply in real time %s", orderState)
                .isTrue();
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
        assertThat(attempt.failure())
                .as("chat setup and delivery report no failure during %s", orderState)
                .isEmpty();
        assertThat(attempt.customerSessionStatus())
                .as("creating the customer chat session %s", orderState)
                .isBetween(200, 299);
        assertThat(attempt.customerWebsocketState())
                .as("authenticated customer chat WebSocket handshake %s", orderState)
                .isEqualTo("STOMP CONNECTED");
        assertThat(attempt.customerChatOpened())
                .as("customer chat launcher opens for the order %s", orderState)
                .isTrue();
        assertThat(attempt.customerMessageVisible())
                .as("customer message is visible in chat %s", orderState)
                .isTrue();

        if (expectRecipientMessage) {
            assertThat(attempt.recipientSessionStatus())
                    .as("creating the recipient chat session %s", orderState)
                    .isBetween(200, 299);
            assertThat(attempt.recipientWebsocketState())
                    .as("authenticated recipient chat WebSocket handshake %s", orderState)
                    .isEqualTo("STOMP CONNECTED");
            assertThat(attempt.recipientMessageVisible())
                    .as("recipient receives the customer message %s", orderState)
                    .isTrue();
            assertThat(attempt.peerReplyVisible())
                    .as("recipient sends a reply %s", orderState)
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
                               boolean customerChatOpened, boolean emptyMessageBlocked,
                               boolean whitespaceMessageBlocked, boolean customerMessageVisible,
                               boolean recipientMessageVisible, boolean peerReplyVisible,
                               boolean customerReplyVisible, String failure) { }
}
