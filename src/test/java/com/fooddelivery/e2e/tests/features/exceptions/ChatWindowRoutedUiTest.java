package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderChatPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Browser-routed coverage for the chat window's local state machine.
 *
 * <p>The customer signs in normally so this renders the deployed customer page, while the active
 * order, chat-session endpoints, message history, and STOMP server are in-browser fixtures. The
 * tests therefore exercise the real browser UI without creating an order or calling the chat
 * service.</p>
 */
@Tag("browser-routed")
@Tag("feature-chat")
public class ChatWindowRoutedUiTest extends TestBase {

    private static final String ORDER_ID = "c1000000-0000-4000-8000-000000000001";
    private static final String RESTAURANT_ID = "c1000000-0000-4000-8000-000000000003";
    private static final String ORDER_ITEM_ID = "c1000000-0000-4000-8000-000000000004";
    private static final String MENU_ITEM_ID = "c1000000-0000-4000-8000-000000000005";
    private static final String SESSION_ID = "c1000000-0000-4000-8000-000000000010";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";

    @Test
    @DisplayName("CHAT-UI-01: session failure exposes retry and a retry restores the composer")
    void sessionInitializationFailureShowsRetryAndSecondAttemptConnects() {
        ChatFixture fixture = new ChatFixture(true, true);
        CustomerOrderChatPage chat = openFixtureChat(fixture);

        PlaywrightAssertions.assertThat(chat.sessionInitializationAlert()).isVisible();
        PlaywrightAssertions.assertThat(chat.composer()).isDisabled();
        org.assertj.core.api.Assertions.assertThat(fixture.sessionRequests.get())
                .as("the first session request is the deliberately failed fixture response")
                .isEqualTo(1);
        assertCreateSessionPayload(fixture);

        chat.retrySession();

        customerPage.waitForCondition(() -> fixture.sessionRequests.get() == 2,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        waitForComposerEnabled(chat, 10_000);
        PlaywrightAssertions.assertThat(chat.sessionInitializationAlert()).isHidden();
    }

    @Test
    @DisplayName("CHAT-UI-02: a disconnected initial socket shows reconnecting until STOMP connects")
    void initialSocketFailureShowsReconnectStatusThenRecovers() {
        ChatFixture fixture = new ChatFixture(false, false);
        CustomerOrderChatPage chat = openFixtureChat(fixture);

        customerPage.waitForCondition(() -> fixture.rejectedSocketConnections.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        PlaywrightAssertions.assertThat(chat.reconnectingStatus()).isVisible();
        PlaywrightAssertions.assertThat(chat.composer()).isDisabled();

        fixture.allowSocketConnections.set(true);

        customerPage.waitForCondition(() -> fixture.socketConnections.get() >= 2,
                new Page.WaitForConditionOptions().setTimeout(12_000));
        waitForComposerEnabled(chat, 12_000);
        PlaywrightAssertions.assertThat(chat.reconnectingStatus()).isHidden();
    }

    @Test
    @DisplayName("CHAT-UI-03: Shift+Enter retains a line break and Enter publishes one text message")
    void shiftEnterAddsLineBreakAndEnterPublishesOnce() {
        ChatFixture fixture = new ChatFixture(false, true);
        CustomerOrderChatPage chat = openFixtureChat(fixture);
        waitForComposerEnabled(chat, 10_000);

        chat.composer().fill("first line");
        chat.composer().press("Shift+Enter");
        customerPage.keyboard().type("second line");

        PlaywrightAssertions.assertThat(chat.composer()).hasValue("first line\nsecond line");
        org.assertj.core.api.Assertions.assertThat(fixture.sentMessageFrame.get())
                .as("Shift+Enter must not publish a text message")
                .isNull();

        chat.composer().press("Enter");

        customerPage.waitForCondition(() -> fixture.sentMessageFrame.get() != null,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        org.assertj.core.api.Assertions.assertThat(fixture.sentMessageFrame.get())
                .contains("destination:/app/chat.send/" + SESSION_ID)
                .contains("\"content\":\"first line\\nsecond line\"")
                .contains("\"messageType\":\"TEXT\"");
        org.assertj.core.api.Assertions.assertThat(fixture.sentMessageCount.get())
                .as("Enter publishes exactly one text message")
                .isEqualTo(1);
        PlaywrightAssertions.assertThat(chat.composer()).hasValue("");
    }

    private CustomerOrderChatPage openFixtureChat(ChatFixture fixture) {
        // Keep the tracker below the xl breakpoint. The fixture is about the chat window, so a
        // live-order map and its SSE stream would only add unrelated transport to this test.
        customerPage.setViewportSize(1_024, 900);
        customerPage.addInitScript("""
                localStorage.setItem('deliveryLat', '12.9716');
                localStorage.setItem('deliveryLng', '77.5946');
                localStorage.setItem('deliveryAddress', 'Chat fixture address');
                localStorage.setItem('deliveryAddressId', 'c1000000-0000-4000-8000-000000000099');
                """);
        registerFixtureRoutes(fixture);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        fixture.authenticatedCustomerId.set(String.valueOf(customerPage.evaluate(
                "() => JSON.parse(localStorage.getItem('user_profile')).id")));
        new CustomerDashboardPage(customerPage).waitForDashboard();

        CustomerOrderChatPage chat = new CustomerOrderChatPage(customerPage);
        PlaywrightAssertions.assertThat(chat.launcher(ORDER_ID)).isVisible();
        chat.openChatLauncher(ORDER_ID);
        return chat;
    }

    private void registerFixtureRoutes(ChatFixture fixture) {
        customerPage.route(url -> url.contains("/api/v1/orders/active"),
                route -> fulfillJson(route, 200, activeOrdersResponse(fixture.authenticatedCustomerId.get())));
        customerPage.route(url -> url.contains("/api/v1/chat/sessions/" + SESSION_ID + "/messages"),
                route -> fulfillJson(route, 200, emptyHistoryResponse()));
        customerPage.route(ChatWindowRoutedUiTest::isCreateSessionUrl, route -> {
            if (!"POST".equals(route.request().method())) {
                route.resume();
                return;
            }

            int requestNumber = fixture.sessionRequests.incrementAndGet();
            fixture.sessionRequestBody.set(route.request().postData());
            if (fixture.failFirstSession && requestNumber == 1) {
                fulfillJson(route, 503, failedSessionResponse());
            } else {
                fulfillJson(route, 201, successfulSessionResponse(fixture.authenticatedCustomerId.get()));
            }
        });
        customerPage.routeWebSocket(url -> url.contains("/ws/chat"), socket -> {
            fixture.socketConnections.incrementAndGet();
            if (!fixture.allowSocketConnections.get()) {
                fixture.rejectedSocketConnections.incrementAndGet();
                socket.close();
                return;
            }

            socket.onMessage(frame -> {
                String message = frame.text();
                if (isStompCommand(message, "CONNECT")) {
                    socket.send("CONNECTED\nversion:1.2\n\n\0");
                } else if (isStompCommand(message, "SEND")
                        && message.contains("destination:/app/chat.send/")) {
                    fixture.sentMessageCount.incrementAndGet();
                    fixture.sentMessageFrame.set(message);
                }
            });
        });
    }

    private void waitForComposerEnabled(CustomerOrderChatPage chat, int timeoutMillis) {
        customerPage.waitForCondition(chat.composer()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(timeoutMillis));
        PlaywrightAssertions.assertThat(chat.composer()).isEnabled();
    }

    private static void assertCreateSessionPayload(ChatFixture fixture) {
        String payload = fixture.sessionRequestBody.get();
        org.assertj.core.api.Assertions.assertThat(payload)
                .as("the chat session request is recorded")
                .isNotBlank()
                .contains("\"orderId\":\"" + ORDER_ID + "\"")
                .doesNotContain("\"userId\"", "\"participants\"", "\"customerId\"");
    }

    private static boolean isCreateSessionUrl(String url) {
        int queryStart = url.indexOf('?');
        String requestPath = queryStart >= 0 ? url.substring(0, queryStart) : url;
        return requestPath.endsWith("/api/v1/chat/sessions");
    }

    private static boolean isStompCommand(String frame, String command) {
        return frame != null && frame.stripLeading().replace("\r\n", "\n").startsWith(command + "\n");
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private static String activeOrdersResponse(String customerId) {
        return """
                {
                  "success": true,
                  "message": "fixture",
                  "data": {
                    "content": [{
                      "id": "%s",
                      "customerId": "%s",
                      "restaurantId": "%s",
                      "restaurantName": "Chat UI Fixture Restaurant",
                      "status": "ACCEPTED",
                      "deliveryStatus": "PENDING",
                      "totalAmount": 199.0,
                      "itemTotal": 159.0,
                      "customerPlatformFee": 0.0,
                      "sgst": 0.0,
                      "cgst": 0.0,
                      "deliveryFee": 40.0,
                      "deliveryAddress": "Chat fixture address",
                      "items": [{
                        "id": "%s",
                        "menuItemId": "%s",
                        "name": "Chat fixture dish",
                        "quantity": 1,
                        "price": 159.0
                      }],
                      "createdAt": "%s",
                      "updatedAt": "%s"
                    }],
                    "totalElements": 1,
                    "totalPages": 1,
                    "last": true,
                    "size": 50,
                    "number": 0,
                    "first": true,
                    "numberOfElements": 1,
                    "empty": false
                  },
                  "timestamp": "%s"
                }
                """.formatted(ORDER_ID, customerId, RESTAURANT_ID, ORDER_ITEM_ID, MENU_ITEM_ID,
                FIXTURE_TIME, FIXTURE_TIME, FIXTURE_TIME);
    }

    private static String successfulSessionResponse(String customerId) {
        return """
                {
                  "success": true,
                  "message": "fixture",
                  "data": {
                    "sessionId": "%s",
                    "sessionType": "ORDER",
                    "referenceId": "%s",
                    "isActive": true,
                    "createdAt": "%s",
                    "participants": [{
                      "userId": "%s",
                      "entityId": "%s",
                      "entityType": "CUSTOMER",
                      "displayName": "Fixture customer"
                    }, {
                      "entityId": "%s",
                      "entityType": "RESTAURANT",
                      "displayName": "Fixture restaurant"
                    }]
                  },
                  "timestamp": "%s"
                }
                """.formatted(SESSION_ID, ORDER_ID, FIXTURE_TIME, customerId, customerId, RESTAURANT_ID, FIXTURE_TIME);
    }

    private static String failedSessionResponse() {
        return """
                {
                  "success": false,
                  "message": "Fixture session unavailable",
                  "timestamp": "%s"
                }
                """.formatted(FIXTURE_TIME);
    }

    private static String emptyHistoryResponse() {
        return """
                {
                  "success": true,
                  "message": "fixture",
                  "data": {
                    "content": [],
                    "totalElements": 0,
                    "totalPages": 0,
                    "last": true,
                    "size": 50,
                    "number": 0,
                    "first": true,
                    "numberOfElements": 0,
                    "empty": true
                  },
                  "timestamp": "%s"
                }
                """.formatted(FIXTURE_TIME);
    }

    private static final class ChatFixture {
        private final boolean failFirstSession;
        private final AtomicBoolean allowSocketConnections;
        private final AtomicInteger sessionRequests = new AtomicInteger();
        private final AtomicInteger socketConnections = new AtomicInteger();
        private final AtomicInteger rejectedSocketConnections = new AtomicInteger();
        private final AtomicInteger sentMessageCount = new AtomicInteger();
        private final AtomicReference<String> authenticatedCustomerId = new AtomicReference<>();
        private final AtomicReference<String> sessionRequestBody = new AtomicReference<>();
        private final AtomicReference<String> sentMessageFrame = new AtomicReference<>();

        private ChatFixture(boolean failFirstSession, boolean initiallyAllowSocketConnections) {
            this.failFirstSession = failFirstSession;
            this.allowSocketConnections = new AtomicBoolean(initiallyAllowSocketConnections);
        }
    }
}
