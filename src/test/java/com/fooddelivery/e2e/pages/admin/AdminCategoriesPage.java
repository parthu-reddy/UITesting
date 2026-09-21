package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Categories view.
 * Maps to: {@code AdminCategories.tsx}
 */
public class AdminCategoriesPage {

    private final Page page;

    public AdminCategoriesPage(Page page) {
        this.page = page;
    }

    public boolean isCategoriesVisible() {
        return page.locator("text=Categories, text=Food Categories").first().isVisible();
    }

    public int getCategoryCount() {
        return page.locator("[data-testid='category-card'], .category-card").count();
    }
}
