package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;

/**
 * Page Object for post-delivery support modal.
 * Maps to: {@code PostDeliverySupportModal.tsx}
 * <p>
 * Opened after an order is delivered — allows customer to report issues.
 * </p>
 */
public class PostDeliverySupportModalPage {

    private final Page page;

    public PostDeliverySupportModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Submit Support Request, text=Report Issue, text=What went wrong").first().isVisible();
    }

    public void selectReason(String reason) {
        page.locator("button:has-text('" + reason + "')").first().click();
        page.waitForTimeout(300);
    }

    public void fillDescription(String text) {
        page.locator("textarea").first().fill(text);
    }

    public void submit() {
        page.locator("button:has-text('Submit'), button:has-text('Send')").first().click();
        page.waitForTimeout(2000);
    }

    public boolean isSubmitSuccess() {
        return page.locator("text=submitted, text=received, text=Thank you, svg.lucide-check").first().isVisible();
    }
}
