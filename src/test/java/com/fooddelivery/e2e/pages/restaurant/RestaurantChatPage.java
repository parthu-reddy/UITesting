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
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Chats").setExact(true)).isVisible();
    }

    /**
     * One button per active order, in the list under the "Active Chats" header
     * (RestaurantChatList.tsx). Not by accessible name: its spans join with no spaces
     * ("Order #1a2b3c4dCustomerChat"), which Chromium confirmed on 2026-09-24.
     */
    private com.microsoft.playwright.Locator chatItems() {
        return page.locator("div:has(> h3:text-is('Active Chats')) + div > button");
    }

    public int getChatCount() {
        return chatItems().count();
    }

    public void openChat(int index) {
        chatItems().nth(index).click();
        page.waitForTimeout(500);
    }

    public void sendMessage(String message) {
        page.locator("input[placeholder*='message'], textarea").first().fill(message);
        page.locator("button:has(svg.lucide-send), button[type='submit']").first().click();
        page.waitForTimeout(500);
    }
}
