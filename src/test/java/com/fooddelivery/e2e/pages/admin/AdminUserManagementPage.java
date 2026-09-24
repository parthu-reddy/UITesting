package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Maps to: {@code AdminUserManagement.tsx, AdminUserDetailPanel.tsx}
 * <p>
 * User management: search, role filter, user list, detail panel,
 * role assignment, active orders for user.
 * </p>
 */
public class AdminUserManagementPage {
    private final Page page;
    public AdminUserManagementPage(Page page) { this.page = page; }

    // Maps to AdminUserManagement.tsx (list) and AdminUserDetailPanel.tsx (detail).

    // ── Visibility ───────────────────────────────────────────────────────

    /** The list panel's search field is always rendered. */
    public boolean isUserListVisible() {
        return page.getByPlaceholder("User ID / Phone").isVisible();
    }

    // ── Search & Filter ──────────────────────────────────────────────────

    /** A form: the field plus a Search submit. */
    public void searchUser(String query) {
        page.getByPlaceholder("User ID / Phone").fill(query);
        page.getByPlaceholder("User ID / Phone").press("Enter");
        page.waitForTimeout(500);
    }

    /** The role filter is the custom Select (options ALL ROLES, ADMIN, CUSTOMER, RESTAURANT, DELIVERY). */
    public void filterByRole(String role) {
        page.locator("div:has(> form input[placeholder='User ID / Phone'])")
                .getByRole(com.microsoft.playwright.options.AriaRole.COMBOBOX).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(role).setExact(true)).click();
        page.waitForTimeout(500);
    }

    // ── User list ────────────────────────────────────────────────────────

    /** One button per user, in the list below the filter bar. It is not a table. */
    private com.microsoft.playwright.Locator users() {
        return page.locator("div:has(> form input[placeholder='User ID / Phone']) + div > button");
    }

    public int getUserCount() {
        return users().count();
    }

    public void selectUser(int index) {
        users().nth(index).click();
        page.waitForTimeout(500);
    }

    // ── Detail panel ─────────────────────────────────────────────────────

    public boolean isDetailPanelOpen() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("User Details").setExact(true)).isVisible();
    }

    /** The panel's fields are ID, Status and Phone -- it shows no name. */
    private String field(String label) {
        return page.getByText(label, new Page.GetByTextOptions().setExact(true)).first()
                .locator("xpath=following-sibling::p[1]").innerText().trim();
    }

    public String getUserId() {
        return field("ID");
    }

    public String getUserPhone() {
        return field("Phone");
    }

    /** The first role chip under the "Roles" heading. */
    public String getUserRole() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Roles").setExact(true))
                .locator("xpath=following-sibling::div[1]/div[1]").innerText().trim();
    }

    // ── Role management ──────────────────────────────────────────────────

    public void fillNewRole(String roleName) {
        page.locator("input[placeholder*='Role'], input[placeholder*='role']").first().fill(roleName);
    }

    public void assignRole() {
        page.locator("button:has-text('Assign'), button:has-text('Add Role')").first().click();
        page.waitForTimeout(1000);
    }

    public void assignRole(String roleName) {
        fillNewRole(roleName);
        assignRole();
    }

    // ── User active orders ───────────────────────────────────────────────

    public int getUserActiveOrderCount() {
        return page.locator("[data-testid='user-active-order'], .user-order-card").count();
    }

    // ── Pagination ───────────────────────────────────────────────────────

    public void nextPage() {
        page.locator("button:has-text('Next')").first().click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        page.locator("button:has-text('Prev')").first().click();
        page.waitForTimeout(500);
    }
}
