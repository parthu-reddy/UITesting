package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderActionsPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantOrderQueuePage;
import com.microsoft.playwright.Page;
import java.util.Arrays;

/**
 * Utility to seed backend state by rapidly executing UI flows in a hidden context.
 * This guarantees the setup steps use the actual system flow but isolates them from the test itself.
 */
public class StateSetupHelper {

    public static class OrderSetupResult {
        public final String orderId;
        public final String outletName;
        public OrderSetupResult(String orderId, String outletName) {
            this.orderId = orderId;
            this.outletName = outletName;
        }
    }

    /**
     * Rapidly places an order and returns the setup result.
     */
    public static OrderSetupResult placeOrder(Page page, String customerPhone, String restaurantPhone) {
        int brandNum = Integer.parseInt(restaurantPhone.substring(7));
        String brandName = "Brand " + brandNum;
        
        try {
            page.navigate(TestConfig.APP_URL);
            new LoginPage(page).loginAs("Order Food", customerPhone);
            
            CustomerHomePage customerHome = new CustomerHomePage(page);
            customerHome.selectAddress("Home");
            customerHome.openRestaurant(brandName);
            
            CustomerMenuViewPage customerMenu = new CustomerMenuViewPage(page);
            String outletName = customerMenu.selectNearestOutlet();
            customerMenu.addQuickPrepItemToCart();
            customerMenu.clickViewCart();
            
            CustomerCartDrawerPage cart = new CustomerCartDrawerPage(page);
            cart.waitForCartOpen();
            cart.checkout();
            
            CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(page);
            tracker.verifyOrderStatus("Order Received");
            return new OrderSetupResult(tracker.getOrderId(), outletName);
        } finally {
            // Context stays open for reuse
        }
    }

    /**
     * Rapidly accepts an order on behalf of the restaurant.
     * This triggers the ORDER_ACCEPTED event, which in turn triggers the rider dispatch ping.
     */
    public static void acceptOrder(Page page, String restaurantPhone, String shortOrderId, String outletName) {
        try {
            page.navigate(TestConfig.APP_URL);
            new LoginPage(page).loginAs("Restaurant Partner", restaurantPhone);
            
            RestaurantOrderQueuePage orderQueue = new RestaurantOrderQueuePage(page);
            orderQueue.waitForQueueLoad();
            if (outletName != null) {
                orderQueue.selectOutlet(outletName);
                orderQueue.waitForQueueLoad();
            }
            
            RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(page);
            actions.acceptOrder(shortOrderId);
        } finally {
            // Context stays open for reuse
        }
    }

    /**
     * Rapidly prepares an order that has already been accepted.
     */
    public static String cookAndPrepareOrder(Page page, String shortOrderId) {
        try {
            RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(page);
            actions.startCooking(shortOrderId);
            actions.markPrepared(shortOrderId);
            
            return actions.getPickupOtp(shortOrderId);
        } finally {
            // Context stays open for reuse
        }
    }

    /**
     * Fetches the delivery OTP from a customer page that is already logged in.
     * Navigates to the home page, clicks the active order tracker, and extracts the OTP.
     */
    public static String getDeliveryOtp(Page page, String customerPhone) {
        try {
            // Navigate to home — customer is already logged in from placeOrder
            page.navigate(TestConfig.APP_URL);
            page.waitForTimeout(3000); // Wait for dashboard + SSE to connect and push order state
            
            // Check if address modal is open and close it if present to avoid intercepting clicks
            com.microsoft.playwright.Locator closeModal = page.locator("button[aria-label='Close dialog']").first();
            try {
                closeModal.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(2000).setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
                closeModal.click();
            } catch (Exception ignored) {}
            
            // If login screen appears (fresh context), log in
            com.microsoft.playwright.Locator roleButton = page.locator("button:has-text('Order Food'):visible").first();
            try {
                roleButton.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(3000).setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
                // Login screen is showing — need to log in
                new LoginPage(page).loginAs("Order Food", customerPhone);
            } catch (Exception ignored) {
                // Already logged in — proceed
            }
            
            // Wait for dashboard to load and click on the active order tracking button
            com.microsoft.playwright.Locator trackButton = page.locator("button[aria-label^='Track your order']").first();
            trackButton.waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(30000));
            trackButton.click();
            page.waitForTimeout(2000); // Let tracker load with latest state from SSE
            
            CustomerOrderTrackerPage tracker = new CustomerOrderTrackerPage(page);
            return tracker.getDeliveryOtp();
        } finally {
            // Context stays open for reuse
        }
    }

    /**
     * Rapidly logs in as a Rider and toggles their status to Online to ensure the system allows orders.
     */
    public static void ensureRiderIsOnline(Page page, String riderPhone) {
        try {
            page.navigate(TestConfig.APP_URL);
            new LoginPage(page).loginAs("Delivery Executive", riderPhone);
            
            try {
                page.waitForTimeout(3000); // Wait for dashboard
            } catch (Exception ignored) {}
            
            com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage onboarding = new com.fooddelivery.e2e.pages.delivery.RiderOnboardingWizardPage(page);
            if (onboarding.isWizardVisible()) {
                onboarding.completeOnboarding();
            }

            com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage toggle = new com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage(page);
            if (toggle.isOnline()) {
                toggle.goOffline();
                page.waitForTimeout(1000); 
            }
            toggle.goOnline();
            page.waitForTimeout(1000); 
        } finally {
            // Context stays open for reuse
        }
    }
}
