package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
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

    @BeforeEach
    public void setupOrder() {
        uniqueRiderPhone = "7000000003";
        uniqueCustomerPhone = "8000000003";
        // First ensure Rider is online so they can receive pings
        setupOnlineRider();
        
        // Quickly place an order
        StateSetupHelper.OrderSetupResult result = StateSetupHelper.placeOrder(browser, uniqueCustomerPhone);
        String fullOrderId = result.orderId;
        String outletName = result.outletName;
        String shortOrderId = fullOrderId.length() >= 8 ? fullOrderId.substring(0, 8) : fullOrderId;
        
        // Rapidly accept and prepare the order on behalf of the restaurant
        pickupOtp = StateSetupHelper.acceptAndPrepareOrder(browser, testRestaurantPhone, shortOrderId, outletName);
        
        // Rapidly fetch the delivery OTP on behalf of the customer
        deliveryOtp = StateSetupHelper.getDeliveryOtp(browser, uniqueCustomerPhone);
    }

    @Test
    @DisplayName("RIDER-01: Rider can accept ping, pickup, and deliver")
    void riderCanCompleteFulfillment() {
        riderPage.bringToFront();
        
        DispatchPingPage pingPage = new DispatchPingPage(riderPage);
        pingPage.waitForPing();
        assertThat(pingPage.hasPing()).isTrue();
        pingPage.acceptDispatch();
        
        com.fooddelivery.e2e.pages.delivery.DeliveryActiveJobPage activeJob = new com.fooddelivery.e2e.pages.delivery.DeliveryActiveJobPage(riderPage);
        activeJob.markArrivedAtRestaurant();
        
        // Use the generated OTPs
        assertThat(pickupOtp).isNotEmpty();
        activeJob.enterPickupOtp(pickupOtp);
        activeJob.swipeToConfirmPickup();

        assertThat(deliveryOtp).isNotEmpty();
        activeJob.enterDeliveryOtp(deliveryOtp);
        activeJob.swipeToConfirmDelivery();

        assertThat(activeJob.hasCompletedDelivery()).isTrue();
    }
    
    private void setupOnlineRider() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", uniqueRiderPhone);
        
        try {
            riderPage.waitForTimeout(5000); // Wait for rider dashboard or onboarding to load
        } catch (Exception e) {
            System.err.println("Timeout waiting for rider dashboard or onboarding to load.");
        }
        
        RiderOnboardingWizardPage onboarding = new RiderOnboardingWizardPage(riderPage);
        if (onboarding.isWizardVisible()) {
            onboarding.completeOnboarding();
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
