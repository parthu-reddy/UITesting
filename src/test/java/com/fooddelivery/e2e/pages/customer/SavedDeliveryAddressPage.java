package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Existing address selection only; never opens address creation or GPS lookup. */
public class SavedDeliveryAddressPage {
    private final Page page;
    public SavedDeliveryAddressPage(Page page) { this.page = page; }
    public void selectHomeFromOpenDialog() {
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        assertThat(dialog.getByText("Select Delivery Location", new Locator.GetByTextOptions().setExact(true))).isVisible();
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b"))).click();
        assertThat(dialog).isHidden();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to"))))
                .containsText("Home:");
    }
}
