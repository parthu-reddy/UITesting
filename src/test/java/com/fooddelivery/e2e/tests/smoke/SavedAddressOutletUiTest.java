package com.fooddelivery.e2e.tests.smoke;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
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
    @Test
    void existingHomeAndNearbyBrand1Outlet() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", TestConfig.CUSTOMER_PHONE);
        new com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        String outletName = new com.fooddelivery.e2e.pages.customer.NearbyOutletPage(customerPage).openBrand1AndSelectNearby();

        customerPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator selected = customerPage.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(outletName));
        assertThat(selected.locator("svg.lucide-check")).isVisible();
    }
}
