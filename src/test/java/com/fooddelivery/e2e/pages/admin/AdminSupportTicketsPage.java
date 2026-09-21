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

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isSupportVisible() {
        return page.locator("text=Support Tickets, text=Support, text=Tickets").first().isVisible();
    }

    // ── Tab navigation ───────────────────────────────────────────────────

    public void openTab(String tabName) {
        page.locator("button:has-text('" + tabName + "')").first().click();
        page.waitForTimeout(500);
    }

    public void openOpenTickets() { openTab("OPEN"); }
    public void openInReviewTickets() { openTab("IN_REVIEW"); }
    public void openResolvedTickets() { openTab("RESOLVED"); }
    public void openRejectedTickets() { openTab("REJECTED"); }

    // ── Ticket list ──────────────────────────────────────────────────────

    public int getTicketCount() {
        return page.locator("[data-testid='support-ticket'], .ticket-card, button:has(text='#')").count();
    }

    public void selectTicket(int index) {
        page.locator("[data-testid='support-ticket'], .ticket-card, button:has(text='#')").nth(index).click();
        page.waitForTimeout(500);
    }

    public boolean isTicketDetailOpen() {
        return page.locator("text=Ticket Details, text=Resolution, text=Resolve").first().isVisible();
    }

    // ── Ticket actions ───────────────────────────────────────────────────

    public void fillResolutionNotes(String notes) {
        page.locator("textarea").first().fill(notes);
    }

    public void resolveTicket() {
        page.locator("button:has-text('Resolve'), button:has(svg.lucide-shield-check)").first().click();
        page.waitForTimeout(2000);
    }

    public void rejectTicket() {
        page.locator("button:has-text('Reject'), button:has(svg.lucide-x-circle)").first().click();
        page.waitForTimeout(1000);
        // Confirm the rejection dialog
        page.locator("button:has-text('Reject request'), button:has-text('Confirm')").first().click();
        page.waitForTimeout(2000);
    }

    public void resolveTicketWithNotes(String notes) {
        fillResolutionNotes(notes);
        resolveTicket();
    }

    // ── Chat ─────────────────────────────────────────────────────────────

    public void openChat() {
        page.locator("button:has-text('Chat'), button:has-text('Open Chat')").first().click();
        page.waitForTimeout(500);
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
