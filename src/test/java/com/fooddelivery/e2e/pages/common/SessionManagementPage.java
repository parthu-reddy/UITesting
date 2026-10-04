package com.fooddelivery.e2e.pages.common;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

/** Visible devices only; no session API, token or browser-storage inspection. */
public class SessionManagementPage {
    private final Page page;
    public SessionManagementPage(Page page) { this.page=page; }
    public boolean isSessionModalOpen() { return section().isVisible(); }
    public int getSessionCount() { return sessionRows().count(); }
    public Locator section() { return page.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName("Logged-in Devices").setExact(true)); }
    public Locator sessionRows() { return section().getByTestId("active-session"); }
    public void waitForSessions() { page.waitForCondition(() -> getSessionCount()>0||section().getByText("No active sessions found.",new Locator.GetByTextOptions().setExact(true)).isVisible()); }
    public void refresh() { section().getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Refresh devices").setExact(true)).click(); waitForSessions(); }
    public void terminateSession(int index) {
        sessionRows().nth(index).getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Remove").setExact(true)).click();
        page.getByRole(AriaRole.DIALOG).getByRole(AriaRole.BUTTON,new Locator.GetByRoleOptions().setName("Remove device").setExact(true)).click();
    }
    public void terminateAll() { throw new UnsupportedOperationException("The UI offers explicit device removal only"); }
}
