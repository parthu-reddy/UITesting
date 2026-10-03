package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.util.OrderMoneyChecks;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.fooddelivery.e2e.util.SupportRefundSteps;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * REFUND-RETRY-01: a refund the payment provider declines is retried by an administrator from Money
 * Operations and completes, with the money and ledger to show for it.
 *
 * <p>Dev's mock gateways decline the first attempt of a refund of exactly ₹1.13 and accept the
 * retry (MockRefundFailureSeam, payment-gateway). The refund is created through the real support
 * path: the customer raises an item ticket in chat and the administrator awards ₹1.13.
 *
 * <p>{@code -Dretry.order.id}: an owned delivered CARD order with no tickets or refunds, inside its
 * two-hour chat window, manifest in target/lifecycle. No order is created.
 */
@Tag("flow") @Tag("support-refund")
public class AdminRefundRetryFlowTest extends TestBase {
    private static final String ORDER = System.getProperty("retry.order.id", "").trim();
    /** The amount the Dev mock gateways decline once (MockRefundFailureSeam.FAIL_FIRST_ATTEMPT_RUPEES). */
    private static final BigDecimal DECLINED_ONCE = new BigDecimal("1.13");
    private SupportRefundSteps steps;

    @BeforeEach
    void ownedDeliveredOrder() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "admin retry flow needs -Dretry.order.id");
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", ORDER + ".json")))
                .contains("\"" + ORDER + "\"").contains(testCustomerPhone).contains(testRestaurantPhone);
        customerPage.route("**/api/v1/orders", route -> {
            if (route.request().method().equals("POST")) throw new AssertionError("A refund retry must not create an order");
            route.resume();
        });
        loginAsAdmin();
        steps = new SupportRefundSteps(customerPage, adminPage, testCustomerPhone);
    }

    @Test
    @DisplayName("REFUND-RETRY-01: a declined refund is retried from Money Operations and completes")
    void declinedRefundIsRetriedAndCompletes() throws java.io.IOException {
        steps.openDeliveredOrder(ORDER);
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).as("fresh delivered fixture").isEmpty();
        Map<?, ?> order = RefundRecoveryChecks.order(customerPage, ORDER);

        // A real support award of exactly the declined-once amount.
        steps.submitItemTicket(ORDER, "E2E admin retry " + ORDER.substring(0, 8), SupportRefundSteps.itemQuote(order));
        steps.awaitTicket(ORDER, "OPEN");
        Locator panel = steps.openQueueTicket(ORDER);
        steps.chooseResolution(panel, true);
        panel.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Fault Attribution")).click();
        adminPage.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Restaurant Fault (Chargeback)").setExact(true)).click();
        panel.getByLabel("Override amount").fill(DECLINED_ONCE.toPlainString());
        panel.getByLabel("Resolution notes").fill("E2E award the Dev provider declines once");
        steps.confirmDecision(true, "₹1.13");

        // The provider's decline lands as a FAILED refund and nothing was returned.
        Map<?, ?> failed = awaitRefundStatus("FAILED");
        String refundId = (String) failed.get("id");
        org.assertj.core.api.Assertions.assertThat(failed.get("completedAt")).isNull();
        org.assertj.core.api.Assertions.assertThat(SupportRefundSteps.categoryTotal(steps.money(ORDER), "REFUND", null, "CREDIT"))
                .as("no refund booked while it is failed").isEqualByComparingTo(BigDecimal.ZERO);

        // The administrator finds it in Money Operations and retries it.
        adminPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/money_ops");
        adminPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Failed Refunds").setExact(true)).click();
        Locator card = adminPage.locator("[data-testid='failed-refund'][data-refund-id='" + refundId + "']");
        assertThat(card).isVisible();
        assertThat(card).containsText("₹1.13");
        card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Retry Refund").setExact(true)).click();
        Locator dialog = adminPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Retry refund of ₹1.13?"));
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Retry refund").setExact(true)).click();
        assertThat(adminPage.getByText("Refund retry queued; completion is pending.")).isVisible();

        // The same refund completes; the money and the ledger follow; it leaves the failed list.
        Map<?, ?> completed = steps.awaitCompletedRefund(ORDER, DECLINED_ONCE);
        org.assertj.core.api.Assertions.assertThat(completed.get("id")).as("the same refund, not a new one").isEqualTo(refundId);
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).hasSize(1);
        steps.awaitMoney(ORDER, "PARTIALLY_REFUNDED", DECLINED_ONCE);
        adminPage.reload();
        adminPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Failed Refunds").setExact(true)).click();
        assertThat(adminPage.locator("[data-testid='failed-refund'][data-refund-id='" + refundId + "']")).hasCount(0);
        steps.record(ORDER, "retry", Map.of("refundId", refundId, "amount", DECLINED_ONCE, "declinedOnce", true));
    }

    private Map<?, ?> awaitRefundStatus(String status) {
        AtomicReference<Map<?, ?>> found = new AtomicReference<>();
        customerPage.waitForCondition(() -> {
            for (Object value : (List<?>) steps.refunds(ORDER)) {
                Map<?, ?> refund = (Map<?, ?>) value;
                if (OrderMoneyChecks.amount(refund, "amount").compareTo(DECLINED_ONCE) != 0) continue;
                if (!status.equals(refund.get("status"))) return false;
                found.set(refund);
                return true;
            }
            return false;
        }, new Page.WaitForConditionOptions().setTimeout(30000));
        return found.get();
    }
}
