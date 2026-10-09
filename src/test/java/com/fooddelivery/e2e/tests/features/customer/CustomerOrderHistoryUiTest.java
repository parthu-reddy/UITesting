package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderHistoryPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
@Tag("feature-order-tracking")
public class CustomerOrderHistoryUiTest extends TestBase {

    private CustomerOrderHistoryPage history;
    private int blockedOrderPosts;

    @org.junit.jupiter.api.AfterEach
    void historyAndReorderSendNoOrderCreation() {
        assertThat(blockedOrderPosts).as("History/reorder checks never submit a new order").isZero();
    }

    @BeforeEach
    void loginCustomer() {
        testCustomerPhone = System.getProperty("customer.phone", "8000000484");
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        history = new CustomerOrderHistoryPage(customerPage);
        customerPage.route("**/api/v1/orders", route -> {
            if (route.request().method().equals("POST")) {
                blockedOrderPosts++;
                route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setStatus(409)
                        .setContentType("application/json").setBody("{\"success\":false,\"message\":\"History/reorder E2E blocked order creation\"}"));
            } else route.resume();
        });
    }

    private void openHistory() {
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        history.waitForHistoryLoad();
    }

    private void requirePopulatedHistory() {
        openHistory();
        assertThat(history.getOrderCount()).as("Requires a retained delivered/cancelled history fixture; run the lifecycle first").isPositive();
    }

    @Test
    @DisplayName("HISTORY-01: A customer with completed orders sees populated history")
    void historyRendersDefinedState() {
        requirePopulatedHistory();
        assertThat(customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)))
                .hasAttribute("aria-selected", "true");
        assertThat(history.hasDefinedState())
                .as("History must render either order rows or its explicit empty state")
                .isTrue();
        assertThat(customerPage.locator("[data-screen='settings']").innerText())
                .doesNotContain("undefined", "null");
    }

    @Test
    @DisplayName("HISTORY-02/03/04: Populated rows expose exact identity, restaurant, date, status and total")
    void populatedRowsHaveRequiredSummaryFields() {
        requirePopulatedHistory();

        for (int index = 0; index < history.getOrderCount(); index++) {
            Locator row = history.historyCards().nth(index);
            String fullId = row.getAttribute("data-order-id");
            assertThat(java.util.UUID.fromString(fullId).toString()).isEqualTo(fullId);
            assertThat(row.getByText(fullId.substring(0, 8), new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(row.locator("time")).isVisible();
            assertThat(java.time.Instant.parse(row.locator("time").getAttribute("datetime"))).isNotNull();
            assertThat(row.locator("time").innerText()).isNotBlank().doesNotContain("Invalid Date");
            String text = row.innerText();
            assertThat(text).contains("₹").doesNotContain("undefined", "null", "Unknown");
            assertThat(row.locator("h5").innerText()).isNotBlank();
            assertThat(row.locator("span.rounded-full")).isVisible();
            assertThat(row.locator("span.rounded-full").innerText()).isNotBlank().doesNotContainIgnoringCase("Unknown", "null", "undefined");
        }
    }

    @Test
    @DisplayName("HISTORY-05: Selecting a history row opens that exact order tracker")
    void historyRowOpensExactOrderTrackerWhenHistoryExists() {
        requirePopulatedHistory();

        String fullId = history.historyCards().first().getAttribute("data-order-id");
        history.clickOrderCard(0);

        assertThat(customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true))).isHidden();
        assertThat(customerPage.locator("[data-testid='order-tracker'][data-order-id='" + fullId + "']")).isVisible();
    }

    @Tag("feature-cancellation")
    @Test
    @DisplayName("HISTORY-06: A real restaurant-cancelled history row opens its exact terminal tracker")
    void cancelledHistoryRowOpensExactTerminalTracker() {
        requirePopulatedHistory();
        String ownedId=System.getProperty("history.cancelled.order.id", "").trim();
        if(!ownedId.isEmpty())assertThat(ownedId).matches("[0-9a-fA-F-]{36}");
        Locator cancelled = ownedId.isEmpty()
            ? history.historyCards().filter(new Locator.FilterOptions().setHasText("Cancelled by Restaurant")).first()
            : customerPage.locator("[data-testid='customer-history-order'][data-order-id='"+ownedId+"']");
        assertThat(cancelled).isVisible();
        String fullId = cancelled.getAttribute("data-order-id");
        if(!ownedId.isEmpty())assertThat(cancelled).containsText("Cancelled by Restaurant");
        cancelled.click();
        Locator tracker = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + fullId + "']");
        assertThat(tracker).hasAttribute("data-status", "CANCELLED_BY_RESTAURANT");
        assertThat(tracker.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Cancelled by Restaurant").setExact(true))).isVisible();
        if(!ownedId.isEmpty())assertThat(tracker.getByTestId("cancellation-reason")).containsText(System.getProperty("history.cancelled.reason", "Item out of stock"));
        new com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage(customerPage, fullId).dismissFailedOrder();
        assertThat(tracker).isHidden();
        assertThat(customerPage.getByPlaceholder("Search restaurants or cuisines")).isVisible();
        // Dismissing details is navigation, not deletion or cleanup.
        requirePopulatedHistory();
        assertThat(customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + fullId + "']")).isVisible();
    }

    @Tag("auto-cancel")
    @Test
    @DisplayName("HISTORY: Loading another real page appends unique rows and preserves the first page")
    void historyPaginationPreservesRowsWithoutDuplicates() {
        requirePopulatedHistory();
        java.util.List<String> before = history.historyCards().evaluateAll("rows => rows.map(row => row.dataset.orderId)")
                instanceof java.util.List<?> ids ? ids.stream().map(Object::toString).toList() : java.util.List.of();
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Load More History").setExact(true)).click();
        customerPage.waitForCondition(() -> history.getOrderCount() > before.size(),
                new Page.WaitForConditionOptions().setTimeout(30000));
        history.waitForHistoryLoad();
        java.util.List<String> after = ((java.util.List<?>) history.historyCards().evaluateAll("rows => rows.map(row => row.dataset.orderId)")).stream().map(Object::toString).toList();
        assertThat(after).containsAll(before);
        assertThat(new java.util.HashSet<>(after)).hasSize(after.size());
    }

    @Test
    @DisplayName("HISTORY UI contract: A successful empty response renders the explicit empty state")
    void successfulEmptyHistoryHasExplicitState() {
        customerPage.route("**/api/v1/orders/history**", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                .setStatus(200).setContentType("application/json")
                .setBody("{\"success\":true,\"data\":{\"content\":[],\"last\":true,\"first\":true,\"totalElements\":0,\"totalPages\":0,\"number\":0,\"size\":10,\"numberOfElements\":0,\"empty\":true},\"message\":\"Order history retrieved\",\"timestamp\":\"2026-09-29T10:00:00Z\"}")));
        openHistory();
        assertThat(history.emptyState()).isVisible();
        assertThat(history.getOrderCount()).isZero();
    }

    @Test
    @DisplayName("HISTORY UI contract: Failed history is recoverable and never reported as empty")
    void failedHistoryRetriesWithoutFalseEmptyState() {
        java.util.concurrent.atomic.AtomicInteger requests = new java.util.concurrent.atomic.AtomicInteger();
        customerPage.route("**/api/v1/orders/history**", route -> {
            if (requests.incrementAndGet() == 1) route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                    .setStatus(503).setContentType("application/json").setBody("{\"success\":false,\"message\":\"History unavailable\"}"));
            else route.resume();
        });
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        Locator error = customerPage.getByRole(AriaRole.ALERT).filter(new Locator.FilterOptions().setHasText("Couldn't load order history."));
        assertThat(error).isVisible();
        assertThat(history.emptyState()).isHidden();
        error.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Try again").setExact(true)).click();
        history.waitForHistoryLoad();
        assertThat(history.getOrderCount()).isPositive();
        assertThat(error).isHidden();
    }

    @Test
    @DisplayName("HISTORY UI contract: A pending response never satisfies settled history readiness")
    void pendingHistoryShowsLoadingUntilTheResponseCompletes() {
        java.util.concurrent.atomic.AtomicReference<com.microsoft.playwright.Route> pending = new java.util.concurrent.atomic.AtomicReference<>();
        customerPage.route("**/api/v1/orders/history**", pending::set);
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.waitForCondition(() -> pending.get() != null);
        assertThat(customerPage.getByTestId("customer-history-state")).hasAttribute("data-state", "loading");
        assertThat(history.emptyState()).isHidden();
        assertThat(history.hasDefinedState()).isFalse();
        com.microsoft.playwright.Route route = pending.get();
        route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setResponse(route.fetch()));
        history.waitForHistoryLoad();
        assertThat(history.getOrderCount()).isPositive();
    }


    private Locator firstReorder() {
        Locator first = customerPage.getByTestId("reorder-order").first();
        assertThat(first).isVisible();
        assertThat(first.getAttribute("data-order-id")).isNotBlank();
        return first;
    }

    @SuppressWarnings("unchecked")
    private java.util.List<java.util.Map<String, Object>> reorderThroughLiveUi() {
        Locator button = firstReorder();
        String id = button.getAttribute("data-order-id");
        com.microsoft.playwright.Response owned = customerPage.waitForResponse(
                response -> response.request().method().equals("GET") && com.fooddelivery.e2e.util.UrlPaths.path(response.url()).equals("/api/v1/orders/" + id), button::click);
        assertThat(owned.status()).isEqualTo(200);
        java.util.Map<?, ?> envelope = (java.util.Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", owned.text());
        java.util.Map<?, ?> order = (java.util.Map<?, ?>) envelope.get("data");
        assertThat(order.get("id")).isEqualTo(id);
        assertThat(order.get("deliveryStatus")).isEqualTo("DELIVERED");
        java.util.List<java.util.Map<String, Object>> lines = (java.util.List<java.util.Map<String, Object>>) order.get("items");
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();
        for (java.util.Map<String, Object> line : lines) {
            String name = (String) line.get("name");
            assertThat(cart.getByText(name, new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(cart.getByLabel(name + " quantity", new Locator.GetByLabelOptions().setExact(true)))
                    .hasText(String.valueOf(((Number) line.get("quantity")).intValue()));
        }
        assertThat(cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Checkout").setExact(true))).isVisible();
        return lines;
    }

    @Tag("feature-cart-checkout")
    @Test
    @DisplayName("REORDER-01/02: A real completed order restores its current items and quantities into the cart without purchasing")
    void completedOrderReordersIntoTheCartWithoutSubmitting() {
        assertThat(reorderThroughLiveUi()).isNotEmpty();
    }

    @Tag("feature-cart-checkout")
    @Test
    @DisplayName("REORDER UI contract: Declining replacement preserves the customer's edited cart")
    void decliningReorderReplacementPreservesEditedCart() {
        java.util.List<java.util.Map<String, Object>> lines = reorderThroughLiveUi();
        String name = (String) lines.get(0).get("name");
        int edited = ((Number) lines.get(0).get("quantity")).intValue() + 1;
        Locator cart = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        cart.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add one " + name).setExact(true)).click();
        assertThat(cart.getByLabel(name + " quantity", new Locator.GetByLabelOptions().setExact(true))).hasText(String.valueOf(edited));
        new com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage(customerPage).closeCart();
        customerPage.goBack();
        firstReorder().click();
        Locator confirm = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Replace this cart?").setExact(true));
        assertThat(confirm).isVisible();
        confirm.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Keep current cart").setExact(true)).click();
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("View Cart"))).click();
        assertThat(cart.getByLabel(name + " quantity", new Locator.GetByLabelOptions().setExact(true))).hasText(String.valueOf(edited));
    }

    @Tag("feature-cart-checkout")
    @Tag("feature-catalog")
    @Test
    @DisplayName("REORDER-03 UI contract: A currently closed outlet shows a warning without creating a cart")
    void closedReorderOutletShowsWarningWithoutCart() {
        customerPage.route("**/api/v1/restaurants/*", route -> {
            com.microsoft.playwright.APIResponse real = route.fetch();
            String closed = (String) customerPage.evaluate("text => { const body=JSON.parse(text); body.data.isOpen=false; return JSON.stringify(body); }", real.text());
            route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setResponse(real).setBody(closed));
        });
        firstReorder().click();
        assertThat(customerPage.getByText("This restaurant is currently unavailable. Please choose another kitchen.", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true))).isHidden();
        assertThat(customerPage.getByPlaceholder("Search restaurants or cuisines")).isVisible();
    }

    @Tag("feature-cart-checkout")
    @Test
    @DisplayName("REORDER-03 UI contract: An out-of-area quote warns and leaves the cart untouched")
    void unavailableReorderQuoteShowsWarningWithoutCart() {
        customerPage.route("**/api/v1/orders/quote", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                .setStatus(400).setContentType("application/json").setBody("{\"success\":false,\"message\":\"The restaurant is too far away (over 5km). Please select a closer restaurant.\"}")));
        firstReorder().click();
        assertThat(customerPage.getByText("The restaurant is too far away (over 5km). Please select a closer restaurant.", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true))).isHidden();
    }

    @Tag("feature-cart-checkout")
    @Tag("feature-catalog")
    @Test
    @DisplayName("REORDER UI contract: Missing catalogue items warn instead of silently building a partial cart")
    void missingReorderItemsShowWarningWithoutPartialCart() {
        customerPage.route("**/api/v1/restaurants/*/catalog/items", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
                .setStatus(200).setContentType("application/json").setBody("{\"success\":true,\"data\":[],\"message\":\"Menu items retrieved\",\"timestamp\":\"2026-09-29T10:00:00Z\"}")));
        firstReorder().click();
        assertThat(customerPage.getByText("Some items from this order are no longer available. Please choose from the current menu.", new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Your cart").setExact(true))).isHidden();
    }

}
