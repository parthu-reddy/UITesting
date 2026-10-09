package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Stable, accessible controls for the deterministic user, category, and review admin fixtures.
 *
 * <p>This object intentionally names the visible controls instead of depending on component
 * classes. It is used only by browser-routed safety tests, where each backing API response is
 * supplied in-browser and every write is blocked before it could reach a shared environment.</p>
 */
public final class AdminUserCatalogModerationSafetyPage {
    private final Page page;

    public AdminUserCatalogModerationSafetyPage(Page page) {
        this.page = page;
    }

    public void waitForUserManagement() {
        page.getByPlaceholder("User ID / Phone").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    public void selectUserByPhone(String phone) {
        page.getByText(phone, new Page.GetByTextOptions().setExact(true))
                .locator("xpath=ancestor::button")
                .click();
    }

    public void chooseNewRole(String role) {
        page.getByRole(AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("New role").setExact(true))
                .click();
        page.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName(role).setExact(true))
                .click();
    }

    public void openAddRoleConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Add").setExact(true))
                .click();
    }

    public void activateUser() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Activate User").setExact(true))
                .click();
    }

    public void openSuspendConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Suspend User").setExact(true))
                .click();
    }

    public void openRemoveRoleConfirmation(String role) {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Remove " + role + " role").setExact(true))
                .click();
    }

    public Locator confirmationDialog(String title) {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(title).setExact(true));
    }

    public void cancelConfirmation(String title) {
        Locator dialog = confirmationDialog(title);
        dialog.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Cancel").setExact(true))
                .click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public void confirmConfirmation(String title, String confirmLabel) {
        Locator dialog = confirmationDialog(title);
        dialog.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName(confirmLabel).setExact(true))
                .click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public boolean hasRemoveRoleButton(String role) {
        Locator remove = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Remove " + role + " role").setExact(true));
        return remove.count() > 0 && remove.isVisible();
    }

    public void waitForRemoveRoleButton(String role) {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Remove " + role + " role").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    /** The selected user's status in the detail panel's "Status" card; list badges also read "Suspended". */
    public void waitForUserStatus(String status) {
        page.getByText("Status", new Page.GetByTextOptions().setExact(true)).locator("..")
                .getByText(status, new Locator.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isUserActionEnabled(String label) {
        Locator action = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(label).setExact(true));
        return action.count() == 1 && action.isEnabled();
    }

    public void waitForToast(String message) {
        page.getByText(message, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForActiveOrderRestaurant(String restaurantName) {
        page.getByText(restaurantName, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isActiveOrderRestaurantVisible(String restaurantName) {
        Locator restaurant = page.getByText(restaurantName,
                new Page.GetByTextOptions().setExact(true));
        return restaurant.count() > 0 && restaurant.isVisible();
    }

    public void waitForCategories() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Existing Categories").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillCategoryName(String name) {
        page.getByPlaceholder("e.g. Italian, Vegan, Burgers").fill(name);
    }

    public void fillCategoryDescription(String description) {
        page.getByPlaceholder("Brief description of the category...").fill(description);
    }

    public void submitCategory() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Create Category").setExact(true))
                .click();
    }

    public void submitCategoryUpdate() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Update Category").setExact(true))
                .click();
    }

    public void openCategoryEdit(String categoryName) {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Edit " + categoryName).setExact(true))
                .click();
    }

    public void cancelCategoryEdit() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Cancel category edit").setExact(true))
                .click();
    }

    public void waitForCategory(String categoryName) {
        page.getByText(categoryName, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForCategoryEditor(String title) {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(title).setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public String categoryNameValue() {
        return page.getByPlaceholder("e.g. Italian, Vegan, Burgers").inputValue();
    }

    public String categoryDescriptionValue() {
        return page.getByPlaceholder("Brief description of the category...").inputValue();
    }

    public boolean isCategoryVisible(String categoryName) {
        Locator category = page.getByText(categoryName, new Page.GetByTextOptions().setExact(true));
        return category.count() > 0 && category.isVisible();
    }

    public boolean isToastVisible(String message) {
        Locator toast = page.getByText(message, new Page.GetByTextOptions().setExact(true));
        return toast.count() > 0 && toast.isVisible();
    }

    public void waitForReviewModeration() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Review Moderation").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillEntityId(String entityId) {
        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Entity ID").setExact(true))
                .fill(entityId);
    }

    public void switchToAuthorLookup() {
        page.getByRole(AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Look up by").setExact(true))
                .click();
        page.getByRole(AriaRole.OPTION,
                        new Page.GetByRoleOptions().setName("Author").setExact(true))
                .click();
    }

    public void fillAuthorUserId(String userId) {
        page.getByRole(AriaRole.TEXTBOX,
                        new Page.GetByRoleOptions().setName("Author user ID").setExact(true))
                .fill(userId);
    }

    public void searchReviews() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Search").setExact(true))
                .click();
    }

    public boolean isReviewSearchEnabled() {
        Locator search = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search").setExact(true));
        return search.count() == 1 && search.isEnabled();
    }

    public void waitForReviewComment(String comment) {
        page.getByText(comment, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isReviewCommentVisible(String comment) {
        Locator review = page.getByText(comment, new Page.GetByTextOptions().setExact(true));
        return review.count() > 0 && review.isVisible();
    }

    public void waitForReviewError(String message) {
        page.getByText(message, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public int reviewArticleCount() {
        return page.locator("article").count();
    }

    public Locator reviewArticleForComment(String comment) {
        return page.getByText(comment, new Page.GetByTextOptions().setExact(true))
                .locator("xpath=ancestor::article");
    }
}
