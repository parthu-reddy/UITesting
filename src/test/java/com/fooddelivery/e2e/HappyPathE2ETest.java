package com.fooddelivery.e2e;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.LoginPage;
import com.fooddelivery.e2e.pages.CustomerPage;
import com.fooddelivery.e2e.pages.RestaurantPage;
import com.fooddelivery.e2e.pages.DeliveryPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Page;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HappyPathE2ETest extends TestBase {

    @Test
    @Order(1)
    public void testHappyDeliveryPath() throws InterruptedException {
        // Initialize POMs
        LoginPage customerLogin = new LoginPage(customerPage);
        LoginPage restaurantLogin = new LoginPage(restaurantPage);
        LoginPage riderLogin = new LoginPage(riderPage);
        
        CustomerPage customerFlow = new CustomerPage(customerPage);
        RestaurantPage restaurantFlow = new RestaurantPage(restaurantPage);
        DeliveryPage deliveryFlow = new DeliveryPage(riderPage);

        System.out.println("Starting Happy Delivery Path Test");
        // 1. Log in to all three apps
        restaurantPage.navigate(APP_URL, new com.microsoft.playwright.Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.LOAD));
        restaurantLogin.loginAs("Restaurant Partner", "9000000001");
        System.out.println("Restaurant logged in");
        
        // 1. Rider Login & Go Online
        riderPage.navigate(APP_URL, new com.microsoft.playwright.Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.LOAD));
        riderLogin.loginAs("Rider 1", "7000000001");
        System.out.println("Rider logged in");
        deliveryFlow.goOnline();
        System.out.println("Rider is online");
        
        customerPage.navigate(APP_URL, new com.microsoft.playwright.Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.LOAD));
        customerLogin.loginAs("Order Food", "8000000015");
        System.out.println("Customer logged in");

        // 2. Customer places an order
        customerFlow.selectAddress("Home");
        System.out.println("Customer selected address");
        customerFlow.openRestaurant("Brand 1");
        System.out.println("Customer opened restaurant");
        
        // Test the new dropdown feature to select another outlet
        customerFlow.selectOutlet("Brand 1 Outlet 2");
        System.out.println("Customer changed outlet to Brand 1 Outlet 2");
        
        // Use generic Add for now matching the old script
        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Add$"))).first().click();
        customerPage.waitForTimeout(1000);
        System.out.println("Customer added item to cart");
        
        try {
            customerFlow.placeOrder();
            System.out.println("Customer placed order");
        } catch (Exception e) {
            System.err.println("TEST FAILED! Page HTML:");
            System.err.println(customerPage.content());
            throw e;
        }

        // 3. Customer checks status
        customerFlow.verifyOrderStatus("Order Received");
        System.out.println("Customer sees Order Received");
        
        // 4. Restaurant accepts order
        restaurantFlow.acceptOrder();
        System.out.println("Restaurant accepted order");
        
        // 5. Customer sees accepted
        customerFlow.verifyOrderStatus("Accepted by Kitchen");
        System.out.println("Customer sees Accepted by Kitchen");
        
        // 6. Restaurant prepares order
        restaurantFlow.startPreparing();
        System.out.println("Restaurant preparing order");
        
        // 7. Customer sees preparing
        customerFlow.verifyOrderStatus("Cooking & Packaging");
        System.out.println("Customer sees Cooking & Packaging");
        
        // 8. Restaurant marks order as ready
        restaurantFlow.markReadyForPickup();
        System.out.println("Restaurant marked order ready");
        
        // 9. Customer does not have a separate 'ready' label in new UI, skips checking
        
        // 10. Rider accepts delivery
        riderPage.locator("button:has-text('Accept & Open Map')").first().waitFor();
        riderPage.locator("button:has-text('Accept & Open Map')").first().click();
        System.out.println("Rider accepted delivery");
        
        // 11. Rider reaches restaurant
        riderPage.locator("button:has-text('Reached Restaurant')").first().waitFor();
        riderPage.locator("button:has-text('Reached Restaurant')").first().click();
        System.out.println("Rider reached restaurant");
        
        // 12. Rider marks order as picked up using Restaurant OTP
        String pickupOtpStr = restaurantFlow.getPickupOtp();
        deliveryFlow.markPickedUp(pickupOtpStr);
        System.out.println("Rider picked up order");
        
        // 13. Customer sees out for delivery
        customerFlow.verifyOrderStatus("Picked up by Delivery Executive");
        System.out.println("Customer sees Picked up by Delivery Executive");
        
        // 14. Rider reaches destination
        riderPage.locator("button:has-text('Reached Destination')").first().waitFor();
        riderPage.locator("button:has-text('Reached Destination')").first().click();
        System.out.println("Rider reached destination");
        
        // 15. Rider marks as delivered with Customer OTP
        String deliveryOtpStr = customerFlow.getDeliveryOtp();
        deliveryFlow.markDelivered(deliveryOtpStr);
        System.out.println("Rider delivered order");
        
        // 16. Customer sees delivered
        customerFlow.verifyOrderStatus("Handed Over & Verified");
        System.out.println("Customer sees Handed Over & Verified");
    }
}
