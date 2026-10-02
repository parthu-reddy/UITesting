package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminLedgerPage;
import com.fooddelivery.e2e.pages.admin.AdminOrderMoneyPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed coverage for the administrator's read-only money views.
 *
 * <p>Each test performs normal admin authentication first.  It then serves only disposable
 * ledger, payout, and order-money responses in the browser.  Any unexpected non-GET request to
 * one of those financial endpoints is aborted and counted, so shared Dev financial data cannot be
 * changed while this UI coverage runs.</p>
 */
@Tag("admin")
@Tag("admin-ledger-read-only")
@Tag("browser-routed")
public class AdminLedgerMoneyReadOnlyRoutedUiTest extends TestBase {

    private static final String PAYEE_ID = "b0000000-0000-4000-8000-000000000001";
    private static final String FIRST_PAYOUT_ID = "b0000000-0000-4000-8000-000000000011";
    private static final String SECOND_PAYOUT_ID = "b0000000-0000-4000-8000-000000000012";
    private static final String ORDER_ID = "b0000000-0000-4000-8000-000000000021";

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("LEDGER-ROUTED-01: category filters and pagination render fixture data without a write")
    void ledgerFilterAndPaginationAreReadOnly() {
        AtomicInteger ledgerReads = new AtomicInteger();
        AtomicInteger ledgerWrites = new AtomicInteger();
        AtomicBoolean sawFoodCostFilter = new AtomicBoolean();

        adminPage.route("**/api/v1/internal/admin/ledger/transactions**", route -> {
            if (!"GET".equals(route.request().method())) {
                ledgerWrites.incrementAndGet();
                route.abort();
                return;
            }

            ledgerReads.incrementAndGet();
            String url = route.request().url();
            if (url.contains("category=FOOD_COST") && url.contains("page=0")) {
                sawFoodCostFilter.set(true);
            }
            route.fulfill(json("{\"success\":true,\"message\":\"Fixture ledger transactions\",\"data\":"
                    + ledgerPage(url.contains("page=1")) + ",\"timestamp\":\"2026-09-29T10:00:00Z\"}"));
        });

        assertThat(portal.openLedgerTab()).isEqualTo(200);
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.waitForResultsLoaded();

        ledger.selectCategory("FOOD_COST");
        assertThat(ledger.applyFilter("category=FOOD_COST")).isEqualTo(200);
        adminPage.getByText("₹120.50", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor();
        assertThat(sawFoodCostFilter.get())
                .as("the selected category must be sent to the server, not only filter the current DOM")
                .isTrue();

        Locator nextPage = adminPage.locator("button:has(svg.lucide-chevron-right)").first();
        assertThat(nextPage.isEnabled()).isTrue();
        nextPage.click();
        adminPage.getByText("₹75.50", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor();

        assertThat(ledger.getPageInfo()).isEqualTo("Page 2 of 2");
        assertThat(ledgerReads.get()).isGreaterThanOrEqualTo(3);
        assertThat(ledgerWrites.get()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-ROUTED-01: paginated history opens its order money link without a financial write")
    void paginatedPayoutHistoryOpensReadOnlyOrderMoney() {
        AtomicInteger payoutReads = new AtomicInteger();
        AtomicInteger payoutWrites = new AtomicInteger();
        AtomicInteger orderMoneyReads = new AtomicInteger();
        AtomicInteger orderMoneyWrites = new AtomicInteger();

        interceptPayoutRequests(payoutReads, payoutWrites);
        adminPage.route("**/api/v1/internal/admin/orders/" + ORDER_ID + "/money", route -> {
            if ("GET".equals(route.request().method())) {
                orderMoneyReads.incrementAndGet();
                route.fulfill(json(orderMoneyResponse()));
            } else {
                orderMoneyWrites.incrementAndGet();
                route.abort();
            }
        });

        portal.openPayoutsTab();
        adminPage.getByRole(AriaRole.HEADING,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Payouts").setExact(true))
                .waitFor();
        adminPage.getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("History").setExact(true))
                .click();

        adminPage.getByPlaceholder("Enter UUID...").fill(PAYEE_ID);
        adminPage.getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Search").setExact(true))
                .click();
        adminPage.getByText("Page 1 of 2", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor();
        assertThat(adminPage.getByText("Fixture payout page one",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();

        adminPage.getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Next").setExact(true))
                .click();
        adminPage.getByText("Page 2 of 2", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor();
        assertThat(adminPage.getByText("Fixture payout page two",
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();

        adminPage.locator("table tbody tr").first().click();
        adminPage.getByRole(AriaRole.HEADING,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Payout Details").setExact(true))
                .waitFor();

        adminPage.getByRole(AriaRole.LINK,
                        new com.microsoft.playwright.Page.GetByRoleOptions()
                                .setName("Ref: " + ORDER_ID).setExact(true))
                .click();

        AdminOrderMoneyPage money = new AdminOrderMoneyPage(adminPage);
        money.waitForOrderMoney();
        assertThat(adminPage.url()).contains("/admin/orders/" + ORDER_ID + "/money");
        assertThat(money.getCustomerTotal()).isEqualTo("₹455.00");
        assertThat(money.getRestaurantNetPayout()).isEqualTo("₹355.00");
        assertThat(money.getRiderNetPayout()).isEqualTo("₹45.00");

        assertThat(payoutReads.get()).isGreaterThanOrEqualTo(4);
        assertThat(orderMoneyReads.get()).isEqualTo(1);
        assertThat(payoutWrites.get()).isZero();
        assertThat(orderMoneyWrites.get()).isZero();
    }

    private void interceptPayoutRequests(AtomicInteger payoutReads, AtomicInteger payoutWrites) {
        adminPage.route("**/api/v1/internal/admin/payouts**", route -> {
            if (!"GET".equals(route.request().method())) {
                payoutWrites.incrementAndGet();
                route.abort();
                return;
            }

            payoutReads.incrementAndGet();
            String url = route.request().url();
            if (url.contains("/payouts/pending")) {
                route.fulfill(json("[]"));
            } else if (url.contains("/payouts/" + SECOND_PAYOUT_ID)) {
                route.fulfill(json(payoutDetailResponse()));
            } else {
                route.fulfill(json(payoutHistoryPage(url.contains("page=1"))));
            }
        });
    }

    private static Route.FulfillOptions json(String body) {
        return new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(body);
    }

    private static String ledgerPage(boolean secondPage) {
        String transactionId = secondPage
                ? "a0000000-0000-4000-8000-000000000002"
                : "a0000000-0000-4000-8000-000000000001";
        String amount = secondPage ? "75.50" : "120.50";
        return """
                {
                  "content": [{
                    "entryId": "a0000000-0000-4000-8000-0000000000%s",
                    "transactionId": "%s",
                    "date": "2026-09-29T00:00:00Z",
                    "category": "FOOD_COST",
                    "accountId": "a0000000-0000-4000-8000-000000000102",
                    "direction": "CREDIT",
                    "fromAccountId": "a0000000-0000-4000-8000-000000000101",
                    "toAccountId": "a0000000-0000-4000-8000-000000000102",
                    "amount": %s
                  }],
                  "totalElements": 2,
                  "totalPages": 2,
                  "last": %s,
                  "size": 20,
                  "number": %d,
                  "first": %s,
                  "numberOfElements": 1,
                  "empty": false
                }
                """.formatted(secondPage ? "02" : "01", transactionId, amount, secondPage, secondPage ? 1 : 0, !secondPage);
    }

    private static String payoutHistoryPage(boolean secondPage) {
        String payoutId = secondPage ? SECOND_PAYOUT_ID : FIRST_PAYOUT_ID;
        String name = secondPage ? "Fixture payout page two" : "Fixture payout page one";
        return """
                {
                  "content": [{
                    "id": "%s",
                    "payeeType": "RESTAURANT",
                    "payeeId": "%s",
                    "payeeDisplayName": "%s",
                    "amount": 455.0,
                    "currency": "INR",
                    "status": "PAID",
                    "createdAt": "2026-09-29T00:00:00Z"
                  }],
                  "totalElements": 2,
                  "totalPages": 2,
                  "last": %s,
                  "size": 20,
                  "number": %d,
                  "first": %s,
                  "numberOfElements": 1,
                  "empty": false
                }
                """.formatted(payoutId, PAYEE_ID, name, secondPage, secondPage ? 1 : 0, !secondPage);
    }

    private static String payoutDetailResponse() {
        return """
                {
                  "id": "%s",
                  "payeeType": "RESTAURANT",
                  "payeeId": "%s",
                  "payeeDisplayName": "Fixture payout page two",
                  "amount": 455.0,
                  "currency": "INR",
                  "status": "PAID",
                  "createdAt": "2026-09-29T00:00:00Z",
                  "lines": [{
                    "id": "b0000000-0000-4000-8000-000000000031",
                    "payoutId": "%s",
                    "ledgerEntryId": "b0000000-0000-4000-8000-000000000032",
                    "referenceId": "%s",
                    "category": "FOOD_COST",
                    "direction": "CREDIT",
                    "amount": 455.0,
                    "entryCreatedAt": "2026-09-29T00:00:00Z",
                    "active": true
                  }]
                }
                """.formatted(SECOND_PAYOUT_ID, PAYEE_ID, SECOND_PAYOUT_ID, ORDER_ID);
    }

    private static String orderMoneyResponse() {
        return """
                {
                  "orderId": "%s",
                  "foodCost": 400.0,
                  "deliveryFee": 30.0,
                  "customerPlatformFee": 5.0,
                  "sgst": 10.0,
                  "cgst": 10.0,
                  "totalAmount": 455.0,
                  "restaurantPayout": 355.0,
                  "restaurantPlatformFee": 25.0,
                  "restaurantDeliveryContribution": 20.0,
                  "driverGrossPayout": 50.0,
                  "driverTaxes": 5.0,
                  "driverNetPayout": 45.0,
                  "platformBonus": 0.0,
                  "ledgerLines": []
                }
                """.formatted(ORDER_ID);
    }
}
