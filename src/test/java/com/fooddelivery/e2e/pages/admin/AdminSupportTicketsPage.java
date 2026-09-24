package com.fooddelivery.e2e.pages.admin;

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

    /**
     * "Resolve Ticket" approves. Exact name: a has-text('Resolve') match finds the RESOLVED tab
     * first. Approving asks nothing -- see the admin-refund finding in reviews-and-support/PENDING.md.
     */
    public void resolveTicket() {
        button("Resolve Ticket").click();
        page.waitForTimeout(2000);
    }

    /** "Reject Request" (enabled once notes exist), then the danger confirm "Reject request". */
    public void rejectTicket() {
        button("Reject Request").click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG)
                .getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Reject request").setExact(true))
                .click();
        page.waitForTimeout(2000);
    }

    public void resolveTicketWithNotes(String notes) {
        fillResolutionNotes(notes);
        resolveTicket();
    }

    // ── Pagination (rendered only when there is more than one page) ─────

    public void nextPage() {
        button("Next").click();
        page.waitForTimeout(500);
    }

    public void prevPage() {
        button("Previous").click();
        page.waitForTimeout(500);
    }
}
