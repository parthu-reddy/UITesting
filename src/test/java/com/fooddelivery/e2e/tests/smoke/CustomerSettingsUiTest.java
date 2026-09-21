package com.fooddelivery.e2e.tests.smoke;
import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("customer-settings-ui")
public class CustomerSettingsUiTest extends TestBase {
    @BeforeEach void openSettings() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food",TestConfig.CUSTOMER_PHONE);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.getByTitle("Profile Settings",new Page.GetByTitleOptions().setExact(true)).click();
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
    }
    @Test void profilePhoneIsReadOnlyAndCloseReturnsHome() {
        Locator phone=customerPage.locator("input[type=tel]");
        assertThat(phone).hasValue(TestConfig.CUSTOMER_PHONE);
        assertThat(phone).isDisabled();
        customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Close settings")).click();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("Deliver to")))).containsText("Home:");
    }
    @Test void keyboardTabsReachHistoryAndWallet() {
        Locator profile=customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Profile").setExact(true));
        profile.focus(); profile.press("ArrowRight");
        assertThat(customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("History").setExact(true)))
                .hasAttribute("aria-selected","true");
        customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Store Credit").setExact(true)).click();
        assertThat(customerPage.getByText("Available Balance",new Page.GetByTextOptions().setExact(true))).isVisible();
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Transaction History"))).isVisible();
        customerPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Profile").setExact(true)).click();
        assertThat(customerPage.locator("input[type=tel]")).hasValue(TestConfig.CUSTOMER_PHONE);
    }
}
