package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object for the ChatWidget and RefundRequestModal.
 * Maps to: {@code ChatWidget.tsx, ChatInput.tsx, ChatMessageList.tsx, RefundRequestModal.tsx}
 */
public class ChatWidgetPage {

    private final Page page;

    public ChatWidgetPage(Page page) {
        this.page = page;
    }

    // ── Chat ─────────────────────────────────────────────────────────────

    /** The widget is open exactly when its composer is on screen (ChatWidget.tsx). */
    public boolean isChatOpen() {
        return page.getByPlaceholder("Type a message...").isVisible();
    }

    public void sendMessage(String message) {
        Locator input = page.locator("input[placeholder*='message'], textarea[placeholder*='message']").first();
        input.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        input.fill(message);
        page.locator("button:has(svg.lucide-send), button[type='submit']").first().click();
        page.waitForTimeout(500);
    }

    public boolean hasMessage(String text) {
        return page.locator("text=" + text).isVisible();
    }

    public int getMessageCount() {
        return page.locator("[data-testid='chat-message'], .chat-message").count();
    }

    // ── Refund Request ───────────────────────────────────────────────────

    /** Opens the refund quote modal through the delivered order's actual support CTA. */
    public void openRefundRequest() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Something wrong with this order?").setExact(true))
                .click();
        refundModal().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void openRefundRequest(String orderId) {
        Locator orderTracker = page.locator(
                "[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        orderTracker.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Something wrong with this order?").setExact(true))
                .click();
        refundModal().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillRefundReason(String reason) {
        refundReasonInput().fill(reason);
    }

    public void submitRefundRequest() {
        requestQuoteButton().click();
    }

    public Locator refundModal() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Request refund quote").setExact(true));
    }

    public Locator refundItemCheckboxes() {
        return refundModal().locator("input[type='checkbox']");
    }

    public Locator refundReasonInput() {
        return refundModal().getByPlaceholder("Please explain why you are requesting a refund...");
    }

    public Locator requestQuoteButton() {
        return refundModal().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Request Quote").setExact(true));
    }
}
