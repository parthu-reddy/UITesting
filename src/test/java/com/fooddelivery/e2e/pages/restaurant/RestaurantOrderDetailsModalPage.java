package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;

/**
 * Page Object for the restaurant order details modal.
 * Maps to: {@code RestaurantOrderDetailsModal.tsx}
 */
public class RestaurantOrderDetailsModalPage {

    private final Page page;

    public RestaurantOrderDetailsModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Order #[0-9a-f]{8}$")))
                .isVisible();
    }

    public String getOrderId() {
        return page.getByRole(AriaRole.DIALOG,
                        new Page.GetByRoleOptions().setName(Pattern.compile("^Order #[0-9a-f]{8}$")))
                .getAttribute("aria-label");
    }

    public String getCustomerName() {
        return page.locator("text=Customer").locator("xpath=..").locator("span, p").last().innerText().trim();
    }

    public void close() {
        Locator dialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Order #[0-9a-f]{8}$")));
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close Details").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
