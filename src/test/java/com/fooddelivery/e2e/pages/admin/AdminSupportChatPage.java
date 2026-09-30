package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.nio.file.Path;

/**
 * Support-ticket chat controls used by the browser-routed admin isolation check.
 *
 * <p>Maps to {@code AdminSupportTickets.tsx} and its embedded {@code ChatWidget}. This is kept
 * separate from {@link AdminSupportTicketsPage} because the test needs to identify the chat
 * instance by its full order ID rather than exercise the general support queue.</p>
 */
public final class AdminSupportChatPage {
    private final Page page;

    public AdminSupportChatPage(Page page) {
        this.page = page;
    }

    public void waitForTickets() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Support Tickets").setExact(true))
                .waitFor(visible());
    }

    public void selectTicketForOrder(String orderId) {
        Locator ticket = page.locator("button:has-text('Order #" + shortOrderId(orderId) + "')").first();
        ticket.waitFor(visible());
        ticket.click();
    }

    public void waitForTicketDetails(String orderId) {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Ticket Details").setExact(true))
                .waitFor(visible());
        page.getByText("Order ID: " + orderId,
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public Locator chatLauncher(String orderId) {
        return page.locator("[data-testid='chat-launcher'][data-order-id='" + orderId + "']");
    }

    /** Opens the selected ticket's widget only when it is still at its launcher state. */
    public void openChatIfLauncherVisible(String orderId) {
        Locator launcher = chatLauncher(orderId);
        if (launcher.isVisible()) {
            launcher.click();
        }
    }

    public Locator chatComposer() {
        return page.getByTestId("chat-composer");
    }

    public Locator chatComposerInput() {
        return page.getByPlaceholder("Type a message...");
    }

    /** The hidden gallery input remains the stable contract behind the visible upload button. */
    public Locator galleryFileInput() {
        return page.getByTestId("chat-gallery-file-input");
    }

    /** Uploads a local fixture through the real rendered chat input. */
    public void uploadGalleryImage(Path image) {
        galleryFileInput().setInputFiles(image);
    }

    /** An IMAGE message renders the URL provided by the authoritative chat broadcast. */
    public Locator attachment(String sourceUrl) {
        return page.locator("[data-testid='chat-message'][data-message-type='IMAGE'] "
                + "img[alt='Attachment'][src='" + sourceUrl + "']");
    }

    public Locator imageUploadError() {
        return page.getByText("Could not upload that image. Check it is under 5MB and try again.",
                new Page.GetByTextOptions().setExact(true));
    }

    /** Sends one ordinary text message through the rendered ticket-chat composer. */
    public void sendChatMessage(String message) {
        Locator input = chatComposerInput();
        input.waitFor(visible());
        input.fill(message);
        input.press("Enter");
    }

    public Locator historyMessage(String message) {
        return page.locator("[data-testid='chat-message']:has-text('" + message + "')");
    }

    public void waitForHistoryMessage(String message) {
        historyMessage(message).waitFor(visible());
    }

    private static Locator.WaitForOptions visible() {
        return new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    }

    private static String shortOrderId(String orderId) {
        return orderId.substring(0, Math.min(8, orderId.length()));
    }
}
