package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Narrow page object for the refund-queue policy checks.
 *
 * <p>This deliberately covers only the controls whose state protects a money-moving action.
 * It keeps the browser-routed fixture tests independent from the shared queue page object,
 * which is also used for read-only checks against deployed support data.</p>
 */
public final class AdminRefundPolicyPage {
    private final Page page;

    public AdminRefundPolicyPage(Page page) {
        this.page = page;
    }

    public void waitForQueue() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Refund Exception Queue").setExact(true))
                .waitFor();
        table().waitFor();
    }

    public void selectTicketWithReason(String reason) {
        table().locator("tbody tr")
                .filter(new Locator.FilterOptions().setHasText(reason))
                .click();
        detailsHeading().waitFor();
    }

    public Locator approvalChoice() {
        return resolutionGroup().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Approve").setExact(true));
    }

    public Locator rejectionChoice() {
        return resolutionGroup().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Reject").setExact(true));
    }

    public Locator approveRefundButton() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Approve Refund").setExact(true));
    }

    public Locator rejectRefundButton() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Reject Refund").setExact(true));
    }

    public Locator quoteUnavailableAlert() {
        return page.getByRole(AriaRole.ALERT);
    }

    public Locator overrideAmount() {
        return page.getByLabel("Override amount", new Page.GetByLabelOptions().setExact(true));
    }

    public Locator resolutionNotes() {
        return page.getByLabel("Resolution notes", new Page.GetByLabelOptions().setExact(true));
    }

    public Locator confirmationDialog() {
        return page.getByRole(AriaRole.DIALOG);
    }

    public void cancelConfirmation() {
        confirmationDialog().getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Cancel").setExact(true))
                .click();
        confirmationDialog().waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    public Locator statusMessages() {
        return page.getByRole(AriaRole.STATUS);
    }

    public Locator detailsHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Ticket Details").setExact(true));
    }

    public void closeDetails() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Close ticket details").setExact(true))
                .click();
        detailsHeading().waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    public Locator queueEmptyHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Queue Empty").setExact(true));
    }

    private Locator resolutionGroup() {
        return page.getByRole(AriaRole.GROUP,
                new Page.GetByRoleOptions().setName("Resolution").setExact(true));
    }

    private Locator table() {
        return page.getByRole(AriaRole.TABLE,
                new Page.GetByRoleOptions().setName("Refund tickets awaiting a decision").setExact(true));
    }
}
