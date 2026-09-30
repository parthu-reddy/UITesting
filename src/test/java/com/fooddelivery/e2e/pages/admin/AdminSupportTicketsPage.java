package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Maps to: {@code AdminSupportTickets.tsx}
 * <p>
 * Support ticket management: tabs (OPEN, IN_REVIEW, RESOLVED, REJECTED),
 * ticket selection, resolution/rejection, chat integration.
 * </p>
 */
public class AdminSupportTicketsPage {
    private final Page page;
    public AdminSupportTicketsPage(Page page) { this.page = page; }

    // Maps to AdminSupportTickets.tsx. Tab and button names below are that file's, verbatim.

    private com.microsoft.playwright.Locator button(String exactName) {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(exactName).setExact(true));
    }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isSupportVisible() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Support Tickets").setExact(true)).isVisible();
    }

    public String getStatusHeader() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile(
                        "^(OPEN|IN REVIEW|RESOLVED|REJECTED) Tickets \\(\\d+\\)$")))
                .innerText().trim();
    }

    public boolean isEmptyStateVisible() {
        return page.getByText("No tickets found",
                new Page.GetByTextOptions().setExact(true)).isVisible();
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    /** Tabs read OPEN, IN REVIEW, RESOLVED, REJECTED -- the status with its underscore replaced. */
    public void openTab(String status) {
        button(status.replace('_', ' ')).click();
        page.waitForTimeout(500);
    }

    public void openOpenTickets() { openTab("OPEN"); }
    public void openInReviewTickets() { openTab("IN_REVIEW"); }
    public void openResolvedTickets() { openTab("RESOLVED"); }
    public void openRejectedTickets() { openTab("REJECTED"); }

    // ── Ticket list ──────────────────────────────────────────────────────

    /** Ticket buttons, in the list under the "<STATUS> Tickets (n)" header. */
    private com.microsoft.playwright.Locator tickets() {
        return page.locator("div:has(> h2:has-text('Tickets')) + div > button");
    }

    public int getTicketCount() {
        return tickets().count();
    }

    public boolean hasPagination() {
        return page.getByText(java.util.regex.Pattern.compile("^Page \\d+ of \\d+$")).count() > 0;
    }

    public String getPageInfo() {
        return page.getByText(java.util.regex.Pattern.compile("^Page \\d+ of \\d+$")).innerText().trim();
    }

    public boolean canGoNextPage() {
        com.microsoft.playwright.Locator next = button("Next");
        return next.count() > 0 && next.isEnabled();
    }

    public boolean canGoPreviousPage() {
        com.microsoft.playwright.Locator previous = button("Previous");
        return previous.count() > 0 && previous.isEnabled();
    }

    public void selectTicket(int index) {
        tickets().nth(index).click();
        page.waitForTimeout(500);
    }

    public boolean isTicketDetailOpen() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Ticket Details").setExact(true)).isVisible();
    }

    // ── Ticket actions ───────────────────────────────────────────────────

    public void fillResolutionNotes(String notes) {
        page.getByPlaceholder("Add admin notes (required for rejection)").fill(notes);
    }

    /** The operator note stays editable when a resolver response is rejected. */
    public Locator resolutionNotes() {
        return page.getByPlaceholder("Add admin notes (required for rejection)");
    }

    public Locator resolveTicketButton() {
        return button("Resolve Ticket");
    }

    public Locator rejectRequestButton() {
        return button("Reject Request");
    }

    public Locator statusMessages() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.STATUS);
    }

    /** Opens a confirmation and leaves the final resolution uncommitted. */
    public void openRejectConfirmation() {
        button("Reject Request").click();
        confirmationDialog().waitFor();
    }

    public Locator confirmationDialog() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG);
    }

    public void cancelConfirmation() {
        confirmationDialog().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        confirmationDialog().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
    }

    // ── Pagination (rendered only when there is more than one page) ─────

    public void nextPage() {
        com.microsoft.playwright.Locator next = button("Next");
        if (next.count() == 0 || !next.isEnabled()) return;
        next.click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        com.microsoft.playwright.Locator previous = button("Previous");
        if (previous.count() == 0 || !previous.isEnabled()) return;
        previous.click();
        page.waitForTimeout(500);
    }
}
