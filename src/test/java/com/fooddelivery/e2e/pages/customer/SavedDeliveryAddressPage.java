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

    /**
     * Chooses Home explicitly in the "Select Delivery Location" dialog.
     *
     * <p>The dashboard opens that prompt only on a mount with no saved location
     * (CustomerDashboard: {@code useState(() => !localStorage.getItem('deliveryLat'))}). A fresh
     * customer login lands on /customer first (activePortal.lastPortal), and the address list there
     * may save Home before LoginPage.openPortal re-enters /customer, so the second mount can start
     * with the prompt closed (P0-2, 2026-10-07: 30 of 225 executions). When the prompt is not open,
     * open it the way a person does, through the "Deliver to" header button.
     */
    public void selectHomeFromOpenDialog() {
        Locator dialog = openSelector();
        Locator deliverTo = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")));
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(Pattern.compile("^Home\\b"))).click();
        assertThat(dialog).isHidden();
        assertThat(deliverTo).containsText("Home:");
    }

    /** The "Select Delivery Location" dialog, opened through the header if the entry prompt is not up. */
    public Locator openSelector() {
        Locator dialog = page.getByRole(AriaRole.DIALOG);
        Locator prompt = dialog.getByText("Select Delivery Location", new Locator.GetByTextOptions().setExact(true));
        Locator deliverTo = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Deliver to")));
        // Settled = the prompt is up, or this mount already shows a chosen address ("Home: ..."); only
        // then is "no prompt" final, so the header click cannot race an opening modal.
        page.waitForCondition(() -> prompt.isVisible()
                || (deliverTo.isVisible() && deliverTo.innerText().matches("(?s).*\\w+: .*")));
        if (!prompt.isVisible()) deliverTo.click();
        assertThat(prompt).isVisible();
        return dialog;
    }
}
