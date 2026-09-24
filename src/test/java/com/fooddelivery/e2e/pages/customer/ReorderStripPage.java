package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the "Order it again" strip on the CUSTOMER HOME feed.
 *
 * Maps to: {@code features/customer-orders/components/ReorderStrip.tsx}, rendered by
 * {@code CustomerRestaurantBrowser} ABOVE the restaurant list -- not on order history, which is
 * what this comment used to say.
 *
 * History, 2026-09-24: when this page object was written no "Reorder" control existed anywhere
 * in the application, and nothing referenced this class. The locator below could not have
 * matched. The redesign has since built the component, so it resolves now -- each suggestion is
 * a real button reading {@code Reorder - <total>}.
 *
 * The strip renders NOTHING when the customer has no completed orders, so any test using this
 * needs a customer with delivery history. Treat an absent strip as a missing prerequisite, not
 * as a failure.
 */
public class ReorderStripPage {

    private final Page page;

    public ReorderStripPage(Page page) {
        this.page = page;
    }

    public boolean isReorderVisible() {
        return page.locator("button:has-text('Reorder'), text=Reorder").first().isVisible();
    }

    public void clickReorder() {
        page.locator("button:has-text('Reorder')").first().click();
        page.waitForTimeout(2000);
    }
}
