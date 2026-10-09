package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminOrderMoneyPage;
import com.fooddelivery.e2e.util.OrderMoneyChecks;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * MONEY-05: the admin order-money panel tells each outcome apart. Read-only, on owned orders that
 * already reached the outcome: {@code -Dmoney.outcomes=<orderId>:<DELIVERED|PARTIAL|CANCELLED|REJECTED>,...},
 * each with a manifest in target/lifecycle.
 *
 * <p>Beyond showing what the API returns, it checks rules that hold per outcome: a delivered order books
 * each payee exactly its quoted payout; a restaurant-fault partial refund books the restaurant its payout
 * less the clawback; a cancelled or rejected order books neither payee anything and shows a full refund.
 */
@Tag("feature-money-ledger")
public class AdminOrderMoneyOutcomesTest extends TestBase {

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> outcomes() {
        return Arrays.stream(System.getProperty("money.outcomes", "").split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(s -> s.split(":"))
                .map(pair -> org.junit.jupiter.params.provider.Arguments.of(pair[0], pair[1]));
    }

    @ParameterizedTest(name = "{1} {0}")
    @MethodSource("outcomes")
    @DisplayName("MONEY-05: the order-money panel shows the payment, refunds and what each payee was booked")
    void panelShowsTheOutcome(String orderId, String kind) throws java.io.IOException {
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", orderId + ".json")))
                .contains("\"" + orderId + "\"").contains(testCustomerPhone);
        loginAsAdmin();
        String path = "/api/v1/internal/admin/orders/" + orderId + "/money";
        Response response = adminPage.waitForResponse(r -> r.request().method().equals("GET")
                && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals(path), () -> adminPage.navigate(
                TestConfig.APP_URL + "/admin/orders/" + orderId + "/money"));
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        Map<?, ?> money = (Map<?, ?>) adminPage.evaluate("text => JSON.parse(text)", response.text());
        BigDecimal total = OrderMoneyChecks.amount(money, "totalAmount");
        BigDecimal restaurantQuoted = OrderMoneyChecks.amount(money, "restaurantPayout");
        BigDecimal riderQuoted = OrderMoneyChecks.amount(money, "driverNetPayout");
        List<?> refunds = (List<?>) money.get("refunds");

        new AdminOrderMoneyPage(adminPage).waitForOrderMoney();
        Locator payment = adminPage.getByTestId("order-payment");
        Locator restaurantPosted = adminPage.getByTestId("restaurant-posted");
        Locator riderPosted = adminPage.getByTestId("rider-posted");
        assertThat(payment).containsText(String.valueOf(money.get("paymentStatus")));
        assertThat(payment).containsText(String.valueOf(money.get("paymentMethod")));
        assertThat(adminPage.getByTestId("order-refund")).hasCount(refunds.size());
        for (Object value : refunds) {
            Map<?, ?> refund = (Map<?, ?>) value;
            Locator row = adminPage.locator("[data-testid='order-refund'][data-refund-id='" + refund.get("id") + "']");
            assertThat(row).hasAttribute("data-status", String.valueOf(refund.get("status")));
            assertThat(row).containsText(inr(OrderMoneyChecks.amount(refund, "amount")));
        }

        switch (kind) {
            case "DELIVERED" -> {
                org.assertj.core.api.Assertions.assertThat(money.get("paymentStatus")).isEqualTo("SUCCESS");
                assertThat(payment).containsText("No refunds.");
                assertThat(restaurantPosted).containsText(inr(restaurantQuoted));
                assertThat(riderPosted).containsText(inr(riderQuoted));
            }
            case "PARTIAL" -> {
                org.assertj.core.api.Assertions.assertThat(money.get("paymentStatus")).isEqualTo("PARTIALLY_REFUNDED");
                BigDecimal clawback = ledgerTotal(money, "RESTAURANT_PAYABLE", "CLAWBACK", "DEBIT");
                org.assertj.core.api.Assertions.assertThat(clawback).as("restaurant-fault refund claws back").isPositive();
                assertThat(restaurantPosted).containsText(inr(restaurantQuoted.subtract(clawback)));
                assertThat(riderPosted).containsText(inr(riderQuoted));
            }
            case "CANCELLED", "REJECTED" -> {
                org.assertj.core.api.Assertions.assertThat(money.get("paymentStatus")).isEqualTo("REFUNDED");
                org.assertj.core.api.Assertions.assertThat(refunds).hasSize(1);
                Map<?, ?> refund = (Map<?, ?>) refunds.get(0);
                org.assertj.core.api.Assertions.assertThat(refund.get("status")).isEqualTo("COMPLETED");
                org.assertj.core.api.Assertions.assertThat(OrderMoneyChecks.amount(refund, "amount")).isEqualByComparingTo(total);
                assertThat(restaurantPosted).containsText("Not posted");
                assertThat(riderPosted).containsText("Not posted");
            }
            default -> throw new IllegalArgumentException("unknown outcome " + kind);
        }
    }

    private static BigDecimal ledgerTotal(Map<?, ?> money, String ownerType, String category, String direction) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Object value : (List<?>) money.get("ledgerLines")) {
            Map<?, ?> line = (Map<?, ?>) value;
            if (ownerType.equals(line.get("ownerType")) && category.equals(line.get("category")) && direction.equals(line.get("direction"))) {
                sum = sum.add(OrderMoneyChecks.amount(line, "amount"));
            }
        }
        return sum;
    }

    private static String inr(BigDecimal amount) {
        return "₹" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
