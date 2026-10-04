package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.delivery.DeliveryActiveJobPage;
import com.fooddelivery.e2e.pages.delivery.DispatchPingPage;
import com.fooddelivery.e2e.util.StateSetupHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flow")
@Tag("ui-only")
public class RiderFulfillmentTest extends TestBase {

    private com.fooddelivery.e2e.util.SeededRiderDuty riderDuty;
    private String pickupOtp;
    private String deliveryOtp;

    private String uniqueRiderPhone;
    private String uniqueCustomerPhone;
    private String uniqueRestaurantPhone;

    @BeforeEach
    public void setupOrder() {
        System.out.println("[TEST] Starting setupOrder...");
        uniqueRiderPhone = testRiderPhone; // seeded, approved rider (TEST-DATA.md); never create one
        uniqueCustomerPhone = "8000000003";
        uniqueRestaurantPhone = "9000000004"; // Brand 4 — known to have orderable items

        System.out.println("[TEST] 1. Logging rider in and going online");
        setupOnlineRider();
        
        System.out.println("[TEST] 2. Placing order for customer");
        riderDuty.assertReadyForCheckout();
        StateSetupHelper.OrderSetupResult result = StateSetupHelper.placeOrder(customerPage, uniqueCustomerPhone, uniqueRestaurantPhone);
        String fullOrderId = result.orderId;
        String outletName = result.outletName;
        String shortOrderId = fullOrderId.length() >= 8 ? fullOrderId.substring(0, 8) : fullOrderId;
        System.out.println("[TEST] Order placed: " + fullOrderId);
        
        System.out.println("[TEST] 3. Accepting order on restaurant side");
        StateSetupHelper.acceptOrder(restaurantPage, uniqueRestaurantPhone, shortOrderId, outletName);
        
        System.out.println("[TEST] 4. Waiting for dispatch ping on rider page");
        riderPage.bringToFront();
        DispatchPingPage pingPage = new DispatchPingPage(riderPage);
        pingPage.waitForPing();
        assertThat(pingPage.hasPing()).isTrue();
        pingPage.acceptDispatch();
        System.out.println("[TEST] Dispatch ping accepted");
        
        System.out.println("[TEST] 5. Preparing order on restaurant side");
        pickupOtp = StateSetupHelper.cookAndPrepareOrder(restaurantPage, shortOrderId);
        System.out.println("[TEST] Order prepared; pickup code is available.");
        
        System.out.println("[TEST] 6. Fetching delivery OTP on customer side");
        deliveryOtp = StateSetupHelper.getDeliveryOtp(customerPage, uniqueCustomerPhone);
        System.out.println("[TEST] Delivery code is available.");
        System.out.println("[TEST] setupOrder complete.");
    }

    @Test
    @DisplayName("RIDER-01: Rider can accept ping, pickup, and deliver")
    void riderCanCompleteFulfillment() {
        System.out.println("[TEST] Starting riderCanCompleteFulfillment");
        riderPage.bringToFront();
        
        DeliveryActiveJobPage activeJob = new DeliveryActiveJobPage(riderPage);
        activeJob.markArrivedAtRestaurant();
        
        // Enter pickup OTP and confirm
        assertThat(pickupOtp).isNotEmpty();
        activeJob.enterPickupOtp(pickupOtp);
        activeJob.swipeToConfirmPickup();

        // Wait for backend to process pickup and UI to transition to delivery phase
        riderPage.waitForTimeout(3000);

        // Enter delivery OTP and confirm
        assertThat(deliveryOtp).isNotEmpty();
        activeJob.enterDeliveryOtp(deliveryOtp);
        activeJob.swipeToConfirmDelivery();

        assertThat(activeJob.hasCompletedDelivery()).isTrue();
    }
    
    private void setupOnlineRider() {
        // A visible duty dashboard and Online Duty control are the E2E approval/readiness proof.
        // Never replace that user-facing preflight with a backend shortcut.
        riderDuty=com.fooddelivery.e2e.util.SeededRiderDuty.ensureOnline(riderPage,uniqueRiderPhone);
        riderDuty.assertReadyForCheckout();
    }
}
