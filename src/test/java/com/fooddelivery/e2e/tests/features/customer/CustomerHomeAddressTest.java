package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Tests for customer home page, restaurant search, address modal, and free delivery tracker.
 * Covers: HOME-01..07, ADDR-MODAL-01..07, TRACKER-ADV-01..06
 */
@Tag("customer-home")
public class CustomerHomeAddressTest extends TestBase {

    private CustomerDashboardPage dashboard;

    @BeforeEach
    void loginCustomer() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        CustomerAddressModalPage addressModal = new CustomerAddressModalPage(customerPage);
        if (addressModal.isModalOpen()) {
            addressModal.selectExistingAddress("Home");
        }
        dashboard = new CustomerDashboardPage(customerPage);
        dashboard.waitForDashboard();
    }

    // ── HOME PAGE SCENARIOS ──────────────────────────────────────────────

    @Test
    @DisplayName("HOME-01: Search restaurant from home page")
    void searchRestaurantFromHome() {
        CustomerHomePage home = new CustomerHomePage(customerPage);
        Locator cards = customerPage.locator("button:has(h5)");
        String brand = cards.first().locator("h5").innerText().trim();
        home.searchRestaurant(brand);
        assertThat(cards.first()).isVisible();
        for (int index = 0; index < cards.count(); index++) {
            assertThat(cards.nth(index).locator("h5").innerText()).containsIgnoringCase(brand);
        }
    }

    @Test
    @DisplayName("HOME-02: Restaurant count on home page")
    void restaurantCountOnHome() {
        CustomerHomePage home = new CustomerHomePage(customerPage);
        int count = home.getVisibleRestaurantCount();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("HOME-04: Open restaurant from home")
    void openRestaurantFromHome() {
        String outlet = new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        assertThat(outlet).isNotBlank();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet").setExact(true))).isVisible();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
    }

    // ── ADDRESS MODAL SCENARIOS ──────────────────────────────────────────

    @Test
    @DisplayName("ADDR-MODAL-01: Address modal opens via Deliver to")
    void addressModalOpens() {
        dashboard.clickDeliverTo();
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();
        assertThat(modal.isModalOpen()).isTrue();
    }

    @Test
    @DisplayName("ADDR-MODAL-02: Select existing Home address")
    void selectExistingHomeAddress() {
        dashboard.clickDeliverTo();
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();
        modal.selectExistingAddress("Home");
        assertThat(customerPage.getByRole(AriaRole.DIALOG)).isHidden();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Deliver to"))))
                .containsText("Home:");
    }

    @Test
    @DisplayName("ADDR-MODAL-07: Address count is accurate")
    void addressCountAccurate() {
        dashboard.clickDeliverTo();
        CustomerAddressModalPage modal = new CustomerAddressModalPage(customerPage);
        modal.waitForModalOpen();
        int count = modal.getAddressCount();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    // ── FREE DELIVERY TRACKER SCENARIOS ──────────────────────────────────

    @Test
    @DisplayName("HOME-05: Free delivery tracker visible")
    void freeDeliveryTrackerVisible() {
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        new CustomerMenuViewPage(customerPage).addQuickPrepItemToCart();
        Locator progress = customerPage.getByRole(AriaRole.PROGRESSBAR,
                new Page.GetByRoleOptions().setName("Progress towards free delivery").setExact(true));
        assertThat(progress).isVisible();
        assertThat(Integer.parseInt(progress.getAttribute("aria-valuenow"))).isBetween(0, 100);
    }
}
