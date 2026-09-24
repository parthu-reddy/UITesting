package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Strict UI coverage for the seeded Brand 1 outlet selector. */
@Tag("feature")
public class OutletSelectionTest extends TestBase {
    private Locator outletDialog;
    private Locator outletChoices;

    @BeforeEach
    void openBrandOneOutletSelector() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        new CustomerDashboardPage(customerPage).waitForDashboard();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Brand 1\\b"))).first().click();
        customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet").setExact(true)).click();
        outletDialog = customerPage.getByRole(AriaRole.DIALOG);
        assertThat(outletDialog.getByText("Select Outlet Location",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        outletChoices = outletDialog.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText("km away"));
        outletChoices.first().waitFor();
    }

    @Test
    @DisplayName("ADDRESS-06/11: Brand outlet selector lists selectable outlets")
    void outletSelectorListsSelectableOutlets() {
        assertThat(outletChoices.count()).isGreaterThan(0);
        for (int index = 0; index < outletChoices.count(); index++) {
            String text = outletChoices.nth(index).innerText().trim();
            assertThat(text).contains("Brand 1 Outlet").contains("km away");
            assertThat(outletChoices.nth(index)).isEnabled();
        }
    }

    @Test
    @DisplayName("ADDRESS-08: Every outlet option exposes a nonnegative distance")
    void everyOutletShowsDistance() {
        for (int index = 0; index < outletChoices.count(); index++) {
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away")
                    .matcher(outletChoices.nth(index).innerText());
            assertThat(distance.find()).as("distance on outlet option %s", index).isTrue();
            assertThat(Double.parseDouble(distance.group(1))).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("ADDRESS-09: Outlets beyond five kilometres are visibly unavailable")
    void farOutletsCannotBeSelected() {
        int farOutlets = 0;
        for (int index = 0; index < outletChoices.count(); index++) {
            Locator outlet = outletChoices.nth(index);
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away")
                    .matcher(outlet.innerText());
            if (!distance.find() || Double.parseDouble(distance.group(1)) <= 5.0) continue;
            farOutlets++;
            boolean labelledUnavailable = Pattern.compile("too far|unavailable|out of range",
                    Pattern.CASE_INSENSITIVE).matcher(outlet.innerText()).find();
            assertThat(outlet.isDisabled() || labelledUnavailable)
                    .as("outlet beyond 5 km must be disabled or clearly unavailable: %s", outlet.innerText())
                    .isTrue();
        }
        assertThat(farOutlets).as("seeded Brand1 far-outlet fixture").isGreaterThan(0);
    }

    @Test
    @DisplayName("ADDRESS-07/12: Select a Brand1 outlet below five kilometres")
    void selectNearbyOutletLoadsItsMenu() {
        OutletChoice choice = nearestEligibleOutlet();
        outletChoices.nth(choice.index()).click();
        assertThat(outletDialog).isHidden();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet").setExact(true))
                .locator("..")).containsText(choice.name());
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
    }

    @Test
    @DisplayName("ADDRESS-15: Selected outlet persists while cart opens and closes")
    void outletPersistsDuringCartNavigation() {
        OutletChoice choice = nearestEligibleOutlet();
        outletChoices.nth(choice.index()).click();
        Locator row = customerPage.locator("[data-menu-item]").filter(new Locator.FilterOptions()
                .setHas(customerPage.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("ADD").setExact(true)))).first();
        assertThat(row).isVisible();
        row.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("ADD").setExact(true)).click();
        customerPage.getByText("View Cart", new Page.GetByTextOptions().setExact(true)).click();
        Locator cart = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        assertThat(cart).isVisible();
        cart.locator("button:has(svg.lucide-x)").click();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Change outlet").setExact(true))
                .locator("..")).containsText(choice.name());
        assertThat(customerPage.locator("[data-menu-item]").first()).isVisible();
    }

    @Test
    @DisplayName("ADDRESS-14: Home address remains selected after reload")
    void homeAddressPersistsAfterReload() {
        customerPage.keyboard().press("Escape");
        assertThat(outletDialog).isHidden();
        customerPage.reload();
        assertThat(customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
    }

    private OutletChoice nearestEligibleOutlet() {
        int selectedIndex = -1;
        double selectedDistance = 5.0;
        String selectedName = null;
        for (int index = 0; index < outletChoices.count(); index++) {
            String text = outletChoices.nth(index).innerText();
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away").matcher(text);
            if (!distance.find()) continue;
            double kilometres = Double.parseDouble(distance.group(1));
            if (kilometres < selectedDistance) {
                selectedIndex = index;
                selectedDistance = kilometres;
                selectedName = outletChoices.nth(index).locator("p").first().innerText().trim();
            }
        }
        assertThat(selectedIndex).as("Brand1 outlet below 5 km from Home").isGreaterThanOrEqualTo(0);
        return new OutletChoice(selectedIndex, selectedName);
    }

    private record OutletChoice(int index, String name) {}
}
