package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryActiveJobPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.delivery.DispatchPingPage;
import com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage;
import com.fooddelivery.e2e.util.StateSetupHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RiderFulfillmentTest extends TestBase {

    private String pickupOtp;
    private String deliveryOtp;

    private String uniqueRiderPhone;
    private String uniqueCustomerPhone;
    private String uniqueRestaurantPhone;

    @BeforeEach
    public void setupOrder() {
        System.out.println("[TEST] Starting setupOrder...");
        uniqueRiderPhone = String.format("70%08d", (int)(Math.random() * 100000000));
        uniqueCustomerPhone = "8000000003";
        uniqueRestaurantPhone = "9000000004"; // Brand 4 — known to have orderable items

        System.out.println("[TEST] 1. Logging rider in and going online");
        setupOnlineRider();
        
        System.out.println("[TEST] 2. Placing order for customer");
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
        System.out.println("[TEST] Order prepared. Pickup OTP: " + pickupOtp);
        
        System.out.println("[TEST] 6. Fetching delivery OTP on customer side");
        deliveryOtp = StateSetupHelper.getDeliveryOtp(customerPage, uniqueCustomerPhone);
        System.out.println("[TEST] Delivery OTP: " + deliveryOtp);
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
        riderPage.navigate(TestConfig.APP_URL);
        
        try {
            riderPage.waitForCondition(() -> 
                riderPage.locator("button:has-text('Delivery Executive')").first().isVisible() ||
                riderPage.locator("text=Go Online").first().isVisible() ||
                riderPage.locator("text=Go Offline").first().isVisible(),
                new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(10000));
        } catch (Exception e) {
            // Ignore, let the next line handle it
        }

        if (riderPage.locator("button:has-text('Delivery Executive')").first().isVisible() && 
            !riderPage.locator("text=Go Online").first().isVisible() && 
            !riderPage.locator("text=Go Offline").first().isVisible()) {
            new LoginPage(riderPage).loginAs("Delivery Executive", uniqueRiderPhone, "E2E Test Rider", "e2e-rider@example.com");
            try {
                riderPage.waitForTimeout(5000); // Wait for rider dashboard or onboarding to load
            } catch (Exception e) {
                System.err.println("Timeout waiting for rider dashboard or onboarding to load.");
            }
        } else {
            System.out.println("[TEST] Rider already logged in.");
        }
        
        RiderOnboardingWizardPage onboarding = new RiderOnboardingWizardPage(riderPage);
        if (onboarding.isWizardVisible()) {
            onboarding.completeDevModeOnboarding();
        }

        DeliveryOnlineTogglePage toggle = new DeliveryOnlineTogglePage(riderPage);
        if (toggle.isOnline()) {
            toggle.goOffline();
            riderPage.waitForTimeout(2000); // Give backend time to register offline status
        }
        toggle.goOnline();
        riderPage.waitForTimeout(2000); // Give backend time to register online status and location
        
        assertThat(toggle.isOnline()).isTrue();
    }
}
