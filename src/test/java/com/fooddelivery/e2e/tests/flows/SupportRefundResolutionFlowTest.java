package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.util.OrderMoneyChecks;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.fooddelivery.e2e.util.SupportRefundSteps;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Support-ticket refund decisions on one owned, already-delivered order, in sequence: a denial, then
 * a reduced award with the restaurant at fault, then refusal of a second refund for the items the
 * award consumed. Only an OPEN ticket blocks a new one (ChatRefundProcessorService), so the decisions
 * share one order instead of each needing its own delivered lifecycle.
 * Customers raise tickets through the real chat path (item quote, then "Submit Refund Request" --
 * the only customer support entry the UI renders); the administrator decides in the refund queue.
 * Money is asserted on the refund, payment and ledger, not on the decision screen.
 *
 * <p>Requires {@code -Dsupport.order.id}: a delivered CARD order with no tickets or refunds, whose
 * manifest is in target/lifecycle, last updated less than two hours ago: the customer UI offers the
 * order chat, and so this support path, only for two hours after that (isOrderChatOffered). No
 * order is created. Methods run in order: each needs the one before.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("feature-money-ledger")
@Tag("feature-refunds-support")
public class SupportRefundResolutionFlowTest extends TestBase {
    private static final String ORDER = System.getProperty("support.order.id", "").trim();
    private static final BigDecimal AWARD = new BigDecimal("12.00");
    private SupportRefundSteps steps;

