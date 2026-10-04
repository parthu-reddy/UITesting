package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * CHAT-REFUND-05: once a delivered order's chat is no longer offered (two hours after its last
 * update, {@code isOrderChatOffered}), its summary offers no support entry either. The button used to
 * stay and click into an unmounted chat, doing nothing.
 *
 * <p>Uses owned delivered orders that have already aged past the window, so nothing waits:
 * {@code -Dsupport.closed.order.ids=<id>[,<id>]}, each with a manifest in target/lifecycle; one
 * invocation per order, each in a fresh login. With no ids the test has no invocations, which is not
 * a pass. Read-only: nothing is clicked except navigation. The open side (button shown and opening the quote form inside
 * the window) is CHAT-REFUND-01, exercised right after delivery by HappyDeliveryFlowTest.
 */
@Tag("chat") @Tag("support-refund")
public class ChatSupportWindowClosedTest extends TestBase {
    private static final List<String> ORDERS = Arrays.stream(System.getProperty("support.closed.order.ids", "").split(","))
            .map(String::trim).filter(id -> !id.isEmpty()).toList();

    static List<String> closedOrders() {
        return ORDERS;
    }

    @ParameterizedTest(name = "order {0}")
    @MethodSource("closedOrders")
    @DisplayName("CHAT-REFUND-05: no support entry or chat after the post-delivery chat window")
    void closedWindowOffersNoSupportEntry(String id) throws java.io.IOException {
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", id + ".json")))
                .contains("\"" + id + "\"").contains(testCustomerPhone);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        Instant updated = Instant.parse((String) RefundRecoveryChecks.order(customerPage, id).get("updatedAt"));
        org.assertj.core.api.Assertions.assertThat(Duration.between(updated, Instant.now()))
                .as("order %s must be past the two-hour chat window (last updated %s)", id, updated)
                .isGreaterThanOrEqualTo(Duration.ofHours(2));

        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + id + "']").click();
        assertThat(new CustomerOrderTrackerPage(customerPage, id).tracker()).isVisible();

        // The invoice button renders in the same receipt as the support button, so once it is
        // visible the summary has rendered and an absent support button is really absent.
        assertThat(customerPage.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Order delivered"))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Tax invoice"))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Something wrong with this order?").setExact(true))).hasCount(0);
        assertThat(customerPage.locator("[data-testid='chat-launcher']")).hasCount(0);
    }
}
