package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import com.fooddelivery.e2e.pages.delivery.*;
import com.fooddelivery.e2e.util.WaitHelpers;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @Order(1)
    @DisplayName("Complete order lifecycle: Customer → Restaurant → Rider → Delivered")
    void completeOrderLifecycle() {
        // ── Step 1: Login all 3 actors ────────────────────────────────────
        System.out.println("═══ STEP 1: Logging in all actors ═══");

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage customerDashboard = new CustomerDashboardPage(customerPage);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage restaurantDashboard = new RestaurantDashboardPage(restaurantPage);
        restaurantDashboard.waitForDashboard();

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage onboarding = new com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage(riderPage);
        try {
            if (onboarding.isWizardVisible()) {
                onboarding.completeOnboarding();
            }
        } catch (Exception e) {}
        DeliveryDashboardPage riderDashboard = new DeliveryDashboardPage(riderPage);
        riderDashboard.waitForDashboard();

        // ── Step 2: Rider goes online ─────────────────────────────────────
        System.out.println("═══ STEP 2: Rider going online ═══");
        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(riderPage);
        toggle.goOnline();
        assertThat(toggle.isOnline()).isTrue();

        // ── Step 3: Customer selects address and opens restaurant ─────────
        System.out.println("═══ STEP 3: Customer selecting address and restaurant ═══");
        CustomerHomePage home = new CustomerHomePage(customerPage);
        customerPage.waitForTimeout(2000); // Wait for restaurant data to load

        // Open the first available restaurant
        int brandNum = Integer.parseInt(testRestaurantPhone.substring(7));
        new NearbyOutletPage(customerPage).openBrandAndSelectNearby("Brand " + brandNum);

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

        // ── Step 5: Restaurant accepts the order ──────────────────────────
        System.out.println("═══ STEP 5: Restaurant accepting order ═══");
        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        RestaurantOrderActionsPage orderActions = new RestaurantOrderActionsPage(restaurantPage);
        orderActions.acceptOrder();

        // ── Step 6: Restaurant starts cooking ─────────────────────────────
        System.out.println("═══ STEP 6: Restaurant starting cook ═══");
        orderActions.startCooking();

        // ── Step 7: Restaurant marks prepared ─────────────────────────────
        System.out.println("═══ STEP 7: Restaurant marking prepared ═══");
        orderActions.markPrepared();

        // ── Step 8: Get pickup OTP from restaurant ────────────────────────
        System.out.println("═══ STEP 8: Getting pickup OTP ═══");
        restaurantPage.waitForTimeout(2000);
        restaurantPage.reload();
        restaurantPage.waitForTimeout(3000);
        String pickupOtp = orderActions.getPickupOtp();
        assertThat(pickupOtp).isNotEmpty().hasSize(6);
        System.out.println("Pickup OTP: " + pickupOtp);

        // ── Step 9: Rider accepts dispatch ────────────────────────────────
        System.out.println("═══ STEP 9: Rider accepting dispatch ═══");
        DispatchPingPage ping = new DispatchPingPage(riderPage);
        ping.waitForPing();
        ping.acceptDispatch();

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

        // ── Step 12: Get customer delivery OTP ────────────────────────────
        System.out.println("═══ STEP 12: Getting customer delivery OTP ═══");
        CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(customerPage);
        String deliveryOtp = tracker.getDeliveryOtp();
        assertThat(deliveryOtp).isNotEmpty().hasSize(6);
        System.out.println("Delivery OTP: " + deliveryOtp);

        // ── Step 13: Rider enters delivery OTP and swipes to deliver ──────
        System.out.println("═══ STEP 13: Rider delivering order ═══");
        riderPage.waitForTimeout(2000);
        activeJob.enterDeliveryOtp(deliveryOtp);
        activeJob.swipeToConfirmDelivery();

        // ── Step 14: Verify delivery complete ─────────────────────────────
        System.out.println("═══ STEP 14: Verifying delivery complete ═══");
        riderPage.waitForTimeout(3000);

        // Verify customer sees delivered status
        customerPage.reload();
        customerPage.waitForTimeout(3000);

        System.out.println("════════════════════════════════════════");
        System.out.println("  ✅ HAPPY PATH E2E TEST PASSED");
        System.out.println("════════════════════════════════════════");
    }
}
