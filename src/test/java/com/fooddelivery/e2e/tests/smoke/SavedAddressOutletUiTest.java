package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Read/select existing Home and a nearby Brand1 outlet, without adding a cart or placing an order. */
@Tag("catalog-ui")
public class SavedAddressOutletUiTest extends TestBase {

    private SavedDeliveryAddressPage selectInitialHome() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        SavedDeliveryAddressPage address = new SavedDeliveryAddressPage(customerPage);
        address.selectHomeFromOpenDialog();
        return address;
    }

    private Locator openAddressDialog() {
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        assertThat(dialog.getByText("Select Delivery Location",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        return dialog;
    }

    @Test
    void existingHomeAndNearbyBrand1Outlet() {
        selectInitialHome();
        String outletName = new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator selected = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(outletName));
        assertThat(selected.locator("svg.lucide-check")).isVisible();
    }

    @Test
    void homeAddressModalCanBeReopenedAndDismissedTwice() {
        selectInitialHome();

        for (int attempt = 0; attempt < 2; attempt++) {
            Locator dialog = openAddressDialog();
            assertThat(dialog.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b")))).isVisible();
            customerPage.keyboard().press("Escape");
            assertThat(dialog).isHidden();
            assertThat(customerPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).containsText("Home:");
        }
    }

    @Test
    void homeAddressPersistsAcrossReload() {
        selectInitialHome();
        customerPage.reload();

        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")))).containsText("Home:");
    }

    @Test
    void visibleBrand1OutletDistancesAreNumeric() {
        selectInitialHome();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Brand 1\\b"))).first().click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();

        Locator choices = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        assertThat(choices.first()).isVisible();
        for (int index = 0; index < choices.count(); index++) {
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away")
                    .matcher(choices.nth(index).innerText());
            org.assertj.core.api.Assertions.assertThat(distance.find())
                    .as("outlet %s exposes a numeric distance", index).isTrue();
            org.assertj.core.api.Assertions.assertThat(Double.parseDouble(distance.group(1))).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void switchBetweenTwoNearbyBrand1Outlets() {
        selectInitialHome();
        String firstOutlet = new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage)
                .openBrand1AndSelectNearby();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        Locator choices = dialog.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        int alternateIndex = -1;
        String alternateName = null;
        for (int index = 0; index < choices.count(); index++) {
            String text = choices.nth(index).innerText();
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away").matcher(text);
            String name = choices.nth(index).locator("p").first().innerText().trim();
            if (!name.equals(firstOutlet) && distance.find() && Double.parseDouble(distance.group(1)) < 5.0) {
                alternateIndex = index;
                alternateName = name;
                break;
            }
        }
        org.assertj.core.api.Assertions.assertThat(alternateIndex)
                .as("Brand1 must expose a second outlet below 5 km for outlet switching")
                .isGreaterThanOrEqualTo(0);
        choices.nth(alternateIndex).click();
        assertThat(dialog).isHidden();
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();

        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator selected = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(alternateName));
        assertThat(selected.locator("svg.lucide-check")).isVisible();
    }

    @Test
    void outletSelectorArrowDownMovesFocus() {
        selectInitialHome();
        new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage).openBrand1AndSelectNearby();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG);
        Locator choices = dialog.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        assertThat(choices.nth(1)).isVisible();
        choices.first().focus();
        customerPage.keyboard().press("ArrowDown");
        org.assertj.core.api.Assertions.assertThat((Boolean) choices.nth(1)
                .evaluate("element => document.activeElement === element"))
                .as("ArrowDown should move focus to the next outlet option")
                .isTrue();
    }
}
