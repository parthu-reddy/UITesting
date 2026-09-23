package com.fooddelivery.e2e.tests.resilience;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerCartDrawerPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerMenuViewPage;
import com.fooddelivery.e2e.pages.customer.NearbyOutletPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** UI-only recovery checks. No order is submitted and no backend state is changed. */
@Tag("resilience")
public class PageReloadRecoveryTest extends TestBase {

    private void loginCustomerAtHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
    }

    private Locator deliverToButton() {
        return customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")));
    }

    @Test
    @DisplayName("RECOVERY-01: Customer session survives a hard reload")
    void sessionPersistsAcrossReload() {
        loginCustomerAtHome();
        assertThat(deliverToButton()).containsText("Home:");

        customerPage.reload();

        assertThat(deliverToButton()).containsText("Home:");
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Order Food").setExact(true))).hasCount(0);
    }

    @Test
    @DisplayName("RECOVERY-03: Selected Home address survives a hard reload")
    void selectedAddressPersistsAcrossReload() {
        loginCustomerAtHome();
        String selectedAddress = deliverToButton().locator("span").last().innerText().trim();

        customerPage.reload();

        assertThat(deliverToButton().locator("span").last()).hasText(selectedAddress);
        assertThat(customerPage.getByRole(AriaRole.DIALOG)).hasCount(0);
    }

    @Test
    @DisplayName("RECOVERY-02: Cart item survives a hard reload")
    void cartPersistsAcrossReload() {
        loginCustomerAtHome();
        new NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        CustomerMenuViewPage menu = new CustomerMenuViewPage(customerPage);
        menu.addQuickPrepItemToCart();
        menu.clickViewCart();
        CustomerCartDrawerPage cart = new CustomerCartDrawerPage(customerPage);
        cart.waitForCartOpen();
        String itemName = cart.getFirstItemName();
        assertThat(customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart"))).containsText(itemName);

        customerPage.reload();
        menu.clickViewCart();
        cart.waitForCartOpen();

        assertThat(customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart"))).containsText(itemName);
    }

    @Test
    @DisplayName("RECOVERY-01: Customer settings route survives a hard reload")
    void settingsRoutePersistsAcrossReload() {
        loginCustomerAtHome();
        new CustomerDashboardPage(customerPage).openSettingsTab();
        Locator heading = customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Account Settings"));
        assertThat(heading).isVisible();
        String settingsUrl = customerPage.url();

        customerPage.reload();

        assertThat(heading).isVisible();
        org.assertj.core.api.Assertions.assertThat(customerPage.url()).isEqualTo(settingsUrl);
        assertThat(customerPage.locator("input[type=tel]")).hasValue(testCustomerPhone);
    }

    @Test
    @DisplayName("RECOVERY-16: Second customer tab shares session and Home selection")
    void secondTabSharesCustomerSession() {
        loginCustomerAtHome();
        String selectedAddress = deliverToButton().locator("span").last().innerText().trim();

        Page secondTab = customerContext.newPage();
        try {
            secondTab.navigate(TestConfig.APP_URL);
            Locator secondDeliverTo = secondTab.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")));
            assertThat(secondDeliverTo.locator("span").last()).hasText(selectedAddress);
            assertThat(secondTab.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Order Food").setExact(true))).hasCount(0);
        } finally {
            secondTab.close();
        }
    }
}
