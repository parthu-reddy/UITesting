package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryActiveJobPage;
import com.fooddelivery.e2e.pages.delivery.DispatchPingPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.assertj.core.api.Assertions;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Creates an actual delivered order through the customer, restaurant, and rider UIs.
 *
 * <p>The fixture is used by tests whose subject is post-delivery history. It deliberately does not
 * toggle an already-online rider offline and back on; the rider's UI state is server-driven and
 * {@link DeliveryOnlineTogglePage#goOnline()} only requests online duty when currently offline.
 */
public final class CompletedDeliveryFixture {

    private CompletedDeliveryFixture() { }

    public record Result(String orderId, String outletName) {
        public String shortOrderId() {
            return orderId.substring(0, Math.min(8, orderId.length()));
        }
    }

    public static Result completeOrder(Page customerPage, Page restaurantPage, Page riderPage,
                                       String customerPhone, String restaurantPhone, String riderPhone) {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", customerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", restaurantPhone);
        RestaurantDashboardPage restaurant = new RestaurantDashboardPage(restaurantPage);
        restaurant.waitForDashboard();

        try (SeededRiderDuty ignored = SeededRiderDuty.ensureOnline(riderPage, riderPhone)) {
            return completeWithOnlineRider(customerPage, restaurantPage, riderPage,
                    customerPhone, restaurantPhone, restaurant);
        }
    }

    private static Result completeWithOnlineRider(Page customerPage, Page restaurantPage, Page riderPage,
                                                   String customerPhone, String restaurantPhone,
                                                   RestaurantDashboardPage restaurant) {

        int brandNumber = Integer.parseInt(restaurantPhone.substring(7));
        String selectedOutlet = new NearbyOutletPage(customerPage)
                .openBrandAndSelectNearby("Brand " + brandNumber);
        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        // addQuickPrepItemToCart may move away from an empty nearby outlet. Use the outlet the
        // customer actually ordered from when processing the matching restaurant order.
        String outletName = menu.getSelectedOutletName();
        if (!selectedOutlet.equals(outletName)) {
            System.out.printf("[E2E] Quick-prep item required switching outlets: %s -> %s%n",
                    selectedOutlet, outletName);
        }
        menu.clickViewCart();

        Set<String> existingOrderIds = new HashSet<>();
        Locator existingTrackers = customerPage.locator("[data-testid='order-tracker']");
        for (int index = 0; index < existingTrackers.count(); index++) {
            String existingId = existingTrackers.nth(index).getAttribute("data-order-id");
            if (existingId != null) existingOrderIds.add(existingId);
        }

        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        cart.checkout();

        customerPage.waitForCondition(() -> hasNewTracker(customerPage, existingOrderIds),
                new Page.WaitForConditionOptions().setTimeout(30000));
        String orderId = findNewTrackerId(customerPage, existingOrderIds);
        Assertions.assertThat(orderId).as("newly placed order ID").isNotBlank();
        Result result = new Result(orderId, outletName);

        restaurant.selectOutlet(outletName);
        restaurant.openOrdersTab();
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        actions.acceptOrder(result.shortOrderId());
        actions.startCooking(result.shortOrderId());
        actions.markPrepared(result.shortOrderId());

        DispatchPingPage dispatch = new DispatchPingPage(riderPage);
        dispatch.waitForPing(outletName);
        assertThat(riderPage.getByText("ORDER CONTRACT #" + result.shortOrderId(),
                new Page.GetByTextOptions().setExact(true))).isVisible();
        dispatch.acceptDispatch(orderId);
        waitForActiveOrder(riderPage, orderId);

        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        String pickupOtp = actions.getPickupOtp(result.shortOrderId());
        Assertions.assertThat(pickupOtp).matches("[0-9]{6}");
        confirmStatus(riderPage, orderId, "AT_RESTAURANT", activeJob::markArrivedAtRestaurant);
        confirmPickupWhenRestaurantReady(riderPage, orderId, activeJob, pickupOtp);

        Locator deliveredTracker = customerPage.locator(
                "[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        Locator deliveryCode = deliveredTracker.locator("[data-testid='delivery-code']");
        deliveryCode.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        String deliveryOtp = deliveryCode.innerText().trim();
        Assertions.assertThat(deliveryOtp).matches("[0-9]{6}");
        activeJob.enterDeliveryOtp(deliveryOtp);
        confirmStatus(riderPage, orderId, "DELIVERED", activeJob::swipeToConfirmDelivery);

        // Wait until the customer service has persisted DELIVERED before querying rider history,
        // which reads the same order store asynchronously from the rider service.
        deliveredTracker.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(90000));

        return result;
    }

    private static boolean hasNewTracker(Page customerPage, Set<String> existingOrderIds) {
        return findNewTrackerId(customerPage, existingOrderIds) != null;
    }

    private static String findNewTrackerId(Page customerPage, Set<String> existingOrderIds) {
        Locator trackers = customerPage.locator("[data-testid='order-tracker']");
        for (int index = 0; index < trackers.count(); index++) {
            String orderId = trackers.nth(index).getAttribute("data-order-id");
            if (orderId != null && !existingOrderIds.contains(orderId)) return orderId;
        }
        return null;
    }

    private static void confirmStatus(Page riderPage, String orderId, String status, Runnable action) {
        Response response = riderPage.waitForResponse(
                candidate -> candidate.request().method().equals("POST")
                        && candidate.url().endsWith("/orders/" + orderId + "/status")
                        && candidate.request().postData() != null
                        && candidate.request().postData().contains("\"" + status + "\""),
                new Page.WaitForResponseOptions().setTimeout(60000), action);
        Assertions.assertThat(response.ok())
                .as("%s status confirmation returns HTTP %s", status, response.status())
                .isTrue();
    }

    /**
     * Restaurant readiness is published through an outbox, so the rider service may briefly
     * reject pickup after the restaurant UI has already shown READY_FOR_PICKUP. Retry only that
     * specific 400 response, for a bounded 15 seconds; other failures remain immediate failures.
     */
    private static void confirmPickupWhenRestaurantReady(Page riderPage, String orderId,
                                                           DeliveryActiveJobPage activeJob,
                                                           String pickupOtp) {
        final long retryWindowMs = 15_000;
        final long retryDelayMs = 1_500;
        long deadline = System.nanoTime() + retryWindowMs * 1_000_000;
        int attempt = 0;
        int lastStatus = -1;
        String lastBody = "";

        do {
            attempt++;
            activeJob.enterPickupOtp(pickupOtp);
            Response response = riderPage.waitForResponse(
                    candidate -> candidate.request().method().equals("POST")
                            && candidate.url().endsWith("/orders/" + orderId + "/status")
                            && candidate.request().postData() != null
                            && candidate.request().postData().contains("\"OUT_FOR_DELIVERY\""),
                    new Page.WaitForResponseOptions().setTimeout(60000),
                    activeJob::swipeToConfirmPickup);
            lastStatus = response.status();
            lastBody = response.text();
            if (response.ok()) return;

            boolean readinessNotPropagated = response.status() == 400
                    && lastBody.contains("Restaurant has not marked the order as ready yet");
            if (!readinessNotPropagated) {
                Assertions.assertThat(response.ok())
                        .as("OUT_FOR_DELIVERY status returns HTTP %s; response: %s", lastStatus, lastBody)
                        .isTrue();
            }

            if (System.nanoTime() >= deadline) break;
            System.out.printf("[E2E] Pickup attempt %d reached the readiness gate; waiting %d ms before retry%n",
                    attempt, retryDelayMs);
            riderPage.waitForTimeout(retryDelayMs);
        } while (System.nanoTime() < deadline);

        Assertions.fail("Delivery service did not observe restaurant readiness within " + retryWindowMs
                + " ms; last pickup response was HTTP " + lastStatus + ": " + lastBody);
    }

    private static void waitForActiveOrder(Page riderPage, String orderId) {
        riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Contract").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(60000));
        riderPage.getByText("#" + orderId, new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
        riderPage.getByPlaceholder("Enter 6-digit pickup OTP")
                .waitFor(new Locator.WaitForOptions().setTimeout(30000));
    }
}
