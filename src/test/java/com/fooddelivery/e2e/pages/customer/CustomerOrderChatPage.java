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
        Locator launcher = launcher(orderId);
        assertThat(launcher).hasCount(1);
        launcher.click();
    }

    public Locator launcher(String orderId) {
        Locator launcher = page.locator("[data-testid='chat-launcher'][data-order-id='" + orderId + "']");
        if (launcher.count() == 0) {
            String floatingLabel = "#" + orderId.substring(0, Math.min(6, orderId.length()));
            launcher = page.locator("button:has-text('" + floatingLabel + "')");
        }
        return launcher;
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

    public Locator composerForm() {
        return page.getByTestId("chat-composer");
    }

    /** The actionable error state shown when the chat session could not be initialized. */
    public Locator sessionInitializationAlert() {
        return page.getByRole(AriaRole.ALERT);
    }

    /** The transport state shown after a session exists but its WebSocket is unavailable. */
    public Locator reconnectingStatus() {
        return page.getByTestId("chat-reconnecting-status");
    }

    public Locator sendButton() {
        return page.locator("form:has(textarea[placeholder='Type a message...']) button[type='submit']");
    }

    public Locator unreadCount(String orderId) {
        Locator unread = page.getByTestId("chat-unread-count");
        return unread.count() > 0 ? unread : launcher(orderId).locator("span.absolute");
    }

    public Locator typingIndicator() {
        return page.getByTestId("chat-typing-indicator");
    }

    public Locator messages() {
        return page.getByTestId("chat-message");
    }

    public Locator messageById(String messageId) {
        return page.locator("[data-testid='chat-message'][data-message-id='" + messageId + "']");
    }

    public Locator galleryFileInput() {
        return page.getByTestId("chat-gallery-file-input");
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
        return messages().count();
    }
}
