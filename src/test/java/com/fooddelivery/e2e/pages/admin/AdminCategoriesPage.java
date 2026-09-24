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

    /** AdminCategories.tsx: the list is headed "Existing Categories". */
    public boolean isCategoriesVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Existing Categories").setExact(true)).isVisible();
    }

    /** One card per category under that heading; the empty state is a <p>, not a card. */
    public int getCategoryCount() {
        return page.locator("h3:text-is('Existing Categories') + div > div").count();
    }
}
