package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminReviewsView.tsx}
 * <p>
 * Admin reviews moderation: search by entity (restaurant/driver) or user,
 * entity type filter, review list, star ratings display.
 * Read-only by design — no delete/hide actions exist.
 * </p>
 */
public class AdminReviewsPage {
    private final Page page;
    public AdminReviewsPage(Page page) { this.page = page; }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isReviewsVisible() {
        return page.locator("text=Reviews, text=Ratings, text=Moderation").first().isVisible();
    }

    // ── Mode selection ───────────────────────────────────────────────────

    public void switchToEntityMode() {
        page.locator("button:has-text('By Entity'), button:has-text('entity')").first().click();
        page.waitForTimeout(300);
    }

    public void switchToUserMode() {
        page.locator("button:has-text('By User'), button:has-text('user')").first().click();
        page.waitForTimeout(300);
    }

    // ── Entity search ────────────────────────────────────────────────────

    public void selectEntityType(String type) {
        page.locator("select").first().selectOption(type);
    }

    public void fillEntityId(String id) {
        page.locator("input[placeholder*='Entity'], input[placeholder*='ID'], input[placeholder*='Restaurant']").first().fill(id);
    }

    public void fillUserId(String userId) {
        page.locator("input[placeholder*='User'], input[placeholder*='user']").first().fill(userId);
    }

    public void search() {
        page.locator("button:has-text('Search'), button:has(svg.lucide-search)").first().click();
        page.waitForTimeout(2000);
    }

    public void searchByEntity(String entityType, String entityId) {
        switchToEntityMode();
        selectEntityType(entityType);
        fillEntityId(entityId);
        search();
    }

    public void searchByUser(String userId) {
        switchToUserMode();
        fillUserId(userId);
        search();
    }

    // ── Results ──────────────────────────────────────────────────────────

    public int getReviewCount() {
        return page.locator("[data-testid='review-card'], .review-card").count();
    }

    public boolean hasStarRatings() {
        return page.locator("svg.lucide-star, [data-testid='star-rating']").first().isVisible();
    }

    public boolean isEmptyState() {
        return page.locator("text=No reviews, text=no reviews").first().isVisible();
    }
}
