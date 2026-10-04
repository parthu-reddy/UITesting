package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests that verify tab navigation renders correctly for all roles.
 */
@Tag("smoke")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NavigationSmokeTest extends TestBase {

    @BeforeEach
    void loginAllRoles() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);

        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);

        loginAsAdmin();
    }

    @Test
    @Order(1)
    @DisplayName("Customer tabs: Home, Orders, Settings")
    void customerTabs() {
        CustomerDashboardPage dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();

        dashboard.openOrdersTab();
        assertThat(customerPage.content()).containsAnyOf("Orders", "Order History", "Your Orders");

        dashboard.openSettingsTab();
        assertThat(customerPage.content()).containsAnyOf("Settings", "Profile", "Wallet", "Log Out");

        dashboard.openHomeTab();
        assertThat(customerPage.content()).containsAnyOf("Deliver to", "Restaurants");
    }

    @Test
    @Order(2)
    @DisplayName("Restaurant tabs: Orders, Menu, Settings, Earnings")
    void restaurantTabs() {
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();

        dashboard.openMenuTab();
        assertThat(restaurantPage.content()).containsAnyOf("Menu", "Items", "Categories");

        dashboard.openSettingsTab();
        assertThat(restaurantPage.content()).containsAnyOf("Settings", "Brand", "Outlet");

        // Close settings overlay before navigating to other tabs
        dashboard.openSettingsTab();

        dashboard.openEarningsTab();
        assertThat(restaurantPage.content()).containsAnyOf("Earnings", "Revenue", "Payout");

        dashboard.openOrdersTab();
    }

    @Test
    @Order(3)
    @DisplayName("Rider tabs: Active, History, Settings, Earnings")
    void riderTabs() {
        DeliveryDashboardPage dashboard = new DeliveryDashboardPage(riderPage);
        dashboard.waitForDashboard();

        dashboard.openHistoryTab();
        assertThat(riderPage.content()).containsAnyOf("History", "Past", "Completed");

        dashboard.openSettingsTab();
        assertThat(riderPage.content()).containsAnyOf("Settings", "Profile", "Vehicle");

        dashboard.openEarningsTab();
        assertThat(riderPage.content()).containsAnyOf("Earnings", "Today", "Total");

        dashboard.openActiveTab();
    }

    @Test
    @Order(4)
    @DisplayName("Admin tabs: Live Ops, Users, Ledger, Reviews, Support, Money Ops, Refunds")
    void adminTabs() {
        AdminPortalPage portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();

        portal.openUsersTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions
                .assertThat(adminPage.getByPlaceholder("User ID / Phone")).isVisible();

        portal.openLedgerTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Ledger Entries").setExact(true))).isVisible();

        portal.openReviewsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Review Moderation").setExact(true))).isVisible();

        portal.openSupportTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Support Tickets").setExact(true))).isVisible();

        portal.openMoneyOperationsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Money Operations").setExact(true))).isVisible();

        portal.openRefundsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Refund Exception Queue").setExact(true))).isVisible();

        portal.openLiveOpsTab();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(adminPage.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Active Orders").setExact(true))).isVisible();
    }
}
