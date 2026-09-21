package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Rate Order modal.
 * Maps to: {@code RateOrderModal.tsx}
 * <p>
 * Allows star ratings per target (restaurant, rider, dish) and a text comment.
 * </p>
 */
public class RateOrderModalPage {

    private final Page page;

    public RateOrderModalPage(Page page) {
        this.page = page;
    }

    public boolean isOpen() {
        return page.locator("text=Rate, text=Review, text=How was your order").first().isVisible();
    }

    /**
     * Sets the star rating. StarRating uses button stars — click the Nth one.
     */
    public void setRating(int stars) {
        // StarRating renders 5 Star icons; click the one at index (stars - 1)
        Locator starButtons = page.locator("svg.lucide-star, button:has(svg.lucide-star)");
        if (starButtons.count() >= stars) {
            starButtons.nth(stars - 1).click();
            page.waitForTimeout(300);
        }
    }

    public void fillComment(String comment) {
        page.locator("textarea").first().fill(comment);
    }

    public void submit() {
        page.locator("button:has-text('Submit'), button:has-text('Send Review')").first().click();
        page.waitForTimeout(2000);
    }

    public boolean isSubmitSuccess() {
        return page.locator("text=submitted, text=Thank you, svg.lucide-check").first().isVisible();
    }
}
