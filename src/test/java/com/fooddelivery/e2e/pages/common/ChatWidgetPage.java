package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

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

    public void openRefundRequest() {
        page.locator("button:has-text('Request Refund'), button:has-text('Refund')").first().click();
        page.waitForTimeout(500);
    }

    public void fillRefundReason(String reason) {
        page.locator("textarea, input[placeholder*='reason']").first().fill(reason);
    }

    public void submitRefundRequest() {
        page.locator("button:has-text('Submit'), button:has-text('Request')").first().click();
        page.waitForTimeout(1000);
    }
}
