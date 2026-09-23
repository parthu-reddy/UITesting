package com.fooddelivery.e2e.tests.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Browser-network recovery without submitting an order or changing shared backend data. */
@Tag("resilience")
public class NetworkRecoveryUiTest extends TestBase {

    @Test
    @DisplayName("RECOVERY-09/11: Offline period does not crash UI or lose cart")
    void cartAndPageRecoverAfterOfflinePeriod() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        String itemName = cart.getFirstItemName();

        try {
            customerContext.setOffline(true);
            assertThat(customerPage.getByRole(AriaRole.DIALOG,
                    new Page.GetByRoleOptions().setName("Your cart"))).containsText(itemName);
            assertThat(customerPage.locator("body")).isVisible();
        } finally {
            customerContext.setOffline(false);
        }

        customerPage.reload();
        menu.clickViewCart();
        cart.waitForCartOpen();
        assertThat(customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart"))).containsText(itemName);
    }
}
