package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class CheckoutUiTest extends TestBase {

    private String openCartWithOneItem() {
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        DeliveryOnlineTogglePage riderDuty = new DeliveryOnlineTogglePage(riderPage);
        riderDuty.goOnline();
        assertThat(riderDuty.isOnline()).as("Seeded rider is available before checkout").isTrue();

        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new CustomerDashboardPage(customerPage).waitForDashboard();
        CustomerAddressModalPage addressModal = new CustomerAddressModalPage(customerPage);
        if (addressModal.isModalOpen()) addressModal.selectExistingAddress("Home");
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        return cart.getFirstItemName();
    }

    private String openCheckoutWithOneItem() {
        String itemName = openCartWithOneItem();
        new CustomerCartDrawerPage(customerPage).clickPlaceOrder();
        return itemName;
    }

    @Test
    @DisplayName("CHECKOUT-01/03-06/14-15: Checkout summary and closing payment preserve the cart")
    void verifyCheckoutAndPaymentOptions() {
        String cartItemName = openCheckoutWithOneItem();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);

        PaymentModalPage payment = new PaymentModalPage(customerPage);
        assertThat(payment.getDeliveryAddress())
                .isNotBlank()
                .doesNotContain("No address selected");
        assertThat(payment.hasItem(cartItemName)).as("Payment summary keeps the cart item").isTrue();
        assertThat(payment.hasQuantity(1)).as("Payment summary shows the cart quantity").isTrue();
        assertThat(payment.hasTotalLine("Item total")).isTrue();
        assertThat(payment.hasTotalLine("Delivery fee")).isTrue();
        assertThat(payment.isPayEnabled()).as("Place order enables once the server quote returns").isTrue();
        assertThat(payment.hasTotalLine("GST & restaurant charges")).isTrue();
        assertThat(payment.hasTotalLine("Total")).isTrue();

        assertThat(payment.hasPaymentMethod("Credit or debit card")).isTrue();
        assertThat(payment.hasPaymentMethod("UPI")).isTrue();
        assertThat(payment.hasPaymentMethod("Wallet")).isTrue();
        payment.selectPaymentMethod("UPI");
        assertThat(payment.isPayEnabled()).as("Selecting an available payment method keeps Pay enabled").isTrue();

        payment.close();
        assertThat(payment.isOpen()).isFalse();
        assertThat(cart.isCartOpen()).as("Closing payment returns to the existing cart").isTrue();
        assertThat(cart.getFirstItemName()).isEqualTo(cartItemName);
    }

    @Test
    @DisplayName("CHECKOUT-07-10: Payment methods remain usable after closing and reopening checkout")
    void paymentChoicesAndCheckoutReentry() {
        String cartItemName = openCheckoutWithOneItem();
        PaymentModalPage payment = new PaymentModalPage(customerPage);

        // Radio names (CustomerCheckout.tsx): "La Bouffe Wallet", "UPI", "Credit or debit card".
        // Only the wallet can be disabled -- when its balance cannot cover the bill.
        assertThat(payment.isPaymentMethodEnabled("Credit or debit card")).isTrue();
        assertThat(payment.isPaymentMethodEnabled("UPI")).isTrue();
        payment.selectPaymentMethod("UPI");
        assertThat(payment.isPayEnabled()).isTrue();
        payment.selectPaymentMethod("Credit or debit card");
        assertThat(payment.isPayEnabled()).isTrue();
        if (payment.isPaymentMethodEnabled("Wallet")) {
            payment.selectPaymentMethod("Wallet");
            assertThat(payment.isPayEnabled()).isTrue();
        }

        payment.close();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        assertThat(cart.isCartOpen()).isTrue();
        assertThat(cart.getFirstItemName()).isEqualTo(cartItemName);
        cart.clickPlaceOrder();

        assertThat(payment.isOpen()).isTrue();
        assertThat(payment.hasItem(cartItemName)).isTrue();
        assertThat(payment.hasPaymentMethod("Credit or debit card")).isTrue();
        assertThat(payment.hasPaymentMethod("UPI")).isTrue();
        assertThat(payment.hasPaymentMethod("Wallet")).isTrue();
        assertThat(payment.isPayEnabled()).isTrue();
        payment.close();
    }

    @Test
    @DisplayName("CHECKOUT-16: Reload during checkout preserves address and cart without submitting payment")
    void reloadDuringCheckoutPreservesCart() {
        String cartItemName = openCheckoutWithOneItem();
        PaymentModalPage payment = new PaymentModalPage(customerPage);
        assertThat(payment.getDeliveryAddress()).isNotBlank().doesNotContain("No address selected");

        customerPage.reload();
        new CustomerDashboardPage(customerPage).waitForDashboard();
        assertThat(payment.isOpen()).as("A reload must not resume an in-progress payment modal").isFalse();

        customerPage.getByText("View Cart", new com.microsoft.playwright.Page.GetByTextOptions()
                .setExact(true)).click();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        assertThat(cart.getFirstItemName()).isEqualTo(cartItemName);
        assertThat(cart.isCheckoutEnabled()).isTrue();
    }

    @Test
    @DisplayName("ACCESS-03: Enter activates checkout from the focused cart action")
    void enterKeyActivatesCheckout() {
        String cartItemName = openCartWithOneItem();
        com.microsoft.playwright.Locator checkout = customerPage.getByRole(
                com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions()
                        .setName(java.util.regex.Pattern.compile("Checkout|Place Order"))).first();
        checkout.focus();
        customerPage.keyboard().press("Enter");

        PaymentModalPage payment = new PaymentModalPage(customerPage);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                customerPage.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                        new com.microsoft.playwright.Page.GetByRoleOptions()
                                .setName("Checkout").setExact(true))).isVisible();
        assertThat(payment.hasItem(cartItemName)).isTrue();
        payment.close();
    }
}
