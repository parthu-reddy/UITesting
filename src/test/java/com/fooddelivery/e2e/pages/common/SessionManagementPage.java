package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Session Management Modal.
 * Maps to: {@code SessionManagementModal.tsx, ActiveSessions.tsx}
 * <p>
 * Shows active device sessions and allows terminating other sessions.
 * </p>
 */
public class SessionManagementPage {

    private final Page page;

    public SessionManagementPage(Page page) {
        this.page = page;
    }

    public boolean isSessionModalOpen() {
        return page.locator("text=Active Sessions, text=Session Management, text=Devices").first().isVisible();
    }

    public int getSessionCount() {
        return page.locator("[data-testid='session-row'], .session-entry").count();
    }

    public void terminateSession(int index) {
        page.locator("button:has-text('Terminate'), button:has-text('End Session')").nth(index).click();
        page.waitForTimeout(1000);
    }

    public void terminateAll() {
        page.locator("button:has-text('Terminate All'), button:has-text('End All Sessions')").first().click();
        page.waitForTimeout(1000);
    }
}
