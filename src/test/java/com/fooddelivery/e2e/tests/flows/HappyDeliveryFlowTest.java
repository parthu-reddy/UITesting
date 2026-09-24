package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Full happy-path E2E flow: Customer places order → Restaurant accepts/cooks/prepares →
 * Rider accepts dispatch, picks up with OTP, delivers with customer OTP → Order marked delivered.
 * <p>
 * This is the most critical test — it exercises the complete 3-actor order lifecycle
 * across 3 separate browser contexts communicating via SSE-driven real-time updates.
 * </p>
 */
@Tag("flow")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HappyDeliveryFlowTest extends TestBase {

    private static double parseInr(String text) {
        Matcher amount = Pattern.compile("₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)").matcher(text);
        org.assertj.core.api.Assertions.assertThat(amount.find())
                .as("INR amount in: %s", text).isTrue();
        return Double.parseDouble(amount.group(1).replace(",", ""));
    }

    private static String valueAfterLabel(String text, String label) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length - 1; i++) {
            if (!lines[i].trim().equalsIgnoreCase(label)) continue;
            for (int next = i + 1; next < lines.length; next++) {
                String value = lines[next].trim();
                if (!value.isEmpty()) return value;
            }
        }
        throw new AssertionError("No value after " + label + " in dispatch: " + text);
    }

    private void resumeAssignedOrder(String orderId, String outletName) {
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));
        Locator todayEarnings = riderPage.getByText("Today’s Earnings",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("span").last();
        double earningsBefore = parseInr(todayEarnings.innerText());

        assertThat(riderPage.getByText("#" + orderId,
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("RESTAURANT ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..")).containsText(outletName);

        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        if (!riderPage.getByPlaceholder("Ask customer for 6-digit OTP").isVisible()) {
            RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
            restaurantDashboard.selectOutlet(outletName);
            restaurantDashboard.openOrdersTab();
            RestaurantOrderActionsPage orderActions = new RestaurantOrderActionsPage(restaurantPage);
            String pickupOtp = orderActions.getPickupOtp(shortOrderId);
            org.assertj.core.api.Assertions.assertThat(pickupOtp).matches("[0-9]{6}");

            Locator arrived = riderPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true));
            if (arrived.isVisible()) activeJob.markArrivedAtRestaurant();
            activeJob.enterPickupOtp(pickupOtp);
            activeJob.swipeToConfirmPickup();
        }
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();

        customerPage.reload();
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String deliveryOtp = tracker.getDeliveryOtp();
        org.assertj.core.api.Assertions.assertThat(deliveryOtp).matches("[0-9]{6}");
        activeJob.enterDeliveryOtp(deliveryOtp);
        activeJob.swipeToConfirmDelivery();

        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Online Duty$")))).isVisible();
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator completedTrip = riderPage.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("ORDER #" + shortOrderId))
                .filter(new Locator.FilterOptions().setHasText("Delivered"));
        completedTrip.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(completedTrip).containsText(outletName);
        double recordedPayout = parseInr(completedTrip.innerText());
        org.assertj.core.api.Assertions.assertThat(recordedPayout).isPositive();
        riderPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .locator("..").getByRole(AriaRole.BUTTON).click();
        riderPage.waitForCondition(() -> parseInr(todayEarnings.innerText()) >= earningsBefore + recordedPayout,
                new Page.WaitForConditionOptions().setTimeout(30000));
    }

    @Test
    @Order(1)
    @DisplayName("Complete order lifecycle: Customer → Restaurant → Rider → Delivered")
    void completeOrderLifecycle() {
        // ── Step 1: Login all 3 actors ────────────────────────────────────
        System.out.println("═══ STEP 1: Logging in all actors ═══");
        System.out.printf("Customer=%s Restaurant=%s Rider=%s%n",
                testCustomerPhone, testRestaurantPhone, testRiderPhone);

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        String restaurantPhone = testRestaurantPhone;
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", restaurantPhone);
        RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
        restaurantDashboard.waitForDashboard();

        riderPage.navigate(TestConfig.APP_URL);
        String riderPhone = testRiderPhone;
        new LoginPage(riderPage).loginAs("Delivery Executive", riderPhone);
        DeliveryDashboardPage riderDashboard = new DeliveryDashboardPage(riderPage);
        riderDashboard.waitForDashboard();

        String resumeOrderId = System.getProperty("resume.order.id", "").trim();
        if (!resumeOrderId.isEmpty()) {
            String resumeOutlet = System.getProperty("resume.outlet", "").trim();
            org.assertj.core.api.Assertions.assertThat(resumeOutlet)
                    .as("-Dresume.outlet is required with -Dresume.order.id").isNotBlank();
            resumeAssignedOrder(resumeOrderId, resumeOutlet);
            return;
        }

        // ── Step 2: Rider goes online ─────────────────────────────────────
        System.out.println("═══ STEP 2: Rider going online ═══");
        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(riderPage);
        toggle.goOnline();
        assertThat(toggle.isOnline()).isTrue();
        Locator todayEarnings = riderPage.getByText("Today’s Earnings",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("span").last();
        assertThat(todayEarnings).hasText(Pattern.compile("^₹[0-9,]+(?:\\.[0-9]{2})?$"));
        double earningsBefore = parseInr(todayEarnings.innerText());

        // ── Step 3: Customer selects address and opens restaurant ─────────
        System.out.println("═══ STEP 3: Customer selecting address and restaurant ═══");
        customerPage.waitForTimeout(2000); // Wait for restaurant data to load

        // Use the seeded restaurant's brand and an outlet that Home can reach.
        int brandNum = Integer.parseInt(restaurantPhone.substring(7));
        String selectedOutlet = new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand " + brandNum);

        // ── Step 4: Customer adds item and places order ───────────────────
        System.out.println("═══ STEP 4: Customer adding item and placing order ═══");
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();

        // Click View Cart and checkout
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();

        // Wait for order to be placed
        customerPage.waitForTimeout(3000);
        System.out.println("═══ Order placed successfully ═══");

        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String orderId = tracker.getOrderId();
        assertThat(orderId).as("New order must appear in the customer tracker").isNotBlank();
        String shortOrderId = orderId.substring(0, Math.min(8, orderId.length()));
        System.out.println("Tracking Order ID: " + orderId);
        System.out.println("Short Order ID: " + shortOrderId);

        // ── Step 5: Restaurant accepts the order ──────────────────────────
        System.out.println("═══ STEP 5: Restaurant accepting order ═══");
        restaurantDashboard.selectOutlet(selectedOutlet);
        restaurantDashboard.openOrdersTab();
        RestaurantOrderActionsPage orderActions = new RestaurantOrderActionsPage(restaurantPage);
        orderActions.acceptOrder(shortOrderId);

        // ── Step 6: Restaurant starts cooking ─────────────────────────────
        System.out.println("═══ STEP 6: Restaurant starting cook ═══");
        orderActions.startCooking(shortOrderId);

        // ── Step 7: Restaurant marks prepared ─────────────────────────────
        System.out.println("═══ STEP 7: Restaurant marking prepared ═══");
        orderActions.markPrepared(shortOrderId);

        // Accept the short-lived dispatch before opening the restaurant OTP.
        System.out.println("═══ STEP 8: Rider accepting dispatch ═══");
        DispatchPingPage ping = new DispatchPingPage(riderPage);
        ping.waitForPing();
        Locator dispatch = riderPage.getByRole(AriaRole.ALERT);
        // Take one text snapshot, then accept immediately. Chaining exact child locators here once
        // consumed the complete server-side 60-second window before the click reached the API.
        String dispatchText = dispatch.innerText();
        String countdownLabel = dispatch.getByRole(AriaRole.TIMER).getAttribute("aria-label");
        String pickupText = valueAfterLabel(dispatchText, "Pickup");
        String deliveryAddress = valueAfterLabel(dispatchText, "Dropoff");
        int declineActions = dispatch.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Decline").setExact(true)).count();
        int acceptActions = dispatch.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Accept Order").setExact(true)).count();
        ping.acceptDispatch();

        org.assertj.core.api.Assertions.assertThat(dispatchText).containsIgnoringCase("New Dispatch");
        org.assertj.core.api.Assertions.assertThat(countdownLabel)
                .matches("^[0-9]+ seconds left to accept$");
        org.assertj.core.api.Assertions.assertThat(pickupText).contains(selectedOutlet);
        org.assertj.core.api.Assertions.assertThat(deliveryAddress).isNotBlank();
        org.assertj.core.api.Assertions.assertThat(declineActions).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(acceptActions).isEqualTo(1);
        double dispatchFee = parseInr(dispatchText);
        org.assertj.core.api.Assertions.assertThat(dispatchFee)
                .as("gross delivery fee shown in the dispatch offer").isPositive();

        Locator activeContract = riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("..");
        assertThat(activeContract).containsText(orderId);
        Locator restaurantAddress = riderPage.getByText("RESTAURANT ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..");
        Locator customerAddress = riderPage.getByText("DELIVERY ADDRESS",
                new Page.GetByTextOptions().setExact(true)).locator("..");
        assertThat(restaurantAddress).containsText(selectedOutlet);
        assertThat(customerAddress).containsText(deliveryAddress);
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Mark Arrived at Restaurant").setExact(true))).isVisible();

        // An accepted assignment must survive a page reload without resetting or duplicating it.
        riderPage.reload();
        waitForActiveOrderAfterReload(orderId);
        assertThat(riderPage.getByPlaceholder("Enter 6-digit pickup OTP")).isVisible();

        System.out.println("═══ STEP 9: Getting pickup OTP ═══");
        String pickupOtp = orderActions.getPickupOtp(shortOrderId);
        assertThat(pickupOtp).matches("[0-9]{6}");

        // ── Step 10: Rider marks arrived at restaurant ────────────────────
        System.out.println("═══ STEP 10: Rider marking arrived ═══");
        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        riderPage.waitForTimeout(2000);
        activeJob.markArrivedAtRestaurant();

        // ── Step 11: Rider enters pickup OTP and swipes to confirm ────────
        System.out.println("═══ STEP 11: Rider picking up order ═══");
        riderPage.waitForTimeout(2000);
        activeJob.enterPickupOtp(pickupOtp);
        activeJob.swipeToConfirmPickup();
        assertThat(riderPage.getByText("Step 2: Deliver to door",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByText("Package Picked Up",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();
        assertThat(customerAddress).containsText(deliveryAddress);

        // Delivery phase is server-backed and must be restored after reload.
        riderPage.reload();
        waitForActiveOrderAfterReload(orderId);
        assertThat(riderPage.getByText("Step 2: Deliver to door",
                new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(riderPage.getByPlaceholder("Ask customer for 6-digit OTP")).isVisible();

        // ── Step 12: Get customer delivery OTP ────────────────────────────
        System.out.println("═══ STEP 12: Getting customer delivery OTP ═══");
        String deliveryOtp = tracker.getDeliveryOtp();
        assertThat(deliveryOtp).isNotEmpty().hasSize(6);

        // ── Step 13: Rider enters delivery OTP and swipes to deliver ──────
        System.out.println("═══ STEP 13: Rider delivering order ═══");
        riderPage.waitForTimeout(2000);
        activeJob.enterDeliveryOtp(deliveryOtp);
        activeJob.swipeToConfirmDelivery();

        // ── Step 14: Verify delivery complete ─────────────────────────────
        System.out.println("═══ STEP 14: Verifying delivery complete ═══");
        // The rider returns to the scanning screen after delivery. Its completed
        // history records the exact order, unlike the still-open customer tracker.
        assertThat(riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Online Duty$")))).isVisible();
        assertThat(riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        riderPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        Locator completedTrip = riderPage.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("ORDER #" + shortOrderId))
                .filter(new Locator.FilterOptions().setHasText("Delivered"));
        completedTrip.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(completedTrip).containsText(selectedOutlet);
        assertThat(completedTrip).containsText(Pattern.compile("\\+₹[0-9,]+(?:\\.[0-9]{2})?"));
        double recordedPayout = parseInr(completedTrip.innerText());
        org.assertj.core.api.Assertions.assertThat(recordedPayout)
                .as("completed-trip net payout should be positive")
                .isPositive();

        riderPage.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .locator("..").getByRole(AriaRole.BUTTON).click();
        riderPage.waitForCondition(() -> parseInr(todayEarnings.innerText()) >= earningsBefore + recordedPayout,
                new Page.WaitForConditionOptions().setTimeout(30000));
        double earningsAfter = parseInr(todayEarnings.innerText());
        org.assertj.core.api.Assertions.assertThat(earningsAfter)
                .as("today's earnings after exact completed delivery")
                .isGreaterThanOrEqualTo(earningsBefore + recordedPayout);

        // The customer's still-open tracker must converge to the terminal state too. This covers
        // the active-order disappearance path, where polling fetches the missing order by ID.
        Locator rateOrderPrompt = customerPage.getByTestId("rate-order-prompt");
        rateOrderPrompt.waitFor(new Locator.WaitForOptions().setTimeout(45000));
        assertThat(rateOrderPrompt).containsText("Order Delivered");

        System.out.println("════════════════════════════════════════");
        System.out.println("  ✅ HAPPY PATH E2E TEST PASSED");
        System.out.println("════════════════════════════════════════");
    }

    private void waitForActiveOrderAfterReload(String orderId) {
        Locator activeContract = riderPage.getByText("Active Contract",
                new Page.GetByTextOptions().setExact(true));
        activeContract.waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(60000));
        riderPage.getByText("#" + orderId,
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                        .setTimeout(10000));
    }
}
