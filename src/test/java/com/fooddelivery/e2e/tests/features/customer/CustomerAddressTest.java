package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Existing-address and unsaved-draft coverage; never creates shared address data. */
@Tag("ui-only")
@Tag("customer-address")
public class CustomerAddressTest extends TestBase {

    @BeforeEach
    void loginWithHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
    }

    @Test
    @DisplayName("ADDRESS-01-03: Existing Home selection updates Deliver-to header")
    void selectExistingHomeAddress() {
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Select Delivery Location").setExact(true));
        assertThat(dialog).isVisible();
        assertThat(dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b")))).isVisible();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b"))).click();

        assertThat(dialog).isHidden();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
    }

    @Test
    @DisplayName("ADDR-MODAL-03/04/08: Unsaved address draft is discarded")
    void unsavedAddressDraftDoesNotCreateSharedData() {
        customerPage.getByTitle("Profile Settings",
                new Page.GetByTitleOptions().setExact(true)).click();
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Addresses").setExact(true)).click();
        int homeCountBefore = customerPage.getByText("Home",
                new Page.GetByTextOptions().setExact(true)).count();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add / Manage Addresses").setExact(true)).click();
        Locator panel = customerPage.getByText("Delivery Location",
                new Page.GetByTextOptions().setExact(true)).locator("xpath=../../..");
        Locator label = panel.getByPlaceholder("Label (e.g. Home, Work)");
        Locator line = panel.getByPlaceholder("Address Line 1");
        label.fill("Unsaved E2E Draft");
        line.fill("123 Unsaved Test Street");
        assertThat(label).hasValue("Unsaved E2E Draft");
        assertThat(line).hasValue("123 Unsaved Test Street");
        assertThat(panel.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Save Address").setExact(true))).isDisabled();

        panel.locator("button:has(svg.lucide-x)").click();
        assertThat(customerPage.getByText("Unsaved E2E Draft",
                new Page.GetByTextOptions().setExact(true))).hasCount(0);
        assertThat(customerPage.getByText("Home",
                new Page.GetByTextOptions().setExact(true))).hasCount(homeCountBefore);
    }
}
