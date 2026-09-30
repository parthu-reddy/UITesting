package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.fooddelivery.e2e.pages.admin.AdminSupportChatPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.WebSocketRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed isolation coverage for the admin support-ticket chat.
 *
 * <p>The admin signs in normally. After that, support ticket reads, chat-session creation,
 * message history, and the WebSocket are all served in the browser. No shared support ticket,
 * chat session, or message is created by this test.</p>
 */
@Tag("admin")
@Tag("admin-support-chat")
@Tag("browser-routed")
public class AdminSupportChatIsolationRoutedUiTest extends TestBase {

    private static final String SUPPORT_TICKETS_PATH =
            "/api/v1/internal/admin/orders/intervention/support-tickets";
    private static final String CHAT_SESSIONS_PATH = "/api/v1/chat/sessions";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";
    private static final String FIXTURE_IMAGE_URL =
            "https://fixture-images.invalid/support-ticket-evidence.png";
    private static final String FIXTURE_IMAGE_MESSAGE_ID = "f1000000-0000-4000-8000-000000000503";
    private static final byte[] FIXTURE_IMAGE_BYTES = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADElEQVR42mNk+M/wHwAF/gL+3Mx2sAAAAABJRU5ErkJggg==");

    private static final TicketFixture TICKET_A = new TicketFixture(
            "f1000000-0000-4000-8000-000000000011",
            "f1000000-0000-4000-8000-000000000101",
            "f1000000-0000-4000-8000-000000000201",
            "f1000000-0000-4000-8000-000000000301",
            "f1000000-0000-4000-8000-000000000401",
            "Fixture support ticket A",
            "Fixture history: ticket A only");
    private static final TicketFixture TICKET_B = new TicketFixture(
            "f1000000-0000-4000-8000-000000000012",
            "f1000000-0000-4000-8000-000000000102",
            "f1000000-0000-4000-8000-000000000202",
            "f1000000-0000-4000-8000-000000000302",
            "f1000000-0000-4000-8000-000000000402",
            "Fixture support ticket B",
            "Fixture history: ticket B only");

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-CHAT-01: ticket chat sessions stay mapped to their own authoritative order roster")
    void switchingSupportTicketsUsesSeparateReadOnlyChatSessions() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportChatFixture fixture = new SupportChatFixture(authenticatedAdminId, List.of(TICKET_A, TICKET_B));
        registerFixtureRoutes(fixture);

