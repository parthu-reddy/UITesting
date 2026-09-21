package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object for the "Reorder" strip on customer order history.
 * Maps to: {@code ReorderStrip.tsx}
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
