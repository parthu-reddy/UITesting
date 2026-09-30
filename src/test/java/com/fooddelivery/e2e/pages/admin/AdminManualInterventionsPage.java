package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/** Page object for the dispatch intervention queue and its read-only details. */
public class AdminManualInterventionsPage {

    private final Page page;

    public AdminManualInterventionsPage(Page page) {
        this.page = page;
    }

    private Locator interventionCards() {
        return page.locator("div.flex-1.overflow-y-auto.p-3.space-y-2 > button");
    }

    public void waitForQueue() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Interventions").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isInterventionsVisible() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Interventions").setExact(true)).isVisible();
    }

    public int getInterventionCount() {
        return interventionCards().count();
    }

    public boolean isEmptyStateVisible() {
        return page.getByText("No orders require dispatch intervention.",
                new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    public void selectIntervention(int index) {
        interventionCards().nth(index).click();
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(Pattern.compile("^Order #.{1,12} requires intervention$")).setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isDetailVisible() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Order #.{1,12} requires intervention$")).setExact(true))
                .isVisible();
    }

    public boolean areDriversUnavailable() {
        return page.getByText("No online drivers available nearby.",
                new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    public boolean hasDriverCandidateError() {
        Locator alert = page.locator("p[role='alert']").first();
        return alert.count() > 0 && alert.isVisible();
    }

    /** Waits until the selected order has either scoped candidates or a visible fetch result. */
    public void waitForDriverCandidates() {
        page.locator("button:has-text('Force Assign'), p:has-text('No online drivers available nearby.'), p[role='alert']")
                .first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
    }

    public int getForceAssignButtonCount() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Force Assign").setExact(true)).count();
    }

    public void fillAssignmentReason(String reason) {
        page.getByPlaceholder("Reason for this manual assignment...").fill(reason);
    }

    public boolean isForceAssignEnabled() {
        Locator forceAssign = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Force Assign").setExact(true)).first();
        return forceAssign.isEnabled();
    }

    /** Opens the force-assignment confirmation. */
    public void openForceAssignConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Force Assign").setExact(true)).first().click();
    }

    public Locator confirmationDialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Force-assign this driver?").setExact(true));
    }

    public void cancelConfirmation() {
        confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        confirmationDialog().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public void confirmForceAssignment() {
        confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Force assign").setExact(true)).click();
    }

    public void fillCancellationReason(String reason) {
        page.getByPlaceholder("Reason for cancellation...").fill(reason);
    }

    public boolean isCancellationRequestEnabled() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Request Cancellation & Refund Review").setExact(true))
                .isEnabled();
    }

    /** Opens the cancellation confirmation. */
    public void openCancellationConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Request Cancellation & Refund Review").setExact(true)).click();
    }

    public Locator cancellationConfirmationDialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Request cancellation for order #.+\\?$")));
    }

    public void cancelCancellationConfirmation() {
        Locator dialog = cancellationConfirmationDialog();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public void confirmCancellation() {
        cancellationConfirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Request cancellation").setExact(true)).click();
    }

    public String cancellationReasonValue() {
        return page.getByPlaceholder("Reason for cancellation...").inputValue();
    }

    public void waitForToast(String message) {
        page.getByText(message, new Page.GetByTextOptions().setExact(true)).waitFor(
                new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isToastVisible(String message) {
        Locator toast = page.getByText(message, new Page.GetByTextOptions().setExact(true));
        return toast.count() > 0 && toast.isVisible();
    }

    public void waitForEmptyQueue() {
        page.getByText("No orders require dispatch intervention.",
                new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public String getPageInfo() {
        return page.getByText(Pattern.compile("^Page \\d+ of \\d+$")).first().innerText().trim();
    }

    public boolean canGoNextPage() {
        Locator next = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Next").setExact(true));
        return next.count() > 0 && next.isEnabled();
    }

    public boolean canGoPreviousPage() {
        Locator previous = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Prev").setExact(true));
        return previous.count() > 0 && previous.isEnabled();
    }
}
