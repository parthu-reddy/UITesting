package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Page Object for RefundQueue.tsx and RefundTicketPanel.tsx. */
public class AdminRefundQueuePage {
    private final Page page;

    public AdminRefundQueuePage(Page page) { this.page = page; }

    public boolean isRefundQueueVisible() {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Refund Exception Queue").setExact(true)).waitFor();
        return true;
    }

    private void waitForQueue() {
        isRefundQueueVisible();
        page.getByRole(AriaRole.TABLE, new Page.GetByRoleOptions().setName("Refund tickets awaiting a decision"))
                .or(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Queue Empty")))
                .first().waitFor();
    }

    private Locator rows() {
        return page.getByRole(AriaRole.TABLE, new Page.GetByRoleOptions().setName("Refund tickets awaiting a decision"))
                .locator("tbody tr");
    }

    public int getRefundCount() {
        waitForQueue();
        return rows().count();
    }

    public void openRefundTicket(int index) {
        waitForQueue();
        rows().nth(index).click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ticket Details")).waitFor();
    }

    public void approveRefund() { resolveRefund(true); }

    public void rejectRefund() { resolveRefund(false); }

    private void resolveRefund(boolean approved) {
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Resolution").setExact(true))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve" : "Reject").setExact(true))
                .click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(approved ? "Approve Refund" : "Reject Refund").setExact(true)).click();
        page.getByRole(AriaRole.DIALOG)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve refund" : "Reject refund").setExact(true))
                .click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ticket Details"))
                .waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }
}
