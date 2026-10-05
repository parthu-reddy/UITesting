package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.WebSocketRoute;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Shared assertions for the happy lifecycle; no order creation or standalone test. */
public final class OrderChatChecks implements AutoCloseable {
    private final Page customer;
    private final Page restaurant;
    private final Page rider;
    private final ChatTelemetry customerTelemetry;
    private final ChatTelemetry restaurantTelemetry;
    private final ChatTelemetry riderTelemetry;
    private ChatAttempt restaurantConversation;
    private ChatAttempt riderConversation;
    private ChatAttempt deliveredConversation;

    public OrderChatChecks(Page customer, Page restaurant, Page rider) {
        this.customer=customer; this.restaurant=restaurant; this.rider=rider;
        customerTelemetry=observeChatTraffic(customer);
        restaurantTelemetry=observeChatTraffic(restaurant);
        riderTelemetry=observeChatTraffic(rider);
    }

    /** The dispatch is accepted first, so chat checks cannot exhaust an offer's countdown. */
    public void afterDispatch(String orderId,String outlet) {
        var order=new CompletedDeliveryFixture.Result(orderId,outlet);
        restaurantConversation=tryRestaurantRoundTrip(customer,restaurant,order,
                customerTelemetry,restaurantTelemetry);
        riderConversation=tryRiderRoundTrip(customer,rider,order,customerTelemetry,riderTelemetry);
    }

    public void afterDelivery(String orderId,String outlet) {
        deliveredConversation=trySendMessage(customer,new CompletedDeliveryFixture.Result(orderId,outlet),
                "E2E delivery-window check "+orderId.substring(0,8),customerTelemetry);
    }

    /** An assigned-order resume retains the earlier participant round-trip proof separately. */
    public void assertDeliveredPassed() {
        assertSuccessfulChatAttempt(deliveredConversation,"after delivery",false);
    }

    /** Assert captured failures after delivering the package; a failed chat check stays failed. */
    public void assertPassed() {
        org.junit.jupiter.api.Assertions.assertAll("Order chat",
                () -> {
                    assertSuccessfulRoundTrip(restaurantConversation,"with the restaurant");
                    assertThat(restaurantConversation.emptyMessageBlocked()).isTrue();
                    assertThat(restaurantConversation.whitespaceMessageBlocked()).isTrue();
                },
                () -> assertSuccessfulRoundTrip(riderConversation,"with the assigned rider"),
                () -> assertSuccessfulChatAttempt(deliveredConversation,"after delivery",false));
    }

    public java.util.Map<String,Object> evidence() {
        var data=new java.util.LinkedHashMap<String,Object>();
        data.put("reliability","DEFERRED O4-INT-002: synthetic retry/socket-close fixtures excluded from deployed UI journey");
        data.put("restaurant",String.valueOf(restaurantConversation));
        data.put("rider",String.valueOf(riderConversation));
        data.put("delivered",String.valueOf(deliveredConversation));
        data.put("orderCreatedByHelper",false);
        return data;
    }

    @Override public void close() { /* Only browser contexts close; no server data cleanup. */ }

