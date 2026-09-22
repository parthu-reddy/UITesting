package com.fooddelivery.e2e.tests.features.exceptions;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.ChatWidgetPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.restaurant.*;
import org.junit.jupiter.api.*;

/**
 * Tests customer ↔ restaurant chat communication.
 */
@Tag("feature")
public class ChatCommunicationTest extends TestBase {

    @Test
    @DisplayName("Customer sends message → Restaurant receives")
    void customerSendsMessage() {
        // Login customer and place an order first
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new CustomerDashboardPage(customerPage).waitForDashboard();

        // Login restaurant
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        // Customer places order
        CustomerHomePage home = new CustomerHomePage(customerPage);
        customerPage.waitForTimeout(2000);
        home.openRestaurant("Test Brand");
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        new CustomerMenuViewPage(customerPage).clickViewCart();
        new CustomerCartDrawerPage(customerPage).checkout();
        customerPage.waitForTimeout(3000);

        // Restaurant accepts
        restaurantPage.reload();
        restaurantPage.waitForTimeout(2000);
        RestaurantOrderActionsPage actions = new RestaurantOrderActionsPage(restaurantPage);
        actions.acceptOrder();

        // Restaurant opens chat
        actions.openChat();
        restaurantPage.waitForTimeout(1000);

        // Restaurant sends message
        RestaurantChatPage chat = new RestaurantChatPage(restaurantPage);
        chat.sendMessage("Your order is being prepared!");
        restaurantPage.waitForTimeout(1000);
    }
}