    @BeforeEach
    void ownedDeliveredOrder() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "support refund flow needs -Dsupport.order.id");
        String manifest = Files.readString(Path.of("target/lifecycle", ORDER + ".json"));
        org.assertj.core.api.Assertions.assertThat(manifest)
                .contains("\"" + ORDER + "\"").contains(testCustomerPhone).contains(testRestaurantPhone);
        customerPage.route("**/api/v1/orders", route -> {
            if (route.request().method().equals("POST")) throw new AssertionError("Support decisions must not create an order");
            route.resume();
        });
        loginAsAdmin();
        steps = new SupportRefundSteps(customerPage, adminPage, testCustomerPhone);
    }

    @Test @Order(1)
    @DisplayName("SUPPORT-REFUND-02: denied item request moves no money")
    void deniedRequestMovesNoMoney() throws java.io.IOException {
        steps.openDeliveredOrder(ORDER);
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).as("fresh delivered fixture").isEmpty();
        Map<?, ?> order = RefundRecoveryChecks.order(customerPage, ORDER);
        Map<?, ?> moneyBefore = steps.money(ORDER);
        int ledgerLinesBefore = ((List<?>) moneyBefore.get("ledgerLines")).size();

        steps.submitItemTicket(ORDER, "E2E denial: claims food was cold", SupportRefundSteps.itemQuote(order));
        steps.awaitTicket(ORDER, "OPEN");

        Locator panel = steps.openQueueTicket(ORDER);
        steps.chooseResolution(panel, false);
        panel.getByLabel("Resolution notes").fill("E2E denial: delivery photo shows sealed hot packaging");
        steps.confirmDecision(false, null);

        Map<?, ?> rejected = steps.awaitTicket(ORDER, "REJECTED");
        org.assertj.core.api.Assertions.assertThat(rejected.get("resolutionNotes")).isEqualTo("E2E denial: delivery photo shows sealed hot packaging");
        org.assertj.core.api.Assertions.assertThat(rejected.get("resolvedBy")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).as("a denial creates no refund").isEmpty();
        Map<?, ?> moneyAfter = steps.money(ORDER);
        org.assertj.core.api.Assertions.assertThat(moneyAfter.get("paymentStatus")).isEqualTo(moneyBefore.get("paymentStatus"));
        org.assertj.core.api.Assertions.assertThat(((List<?>) moneyAfter.get("ledgerLines")).size()).isEqualTo(ledgerLinesBefore);
        steps.record(ORDER, "denial", Map.of("ticketId", rejected.get("id"), "refunds", 0));
    }

    @Test @Order(2)
    @DisplayName("SUPPORT-REFUND-01: reduced award on an item ticket, restaurant at fault")
    void reducedAwardWithRestaurantFault() throws java.io.IOException {
        steps.openDeliveredOrder(ORDER);
        Map<?, ?> order = RefundRecoveryChecks.order(customerPage, ORDER);
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).as("SUPPORT-REFUND-02's denial moved no money").isEmpty();
        BigDecimal itemQuote = SupportRefundSteps.itemQuote(order);

        steps.submitItemTicket(ORDER, "E2E support award " + ORDER.substring(0, 8), itemQuote);
        Map<?, ?> ticket = steps.awaitTicket(ORDER, "OPEN");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.amount(ticket, "refundAmount")).isEqualByComparingTo(itemQuote);
        org.assertj.core.api.Assertions.assertThat(ticket.get("requestedRefundItems")).as("item-level ticket").isNotNull();

        // Admin: award less than the quote, attributing the fault to the restaurant.
        Locator panel = steps.openQueueTicket(ORDER);
        steps.chooseResolution(panel, true);
        panel.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Fault Attribution")).click();
        adminPage.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("Restaurant Fault (Chargeback)").setExact(true)).click();
        panel.getByLabel("Override amount").fill(AWARD.toPlainString());
        panel.getByLabel("Resolution notes").fill("E2E reduced award: one item partially affected");
        steps.confirmDecision(true, "₹12.00");

        Map<?, ?> refund = steps.awaitCompletedRefund(ORDER, AWARD);
        Map<?, ?> resolved = steps.awaitTicket(ORDER, "RESOLVED");
        org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.amount(resolved, "refundAmount")).isEqualByComparingTo(AWARD);
        org.assertj.core.api.Assertions.assertThat(resolved.get("resolutionNotes")).isEqualTo("E2E reduced award: one item partially affected");
        org.assertj.core.api.Assertions.assertThat(resolved.get("resolvedBy")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(steps.awaitTicket(ORDER, "REJECTED").get("resolutionNotes"))
                .as("the earlier denial is a separate, unchanged decision")
                .isEqualTo("E2E denial: delivery photo shows sealed hot packaging");

        // Restaurant pays its share of the award: payout x (award / total), as LedgerBookkeeper books it.
        BigDecimal ratio = AWARD.divide(OrderMoneyChecks.amount(order, "totalAmount"), 4, RoundingMode.HALF_UP);
        Map<?, ?> money = steps.awaitMoney(ORDER, "PARTIALLY_REFUNDED", AWARD);
        BigDecimal clawback = OrderMoneyChecks.amount(money, "restaurantPayout").multiply(ratio).setScale(2, RoundingMode.HALF_UP);
        org.assertj.core.api.Assertions.assertThat(clawback).isPositive();
        org.assertj.core.api.Assertions.assertThat(SupportRefundSteps.categoryTotal(money, "CLAWBACK", "RESTAURANT_PAYABLE", "DEBIT"))
                .isEqualByComparingTo(clawback);
        steps.record(ORDER, "award", Map.of("refundId", refund.get("id"), "award", AWARD, "clawback", clawback));
    }

    @Test @Order(3)
    @DisplayName("SUPPORT-REFUND-03: an item already refunded cannot be quoted or refunded again")
    void refundedItemCannotBeRefundedAgain() throws java.io.IOException {
        steps.openDeliveredOrder(ORDER);
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).as("needs SUPPORT-REFUND-01's completed award").hasSize(1);
        Map<?, ?> resolved = steps.awaitTicket(ORDER, "RESOLVED");

        Locator reply = steps.requestItemQuote(ORDER, "E2E second request for the same item");
        assertThat(reply).hasAttribute("data-message-type", "REFUND_ERROR");
        // The server's refusal (ITEM_ALREADY_REFUNDED) reaches the customer as a sentence (ChatRefundProcessorService.CUSTOMER_TEXT).
        assertThat(reply).containsText("These items have already been refunded.");

        Map<?, ?> body = (Map<?, ?>) RefundRecoveryChecks.read(adminPage, "/api/v1/internal/admin/refunds?status=OPEN&size=50").get("body");
        org.assertj.core.api.Assertions.assertThat(((List<?>) body.get("content")).stream().map(t -> (Map<?, ?>) t)
                .filter(t -> ORDER.equals(t.get("orderId")))).as("no ticket for a refused quote").isEmpty();
        org.assertj.core.api.Assertions.assertThat(steps.refunds(ORDER)).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(steps.awaitTicket(ORDER, "RESOLVED").get("id")).isEqualTo(resolved.get("id"));
        steps.record(ORDER, "item-consumed", Map.of("refunds", 1, "error", "ITEM_ALREADY_REFUNDED"));
    }
}
