package com.fooddelivery.e2e.pages.customer;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
/** Opens Brand1 and explicitly selects the nearest displayed outlet below 5 km. */
public class NearbyOutletPage {
    private final Page page;
    public NearbyOutletPage(Page page) { this.page = page; }
    public String openBrand1AndSelectNearby() {
        return openBrandAndSelectNearby("Brand 1");
    }

    public String openBrandAndSelectNearby(String brandName) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile(brandName + "\\b"))).first().click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator outlets = page.getByRole(AriaRole.DIALOG);
        assertThat(outlets.getByText("Select Outlet Location", new Locator.GetByTextOptions().setExact(true))).isVisible();
        Locator choices = outlets.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText(brandName + " Outlet 1"));
        assertThat(choices.first()).isVisible();
        String outletName = brandName + " Outlet 1";
        choices.first().click();
        assertThat(outlets).isHidden();
        return outletName;
    }
}
