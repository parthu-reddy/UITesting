package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Page;

/**
 * Page Object for Restaurant Chat.
 * Maps to: {@code RestaurantChatList.tsx, RestaurantOrderChat.tsx}
 */
public class RestaurantChatPage {

    private final Page page;

    public RestaurantChatPage(Page page) {
        this.page = page;
    }

    public boolean isChatListVisible() {
        return page.locator("text=Chats, text=Messages, text=Conversations").first().isVisible();
    }

    public int getChatCount() {
        return page.locator("[data-testid='chat-item'], .chat-list-item").count();
    }

    public void openChat(int index) {
        page.locator("[data-testid='chat-item'], .chat-list-item").nth(index).click();
        page.waitForTimeout(500);
    }

    public void sendMessage(String message) {
        page.locator("input[placeholder*='message'], textarea").first().fill(message);
        page.locator("button:has(svg.lucide-send), button[type='submit']").first().click();
        page.waitForTimeout(500);
    }
}
