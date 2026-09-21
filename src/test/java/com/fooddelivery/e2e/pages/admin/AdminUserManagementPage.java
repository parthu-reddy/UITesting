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

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isUserListVisible() {
        return page.locator("text=User Management, text=Users, table").first().isVisible();
    }

    // ── Search & Filter ──────────────────────────────────────────────────

    public void searchUser(String query) {
        page.locator("input[placeholder*='Search'], input[type='search']").first().fill(query);
        page.waitForTimeout(500);
    }

    public void filterByRole(String role) {
        page.locator("select").first().selectOption(role);
        page.waitForTimeout(500);
    }

    // ── User list ────────────────────────────────────────────────────────

    public int getUserCount() {
        return page.locator("tr, [data-testid='user-row']").count() - 1; // subtract header
    }

    public void selectUser(int index) {
        page.locator("tr, [data-testid='user-row']").nth(index + 1).click(); // +1 for header
        page.waitForTimeout(500);
    }

    // ── Detail panel ─────────────────────────────────────────────────────

    public boolean isDetailPanelOpen() {
        return page.locator("text=User Details, text=Account Info").first().isVisible();
    }

    public String getUserName() {
        return page.locator("text=Name").locator("xpath=..").locator("span, p").last().innerText().trim();
    }

    public String getUserPhone() {
        return page.locator("text=Phone").locator("xpath=..").locator("span, p").last().innerText().trim();
    }

    public String getUserRole() {
        return page.locator("text=Role, text=Roles").locator("xpath=..").locator("span, p, .badge").last().innerText().trim();
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
