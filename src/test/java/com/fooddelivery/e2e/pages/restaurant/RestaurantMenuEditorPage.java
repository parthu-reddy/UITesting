package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for Restaurant Menu Editor.
 * Maps to: {@code BrandMasterMenu.tsx, OutletMenuEditor.tsx, RestaurantMenuTogglesView.tsx}
 * <p>
 * Also covers: {@code MenuItemRow.tsx}, {@code MasterItemForm.tsx}, {@code MenuCategoryGroup.tsx},
 * {@code CategoryTimingPanel.tsx}, {@code CategorySelector.tsx}
 * </p>
 * <p>
 * Full menu lifecycle: add categories, add items (with veg/price/prep time),
 * edit items, toggle availability, category timings.
 * </p>
 */
public class RestaurantMenuEditorPage {

    private final Page page;

    public RestaurantMenuEditorPage(Page page) {
        this.page = page;
    }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isMenuEditorVisible() {
        return page.locator("text=Menu, text=Categories, text=Items").first().isVisible();
    }

    // ── Categories ───────────────────────────────────────────────────────

    public void openAddCategory() {
        page.locator("button:has-text('Add Category'), button:has-text('New Category')").first().click();
        page.waitForTimeout(300);
    }

    public void fillCategoryName(String name) {
        page.locator("input[placeholder*='Category'], input[placeholder*='category']").first().fill(name);
    }

    public void fillCategoryDescription(String desc) {
        page.locator("input[placeholder*='Description'], input[placeholder*='description'], textarea").first().fill(desc);
    }

    public void submitCategory() {
        page.locator("button:has-text('Save'), button:has-text('Create'), button:has-text('Add')").first().click();
        page.waitForTimeout(1000);
    }

    public void addCategory(String name, String description) {
        openAddCategory();
        fillCategoryName(name);
        if (description != null && !description.isEmpty()) {
            fillCategoryDescription(description);
        }
        submitCategory();
    }

    // ── Items ────────────────────────────────────────────────────────────

    public void openAddItem() {
        page.locator("button:has-text('Add Item'), button:has-text('New Item'), button:has(svg.lucide-plus)").first().click();
        page.waitForTimeout(300);
    }

    public void fillItemName(String name) {
        page.locator("label:has-text('Name') ~ input, input[placeholder*='Item name']").first().fill(name);
    }

    public void fillItemPrice(String price) {
        page.locator("label:has-text('Price') ~ input, input[placeholder*='Price']").first().fill(price);
    }

    public void fillItemPrepTime(String minutes) {
        page.locator("label:has-text('Prep') ~ input, input[placeholder*='Prep'], input[placeholder*='min']").first().fill(minutes);
    }

    public void selectItemCategory(String categoryName) {
        page.locator("select").first().selectOption(categoryName);
    }

    public void toggleVeg(boolean isVeg) {
        var toggle = page.locator("label:has-text('Veg') input[type='checkbox'], label:has-text('Veg') button").first();
        if (isVeg != toggle.isChecked()) {
            toggle.click();
        }
    }

    public void submitItem() {
        page.locator("button:has-text('Save'), button:has-text('Create')").first().click();
        page.waitForTimeout(1000);
    }

    public void addMenuItem(String name, String price, String prepTime) {
        openAddItem();
        fillItemName(name);
        fillItemPrice(price);
        fillItemPrepTime(prepTime);
        submitItem();
    }

    // ── Item actions ─────────────────────────────────────────────────────

    public void toggleItemAvailability(String itemName) {
        page.locator("text=" + itemName).locator("xpath=../..").locator("input[type='checkbox'], button:has(svg)").first().click();
        page.waitForTimeout(500);
    }

    public boolean isItemAvailable(String itemName) {
        return page.locator("text=" + itemName).locator("xpath=../..").locator("input[type='checkbox']:checked, .text-green").first().isVisible();
    }

    public void editItem(String itemName) {
        page.locator("text=" + itemName).locator("xpath=../..").locator("button:has-text('Edit'), button:has(svg.lucide-pencil)").first().click();
        page.waitForTimeout(300);
    }

    public void deleteItem(String itemName) {
        page.locator("text=" + itemName).locator("xpath=../..").locator("button:has-text('Delete'), button:has(svg.lucide-trash)").first().click();
        page.waitForTimeout(500);
    }

    // ── Counts ───────────────────────────────────────────────────────────

    public int getItemCount() {
        return page.locator("[data-testid='menu-item'], .menu-item-row").count();
    }

    public int getCategoryCount() {
        return page.locator("[data-testid='category-group'], .category-group").count();
    }
}
