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
        new LoginPage(customerPage).loginAs("Order Food",testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.getByTitle("Profile Settings",new Page.GetByTitleOptions().setExact(true)).click();
        assertThat(customerPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Account Settings"))).isVisible();
    }
    @Test void profilePhoneIsReadOnlyAndCloseReturnsHome() {
        Locator phone=customerPage.locator("input[type=tel]");
        assertThat(phone).hasValue(testCustomerPhone);
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
        assertThat(customerPage.locator("input[type=tel]")).hasValue(testCustomerPhone);
    }
    @Test void profileIdentityAndStoreCreditBalanceRender() {
        Locator name = customerPage.locator("input[type=text]").first();
        assertThat(name).not().hasValue("");
        Locator email = customerPage.locator("input[type=email]").first();
        assertThat(email).not().hasValue("");

        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Store Credit").setExact(true)).click();
        Locator balance = customerPage.getByText("Available Balance",
                new Page.GetByTextOptions().setExact(true)).locator("..").locator("h2");
        assertThat(balance).hasText(java.util.regex.Pattern.compile("^₹[0-9,]+(?:\\.[0-9]{2})?$"));
        String amount = balance.innerText().trim().substring(1).replace(",", "");
        org.assertj.core.api.Assertions.assertThat(Double.parseDouble(amount)).isGreaterThanOrEqualTo(0);
    }
    @Test void currentLoggedInDeviceIsListedWithoutRemovingIt() {
        Locator sessions = customerPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Logged-in Devices")).locator("..").locator("..");
        assertThat(sessions).isVisible();
        assertThat(sessions.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true)).first()).isVisible();
        assertThat(sessions).containsText("Last Active:");
    }
    @Test void savedHomeAddressIsVisibleWithoutEditingIt() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        assertThat(customerPage.getByText("Home", new Page.GetByTextOptions().setExact(true)).first()).isVisible();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true))).isVisible();
    }
    @Test void blankNewAddressFormIsBlockedAndCanBeClosed() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator panel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        assertThat(panel).isVisible();
        assertThat(panel.getByPlaceholder("Label (e.g. Home, Work)")).isVisible();
        assertThat(panel.getByPlaceholder("Address Line 1")).isVisible();
        assertThat(panel.getByPlaceholder("City")).isVisible();
        assertThat(panel.getByPlaceholder("State")).isVisible();
        assertThat(panel.getByPlaceholder("ZIP Code")).isVisible();
        Locator save = panel.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Save Address").setExact(true));
        assertThat(save).isDisabled();
        panel.getByPlaceholder("Label (e.g. Home, Work)").fill("Unsaved E2E address");
        panel.getByPlaceholder("Address Line 1").fill("123 Unsaved Test Street");
        assertThat(panel.getByPlaceholder("Label (e.g. Home, Work)")).hasValue("Unsaved E2E address");
        assertThat(panel.getByPlaceholder("Address Line 1")).hasValue("123 Unsaved Test Street");
        assertThat(save).isDisabled();
        panel.getByPlaceholder("City").fill("Bengaluru");
        panel.getByPlaceholder("State").fill("Karnataka");
        panel.getByPlaceholder("ZIP Code").fill("560001");
        assertThat(save).isEnabled();
        panel.locator("button:has(svg.lucide-x)").click();
        assertThat(customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        assertThat(customerPage.getByText("Unsaved E2E address",
                new Page.GetByTextOptions().setExact(true))).isHidden();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator reopenedPanel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        assertThat(reopenedPanel.getByPlaceholder("Label (e.g. Home, Work)")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("Address Line 1")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("City")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("State")).hasValue("");
        assertThat(reopenedPanel.getByPlaceholder("ZIP Code")).hasValue("");
        reopenedPanel.locator("button:has(svg.lucide-x)").click();
    }
    @Test void myReviewsTabShowsReviewsOrDefinedEmptyState() {
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("My Reviews").setExact(true)).click();
        Locator outcome = customerPage.locator("article").first().or(
                customerPage.getByText("You haven't reviewed anything yet",
                        new Page.GetByTextOptions().setExact(true)));
        assertThat(outcome).isVisible();
    }
}
