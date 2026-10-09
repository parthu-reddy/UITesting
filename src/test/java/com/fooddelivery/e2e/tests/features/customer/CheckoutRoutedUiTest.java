package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.util.CheckoutAvailability;
import com.fooddelivery.e2e.util.SeededRiderDuty;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Routed error/balance UI contracts; real login, menu, availability and quotes. No real order POST. */
@Tag("feature-cart-checkout")
public class CheckoutRoutedUiTest extends TestBase {
    private SeededRiderDuty duty;
    private final AtomicInteger orderPosts = new AtomicInteger();
    private List<String> itemNames;

    @BeforeEach
    void interceptEveryOrderSubmission() {
        customerPage.route("**/api/v1/orders", route -> {
            if (!route.request().method().equals("POST")) { route.resume(); return; }
            orderPosts.incrementAndGet();
            route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                    .setBody("{\"success\":false,\"message\":\"Unexpected test submission intercepted\"}"));
        });
    }

    @AfterEach
    void finishIdleDuty() {
        if (duty != null) duty.close();
    }

    private PaymentModalPage openCheckout(boolean addSecondItem) {
        duty = SeededRiderDuty.ensureOnline(riderPage, testRiderPhone);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        if (addSecondItem) {
            customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                    .setHas(customerPage.getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("ADD").setExact(true))))
                    .first().getByRole(AriaRole.BUTTON,
                            new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        }
        menu.clickViewCart();
        Locator cart = cart();
        assertThat(cart).isVisible();
        itemNames = cart.locator("div.space-y-3 span.font-semibold").allTextContents();
        org.assertj.core.api.Assertions.assertThat(itemNames).hasSize(addSecondItem ? 2 : 1);
        duty.assertReadyForCheckout();
        CheckoutAvailability.requireDeliveryAvailable(
                CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));
        PaymentModalPage payment = new PaymentModalPage(customerPage);
        payment.waitForOpen();payment.waitForFinalQuote();
        return payment;
    }

    private Locator cart() {
        return customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
    }
    private Locator sheet() {
        return customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Checkout").setExact(true));
    }
    private Locator wallet() {
        return sheet().getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("La Bouffe Wallet")));
    }
    private Locator placeOrder() {
        return sheet().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Place order")));
    }
    private void walletResponse(Route route, int balance) {
        route.fulfill(new Route.FulfillOptions().setStatus(200).setContentType("application/json")
                .setBody("{\"id\":\"11111111-1111-4111-8111-111111111111\",\"entityId\":\"22222222-2222-4222-8222-222222222222\",\"entityType\":\"CUSTOMER\",\"balance\":"
                        + balance + ",\"currency\":\"INR\",\"status\":\"ACTIVE\"}"));
    }

    @ParameterizedTest
    @CsvSource({"503,Payment provider timed out", "402,Payment was declined"})
    void orderErrorPreservesCartAndExplicitRetryUsesChosenMethod(int status, String message) {
        PaymentModalPage payment = openCheckout(false);
        customerPage.unroute("**/api/v1/orders");
        List<String> methods = new ArrayList<>();
        customerPage.route("**/api/v1/orders", route -> {
            if (!route.request().method().equals("POST")) { route.resume(); return; }
            Map<?, ?> payload = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", route.request().postData());
            org.assertj.core.api.Assertions.assertThat(payload.get("quoteId")).isInstanceOf(String.class);
            org.assertj.core.api.Assertions.assertThat((String) payload.get("quoteId")).isNotBlank();
            methods.add((String) payload.get("paymentMethod"));
            int attempt = orderPosts.incrementAndGet();
            route.fulfill(new Route.FulfillOptions().setStatus(status).setContentType("application/json")
                    .setBody("{\"success\":false,\"message\":\"" + message + " attempt " + attempt + "\"}"));
        });
        payment.placeOrder("UPI");
        assertThat(sheet().getByRole(AriaRole.ALERT)).hasText(message + " attempt 1");
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isEqualTo(1);
        assertThat(sheet().getByText(itemNames.get(0), new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(placeOrder()).isEnabled();
        payment.placeOrder("Credit or debit card");
        assertThat(sheet().getByRole(AriaRole.ALERT)).hasText(message + " attempt 2");
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(methods).containsExactly("UPI", "CARD");
        payment.close();
        assertThat(cart().getByText(itemNames.get(0), new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart().locator("output")).hasText("1");
    }

    @Test
    void unavailableItemResponseRemovesOnlyTheRejectedItem() {
        PaymentModalPage payment = openCheckout(true);
        customerPage.unroute("**/api/v1/orders");
        customerPage.route("**/api/v1/orders", route -> {
            if (!route.request().method().equals("POST")) { route.resume(); return; }
            orderPosts.incrementAndGet();
            Map<?, ?> payload = (Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", route.request().postData());
            List<?> items = (List<?>) payload.get("items");
            String rejected = (String) ((Map<?, ?>) items.get(0)).get("menuItemId");
            route.fulfill(new Route.FulfillOptions().setStatus(400).setContentType("application/json")
                    .setBody("{\"success\":false,\"message\":\"Menu item unavailable\",\"data\":[\"" + rejected + "\"]}"));
        });
        payment.placeOrder("Credit or debit card");
        assertThat(sheet().getByRole(AriaRole.ALERT)).hasText("Removed unavailable items from cart: " + itemNames.get(0));
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isEqualTo(1);
        assertThat(sheet().getByText(itemNames.get(0), new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(sheet().getByText(itemNames.get(1), new Locator.GetByTextOptions().setExact(true))).isVisible();
        payment.close();
        assertThat(cart().getByText(itemNames.get(0), new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(cart().getByText(itemNames.get(1), new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart().locator("output")).hasText("1");
    }

    @Tag("feature-wallet")
    @Test
    void walletCannotBeSelectedBeforeItsBalanceArrives() {
        AtomicReference<Route> pending = new AtomicReference<>();
        customerPage.route("**/api/v1/money/customer/wallet", pending::set);
        PaymentModalPage payment = openCheckout(false);
        customerPage.waitForCondition(() -> pending.get() != null);
        assertThat(wallet()).isDisabled();assertThat(wallet()).hasAttribute("aria-checked", "false");
        assertThat(placeOrder()).isDisabled();
        walletResponse(pending.get(), 0);
        assertThat(wallet()).isDisabled();
        assertThat(sheet().getByText(java.util.regex.Pattern.compile("not enough for this order"))).isVisible();
        payment.selectPaymentMethod("Credit or debit card");
        payment.assertPaymentMethodSelected("Credit or debit card");assertThat(placeOrder()).isEnabled();
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isZero();
    }

    @Tag("feature-wallet")
    @Test
    void reopeningCheckoutWaitsForCurrentWalletBalance() {
        AtomicInteger requests = new AtomicInteger();AtomicReference<Route> pending = new AtomicReference<>();
        customerPage.route("**/api/v1/money/customer/wallet", route -> {
            if (requests.incrementAndGet() == 1) walletResponse(route, 10000); else pending.set(route);
        });
        PaymentModalPage payment = openCheckout(false);
        assertThat(wallet()).isEnabled();payment.selectPaymentMethod("Wallet");
        payment.assertPaymentMethodSelected("Wallet");assertThat(placeOrder()).isEnabled();
        payment.close();duty.assertReadyForCheckout();
        CheckoutAvailability.requireDeliveryAvailable(
                CheckoutAvailability.clickCheckoutAndWaitForAvailability(customerPage));
        payment.waitForOpen();customerPage.waitForCondition(() -> pending.get() != null);
        assertThat(wallet()).isDisabled();assertThat(wallet()).hasAttribute("aria-checked", "false");
        assertThat(placeOrder()).isDisabled();
        walletResponse(pending.get(), 0);
        assertThat(sheet().getByText(java.util.regex.Pattern.compile("not enough for this order"))).isVisible();
        assertThat(wallet()).isDisabled();
        payment.selectPaymentMethod("UPI");payment.assertPaymentMethodSelected("UPI");
        assertThat(placeOrder()).isEnabled();org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isZero();
    }

    private BigDecimal amount(String text) {
        Matcher match = Pattern.compile("₹\\s*([0-9,]+(?:\\.[0-9]+)?)").matcher(text);
        org.assertj.core.api.Assertions.assertThat(match.find()).as("An INR amount in %s", text).isTrue();
        return new BigDecimal(match.group(1).replace(",", ""));
    }

    @Tag("feature-wallet")
    @Test
    void tipChangesChargeWalletEligibilityAndExactSubmissionPayload() {
        AtomicReference<Route> pending = new AtomicReference<>();
        AtomicReference<Map<?, ?>> submitted = new AtomicReference<>();
        customerPage.route("**/api/v1/money/customer/wallet", pending::set);
        PaymentModalPage payment = openCheckout(false);
        customerPage.waitForCondition(() -> pending.get() != null);
        BigDecimal base = amount(placeOrder().innerText());
        Locator tipChoices = sheet().getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Tip your rider").setExact(true));
        assertThat(tipChoices.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName("₹0").setExact(true))).hasAttribute("aria-checked", "true");
        assertThat(sheet().getByText("Rider tip", new Locator.GetByTextOptions().setExact(true))).hasCount(0);
        walletResponse(pending.get(), base.setScale(0, java.math.RoundingMode.CEILING).intValueExact());
        assertThat(wallet()).isEnabled();payment.selectPaymentMethod("Wallet");
        payment.assertPaymentMethodSelected("Wallet");assertThat(placeOrder()).isEnabled();
        tipChoices.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName("₹20").setExact(true)).click();
        assertThat(tipChoices.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName("₹20").setExact(true))).hasAttribute("aria-checked", "true");
        assertThat(wallet()).isDisabled();assertThat(wallet()).hasAttribute("aria-checked", "false");
        org.assertj.core.api.Assertions.assertThat(amount(placeOrder().innerText()))
                .isEqualByComparingTo(base.add(new BigDecimal("20")));
        Locator tipLine = sheet().getByText("Rider tip", new Locator.GetByTextOptions().setExact(true)).locator("xpath=ancestor::div[contains(., '₹')][1]");
        org.assertj.core.api.Assertions.assertThat(amount(tipLine.innerText())).isEqualByComparingTo("20");
        customerPage.unroute("**/api/v1/orders");
        customerPage.route("**/api/v1/orders", route -> {
            if (!route.request().method().equals("POST")) { route.resume();return; }
            orderPosts.incrementAndGet();
            submitted.set((Map<?, ?>) customerPage.evaluate("text => JSON.parse(text)", route.request().postData()));
            route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                    .setBody("{\"success\":false,\"message\":\"Tip test submission intercepted\"}"));
        });
        payment.placeOrder("Credit or debit card");
        assertThat(sheet().getByRole(AriaRole.ALERT)).hasText("Tip test submission intercepted");
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(((Number) submitted.get().get("tipAmount")).intValue()).isEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(submitted.get().get("paymentMethod")).isEqualTo("CARD");
        org.assertj.core.api.Assertions.assertThat((String) submitted.get().get("quoteId")).isNotBlank();
        payment.close();assertThat(cart().getByText(itemNames.get(0),
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart().locator("output")).hasText("1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"close", "back", "reload"})
    void abandoningCheckoutPreservesCartAndSubmitsNoOrder(String action) {
        PaymentModalPage payment = openCheckout(false);
        payment.selectPaymentMethod("UPI");payment.assertPaymentMethodSelected("UPI");
        assertThat(placeOrder()).isEnabled();
        switch (action) {
            case "close" -> payment.close();
            case "back" -> customerPage.goBack();
            case "reload" -> {
                customerPage.reload();new CustomerDashboardPage(customerPage).waitForDashboard();
                customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
            }
            default -> throw new IllegalArgumentException(action);
        }
        assertThat(sheet()).isHidden();assertThat(cart()).isVisible();
        assertThat(cart().getByText(itemNames.get(0), new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(cart().locator("output")).hasText("1");
        org.assertj.core.api.Assertions.assertThat(orderPosts.get()).as("Abandonment must never submit an order").isZero();
    }

}
