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

    // Maps to features/reviews/components/AdminReviewsView.tsx ("Review Moderation").

    public boolean isReviewsVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Review Moderation").setExact(true)).isVisible();
    }

    private void choose(String comboName, String option) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(comboName).setExact(true)).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(option).setExact(true)).click();
        page.waitForTimeout(300);
    }

    // ── Mode selection: a "Look up by" Select with Entity / Author ───────

    public void switchToEntityMode() { choose("Look up by", "Entity"); }

    public void switchToUserMode() { choose("Look up by", "Author"); }

    // ── Entity search ────────────────────────────────────────────────────

    /** Takes the enum (RESTAURANT, DRIVER, PRODUCT); the options read Restaurant, Driver, Product. */
    public void selectEntityType(String type) {
        choose("Entity type", type.charAt(0) + type.substring(1).toLowerCase());
    }

    public void fillEntityId(String id) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Entity ID").setExact(true)).fill(id);
    }

    public void fillUserId(String userId) {
        page.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Author user ID").setExact(true)).fill(userId);
    }

    /** Disabled until the lookup has an id to search for. */
    public void search() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search").setExact(true)).click();
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

    // ── Results: one <article> per review, each with a read-only StarRating (role img) ──

    public int getReviewCount() {
        return page.locator("article").count();
    }

    public boolean hasStarRatings() {
        return page.locator("article [role='img']").first().isVisible();
    }

    public boolean isEmptyState() {
        return page.getByText("No reviews found", new Page.GetByTextOptions().setExact(true)).isVisible();
    }
}
