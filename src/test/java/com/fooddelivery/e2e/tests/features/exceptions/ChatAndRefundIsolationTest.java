package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CHAT-ISO-01..03: an authenticated actor who is not on an order cannot read or join its chat, and a
 * customer cannot read another customer's order or refunds. The order's own customer reads the same
 * chat first, so every refusal is checked against a session that exists and answers its owner.
 *
 * <p>Run with the owning customer as {@code -Dcustomer.phone} and the intruders as
 * {@code -Disolation.customer.phone}, {@code -Drestaurant.phone} (another brand's owner) and
 * {@code -Drider.phone} (a rider never assigned to the order), on {@code -Disolation.order.id}: an
 * owned order with a chat session whose manifest is in target/lifecycle. Nothing is created; the one
 * write attempted (an intruder's STOMP SEND) must be refused, and the owner's history proves it was.
 */
@org.junit.jupiter.api.Disabled("O4-INT-002: direct request, storage and handcrafted STOMP assertions are deferred under the UI-only policy")
@Tag("feature-chat")
@Tag("feature-refunds-support")
public class ChatAndRefundIsolationTest extends TestBase {
    private static final String ORDER = System.getProperty("isolation.order.id", "").trim();
    private static final String INTRUDER_CUSTOMER = System.getProperty("isolation.customer.phone", "8000000485").trim();
    private static final String PROBE = "E2E isolation probe: a message from outside the order must be refused";

    /** A raw STOMP exchange over the app's own socket URL; returns frame commands and ERROR messages, never the token. */
    private static final String STOMP = """
            async ({sessionId, mode}) => {
              const token = localStorage.getItem('auth_token');
              const url = (location.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + location.host
                  + '/ws/chat?token=' + encodeURIComponent(token);
              return await new Promise(resolve => {
                const frames = []; let done = false, buffer = '', actionSent = false;
                const ws = new WebSocket(url); ws.binaryType = 'arraybuffer';
                const finish = result => { if (done) return; done = true; clearTimeout(limit);
                  try { ws.close(); } catch (e) {} resolve(result); };
                const limit = setTimeout(() => finish({frames, error: false}), mode === 'owner-subscribe' ? 4000 : 15000);
                ws.onopen = () => ws.send('CONNECT\\naccept-version:1.2,1.1\\nheart-beat:0,0\\nhost:' + location.host
                    + '\\nAuthorization:Bearer ' + token + '\\n\\n\\0');
                ws.onmessage = event => {
                  buffer += typeof event.data === 'string' ? event.data : new TextDecoder().decode(event.data);
                  let boundary;
                  while ((boundary = buffer.indexOf('\\0')) >= 0) {
                    const frame = buffer.slice(0, boundary).replace(/^[\\r\\n]+/, '');
                    buffer = buffer.slice(boundary + 1);
                    if (!frame) continue;
                    const command = frame.split('\\n')[0].replace(/\\r$/, '');
                    const message = (frame.match(/\\nmessage:([^\\r\\n]*)/) || [])[1] || '';
                    frames.push(command + (message ? ':' + message : ''));
                    if (command === 'CONNECTED' && !actionSent) {
                      actionSent = true;
                      if (mode === 'send') {
                        ws.send('SEND\\ndestination:/app/chat.send/' + sessionId + '\\ncontent-type:application/json\\n\\n'
                            + JSON.stringify({content: %s, messageType: 'TEXT'}) + '\\0');
                      } else {
                        ws.send('SUBSCRIBE\\nid:probe-0\\ndestination:/user/queue/chat/' + sessionId + '\\n\\n\\0');
                      }
                    }
                    if (command === 'ERROR') { finish({frames, error: true}); return; }
                  }
                };
                ws.onclose = event => { clearTimeout(limit); finish({frames, error: frames.some(f => f.startsWith('ERROR')), closed: event.code}); };
              });
            }
            """.formatted("\"" + PROBE + "\"");

    @Test
    @DisplayName("CHAT-ISO-01..03: outsiders cannot read or join an order's chat, order or refunds")
    void outsidersAreRefused() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "needs -Disolation.order.id");
        String manifest = Files.readString(Path.of("target/lifecycle", ORDER + ".json"));
        assertThat(manifest).contains("\"" + ORDER + "\"").contains(testCustomerPhone)
                .doesNotContain(INTRUDER_CUSTOMER).doesNotContain(testRestaurantPhone).doesNotContain(testRiderPhone);

        // Owner control: the session exists, answers its customer, and accepts their subscription.
        login(customerPage, "CUSTOMER", testCustomerPhone);
        Map<?, ?> ownerSession = (Map<?, ?>) RefundRecoveryChecks.read(customerPage, "/api/v1/chat/sessions?orderId=" + ORDER).get("body");
        String sessionId = (String) ((Map<?, ?>) ownerSession.get("data")).get("sessionId");
        assertThat(sessionId).as("owner sees the order's chat session").isNotBlank();
        String messagesPath = "/api/v1/chat/sessions/" + sessionId + "/messages";
        int ownerMessages = messageCount(customerPage, messagesPath);
        assertThat(ownerMessages).as("owner reads the history").isPositive();
        assertParticipantSubscription(customerPage, sessionId);
        Map<?, ?> actors = (Map<?, ?>) customerPage.evaluate("json => JSON.parse(json)", manifest);
        // Check every legitimate participant before the outsider refusals. These real member
        // reads also exercise the deployed organisation lookup after the Chat resilience rollout.
        participantReadsAndSubscribes("RESTAURANT", (String) actors.get("restaurantPhone"),
                messagesPath, sessionId, ownerMessages);
        participantReadsAndSubscribes("DELIVERY", (String) actors.get("riderPhone"),
                messagesPath, sessionId, ownerMessages);

        // CHAT-ISO-01: an unrelated customer.
        int orderRead;
        BrowserContext intruderContext = customerPage.context().browser().newContext(
                new com.microsoft.playwright.Browser.NewContextOptions().setViewportSize(1280, 900));
        try {
            Page intruder = intruderContext.newPage();
            login(intruder, "CUSTOMER", INTRUDER_CUSTOMER);
            assertThat(status(intruder, "GET", messagesPath, null)).as("outsider reads history").isEqualTo(403);
            assertThat(status(intruder, "GET", "/api/v1/chat/sessions?orderId=" + ORDER, null)).as("outsider finds the session").isEqualTo(403);
            assertThat(status(intruder, "POST", "/api/v1/chat/sessions", "{\"orderId\":\"" + ORDER + "\"}")).as("outsider joins").isEqualTo(403);
            assertThat(status(intruder, "GET", "/api/v1/money/customer/orders/" + ORDER + "/refunds", null)).as("outsider reads refunds").isEqualTo(403);
            orderRead = status(intruder, "GET", "/api/v1/orders/" + ORDER, null);

            Map<?, ?> subscribe = stomp(intruder, sessionId, "subscribe");
            assertRefused(subscribe, "subscription");
            Map<?, ?> send = stomp(intruder, sessionId, "send");
            assertRefused(send, "send");
        } finally {
            intruderContext.close();
        }

        // CHAT-ISO-02 and 03: another brand's owner and a rider never assigned to this order.
        login(restaurantPage, "RESTAURANT", testRestaurantPhone);
        assertThat(status(restaurantPage, "GET", messagesPath, null)).as("other restaurant reads history").isEqualTo(403);
        login(riderPage, "DELIVERY", testRiderPhone);
        assertThat(status(riderPage, "GET", messagesPath, null)).as("unassigned rider reads history").isEqualTo(403);

        // The refused SEND left nothing behind.
        assertThat(messageCount(customerPage, messagesPath)).as("history unchanged by refused probes").isEqualTo(ownerMessages);
        assertThat(String.valueOf(RefundRecoveryChecks.read(customerPage, messagesPath).get("body"))).doesNotContain(PROBE);
        // Last, so a failure here cannot hide the probes above: another customer's order is "not found".
        assertThat(orderRead).as("outsider reads the order").isEqualTo(404);
    }

    private static void login(Page page, String portal, String phone) {
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).login(phone).openPortal(Portal.valueOf(portal));
        page.waitForCondition(() -> (Boolean) page.evaluate("() => !!localStorage.getItem('auth_token')"));
    }

    private void participantReadsAndSubscribes(String portal, String phone, String path, String sessionId, int expectedMessages) {
        assertThat(phone).as("retained manifest identifies its %s", portal).isNotBlank();
        BrowserContext context = customerPage.context().browser().newContext(
                new com.microsoft.playwright.Browser.NewContextOptions().setViewportSize(1280, 900));
        try {
            Page participant = context.newPage();
            login(participant, portal, phone);
            assertThat(status(participant, "GET", path, null)).as("%s participant reads history", portal).isEqualTo(200);
            assertThat(messageCount(participant, path)).isEqualTo(expectedMessages);
            assertParticipantSubscription(participant, sessionId);
        } finally {
            context.close();
        }
    }

    private static void assertParticipantSubscription(Page page, String sessionId) {
        Map<?, ?> subscribed = stomp(page, sessionId, "owner-subscribe");
        assertThat(subscribed.get("error")).as("legitimate participant subscription has no refusal").isEqualTo(false);
        assertThat((List<?>) subscribed.get("frames"))
                .as("the real socket must authenticate, an empty timeout is not a pass")
                .anyMatch(frame -> "CONNECTED".equals(frame));
        assertThat(subscribed.get("closed")).as("a legitimate subscription stays open for the observation window")
                .isNull();
    }

    private static void assertRefused(Map<?, ?> result, String operation) {
        assertThat((List<?>) result.get("frames")).as("the outsider authenticated before the %s probe", operation)
                .anyMatch(frame -> "CONNECTED".equals(frame));
        if (Boolean.TRUE.equals(result.get("error"))) {
            assertThat((List<?>) result.get("frames")).as("explicit %s refusal: %s", operation, result)
                    .anyMatch(frame -> String.valueOf(frame).contains("Access Denied"));
        } else {
            // Spring 6.1.8 StompSubProtocolHandler closes with PROTOCOL_ERROR after
            // rejecting an inbound frame, including if delivering its ERROR frame fails.
            // A timeout, normal close, missing authentication, or live subscription fails.
            assertThat(result.get("closed")).as("server closed the refused %s: %s", operation, result)
                    .isEqualTo(1002);
        }
    }

    private static int messageCount(Page page, String path) {
        Map<?, ?> data = (Map<?, ?>) ((Map<?, ?>) RefundRecoveryChecks.read(page, path).get("body")).get("data");
        return ((List<?>) data.get("content")).size();
    }

    private static int status(Page page, String method, String path, String body) {
        return ((Number) page.evaluate("""
                async ([method, path, body]) => (await fetch(path, {method, body: body || undefined,
                    headers: {Authorization: 'Bearer ' + localStorage.getItem('auth_token'), 'Content-Type': 'application/json'}})).status
                """, List.of(method, path, body == null ? "" : body).toArray())).intValue();
    }

    private static Map<?, ?> stomp(Page page, String sessionId, String mode) {
        return (Map<?, ?>) page.evaluate(STOMP, Map.of("sessionId", sessionId, "mode", mode));
    }
}
