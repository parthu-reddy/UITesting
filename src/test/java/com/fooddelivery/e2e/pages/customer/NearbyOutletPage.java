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
        return openBrandCardAndSelectNearby(brandName);
    }

    /** Opens a brand card from the post-reload restaurant feed, then selects its nearest outlet. */
    public String openBrandCardAndSelectNearby(String brandName) {
        openBrandCard(brandName);
        return selectNearbyOutlet(brandName);
    }

    /**
     * Opens the feed card whose title is exactly {@code brandName}. Never match by a name regex:
     * the "Order it again" strip's button is named "Order Brand 1 Item 1-3 from Brand 1 Outlet 5
     * again", sits above the feed, and a first() regex match clicks Reorder instead (P0-2 run 2).
     */
    public void openBrandCard(String brandName) {
        // The feed initially renders six cards; an unrendered brand cannot be clicked.
        // Search filters the actual nearby list before that visible-count limit.
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions()
                .setName("Search restaurants or cuisines").setExact(true)).fill(brandName);
        Locator brand = page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions()
                .setHas(page.getByText(brandName, new Page.GetByTextOptions().setExact(true))));
        assertThat(brand).hasCount(1, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions()
                .setTimeout(30000));
        brand.click();
    }

    private String selectNearbyOutlet(String brandName) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change outlet")).click();
        Locator outlets = page.getByRole(AriaRole.DIALOG);
        assertThat(outlets.getByText("Select Outlet Location", new Locator.GetByTextOptions().setExact(true))).isVisible();
        Locator choices = outlets.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("km away"));
        choices.first().waitFor();
        int chosen = -1;
        double nearest = 5.0;
        for (int i = 0; i < choices.count(); i++) {
            String label = choices.nth(i).innerText();
            Matcher distance = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*km away").matcher(label);
            if (label.contains(brandName + " Outlet") && distance.find()) {
                double km = Double.parseDouble(distance.group(1));
                if (km < nearest) { nearest = km; chosen = i; }
            }
        }
        if (chosen < 0) throw new AssertionError("No " + brandName + " outlet is below 5 km from Home");
        String outletName = choices.nth(chosen).locator("p").first().innerText().trim();
        System.out.println("Selected " + outletName + " at " + nearest + " km");
        choices.nth(chosen).click();
        assertThat(outlets).isHidden();
        return outletName;
    }
}