        portal.openSupportTab();
        AdminSupportChatPage support = new AdminSupportChatPage(adminPage);
        support.waitForTickets();
        adminPage.waitForCondition(() -> fixture.supportReads.get() > 0,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        support.selectTicketForOrder(TICKET_A.orderId());
        support.waitForTicketDetails(TICKET_A.orderId());
        openTicketChat(support, fixture, TICKET_A);
        support.waitForHistoryMessage(TICKET_A.historyMessage());

        SessionRequest firstRequest = fixture.sessionRequestFor(TICKET_A);
        assertSessionPayload(firstRequest.body(), TICKET_A, authenticatedAdminId, TICKET_B);
        assertThat(fixture.historyReadsFor(TICKET_A.sessionId())).isEqualTo(1);
        assertThat(support.chatComposer().isVisible()).isTrue();

        support.selectTicketForOrder(TICKET_B.orderId());
        support.waitForTicketDetails(TICKET_B.orderId());
        openTicketChat(support, fixture, TICKET_B);
        support.waitForHistoryMessage(TICKET_B.historyMessage());

        SessionRequest secondRequest = fixture.sessionRequestFor(TICKET_B);
        assertSessionPayload(secondRequest.body(), TICKET_B, authenticatedAdminId, TICKET_A);
        assertThat(fixture.historyReadsFor(TICKET_B.sessionId())).isEqualTo(1);
        assertThat(fixture.sessionRequests).hasSize(2);
        assertThat(fixture.sessionIdsInRequestOrder()).containsExactly(TICKET_A.sessionId(), TICKET_B.sessionId());
        assertThat(support.historyMessage(TICKET_A.historyMessage()).count())
                .as("the ticket A message cannot survive in ticket B's remounted widget")
                .isZero();
        assertThat(support.chatLauncher(TICKET_A.orderId()).count())
                .as("the ticket A chat launcher is removed with its widget")
                .isZero();

        assertThat(fixture.supportWrites.get()).as("support ticket actions never leave this test").isZero();
        assertThat(fixture.unexpectedChatRequests.get()).as("only fixture session and history endpoints are used").isZero();
        assertThat(fixture.sentChatFrames.get()).as("the test never sends a chat message").isZero();
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-CHAT-02: moderator send and canonical customer receipt stay inside the selected ticket session")
    void supportModeratorCanSendAndReceiveWithinTheSelectedTicketSession() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportChatFixture fixture = new SupportChatFixture(authenticatedAdminId, List.of(TICKET_A));
        registerFixtureRoutes(fixture);

        portal.openSupportTab();
        AdminSupportChatPage support = new AdminSupportChatPage(adminPage);
        support.waitForTickets();
        support.selectTicketForOrder(TICKET_A.orderId());
        support.waitForTicketDetails(TICKET_A.orderId());
        openTicketChat(support, fixture, TICKET_A);
        adminPage.waitForCondition(() -> fixture.hasMessageSubscription(TICKET_A.sessionId()),
                new Page.WaitForConditionOptions().setTimeout(5_000));
        adminPage.waitForCondition(support.chatComposerInput()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        String moderatorReply = "Fixture support reply for ticket A";
        String customerReceipt = "Fixture customer acknowledgement for ticket A";
        support.sendChatMessage(moderatorReply);

        adminPage.waitForCondition(() -> fixture.sentChatFrames.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        assertThat(fixture.outboundFrameFor(TICKET_A.sessionId()))
                .contains("destination:/app/chat.send/" + TICKET_A.sessionId())
                .contains("\"content\":\"" + moderatorReply + "\"")
                .contains("\"messageType\":\"TEXT\"");
        support.waitForHistoryMessage(moderatorReply);
        support.waitForHistoryMessage(customerReceipt);

        assertThat(fixture.receiptsFor(TICKET_A.sessionId()))
                .as("the fixture delivers exactly one canonical participant receipt to this session")
                .isEqualTo(1);
        assertThat(fixture.supportWrites.get()).as("ticket state is not mutated by chat messaging").isZero();
        assertThat(fixture.unexpectedChatRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-CHAT-03: a moderator image upload uses data.url and renders only in its selected ticket session")
    void moderatorImageUploadUsesTheSelectedSessionAndAuthoritativeBroadcast() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportChatFixture fixture = new SupportChatFixture(authenticatedAdminId, List.of(TICKET_A, TICKET_B));
        registerFixtureRoutes(fixture);

        portal.openSupportTab();
        AdminSupportChatPage support = new AdminSupportChatPage(adminPage);
        support.waitForTickets();
        support.selectTicketForOrder(TICKET_A.orderId());
        support.waitForTicketDetails(TICKET_A.orderId());
        openTicketChat(support, fixture, TICKET_A);
        adminPage.waitForCondition(() -> fixture.hasMessageSubscription(TICKET_A.sessionId()),
                new Page.WaitForConditionOptions().setTimeout(5_000));
        adminPage.waitForCondition(support.chatComposerInput()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        support.uploadGalleryImage(Paths.get("src/test/resources/dummy.png"));

        adminPage.waitForCondition(() -> fixture.imageUploads.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        support.attachment(FIXTURE_IMAGE_URL).waitFor();
        adminPage.waitForCondition(support.chatComposerInput()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        assertThat(fixture.imageUploadSessionIds).containsExactly(TICKET_A.sessionId());
        assertThat(fixture.imageUploadContentTypes).allSatisfy(contentType ->
                assertThat(contentType).contains("multipart/form-data"));
        assertThat(fixture.imageBroadcastsFor(TICKET_A.sessionId()))
                .as("the upload endpoint's saved IMAGE message is broadcast only to the selected session")
                .isEqualTo(1);
        assertThat(fixture.imageBroadcastsFor(TICKET_B.sessionId()))
                .as("an attachment for ticket A cannot be injected into ticket B's session")
                .isZero();
        assertThat(support.attachment(FIXTURE_IMAGE_URL).getAttribute("src")).isEqualTo(FIXTURE_IMAGE_URL);
        assertThat(support.imageUploadError().count()).isZero();
        assertThat(fixture.supportWrites.get()).as("support ticket actions never leave this test").isZero();
        assertThat(fixture.unexpectedChatRequests.get()).isZero();
        assertThat(fixture.sentChatFrames.get()).as("image upload is an HTTP contract, not a forged chat send").isZero();
    }

    @Test
    @DisplayName("ADMIN-SUPPORT-CHAT-04: an upload response without data.url keeps the moderator's chat recoverable")
    void imageUploadWithoutUsableUrlShowsAnErrorWithoutForgingAnAttachment() {
        String authenticatedAdminId = authenticatedAdminId();
        SupportChatFixture fixture = new SupportChatFixture(authenticatedAdminId, List.of(TICKET_A),
                ImageUploadResponse.MISSING_URL);
        registerFixtureRoutes(fixture);

        portal.openSupportTab();
        AdminSupportChatPage support = new AdminSupportChatPage(adminPage);
        support.waitForTickets();
        support.selectTicketForOrder(TICKET_A.orderId());
        support.waitForTicketDetails(TICKET_A.orderId());
        openTicketChat(support, fixture, TICKET_A);
        adminPage.waitForCondition(() -> fixture.hasMessageSubscription(TICKET_A.sessionId()),
                new Page.WaitForConditionOptions().setTimeout(5_000));
        adminPage.waitForCondition(support.chatComposerInput()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        support.uploadGalleryImage(Paths.get("src/test/resources/dummy.png"));

        adminPage.waitForCondition(() -> fixture.imageUploads.get() == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
        support.imageUploadError().waitFor();
        adminPage.waitForCondition(support.chatComposerInput()::isEnabled,
                new Page.WaitForConditionOptions().setTimeout(5_000));

        assertThat(fixture.imageUploadSessionIds).containsExactly(TICKET_A.sessionId());
        assertThat(fixture.imageBroadcastsFor(TICKET_A.sessionId())).isZero();
        assertThat(support.attachment(FIXTURE_IMAGE_URL).count())
                .as("a success envelope without data.url cannot create a fake attachment")
                .isZero();
        assertThat(fixture.supportWrites.get()).isZero();
        assertThat(fixture.unexpectedChatRequests.get()).isZero();
    }

    private void openTicketChat(AdminSupportChatPage support, SupportChatFixture fixture, TicketFixture ticket) {
        // The admin wrapper normally opens the widget through its ref. If a remount leaves the
        // new ticket at the launcher, open that launcher directly; both paths remain in-browser.
        adminPage.waitForCondition(
                () -> fixture.hasSessionFor(ticket) || support.chatLauncher(ticket.orderId()).isVisible(),
                new Page.WaitForConditionOptions().setTimeout(5_000));
        if (!fixture.hasSessionFor(ticket)) {
            support.openChatIfLauncherVisible(ticket.orderId());
        }
        adminPage.waitForCondition(() -> fixture.hasSessionFor(ticket),
                new Page.WaitForConditionOptions().setTimeout(5_000));
        adminPage.waitForCondition(() -> fixture.historyReadsFor(ticket.sessionId()) == 1,
                new Page.WaitForConditionOptions().setTimeout(5_000));
    }

    private void registerFixtureRoutes(SupportChatFixture fixture) {
        adminPage.route(url -> SUPPORT_TICKETS_PATH.equals(pathOf(url)), fixture::handleSupportTickets);
        adminPage.route(url -> pathOf(url).startsWith(CHAT_SESSIONS_PATH), fixture::handleChatRequests);
        adminPage.route(FIXTURE_IMAGE_URL, route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("image/png")
                .setBodyBytes(FIXTURE_IMAGE_BYTES)));
        adminPage.routeWebSocket(url -> url.contains("/ws/chat"), socket -> {
            fixture.webSocketConnections.incrementAndGet();
            socket.onMessage(frame -> {
                String message = frame.text();
                if (message != null && message.stripLeading().startsWith("CONNECT\n")) {
                    socket.send("CONNECTED\nversion:1.2\n\n\0");
                } else if (isStompCommand(message, "SUBSCRIBE")) {
                    fixture.captureMessageSubscription(message, socket);
                } else if (message != null && message.contains("destination:/app/chat.send/")) {
                    String sessionId = sessionIdFromSendFrame(message);
                    fixture.recordOutboundChatFrame(sessionId, message);
                    TicketFixture ticket = fixture.ticketForSession(sessionId);
                    String subscriptionId = fixture.messageSubscriptionFor(sessionId);
                    if (ticket != null && subscriptionId != null) {
                        socket.send(stompMessage(subscriptionId, sessionId,
                                "f1000000-0000-4000-8000-000000000501",
                                fixture.authenticatedAdminId, "Support administrator", "SUPPORT_MODERATOR",
                                "TEXT", "Fixture support reply for ticket A"));
                        socket.send(stompMessage(subscriptionId, sessionId,
                                "f1000000-0000-4000-8000-000000000502",
                                ticket.customerId(), "Fixture customer", "CUSTOMER",
                                "TEXT", "Fixture customer acknowledgement for ticket A"));
                        fixture.recordReceipt(sessionId);
                    }
                }
            });
        });
    }

    private String authenticatedAdminId() {
        Object userId = adminPage.evaluate("() => JSON.parse(localStorage.getItem('user_profile')).id");
        assertThat(userId).as("normal admin login stores its authenticated actor").isInstanceOf(String.class);
        return (String) userId;
    }

    private static void assertSessionPayload(String body, TicketFixture expected,
                                             String authenticatedAdminId, TicketFixture otherTicket) {
        assertThat(jsonStringField(body, "orderId")).isEqualTo(expected.orderId());
        assertThat(body)
                .as("the browser requests only the order; the server owns participant selection")
                .doesNotContain("participants")
                .doesNotContain(authenticatedAdminId)
                .doesNotContain(expected.customerId())
                .doesNotContain(otherTicket.orderId())
                .doesNotContain(otherTicket.customerId());
    }

    private static String jsonStringField(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(json == null ? "" : json);
        assertThat(matcher.find()).as("session request contains %s", field).isTrue();
        return matcher.group(1);
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static boolean isStompCommand(String frame, String command) {
        return frame != null && frame.stripLeading().startsWith(command + "\n");
    }

    private static String sessionIdFromSendFrame(String frame) {
        Matcher matcher = Pattern.compile("destination:/app/chat\\.send/([0-9a-fA-F-]+)").matcher(frame);
        assertThat(matcher.find()).as("chat send frame has a session-specific destination").isTrue();
        return matcher.group(1);
    }

    private static String stompMessage(String subscriptionId, String sessionId, String messageId,
                                       String senderId, String senderName, String senderType,
                                       String messageType, String content) {
        String body = """
                {"id":"%s","sessionId":"%s","senderId":"%s","senderName":"%s","senderType":"%s","messageType":"%s","content":"%s","timestamp":"%s"}
                """.formatted(messageId, sessionId, senderId, senderName, senderType, messageType, content,
                FIXTURE_TIME).trim();
        return "MESSAGE\nsubscription:" + subscriptionId
                + "\nmessage-id:" + messageId
                + "\ndestination:/user/queue/chat/" + sessionId
                + "\ncontent-type:application/json\ncontent-length:" + body.length()
                + "\n\n" + body + "\0";
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private record TicketFixture(String id, String orderId, String customerId, String sessionId,
                                 String historyMessageId, String reason, String historyMessage) {
        private String asSupportTicketJson() {
            return """
                    {
                      "id": "%s",
                      "orderId": "%s",
                      "customerId": "%s",
                      "reason": "%s",
                      "status": "OPEN",
                      "createdAt": "%s"
                    }
                    """.formatted(id, orderId, customerId, reason, FIXTURE_TIME);
        }
    }

    private record SessionRequest(TicketFixture ticket, String body) {
    }

    private enum ImageUploadResponse {
        SUCCESS,
        MISSING_URL
    }

    private static final class SupportChatFixture {
        private final String authenticatedAdminId;
        private final List<TicketFixture> tickets;
        private final Map<String, TicketFixture> ticketsByOrderId;
        private final Map<String, TicketFixture> ticketsBySessionId;
        private final ImageUploadResponse imageUploadResponse;
        private final Map<String, String> messageSubscriptionIds = new ConcurrentHashMap<>();
        private final Map<String, WebSocketRoute> messageSocketsBySessionId = new ConcurrentHashMap<>();
        private final Map<String, String> outboundFramesBySessionId = new ConcurrentHashMap<>();
        private final Map<String, AtomicInteger> receiptCountsBySessionId = new ConcurrentHashMap<>();
        private final Map<String, AtomicInteger> imageBroadcastCountsBySessionId = new ConcurrentHashMap<>();
        private final List<SessionRequest> sessionRequests = new CopyOnWriteArrayList<>();
        private final List<String> historySessionIds = new CopyOnWriteArrayList<>();
        private final List<String> imageUploadSessionIds = new CopyOnWriteArrayList<>();
        private final List<String> imageUploadContentTypes = new CopyOnWriteArrayList<>();
        private final AtomicInteger supportReads = new AtomicInteger();
        private final AtomicInteger supportWrites = new AtomicInteger();
        private final AtomicInteger unexpectedChatRequests = new AtomicInteger();
        private final AtomicInteger sentChatFrames = new AtomicInteger();
        private final AtomicInteger imageUploads = new AtomicInteger();
        private final AtomicInteger webSocketConnections = new AtomicInteger();

        private SupportChatFixture(String authenticatedAdminId, List<TicketFixture> tickets) {
            this(authenticatedAdminId, tickets, ImageUploadResponse.SUCCESS);
        }

        private SupportChatFixture(String authenticatedAdminId, List<TicketFixture> tickets,
                                   ImageUploadResponse imageUploadResponse) {
            this.authenticatedAdminId = authenticatedAdminId;
            this.tickets = tickets;
            this.imageUploadResponse = imageUploadResponse;
            this.ticketsByOrderId = tickets.stream()
                    .collect(Collectors.toUnmodifiableMap(TicketFixture::orderId, ticket -> ticket));
            this.ticketsBySessionId = tickets.stream()
                    .collect(Collectors.toUnmodifiableMap(TicketFixture::sessionId, ticket -> ticket));
        }

        private void handleSupportTickets(Route route) {
            Request request = route.request();
            if (!"GET".equals(request.method())) {
                supportWrites.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Support writes are blocked by this fixture\"}");
                return;
            }

            supportReads.incrementAndGet();
            String content = tickets.stream()
                    .map(TicketFixture::asSupportTicketJson)
                    .collect(Collectors.joining(","));
            fulfillJson(route, 200, """
                    {
                      "content": [%s],
                      "totalElements": %d,
                      "totalPages": 1,
                      "numberOfElements": %d,
                      "first": true,
                      "last": true,
                      "number": 0,
                      "size": 20,
                      "empty": false
                    }
                    """.formatted(content, tickets.size(), tickets.size()));
        }

        private void handleChatRequests(Route route) {
            Request request = route.request();
            String path = pathOf(request.url());
            if ("POST".equals(request.method()) && CHAT_SESSIONS_PATH.equals(path)) {
                String body = request.postData();
                TicketFixture ticket = ticketsByOrderId.get(jsonStringField(body, "orderId"));
                if (ticket == null) {
                    unexpectedChatRequests.incrementAndGet();
                    fulfillJson(route, 400, "{\"message\":\"Unexpected fixture order\"}");
                    return;
                }
                sessionRequests.add(new SessionRequest(ticket, body));
                fulfillJson(route, 201, sessionResponse(ticket));
                return;
            }

            if ("POST".equals(request.method()) && path.endsWith("/upload-image")) {
                String sessionId = path.substring(CHAT_SESSIONS_PATH.length() + 1,
                        path.length() - "/upload-image".length());
                TicketFixture ticket = ticketsBySessionId.get(sessionId);
                if (ticket == null) {
                    unexpectedChatRequests.incrementAndGet();
                    fulfillJson(route, 400, "{\"message\":\"Unexpected fixture session\"}");
                    return;
                }

                imageUploads.incrementAndGet();
                imageUploadSessionIds.add(sessionId);
                String contentType = request.headerValue("content-type");
                imageUploadContentTypes.add(contentType == null ? "" : contentType);
                if (imageUploadResponse == ImageUploadResponse.MISSING_URL) {
                    fulfillJson(route, 200, imageUploadResponseWithoutUrl());
                    return;
                }

                fulfillJson(route, 200, imageUploadResponse(ticket));
                broadcastImageUpload(ticket);
                return;
            }

            if ("GET".equals(request.method()) && path.endsWith("/messages")) {
                String sessionId = path.substring(CHAT_SESSIONS_PATH.length() + 1,
                        path.length() - "/messages".length());
                TicketFixture ticket = ticketsBySessionId.get(sessionId);
                if (ticket != null) {
                    historySessionIds.add(sessionId);
                    fulfillJson(route, 200, historyResponse(ticket));
                    return;
                }
            }

            unexpectedChatRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected chat request blocked by fixture\"}");
        }

        private boolean hasSessionFor(TicketFixture ticket) {
            return sessionRequests.stream().anyMatch(request -> request.ticket().equals(ticket));
        }

        private SessionRequest sessionRequestFor(TicketFixture ticket) {
            return sessionRequests.stream()
                    .filter(request -> request.ticket().equals(ticket))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No session request was captured for " + ticket.orderId()));
        }

        private int historyReadsFor(String sessionId) {
            return (int) historySessionIds.stream().filter(sessionId::equals).count();
        }

        private List<String> sessionIdsInRequestOrder() {
            return sessionRequests.stream().map(request -> request.ticket().sessionId()).toList();
        }

        private void captureMessageSubscription(String frame, WebSocketRoute socket) {
            String destination = stompHeader(frame, "destination");
            if (destination == null || !destination.startsWith("/user/queue/chat/")
                    || destination.endsWith("/typing")) {
                return;
            }
            String sessionId = destination.substring("/user/queue/chat/".length());
            String subscriptionId = stompHeader(frame, "id");
            if (ticketsBySessionId.containsKey(sessionId) && subscriptionId != null) {
                messageSubscriptionIds.put(sessionId, subscriptionId);
                messageSocketsBySessionId.put(sessionId, socket);
            }
        }

        private boolean hasMessageSubscription(String sessionId) {
            return messageSubscriptionIds.containsKey(sessionId);
        }

        private String messageSubscriptionFor(String sessionId) {
            return messageSubscriptionIds.get(sessionId);
        }

        private TicketFixture ticketForSession(String sessionId) {
            return ticketsBySessionId.get(sessionId);
        }

        private void recordOutboundChatFrame(String sessionId, String frame) {
            if (ticketsBySessionId.containsKey(sessionId)) {
                sentChatFrames.incrementAndGet();
                outboundFramesBySessionId.put(sessionId, frame);
            } else {
                unexpectedChatRequests.incrementAndGet();
            }
        }

        private String outboundFrameFor(String sessionId) {
            String frame = outboundFramesBySessionId.get(sessionId);
            if (frame == null) throw new AssertionError("No outbound chat frame captured for " + sessionId);
            return frame;
        }

        private void recordReceipt(String sessionId) {
            receiptCountsBySessionId.computeIfAbsent(sessionId, ignored -> new AtomicInteger()).incrementAndGet();
        }

        private int receiptsFor(String sessionId) {
            AtomicInteger count = receiptCountsBySessionId.get(sessionId);
            return count == null ? 0 : count.get();
        }

        private int imageBroadcastsFor(String sessionId) {
            AtomicInteger count = imageBroadcastCountsBySessionId.get(sessionId);
            return count == null ? 0 : count.get();
        }

        private void broadcastImageUpload(TicketFixture ticket) {
            String sessionId = ticket.sessionId();
            String subscriptionId = messageSubscriptionFor(sessionId);
            WebSocketRoute socket = messageSocketsBySessionId.get(sessionId);
            if (subscriptionId == null || socket == null) {
                unexpectedChatRequests.incrementAndGet();
                return;
            }
            socket.send(stompMessage(subscriptionId, sessionId, FIXTURE_IMAGE_MESSAGE_ID,
                    authenticatedAdminId, "Support administrator", "SUPPORT_MODERATOR",
                    "IMAGE", FIXTURE_IMAGE_URL));
            imageBroadcastCountsBySessionId.computeIfAbsent(sessionId, ignored -> new AtomicInteger())
                    .incrementAndGet();
        }

        private static String stompHeader(String frame, String header) {
            Matcher matcher = Pattern.compile("(?m)^" + Pattern.quote(header) + ":([^\\r\\n]+)\\r?$").matcher(frame);
            return matcher.find() ? matcher.group(1) : null;
        }

        private static String sessionResponse(TicketFixture ticket) {
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
                          "entityType": "CUSTOMER",
                          "displayName": "Fixture customer"
                        }]
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(ticket.sessionId(), ticket.orderId(), FIXTURE_TIME, ticket.customerId(), FIXTURE_TIME);
        }

        private static String imageUploadResponse(TicketFixture ticket) {
            return """
                    {
                      "success": true,
                      "message": "fixture image uploaded",
                      "data": {
                        "url": "%s",
                        "messageId": "%s"
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(FIXTURE_IMAGE_URL, FIXTURE_IMAGE_MESSAGE_ID, FIXTURE_TIME);
        }

        private static String imageUploadResponseWithoutUrl() {
            return """
                    {
                      "success": true,
                      "message": "fixture omitted the image URL",
                      "data": {},
                      "timestamp": "%s"
                    }
                    """.formatted(FIXTURE_TIME);
        }

        private static String historyResponse(TicketFixture ticket) {
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [{
                          "id": "%s",
                          "sessionId": "%s",
                          "senderId": "%s",
                          "senderName": "Fixture customer",
                          "senderType": "CUSTOMER",
                          "messageType": "TEXT",
                          "content": "%s",
                          "timestamp": "%s"
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
                    """.formatted(ticket.historyMessageId(), ticket.sessionId(), ticket.customerId(),
                    ticket.historyMessage(), FIXTURE_TIME, FIXTURE_TIME);
        }
    }
}
