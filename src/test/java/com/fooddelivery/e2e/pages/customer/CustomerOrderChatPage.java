package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object for the customer order chat.
 * Maps to: {@code CustomerOrderChat.tsx}
 */
public class CustomerOrderChatPage {

    private final Page page;

    public CustomerOrderChatPage(Page page) {
        this.page = page;
    }

    /** The order chat is ChatWidget: open exactly when its composer is on screen. */
    public boolean isChatOpen() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Close chat").setExact(true)).isVisible();
    }

    /** Opens the customer order chat from its order-specific floating button. */
    public void openChat(String orderId) {
        openChatLauncher(orderId);
        page.getByPlaceholder("Type a message...").waitFor();
    }

    /** Clicks the launcher without assuming session initialization succeeds. */
    public void openChatLauncher(String orderId) {
        String floatingLabel = "#" + orderId.substring(0, Math.min(6, orderId.length()));
        Locator launcher = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(floatingLabel).setExact(true));
        assertThat(launcher).hasCount(1);
        launcher.click();
    }

    /** Retries session initialization from the error state shown by ChatWidget. */
    public void retrySession() {
        Locator retry = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Try again").setExact(true));
        retry.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        retry.click();
    }

    public Locator composer() {
        return page.getByPlaceholder("Type a message...");
    }

    public Locator sendButton() {
        return page.locator("form:has(textarea[placeholder='Type a message...']) button[type='submit']");
    }

    public void closeChat() {
        Locator accessibleClose = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Close chat").setExact(true));
        if (accessibleClose.count() > 0) {
            accessibleClose.click();
        } else {
            page.locator("button:has(svg.lucide-x)").first().click();
        }
    }

    public void sendMessage(String text) {
        Locator composer = composer();
        assertThat(composer).isEnabled();
        composer.fill(text);
        sendButton().click();
        assertThat(page.getByText(text, new Page.GetByTextOptions().setExact(true))).isVisible();
    }

    public int getMessageCount() {
        return page.locator(".chat-message, [data-testid='chat-bubble']").count();
    }
}
