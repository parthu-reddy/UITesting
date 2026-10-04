package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * CHAT-22: a conversation longer than one history page (50) can be read back in full. The chat opens
 * on the newest page; "Load earlier messages" brings the rest. Before 2026-10-03 the UI asked for page
 * 0 only, so after a reload everything older than the newest 50 was unreachable.
 *
 * <p>{@code -Dchat.history.order.id}: an owned order inside its chat window (last updated under two
 * hours ago), manifest in target/lifecycle. If its chat holds fewer than {@value #TARGET} messages the
 * test tops it up as the order's customer, through the app's own STOMP path; they are retained.
 */
@Tag("chat")
public class ChatHistoryPagingTest extends TestBase {
    private static final String ORDER = System.getProperty("chat.history.order.id", "").trim();
    private static final int TARGET = 60;

    private static final String SEND = """
            async ({sessionId, contents}) => {
              const token = localStorage.getItem('auth_token');
              const url = (location.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + location.host
                  + '/ws/chat?token=' + encodeURIComponent(token);
              return await new Promise(resolve => {
                const ws = new WebSocket(url); const frames = [];
                const finish = r => { try { ws.close(); } catch (e) {} resolve(r); };
                const limit = setTimeout(() => finish({sent: 0, frames}), 30000);
                ws.onopen = () => ws.send('CONNECT\\naccept-version:1.2,1.1\\nheart-beat:0,0\\nhost:' + location.host
                    + '\\nAuthorization:Bearer ' + token + '\\n\\n\\0');
                ws.onmessage = async event => {
                  const command = String(event.data).split('\\n')[0]; frames.push(command);
                  if (command === 'ERROR') { clearTimeout(limit); finish({sent: 0, frames}); }
                  if (command !== 'CONNECTED') return;
                  for (const content of contents) {
                    ws.send('SEND\\ndestination:/app/chat.send/' + sessionId + '\\ncontent-type:application/json\\n\\n'
                        + JSON.stringify({content, messageType: 'TEXT'}) + '\\0');
                    await new Promise(r => setTimeout(r, 40));
                  }
                  setTimeout(() => { clearTimeout(limit); finish({sent: contents.length, frames}); }, 1500);
                };
              });
            }
            """;

    @Test
    @DisplayName("CHAT-22: history longer than one page is reachable with 'Load earlier messages'")
    void earlierMessagesCanBeLoaded() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "needs -Dchat.history.order.id");
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", ORDER + ".json")))
                .contains("\"" + ORDER + "\"").contains(testCustomerPhone);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        Instant updated = Instant.parse((String) RefundRecoveryChecks.order(customerPage, ORDER).get("updatedAt"));
        org.assertj.core.api.Assertions.assertThat(Duration.between(updated, Instant.now()))
                .as("order %s must be inside the two-hour chat window (last updated %s)", ORDER, updated)
                .isLessThan(Duration.ofHours(2));

        Map<?, ?> session = (Map<?, ?>) ((Map<?, ?>) RefundRecoveryChecks.read(customerPage, "/api/v1/chat/sessions?orderId=" + ORDER).get("body")).get("data");
        String sessionId = (String) session.get("sessionId");
        String messages = "/api/v1/chat/sessions/" + sessionId + "/messages";
        long total = totalMessages(messages);
        if (total < TARGET) {
            String run = Long.toString(System.currentTimeMillis(), 36);
            List<String> contents = java.util.stream.IntStream.rangeClosed(1, (int) (TARGET - total))
                    .mapToObj(i -> "E2E history " + run + " #" + i).toList();
            Map<?, ?> sent = (Map<?, ?>) customerPage.evaluate(SEND, Map.of("sessionId", sessionId, "contents", contents));
            org.assertj.core.api.Assertions.assertThat(((Number) sent.get("sent")).intValue()).as("frames %s", sent.get("frames")).isEqualTo(contents.size());
            customerPage.waitForCondition(() -> totalMessages(messages) >= TARGET, new Page.WaitForConditionOptions().setTimeout(30000));
            total = totalMessages(messages);
        }
        org.assertj.core.api.Assertions.assertThat(total).as("more than one page of history").isGreaterThan(50).isLessThanOrEqualTo(100);

        // Newest first from the server. Compare text messages only (an image or refund card renders
        // differently from its stored content), and pick an oldest text that the newest page lacks.
        List<String> newestPage = texts(messages + "?page=0&size=50");
        String newest = newestPage.get(0);
        List<String> olderPage = texts(messages + "?page=1&size=50");
        String oldest = olderPage.reversed().stream().filter(text -> newestPage.stream().noneMatch(n -> n.contains(text) || text.contains(n)))
                .findFirst().orElseThrow(() -> new AssertionError("no distinct text message on the older page"));

        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']").click();
        assertThat(new CustomerOrderTrackerPage(customerPage, ORDER).tracker()).isVisible();
        customerPage.locator("[data-testid='chat-launcher']").click();

        Locator thread = customerPage.locator("[data-testid='chat-message']");
        assertThat(thread.filter(new Locator.FilterOptions().setHasText(newest)).first()).isVisible();
        assertThat(thread.filter(new Locator.FilterOptions().setHasText(oldest))).hasCount(0);
        int shown = thread.count();

        Locator earlier = customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Load earlier messages").setExact(true));
        assertThat(earlier).isVisible();
        earlier.click();
        assertThat(thread.filter(new Locator.FilterOptions().setHasText(oldest)).first()).isVisible();
        org.assertj.core.api.Assertions.assertThat(thread.count()).as("earlier page added").isGreaterThan(shown);
        assertThat(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Load earlier messages"))).hasCount(0);
    }

    private long totalMessages(String path) {
        Map<?, ?> data = (Map<?, ?>) ((Map<?, ?>) RefundRecoveryChecks.read(customerPage, path + "?page=0&size=1").get("body")).get("data");
        return ((Number) data.get("totalElements")).longValue();
    }

    /** The TEXT messages of one history page, newest first, as the server returns them. */
    private List<String> texts(String path) {
        List<?> content = (List<?>) ((Map<?, ?>) ((Map<?, ?>) RefundRecoveryChecks.read(customerPage, path).get("body")).get("data")).get("content");
        return content.stream().map(m -> (Map<?, ?>) m).filter(m -> "TEXT".equals(m.get("messageType")))
                .map(m -> (String) m.get("content")).toList();
    }
}
