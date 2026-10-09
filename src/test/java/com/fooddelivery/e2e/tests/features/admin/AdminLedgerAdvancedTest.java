package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.*;
import org.junit.jupiter.api.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Read-only checks for the Admin Ledger and payout navigation. Mutating payout actions require
 * a disposable financial fixture and are deliberately not exercised against shared Dev data.
 */
@Tag("feature-money-ledger")
public class AdminLedgerAdvancedTest extends TestBase {

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    // ── LEDGER ADVANCED FILTER SCENARIOS ─────────────────────────────────

    @Test
    @DisplayName("LEDGER-ADV-01: Ledger view visible")
    void ledgerViewVisible() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        assertThat(ledger.isLedgerVisible()).isTrue();
        com.microsoft.playwright.assertions.PlaywrightAssertions
                .assertThat(adminPage.getByPlaceholder("Transaction ID")).isVisible();
        com.microsoft.playwright.assertions.PlaywrightAssertions
                .assertThat(adminPage.getByPlaceholder("Owner ID")).isVisible();
    }

    @Test
    @DisplayName("LEDGER-ADV-02: Filter by transaction ID")
    void filterByTransactionId() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        // The API binds transactionId as a UUID; use a valid UUID that cannot match seeded data.
        ledger.filterByTransactionId("00000000-0000-0000-0000-000000000000");
        assertThat(ledger.applyFilter("transactionId=00000000-0000-0000-0000-000000000000")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
    }

    @Test
    @DisplayName("LEDGER-ADV-04: Filter by restaurant payable account")
    void filterByOwnerType() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.filterByOwnerId("00000000-0000-0000-0000-000000000000");
        ledger.selectOwnerType("RESTAURANT_PAYABLE");
        assertThat(ledger.applyFilter("ownerId=00000000-0000-0000-0000-000000000000&ownerType=RESTAURANT_PAYABLE")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
    }

    @Test
    @DisplayName("LEDGER-ADV-06: CREDIT direction is sent and a known-empty result renders explicitly")
    void filterByDirectionCredit() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        // A zero UUID is not present in the seeded ledger.  Combining it with CREDIT lets this
        // read-only live check assert both the actual request parameter and the visible empty
        // result without claiming that shared Dev data contains a representative credit row.
        ledger.filterByTransactionId("00000000-0000-0000-0000-000000000000");
        ledger.selectDirection("CREDIT");
        assertThat(ledger.applyFilter(
                "transactionId=00000000-0000-0000-0000-000000000000&direction=CREDIT"))
                .isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
    }

    @Test
    @DisplayName("LEDGER-ADV-05: Category filter uses a supported category and returns matching rows")
    void filterByCategory() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.selectCategory("DELIVERY_FEE");
        assertThat(ledger.applyFilter("category=DELIVERY_FEE")).isEqualTo(200);
        ledger.waitForResultsLoaded();

        List<Locator> rows = ledger.getTransactionRows();
        if (rows.isEmpty()) {
            assertThat(adminPage.getByText("No ledger transactions found.",
                    new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible()).isTrue();
            return;
        }
        for (Locator row : rows) {
            com.microsoft.playwright.assertions.PlaywrightAssertions
                    .assertThat(row.locator("td").nth(1)).containsText("DELIVERY FEE");
        }
    }

    @Test
    @DisplayName("LEDGER-ADV-03: Owner type without owner ID is rejected before a request is sent")
    void ownerTypeRequiresOwnerId() {
        AtomicInteger ledgerRequests = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("GET".equals(request.method())
                    && request.url().contains("/api/v1/internal/admin/ledger/transactions")) {
                ledgerRequests.incrementAndGet();
            }
        });

        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.waitForResultsLoaded();
        int requestsBeforeInvalidFilter = ledgerRequests.get();

        ledger.selectOwnerType("RESTAURANT_PAYABLE");
        adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Apply Filters").setExact(true)).click();

        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                adminPage.getByText("Please enter an Owner ID when filtering by Owner Type",
                        new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(ledgerRequests.get()).isEqualTo(requestsBeforeInvalidFilter);
    }

    @Test
    @DisplayName("LEDGER-ADV-10: Visible ledger rows have real amounts, dates and account values")
    void ledgerRowsHaveCompleteFinancialValues() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/ledger/transactions")
                                && "GET".equals(r.request().method()),
                portal::openLedgerTab);
        assertThat(response.status()).isEqualTo(200);
        String payload = response.text();
        assertThat(Pattern.compile("\"amount\"\\s*:\\s*null").matcher(payload).find())
                .as("the API must not omit a ledger amount and let the UI render it as ₹0.00")
                .isFalse();

        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.waitForResultsLoaded();
        List<Locator> rows = ledger.getTransactionRows();
        if (rows.isEmpty()) {
            assertThat(adminPage.getByText("No ledger transactions found.",
                    new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).isVisible())
                    .isTrue();
            return;
        }

        for (Locator row : rows) {
            List<Locator> cells = row.locator("td").all();
            assertThat(cells).hasSize(5);
            assertThat(cells.get(0).innerText().trim()).isNotBlank();
            assertThat(cells.get(1).innerText().trim()).isNotBlank();
            assertThat(cells.get(2).innerText().trim()).isNotBlank();
            assertThat(cells.get(3).innerText().trim()).isNotBlank();
            String displayedAmount = cells.get(4).innerText().trim();
            assertThat(displayedAmount).matches("^₹[0-9,]+\\.[0-9]{2}$");
            double amount = Double.parseDouble(displayedAmount.substring(1).replace(",", ""));
            assertThat(amount).isGreaterThanOrEqualTo(0d);
        }
    }

    @Test
    @DisplayName("LEDGER-ADV-09: Clear filters resets all")
    void clearFiltersResetsAll() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.filterByTransactionId("00000000-0000-0000-0000-000000000000");
        ledger.filterByOwnerId("00000000-0000-0000-0000-000000000000");
        ledger.selectOwnerType("RESTAURANT_PAYABLE");
        assertThat(ledger.applyFilter("transactionId=00000000-0000-0000-0000-000000000000&ownerId=00000000-0000-0000-0000-000000000000&ownerType=RESTAURANT_PAYABLE")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
        assertThat(ledger.clearFilters()).isEqualTo(200);
        assertThat(adminPage.getByPlaceholder("Transaction ID").inputValue()).isEmpty();
        assertThat(adminPage.getByPlaceholder("Owner ID").inputValue()).isEmpty();
    }

    @Test
    @DisplayName("LEDGER-ADV-12/13: Ledger pagination next and prev")
    void ledgerPagination() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.waitForResultsLoaded();
        String initialPage = ledger.getPageInfo();
        assertThat(initialPage).matches("Page 1 of [1-9][0-9]*");
        int totalPages = Integer.parseInt(initialPage.substring("Page 1 of ".length()));

        if (totalPages == 1) {
            assertThat(ledger.canGoPreviousPage()).isFalse();
            assertThat(ledger.canGoNextPage()).isFalse();
            return;
        }

        Response next = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/ledger/transactions")
                                && r.url().matches(".*[?&]page=1(?:&|$).*")
                                && "GET".equals(r.request().method()),
                ledger::nextPage);
        assertThat(next.status()).isEqualTo(200);
        adminPage.getByText(Pattern.compile("^Page 2 of [0-9]+$"))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
        assertThat(ledger.getPageInfo()).startsWith("Page 2 of ");

        Response previous = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/ledger/transactions")
                                && r.url().matches(".*[?&]page=0(?:&|$).*")
                                && "GET".equals(r.request().method()),
                ledger::prevPage);
        assertThat(previous.status()).isEqualTo(200);
        adminPage.getByText(Pattern.compile("^Page 1 of [0-9]+$"))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
        assertThat(ledger.getPageInfo()).startsWith("Page 1 of ");
    }

    // ── PAYOUT ADVANCED SCENARIOS ────────────────────────────────────────

    @Test
    @DisplayName("PAYOUT-11: Open payout history tab")
    void openPayoutHistoryTab() {
        portal.openPayoutsTab(); // "Pending Payouts" in the admin sidebar
        AdminPayoutsPage payouts = new AdminPayoutsPage(adminPage);
        assertThat(payouts.isPayoutsVisible()).isTrue();
    }

    @Test
    @DisplayName("PAYOUT-05/07: Pending queue has an explicit empty state or positive unsettled balances")
    void pendingPayoutsHavePositiveBalances() {
        AtomicInteger payoutWrites = new AtomicInteger();
        adminPage.onRequest(request -> {
            if (!"GET".equals(request.method()) && request.url().contains("/api/v1/internal/admin/payouts")) {
                payoutWrites.incrementAndGet();
            }
        });
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/payouts/pending")
                                && "GET".equals(r.request().method()),
                portal::openPayoutsTab);
        assertThat(response.status()).isEqualTo(200);
        adminPage.getByText("Loading pending payouts...",
                        new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                        .setTimeout(30000));

        Locator cards = adminPage.locator("div.cursor-pointer:has-text('Unsettled Balance')");
        String expectedPayee = System.getProperty("payout.expected.restaurant", "").trim();
        if (!expectedPayee.isEmpty()) {
            Locator owned = cards.filter(new Locator.FilterOptions().setHasText(expectedPayee));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(owned).hasCount(1);
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(owned.getByText("VERIFIED",
                    new Locator.GetByTextOptions().setExact(true))).isVisible();
            java.math.BigDecimal expected = new java.math.BigDecimal(System.getProperty("payout.expected.balance"));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(owned.locator("div.text-4xl"))
                    .hasText("₹" + expected.setScale(2).toPlainString());
        }
        if (cards.count() == 0) {
            assertThat(adminPage.getByRole(AriaRole.HEADING,
                    new com.microsoft.playwright.Page.GetByRoleOptions().setName("All Caught Up!").setExact(true)).isVisible())
                    .isTrue();
            assertThat(payoutWrites.get()).isZero();
            return;
        }

        for (Locator card : cards.all()) {
            assertThat(card.getByText(Pattern.compile("^(RESTAURANT|DRIVER)$")).first().innerText().trim())
                    .matches("RESTAURANT|DRIVER");
            String displayedBalance = card.locator("div.text-4xl").innerText().trim();
            assertThat(displayedBalance).matches("^₹[0-9,]+\\.[0-9]{2}$");
            double amount = Double.parseDouble(displayedBalance.substring(1).replace(",", ""));
            assertThat(amount).isPositive();
        }
        assertThat(payoutWrites.get()).as("reading the queue never creates or transitions a payout").isZero();
    }

    @Test
    @DisplayName("PAYOUT-03: Empty payout search is disabled and an unknown payee returns an empty result")
    void payoutHistoryRequiresPayeeAndShowsUnknownAsEmpty() {
        portal.openPayoutsTab();
        AdminPayoutsPage payouts = new AdminPayoutsPage(adminPage);
        payouts.openHistory();

        Locator search = adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Search").setExact(true));
        assertThat(search.isDisabled()).isTrue();
        assertThat(adminPage.getByRole(AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Search Payouts").setExact(true)).isVisible())
                .isTrue();

        String unknownPayee = "00000000-0000-0000-0000-000000000000";
        adminPage.getByPlaceholder("Enter UUID...").fill(unknownPayee);
        assertThat(search.isEnabled()).isTrue();
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/payouts")
                                && r.url().contains("payeeId=" + unknownPayee)
                                && "GET".equals(r.request().method()),
                search::click);
        assertThat(response.status()).isEqualTo(200);
        adminPage.getByText("Loading payout history...",
                        new com.microsoft.playwright.Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                        .setTimeout(30000));
        assertThat(adminPage.getByRole(AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Search Payouts").setExact(true)).isVisible())
                .isTrue();
        assertThat(adminPage.locator("table tbody tr").count()).isZero();
    }

    @Test
    @DisplayName("PAYOUT-ORDER-01: A payout order link opens its read-only money breakdown")
    void payoutOrderMoneyRouteUsesTheSelectedOrderAndSendsNoWrite() {
        String orderId = "d0000000-0000-4000-8000-000000000101";
        AtomicInteger moneyReads = new AtomicInteger();
        AtomicInteger moneyWrites = new AtomicInteger();
        String moneyResponse = """
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
                """.formatted(orderId);

        adminPage.route("**/api/v1/internal/admin/orders/" + orderId + "/money", route -> {
            if ("GET".equals(route.request().method())) {
                moneyReads.incrementAndGet();
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(moneyResponse));
            } else {
                moneyWrites.incrementAndGet();
                route.abort();
            }
        });

        adminPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/orders/" + orderId + "/money");
        AdminOrderMoneyPage money = new AdminOrderMoneyPage(adminPage);
        money.waitForOrderMoney();

        assertThat(adminPage.url()).contains("/admin/orders/" + orderId + "/money");
        assertThat(money.isOrderMoneyVisible()).isTrue();
        assertThat(money.getCustomerTotal()).isEqualTo("₹455.00");
        assertThat(money.getRestaurantNetPayout()).isEqualTo("₹355.00");
        assertThat(money.getRiderNetPayout()).isEqualTo("₹45.00");
        assertThat(moneyReads.get()).isEqualTo(1);
        assertThat(moneyWrites.get()).isZero();
    }
}