    private static void observeStompFrame(ChatTelemetry telemetry,String text) {
        if(text.stripLeading().startsWith("CONNECTED")) {
            telemetry.websocketState().set("STOMP CONNECTED");
            telemetry.stompConnectedCount().incrementAndGet();
        } else if(text.stripLeading().startsWith("ERROR")) telemetry.websocketState().set("STOMP ERROR");
    }
    private static ChatWindowAttempt tryChatWindowInteractions(
            Page customerPage, Page restaurantPage, CompletedDeliveryFixture.Result order,
            ChatTelemetry customerTelemetry, ChatTelemetry restaurantTelemetry,
            AtomicReference<WebSocketRoute> customerSocket, AtomicInteger customerSocketConnections,
            Consumer<Route> rejectInitialSession) {
        CustomerOrderChatPage customerChat = new CustomerOrderChatPage(customerPage);
        CustomerOrderChatPage restaurantChat = new CustomerOrderChatPage(restaurantPage);
        Integer customerSessionStatus = null;
        Integer restaurantSessionStatus = null;
        Integer imageUploadStatus = null;
        boolean typingVisible = false;
        boolean typingCleared = false;
        boolean sessionRetryVisible = false;
        boolean sessionRetrySucceeded = false;
        boolean overlongMessageBlocked = false;
        boolean overlongMessageExplained = false;
        boolean historySurvivedReload = false;
        boolean unreadVisible = false;
        boolean unreadCleared = false;
        boolean reconnectBannerVisible = false;
        boolean composerDisabledDuringReconnect = false;
        boolean reconnected = false;
        boolean postReconnectMessageVisible = false;
        boolean customerImageVisible = false;
        boolean restaurantImageVisible = false;
        boolean uploadErrorToastVisible = false;
        String stage = "opening customer chat";

        try {
            customerChat.openChatLauncher(order.orderId());
            customerPage.waitForCondition(customerChat.sessionInitializationAlert()::isVisible,
                    new Page.WaitForConditionOptions().setTimeout(10_000));
            sessionRetryVisible = true;
            customerPage.unroute("**/api/v1/chat/sessions", rejectInitialSession);
            Response retriedSession = customerPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().contains("/api/v1/chat/sessions"),
                    new Page.WaitForResponseOptions().setTimeout(20_000),
                    customerChat::retrySession);
            customerSessionStatus = retriedSession.status();
            sessionRetrySucceeded = retriedSession.ok();
            if (!isSuccessfulStatus(customerSessionStatus)) {
                return snapshotChatWindow(customerSessionStatus, restaurantSessionStatus, imageUploadStatus,
                        sessionRetryVisible, sessionRetrySucceeded, overlongMessageBlocked,
                        overlongMessageExplained, historySurvivedReload, typingVisible, typingCleared,
                        unreadVisible, unreadCleared, reconnectBannerVisible,
                        composerDisabledDuringReconnect, reconnected, postReconnectMessageVisible,
                        customerImageVisible, restaurantImageVisible, uploadErrorToastVisible,
                        "customer session request was rejected");
            }
            stage = "waiting for customer chat connection";
            waitForConnected(customerPage, customerChat, customerTelemetry);

            stage = "checking the server message-length boundary in the composer";
            customerChat.composer().fill("x".repeat(10_001));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerChat.sendButton())
                    .isDisabled();
            overlongMessageBlocked = true;
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText(
                    "Messages can contain up to 10,000 characters.",
                    new Page.GetByTextOptions().setExact(true))).isVisible();
            overlongMessageExplained = true;
            customerChat.composer().fill("");

            stage = "opening restaurant chat";
            Response restaurantSession = restaurantPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().contains("/api/v1/chat/sessions"),
                    new Page.WaitForResponseOptions().setTimeout(20_000),
                    () -> {
                        new RestaurantOrderActionsPage(restaurantPage).openChat(order.orderId());
                        restaurantChat.openChat(order.orderId());
                    });
            restaurantSessionStatus = restaurantSession.status();
            if (!restaurantSession.ok()) {
                return snapshotChatWindow(customerSessionStatus, restaurantSessionStatus, imageUploadStatus,
                        sessionRetryVisible, sessionRetrySucceeded, overlongMessageBlocked,
                        overlongMessageExplained, historySurvivedReload, typingVisible, typingCleared,
                        unreadVisible, unreadCleared, reconnectBannerVisible,
                        composerDisabledDuringReconnect, reconnected, postReconnectMessageVisible,
                        customerImageVisible, restaurantImageVisible, uploadErrorToastVisible,
                        "restaurant session request was rejected");
            }
            stage = "waiting for restaurant chat connection";
            waitForConnected(restaurantPage, restaurantChat, restaurantTelemetry);

            stage = "accepting exactly the server maximum message length";
            String maximumLengthMessage = "x".repeat(10_000);
            customerChat.composer().fill(maximumLengthMessage);
            assertThat(customerChat.sendButton().isDisabled())
                    .as("the composer accepts a message at the 10,000-character server limit")
                    .isFalse();
            customerChat.sendButton().click();
            assertMessageVisible(restaurantPage, maximumLengthMessage);

            stage = "proving messages survive a customer page reload";
            String persistedMessage = "E2E persisted chat message " + order.shortOrderId();
            customerChat.sendMessage(persistedMessage);
            assertMessageVisible(restaurantPage, persistedMessage);
            customerPage.reload();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                    customerChat.launcher(order.orderId())).isVisible();
            customerChat.openChat(order.orderId());
            customerPage.waitForCondition(customerChat.composer()::isEnabled,
                    new Page.WaitForConditionOptions().setTimeout(20_000));
            assertMessageVisible(customerPage, persistedMessage);
            historySurvivedReload = true;

            stage = "sending typing indicator";
            customerChat.composer().fill("E2E typing indicator " + order.shortOrderId());
            Locator typingIndicator = restaurantPage.getByText("Someone is typing...",
                    new Page.GetByTextOptions().setExact(true));
            restaurantPage.waitForCondition(typingIndicator::isVisible,
                    new Page.WaitForConditionOptions().setTimeout(10_000));
            typingVisible = true;

            String typingMessage = "E2E typing completion " + order.shortOrderId();
            customerChat.sendMessage(typingMessage);
            assertMessageVisible(restaurantPage, typingMessage);
            restaurantPage.waitForCondition(typingIndicator::isHidden,
                    new Page.WaitForConditionOptions().setTimeout(10_000));
            typingCleared = true;

            stage = "recording unread message while the customer chat is closed";
            customerChat.closeChat();
            customerPage.waitForCondition(() -> customerChat.launcher(order.orderId()).isVisible(),
                    new Page.WaitForConditionOptions().setTimeout(5_000));
            String unreadMessage = "E2E unread message " + order.shortOrderId();
            restaurantChat.sendMessage(unreadMessage);
            customerPage.waitForCondition(
                    () -> customerChat.unreadCount(order.orderId()).isVisible()
                            && "1".equals(customerChat.unreadCount(order.orderId()).innerText().trim()),
                    new Page.WaitForConditionOptions().setTimeout(10_000));
            unreadVisible = true;
            customerChat.openChat(order.orderId());
            assertMessageVisible(customerPage, unreadMessage);
            customerPage.waitForCondition(
                    () -> customerChat.unreadCount(order.orderId()).count() == 0,
                    new Page.WaitForConditionOptions().setTimeout(5_000));
            unreadCleared = true;

            stage = "closing an established customer socket";
            WebSocketRoute activeSocket = customerSocket.get();
            if (activeSocket == null) {
                throw new AssertionError("the customer chat WebSocket route was not established");
            }
            int connectionsBeforeClose = customerSocketConnections.get();
            int stompConnectionsBeforeClose = customerTelemetry.stompConnectedCount().get();
            activeSocket.close();
            Locator reconnectBanner = customerChat.reconnectingStatus();
            customerPage.waitForCondition(reconnectBanner::isVisible,
                    new Page.WaitForConditionOptions().setTimeout(10_000));
            reconnectBannerVisible = true;
            composerDisabledDuringReconnect = customerChat.composer().isDisabled();

            customerPage.waitForCondition(() -> customerSocketConnections.get() > connectionsBeforeClose,
                    new Page.WaitForConditionOptions().setTimeout(15_000));
            customerPage.waitForCondition(
                    () -> customerTelemetry.stompConnectedCount().get() > stompConnectionsBeforeClose,
                    new Page.WaitForConditionOptions().setTimeout(15_000));
            customerPage.waitForCondition(customerChat.composer()::isEnabled,
                    new Page.WaitForConditionOptions().setTimeout(15_000));
            reconnected = true;

            stage = "sending after socket reconnection";
            String postReconnectMessage = "E2E message after reconnect " + order.shortOrderId();
            customerChat.sendMessage(postReconnectMessage);
            assertMessageVisible(restaurantPage, postReconnectMessage);
            String postReconnectReply = "E2E reply after reconnect " + order.shortOrderId();
            restaurantChat.sendMessage(postReconnectReply);
            assertMessageVisible(customerPage, postReconnectReply);
            postReconnectMessageVisible = true;

            stage = "uploading an image attachment";
            Response imageUpload = customerPage.waitForResponse(
                    response -> response.request().method().equals("POST")
                            && response.url().contains("/upload-image"),
                    new Page.WaitForResponseOptions().setTimeout(20_000),
                    () -> customerChat.galleryFileInput().setInputFiles(Paths.get("src/test/resources/dummy.png")));
            imageUploadStatus = imageUpload.status();
            String imageMessageId = uploadedMessageId(imageUpload);
            customerPage.waitForCondition(
                    () -> customerChat.messageById(imageMessageId).isVisible(),
                    new Page.WaitForConditionOptions().setTimeout(15_000));
            customerImageVisible = true;
            restaurantPage.waitForCondition(
                    () -> restaurantChat.messageById(imageMessageId).isVisible(),
                    new Page.WaitForConditionOptions().setTimeout(15_000));
            restaurantImageVisible = true;
            customerPage.waitForCondition(
                    () -> "false".equals(customerChat.composerForm().getAttribute("aria-busy"))
                            && customerChat.composer().isEnabled(),
                    new Page.WaitForConditionOptions().setTimeout(5_000));
            uploadErrorToastVisible = customerPage.getByText(
                    "Could not upload that image. Check it is under 5MB and try again.",
                    new Page.GetByTextOptions().setExact(true)).isVisible();

            return snapshotChatWindow(customerSessionStatus, restaurantSessionStatus, imageUploadStatus,
                    sessionRetryVisible, sessionRetrySucceeded, overlongMessageBlocked,
                    overlongMessageExplained, historySurvivedReload, typingVisible, typingCleared,
                    unreadVisible, unreadCleared, reconnectBannerVisible,
                    composerDisabledDuringReconnect, reconnected, postReconnectMessageVisible,
                    customerImageVisible, restaurantImageVisible, uploadErrorToastVisible, "");
        } catch (RuntimeException | AssertionError failure) {
            return snapshotChatWindow(customerSessionStatus, restaurantSessionStatus, imageUploadStatus,
                    sessionRetryVisible, sessionRetrySucceeded, overlongMessageBlocked,
                    overlongMessageExplained, historySurvivedReload, typingVisible, typingCleared,
                    unreadVisible, unreadCleared, reconnectBannerVisible,
                    composerDisabledDuringReconnect, reconnected, postReconnectMessageVisible,
                    customerImageVisible, restaurantImageVisible, uploadErrorToastVisible,
                    failure.getClass().getSimpleName() + " at " + stage
                            + (failure.getMessage() == null ? "" : ": " + failure.getMessage()));
        } finally {
            closeChatIfOpen(customerChat);
            closeChatIfOpen(restaurantChat);
        }
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

            if (!restaurantChat.launcher(order.orderId()).isVisible()) {
                new RestaurantOrderActionsPage(restaurantPage).openChat(order.orderId());
            }
            restaurantSessionStatus = openParticipantChat(restaurantPage, restaurantChat,
                    order.orderId(), restaurantTelemetry);
            assertThat(restaurantSessionStatus).isBetween(200,299);
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
                if (trimmed.startsWith("CONNECTED")) {
                    telemetry.websocketState().set("STOMP CONNECTED");
                    telemetry.stompConnectedCount().incrementAndGet();
                }
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

    private static ChatWindowAttempt snapshotChatWindow(
            Integer customerSessionStatus, Integer restaurantSessionStatus, Integer imageUploadStatus,
            boolean sessionRetryVisible, boolean sessionRetrySucceeded,
            boolean overlongMessageBlocked, boolean overlongMessageExplained,
            boolean historySurvivedReload, boolean typingVisible, boolean typingCleared,
            boolean unreadVisible, boolean unreadCleared,
            boolean reconnectBannerVisible, boolean composerDisabledDuringReconnect, boolean reconnected,
            boolean postReconnectMessageVisible, boolean customerImageVisible, boolean restaurantImageVisible,
            boolean uploadErrorToastVisible, String failure) {
        return new ChatWindowAttempt(customerSessionStatus, restaurantSessionStatus, imageUploadStatus,
                sessionRetryVisible, sessionRetrySucceeded, overlongMessageBlocked,
                overlongMessageExplained, historySurvivedReload, typingVisible, typingCleared,
                unreadVisible, unreadCleared, reconnectBannerVisible,
                composerDisabledDuringReconnect, reconnected, postReconnectMessageVisible,
                customerImageVisible, restaurantImageVisible, uploadErrorToastVisible, failure);
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

    private static String uploadedMessageId(Response uploadResponse) {
        Matcher matcher = Pattern.compile("\\\"messageId\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .matcher(uploadResponse.text());
        if (!matcher.find()) {
            throw new AssertionError("a successful image upload response did not include data.messageId");
        }
        return matcher.group(1);
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

    private static void assertSuccessfulChatWindowInteraction(ChatWindowAttempt attempt) {
        assertThat(attempt)
                .as("chat window interaction result")
                .isNotNull();
        assertThat(attempt.failure())
                .as("chat window interaction reports no failure")
                .isEmpty();
        assertThat(attempt.customerSessionStatus())
                .as("customer chat session is created")
                .isBetween(200, 299);
        assertThat(attempt.restaurantSessionStatus())
                .as("restaurant chat session is created")
                .isBetween(200, 299);
        assertThat(attempt.sessionRetryVisible())
                .as("a failed initial session request shows the retry action")
                .isTrue();
        assertThat(attempt.sessionRetrySucceeded())
                .as("retrying the initial session request opens chat")
                .isTrue();
        assertThat(attempt.overlongMessageBlocked())
                .as("the composer blocks text above the server's 10,000-character limit")
                .isTrue();
        assertThat(attempt.overlongMessageExplained())
                .as("the composer explains the text-length limit")
                .isTrue();
        assertThat(attempt.historySurvivedReload())
                .as("messages are loaded from history after the customer reloads the page")
                .isTrue();
        assertThat(attempt.typingVisible())
                .as("restaurant sees the customer typing")
                .isTrue();
        assertThat(attempt.typingCleared())
                .as("sending a message clears the peer typing indicator")
                .isTrue();
        assertThat(attempt.unreadVisible())
                .as("a peer message increments the closed customer chat unread count")
                .isTrue();
        assertThat(attempt.unreadCleared())
                .as("opening the customer chat clears its unread count")
                .isTrue();
        assertThat(attempt.reconnectBannerVisible())
                .as("an established socket close shows the reconnect state")
                .isTrue();
        assertThat(attempt.composerDisabledDuringReconnect())
                .as("the composer cannot send while the socket is disconnected")
                .isTrue();
        assertThat(attempt.reconnected())
                .as("the widget reconnects after the socket closes")
                .isTrue();
        assertThat(attempt.postReconnectMessageVisible())
                .as("both participants receive a message after the customer reconnects")
                .isTrue();
        assertThat(attempt.imageUploadStatus())
                .as("image upload returns a successful HTTP response")
                .isBetween(200, 299);
        assertThat(attempt.customerImageVisible())
                .as("the customer sees the uploaded attachment")
                .isTrue();
        assertThat(attempt.restaurantImageVisible())
                .as("the restaurant receives the uploaded attachment")
                .isTrue();
        assertThat(attempt.uploadErrorToastVisible())
                .as("a successful image upload does not show an upload-failed toast")
                .isFalse();
    }

    private record ChatTelemetry(AtomicReference<Integer> sessionResponseStatus,
                                 AtomicReference<Integer> websocketResponseStatus,
                                 AtomicReference<String> websocketState,
                                 AtomicInteger stompConnectedCount) {
        private ChatTelemetry() {
            this(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>("not opened"),
                    new AtomicInteger());
        }
    }

    private record ChatAttempt(Integer customerSessionStatus, Integer customerWebsocketResponseStatus,
                               String customerWebsocketState, Integer recipientSessionStatus,
                               Integer recipientWebsocketResponseStatus, String recipientWebsocketState,
                               boolean customerChatOpened, boolean emptyMessageBlocked,
                               boolean whitespaceMessageBlocked, boolean customerMessageVisible,
                               boolean recipientMessageVisible, boolean peerReplyVisible,
                               boolean customerReplyVisible, String failure) { }

    private record ChatWindowAttempt(Integer customerSessionStatus, Integer restaurantSessionStatus,
                                     Integer imageUploadStatus, boolean sessionRetryVisible,
                                     boolean sessionRetrySucceeded, boolean overlongMessageBlocked,
                                     boolean overlongMessageExplained, boolean historySurvivedReload,
                                     boolean typingVisible,
                                     boolean typingCleared, boolean unreadVisible, boolean unreadCleared,
                                     boolean reconnectBannerVisible,
                                     boolean composerDisabledDuringReconnect, boolean reconnected,
                                     boolean postReconnectMessageVisible, boolean customerImageVisible,
                                     boolean restaurantImageVisible, boolean uploadErrorToastVisible,
                                     String failure) { }
}
