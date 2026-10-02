package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/** Page object for the inline Logged-in Devices section in ActiveSessions.tsx. */
public class SessionManagementPage {

    private final Page page;

    public SessionManagementPage(Page page) {
        this.page = page;
    }

    public boolean isSessionModalOpen() {
        return section().isVisible();
    }

    public int getSessionCount() {
        return sessionRows().count();
    }

    public void terminateSession(int index) {
        sessionRows().nth(index).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true)).click();
    }

    public void terminateAll() {
        throw new UnsupportedOperationException("The current UI has no remove-all-sessions action");
    }

    public Locator section() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Logged-in Devices").setExact(true))
                .locator("xpath=../..");
    }

    public Locator sessionRows() {
        return section().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Remove").setExact(true))
                .locator("xpath=..");
    }

    public void waitForSessions() {
        // A hidden loading indicator alone is insufficient: it is hidden before the effect starts.
        // Wait for a row or explicit empty state, then fail if the expected browser row is absent.
        page.waitForCondition(() -> getSessionCount() > 0
                || section().getByText("No active sessions found.",
                        new Locator.GetByTextOptions().setExact(true)).isVisible());
    }
}
