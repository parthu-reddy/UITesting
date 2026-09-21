package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Customer Reviews list.
 * Maps to: {@code MyReviewsList.tsx}
 * <p>
 * Also covers sub-components: {@code ReviewList.tsx}, {@code ReviewsPanel.tsx},
 * {@code InlineRating.tsx}, {@code DishRatingsPanel.tsx}, {@code DriverRatingCard.tsx},
 * {@code RatingSummary.tsx}, {@code ReviewTargetIcon.tsx}
 * </p>
 */
public class CustomerReviewsPage {

    private final Page page;

    public CustomerReviewsPage(Page page) {
        this.page = page;
    }

    public boolean isReviewsListVisible() {
        return page.locator("text=My Reviews, text=Reviews, text=Your Reviews").first().isVisible();
    }

    public int getReviewCount() {
        return page.locator("[data-testid='review-card'], .review-entry").count();
    }

    public boolean hasRatingStars() {
        return page.locator("svg.lucide-star").first().isVisible();
    }
}
