package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Page;

/**
 * Page Object for the delivery order details modal.
 * Maps to: {@code DeliveryOrderDetailsModal.tsx}
 */
public class DeliveryOrderDetailsModalPage {

    private final Page page;

    public DeliveryOrderDetailsModalPage(Page page) {
        this.page = page;
    }

    /** A Modal titled "Order #1a2b3c4d" (DeliveryOrderDetailsModal.tsx); the title is the dialog's name. */
    private com.microsoft.playwright.Locator dialog() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Order #[0-9a-f]{8}$")));
    }

    public boolean isOpen() {
        return dialog().isVisible();
    }

    public String getOrderId() {
        return dialog().getAttribute("aria-label");
    }

    /** The modal is headerless; Escape closes every Overlay. */
    public void close() {
        page.keyboard().press("Escape");
        dialog().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }
}
