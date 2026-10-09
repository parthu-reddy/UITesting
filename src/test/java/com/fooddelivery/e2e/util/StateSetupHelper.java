package com.fooddelivery.e2e.util;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerHomePage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.microsoft.playwright.Page;

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
            new LoginPage(page).login(customerPhone).openPortal(Portal.CUSTOMER);
            
            CustomerHomePage customerHome = new CustomerHomePage(page);
            customerHome.selectAddress("Home");
            customerHome.openRestaurant(brandName);
            
            CustomerMenuViewPage customerMenu = new CustomerMenuViewPage(page);
            String outletName = customerMenu.selectNearestOutlet();
            customerMenu.addQuickPrepItemToCart();
            customerMenu.clickViewCart();
            
            CustomerCartDrawerPage cart = new CustomerCartDrawerPage(page);
            cart.waitForCartOpen();
            // The id comes from this checkout's own response. Reading the tracker straight after checkout
            // returned an older active order's id when the customer had one (2026-10-08, fast defaults).
            com.microsoft.playwright.Response created = page.waitForResponse(r -> r.request().method().equals("POST")
                    && UrlPaths.path(r.url()).equals("/api/v1/orders"), cart::checkout);
            org.assertj.core.api.Assertions.assertThat(created.status()).isBetween(200, 299);
            java.util.Map<?, ?> body = (java.util.Map<?, ?>) page.evaluate("text => JSON.parse(text)", created.text());
            String orderId = (String) ((java.util.Map<?, ?>) body.get("data")).get("id");
            System.out.println("[E2E ORDER] created " + orderId + " at " + outletName);
            org.assertj.core.api.Assertions.assertThat(orderId).matches("[0-9a-fA-F-]{36}");

            new CustomerOrderTrackerPage(page, orderId).waitForTracker();
            return new OrderSetupResult(orderId, outletName);
        } finally {
            // Context stays open for reuse
        }
    }

    /**
     * Logs in through the rider portal and enables its visible Online Duty control before an order flow.
     */
    public static void ensureRiderIsOnline(Page page, String riderPhone) {
        // The dashboard and Online Duty control are the rider-visible approval/readiness contract.
        // Do not add a backend shortcut to this browser-only preflight.
        SeededRiderDuty.ensureOnline(page, riderPhone).assertReadyForCheckout();
    }
}
