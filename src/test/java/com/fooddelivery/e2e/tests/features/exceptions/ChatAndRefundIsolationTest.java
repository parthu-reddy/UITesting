package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
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
@Tag("chat") @Tag("isolation")
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
                const frames = []; let done = false; const ws = new WebSocket(url);
                const finish = result => { if (done) return; done = true; try { ws.close(); } catch (e) {} resolve(result); };
                const limit = setTimeout(() => finish({frames, error: false}), mode === 'owner-subscribe' ? 4000 : 15000);
                ws.onopen = () => ws.send('CONNECT\\naccept-version:1.2,1.1\\nheart-beat:0,0\\nhost:' + location.host
                    + '\\nAuthorization:Bearer ' + token + '\\n\\n\\0');
                ws.onmessage = event => {
                  const frame = String(event.data); const command = frame.split('\\n')[0];
                  const message = (frame.match(/\\nmessage:([^\\n]*)/) || [])[1] || '';
                  frames.push(command + (message ? ':' + message : ''));
                  if (command === 'CONNECTED') {
                    if (mode === 'send') {
                      ws.send('SEND\\ndestination:/app/chat.send/' + sessionId + '\\ncontent-type:application/json\\n\\n'
                          + JSON.stringify({content: %s, messageType: 'TEXT'}) + '\\0');
                    } else {
                      ws.send('SUBSCRIBE\\nid:probe-0\\ndestination:/user/queue/chat/' + sessionId + '\\n\\n\\0');
                    }
                  }
                  if (command === 'ERROR') { clearTimeout(limit); finish({frames, error: true}); }
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
        login(customerPage, "Order Food", testCustomerPhone);
        Map<?, ?> ownerSession = (Map<?, ?>) RefundRecoveryChecks.read(customerPage, "/api/v1/chat/sessions?orderId=" + ORDER).get("body");
        String sessionId = (String) ((Map<?, ?>) ownerSession.get("data")).get("sessionId");
        assertThat(sessionId).as("owner sees the order's chat session").isNotBlank();
        String messagesPath = "/api/v1/chat/sessions/" + sessionId + "/messages";
        int ownerMessages = messageCount(customerPage, messagesPath);
        assertThat(ownerMessages).as("owner reads the history").isPositive();
        assertThat(stomp(customerPage, sessionId, "owner-subscribe").get("error")).as("owner subscription accepted").isEqualTo(false);

        // CHAT-ISO-01: an unrelated customer.
        int orderRead;
        BrowserContext intruderContext = customerPage.context().browser().newContext(
                new com.microsoft.playwright.Browser.NewContextOptions().setViewportSize(1280, 900));
        try {
            Page intruder = intruderContext.newPage();
            login(intruder, "Order Food", INTRUDER_CUSTOMER);
            assertThat(status(intruder, "GET", messagesPath, null)).as("outsider reads history").isEqualTo(403);
            assertThat(status(intruder, "GET", "/api/v1/chat/sessions?orderId=" + ORDER, null)).as("outsider finds the session").isEqualTo(403);
            assertThat(status(intruder, "POST", "/api/v1/chat/sessions", "{\"orderId\":\"" + ORDER + "\"}")).as("outsider joins").isEqualTo(403);
            assertThat(status(intruder, "GET", "/api/v1/money/customer/orders/" + ORDER + "/refunds", null)).as("outsider reads refunds").isEqualTo(403);
            orderRead = status(intruder, "GET", "/api/v1/orders/" + ORDER, null);

            Map<?, ?> subscribe = stomp(intruder, sessionId, "subscribe");
            assertThat(subscribe.get("error")).as("outsider subscription refused: %s", subscribe.get("frames")).isEqualTo(true);
            assertThat((List<?>) subscribe.get("frames")).anyMatch(f -> String.valueOf(f).contains("Access Denied"));
            Map<?, ?> send = stomp(intruder, sessionId, "send");
            assertThat(send.get("error")).as("outsider send refused: %s", send.get("frames")).isEqualTo(true);
        } finally {
            intruderContext.close();
        }

        // CHAT-ISO-02 and 03: another brand's owner and a rider never assigned to this order.
        login(restaurantPage, "Restaurant Partner", testRestaurantPhone);
        assertThat(status(restaurantPage, "GET", messagesPath, null)).as("other restaurant reads history").isEqualTo(403);
        login(riderPage, "Delivery Executive", testRiderPhone);
        assertThat(status(riderPage, "GET", messagesPath, null)).as("unassigned rider reads history").isEqualTo(403);

        // The refused SEND left nothing behind.
        assertThat(messageCount(customerPage, messagesPath)).as("history unchanged by refused probes").isEqualTo(ownerMessages);
        assertThat(String.valueOf(RefundRecoveryChecks.read(customerPage, messagesPath).get("body"))).doesNotContain(PROBE);
        // Last, so a failure here cannot hide the probes above: another customer's order is "not found".
        assertThat(orderRead).as("outsider reads the order").isEqualTo(404);
    }

    private static void login(Page page, String portal, String phone) {
        page.navigate(TestConfig.APP_URL);
        new LoginPage(page).loginAs(portal, phone);
        page.waitForCondition(() -> (Boolean) page.evaluate("() => !!localStorage.getItem('auth_token')"));
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
