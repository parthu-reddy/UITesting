package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;

/**
 * Page Object for the customer order chat.
 * Maps to: {@code CustomerOrderChat.tsx}
 */
public class CustomerOrderChatPage {

    private final Page page;

    public CustomerOrderChatPage(Page page) {
        this.page = page;
    }

    public boolean isChatOpen() {
        return page.locator("text=Chat, text=Send a message, textarea, input[placeholder*='message']").first().isVisible();
    }

    public void sendMessage(String text) {
        page.locator("textarea, input[placeholder*='message']").first().fill(text);
        page.locator("button:has-text('Send'), button:has(svg.lucide-send)").first().click();
        page.waitForTimeout(1000);
    }

    public int getMessageCount() {
        return page.locator(".chat-message, [data-testid='chat-bubble']").count();
    }
}
