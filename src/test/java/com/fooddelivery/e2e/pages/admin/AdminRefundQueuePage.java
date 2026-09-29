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

    public boolean isQueueEmpty() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Queue Empty").setExact(true)).isVisible();
    }

    public void openRefundTicket(int index) {
        waitForQueue();
        rows().nth(index).click();
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ticket Details")).waitFor();
    }

    /** Opens the final confirmation only; tests must cancel against shared live data. */
    public void openResolutionConfirmation(boolean approved) {
        page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName("Resolution").setExact(true))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(approved ? "Approve" : "Reject").setExact(true))
                .click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(approved ? "Approve Refund" : "Reject Refund").setExact(true)).click();
        confirmationDialog().waitFor();
        confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(approved ? "Approve refund" : "Reject refund").setExact(true))
                .waitFor();
    }

    public Locator confirmationDialog() {
        return page.getByRole(AriaRole.DIALOG);
    }

    public void cancelConfirmation() {
        confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        confirmationDialog().waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }
}
