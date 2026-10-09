package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.util.CheckoutAvailability;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("feature-cart-checkout")
@Tag("feature-order-tracking")
public class CustomerOrderPlacementTest extends TestBase {
    @Tag("auto-cancel")
    @Test
    @DisplayName("CHECKOUT-22: Dev mock payment persists the tip-inclusive charge")
    void tippedOrderMatchesMockPaidTotal() throws Exception {
        placeOrderAndValidate(20, false);
    }

    @Tag("auto-cancel")
    @Test
    @DisplayName("CHECKOUT-11-13/24: Real Dev order matches creation response/tracker, cannot redeem its quote twice, and a failed tracking read preserves one order")
    void trackingReadFailureDoesNotCreateDuplicateOrder() throws Exception {
        placeOrderAndValidate(0, true);
    }

    private void placeOrderAndValidate(int tip, boolean failTrackingRead) throws Exception {
        AtomicInteger successfulCreates = new AtomicInteger();
        AtomicInteger failedTrackingReads = new AtomicInteger();
        AtomicReference<String> trackedId = new AtomicReference<>();
        customerPage.onResponse(response -> {
            if (response.request().method().equals("POST") && response.status() == 200
                    && com.fooddelivery.e2e.util.UrlPaths.path(response.url()).equals("/api/v1/orders")) successfulCreates.incrementAndGet();
        });
        if (failTrackingRead) customerPage.route("**/api/v1/orders/*", route -> {
            if (route.request().method().equals("GET") && trackedId.get() != null
                    && java.net.URI.create(route.request().url()).getPath().equals("/api/v1/orders/" + trackedId.get())) {
                failedTrackingReads.incrementAndGet();
                route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                        .setBody("{\"success\":false,\"message\":\"Tracking refresh temporarily unavailable\"}"));
            } else route.resume();
        });
        try (SeededRiderDuty duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone)) {
            customerPage.navigate(TestConfig.APP_URL);
            new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
            new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
            String outletName = new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
            String customerId = (String) customerPage.evaluate(
                    "() => JSON.parse(localStorage.getItem('user_profile')).id");
            CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
            menu.addQuickPrepItemToCart();menu.clickViewCart();
            CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
            cart.waitForCartOpen();String itemName = cart.getFirstItemName();
            duty.assertReadyForCheckout();
            CheckoutAvailability.requireDeliveryAvailable(
                    CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));
            PaymentModalPage payment = new PaymentModalPage(customerPage);
            payment.waitForOpen();payment.waitForFinalQuote();
            Locator sheet = customerPage.getByRole(AriaRole.DIALOG,
                    new Page.GetByRoleOptions().setName("Checkout").setExact(true));
            String totalText = sheet.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Place order"))).innerText();
            java.util.regex.Matcher amount = java.util.regex.Pattern.compile("₹\\s*([0-9,]+(?:\\.[0-9]+)?)").matcher(totalText);
            assertThat(amount.find()).as("Final quoted amount on Place order").isTrue();
            BigDecimal quotedBase = new BigDecimal(amount.group(1).replace(",", ""));
            if (tip > 0) sheet.getByRole(AriaRole.RADIOGROUP,
                    new Locator.GetByRoleOptions().setName("Tip your rider").setExact(true))
                    .getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName("₹" + tip).setExact(true)).click();
            // response.request().postData() was null for this POST on 2026-10-08 (cause not established); an
            // intercepted request exposes its body. The route only records it and resumes it unchanged to Dev.
            java.util.concurrent.atomic.AtomicReference<String> sentBody = new java.util.concurrent.atomic.AtomicReference<>();
            customerPage.route("**/api/v1/orders", route -> {
                if ("POST".equals(route.request().method())) sentBody.set(route.request().postData());
                route.resume();
            });
            Response created = customerPage.waitForResponse(r -> r.request().method().equals("POST")
                    && com.fooddelivery.e2e.util.UrlPaths.path(r.url()).equals("/api/v1/orders"),
                    () -> payment.placeOrder("Credit or debit card"));
            assertThat(created.status()).as("Real Dev order creation").isEqualTo(200);
            Map<?, ?> body = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", created.text());
            Map<?, ?> order = (Map<?, ?>) body.get("data");
            customerPage.unroute("**/api/v1/orders");
            assertThat(sentBody.get()).as("order request body seen by the pass-through route").isNotNull();
            Map<?, ?> payload = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", sentBody.get());
            String orderId = (String) order.get("id");
            assertThat(UUID.fromString(orderId).toString()).isEqualTo(orderId);
            trackedId.set(orderId);
            Path manifests = Path.of("target", "checkout");Files.createDirectories(manifests);
            String manifest = (String) customerPage.evaluate("o => JSON.stringify(o, null, 2)", Map.of(
                    "orderId", orderId, "customerId", customerId, "customerPhone", testCustomerPhone,
                    "riderPhone", testRiderPhone, "outletName", outletName,
                    "quoteId", payload.get("quoteId"), "paymentMethod", "CARD", "tipAmount", tip, "dataPolicy", "retain"));
            Files.writeString(manifests.resolve(orderId + ".json"), manifest);
            assertThat(body.get("success")).isEqualTo(true);
            assertThat(order.get("customerId")).isEqualTo(customerId);
            assertThat(order.get("restaurantId")).isEqualTo(payload.get("restaurantId"));
            assertThat(order.get("paymentMethod")).isEqualTo("CARD");
            assertThat(new BigDecimal(order.get("totalAmount").toString())).isEqualByComparingTo(quotedBase.add(BigDecimal.valueOf(tip)));
            assertThat(new BigDecimal(order.get("tipAmount").toString())).isEqualByComparingTo(BigDecimal.valueOf(tip));
            assertThat(new BigDecimal((payload.containsKey("tipAmount") ? payload.get("tipAmount") : 0).toString())).isEqualByComparingTo(BigDecimal.valueOf(tip));
            assertThat((List<?>) order.get("items")).hasSize(1);
            assertThat(((Map<?, ?>) ((List<?>) order.get("items")).get(0)).get("name")).isEqualTo(itemName);
            Locator tracker = customerPage.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(tracker).isVisible();
            // Dev mock payment advances CREATED to PENDING_ACCEPTANCE asynchronously.
            // Require this exact order's paid/restaurant-ready state, independent of older retained orders.
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(tracker)
                    .hasAttribute("data-status", "PENDING_ACCEPTANCE",
                            new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(20000));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByRole(AriaRole.DIALOG,
                    new Page.GetByRoleOptions().setName("Checkout").setExact(true))).isHidden();
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(customerPage.getByText("View Cart",
                    new Page.GetByTextOptions().setExact(true))).isHidden();

            // A consumed quote is rejected immediately, without waiting for expiry or making a second charge.
            Map<?, ?> replay = (Map<?, ?>) customerPage.evaluate("""
                async payload => {
                    const response = await fetch('/api/v1/orders', {
                        method: 'POST', credentials: 'omit',
                        headers: {'Content-Type': 'application/json', Authorization: 'Bearer ' + localStorage.getItem('auth_token')},
                        body: JSON.stringify(payload)
                    });
                    const body = await response.json();
                    return {status: response.status, errorCode: body.errorCode, success: body.success};
                }
                """, payload);
            assertThat(((Number) replay.get("status")).intValue()).isEqualTo(409);
            assertThat(replay.get("errorCode")).isEqualTo("QUOTE_EXPIRED");
            assertThat(replay.get("success")).isEqualTo(false);
            assertThat(successfulCreates.get()).as("Exactly one successful order creation, including replay").isEqualTo(1);
            if (failTrackingRead) assertThat(failedTrackingReads.get()).as("The tracking-read failure must actually execute").isEqualTo(1);
            System.out.println("[CHECKOUT] Retained Dev order " + orderId + "; consumed quote replay rejected; no cleanup");
        }
    }
}
