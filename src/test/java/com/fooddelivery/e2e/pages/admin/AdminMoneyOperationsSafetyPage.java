package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * UI-only page object for the destructive controls in the Admin Money Operations screen.
 *
 * <p>The associated browser-routed tests use one fixture item per tab. That makes every role
 * locator below unique without tying the test to generated CSS classes or row positions.</p>
 */
public final class AdminMoneyOperationsSafetyPage {

    private final Page page;

    public AdminMoneyOperationsSafetyPage(Page page) {
        this.page = page;
    }

    public void waitForOperations() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Money Operations").setExact(true))
                .waitFor(visible());
    }

    public void waitForRejectedMovement(String eventId) {
        page.locator("span.font-mono").filter(new Locator.FilterOptions().setHasText(eventId)).waitFor(visible());
    }

    public void waitForNoUnresolvedRejections() {
        page.getByText("No unresolved rejections.", new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public void openResolution() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Resolve").setExact(true))
                .click();
        resolutionNote().waitFor(visible());
    }

    public void fillResolutionNote(String note) {
        resolutionNote().fill(note);
    }

    public boolean isResolutionConfirmationDisabled() {
        return resolutionForm().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Confirm").setExact(true)).isDisabled();
    }

    public boolean isResolutionConfirmationEnabled() {
        return resolutionForm().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Confirm").setExact(true)).isEnabled();
    }

    public void openResolutionConfirmation() {
        resolutionForm().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Confirm").setExact(true)).click();
        resolutionDialog().waitFor(visible());
    }

    public Locator resolutionDialog() {
        return dialog("Resolve rejected ledger movement?");
    }

    public void cancelResolutionConfirmation() {
        Locator dialog = resolutionDialog();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(hidden());
    }

    public void confirmResolution() {
        resolutionDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Resolve movement").setExact(true)).click();
    }

    public String resolutionNoteValue() {
        return resolutionNote().inputValue();
    }

    public void openPaymentDlq() {
        page.getByRole(AriaRole.TAB,
                        new Page.GetByRoleOptions().setName("Payment DLQ").setExact(true))
                .click();
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Failed Payment Webhooks").setExact(true))
                .waitFor(visible());
    }

    public void waitForPaymentWebhook(String eventId) {
        page.getByText(eventId, new Page.GetByTextOptions().setExact(true)).waitFor(visible());
    }

    public void openPaymentWebhookRetryConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Retry Event").setExact(true))
                .click();
        paymentRetryDialog().waitFor(visible());
    }

    public Locator paymentRetryDialog() {
        return dialog("Retry payment webhook?");
    }

    public void confirmPaymentWebhookRetry() {
        paymentRetryDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Retry webhook").setExact(true)).click();
    }

    public void waitForNoFailedPaymentWebhooks() {
        page.getByText("No failed webhooks found.", new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public boolean isPaymentWebhookRetryEnabled() {
        return retryEventButton().isEnabled();
    }

    public void openWalletDlq() {
        page.getByRole(AriaRole.TAB,
                        new Page.GetByRoleOptions().setName("Wallet DLQ").setExact(true))
                .click();
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Wallet Outbox DLQ").setExact(true))
                .waitFor(visible());
    }

    public void waitForWalletOutboxAggregate(String aggregateId) {
        page.getByText("Aggregate ID: " + aggregateId,
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public void openWalletOutboxRetryConfirmation() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Retry Event").setExact(true))
                .click();
        walletRetryDialog().waitFor(visible());
    }

    public Locator walletRetryDialog() {
        return dialog("Retry wallet outbox event?");
    }

    public void confirmWalletOutboxRetry() {
        walletRetryDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Retry outbox event").setExact(true)).click();
    }

    public void waitForNoWalletOutboxEvents() {
        page.getByText("No wallet outbox events in DLQ.", new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public boolean isWalletOutboxRetryEnabled() {
        return retryEventButton().isEnabled();
    }

    public void waitForToast(String message) {
        page.getByRole(AriaRole.STATUS)
                .getByText(message, new Locator.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    private Locator resolutionNote() {
        return page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions()
                        .setName("Why this no longer needs booking")
                        .setExact(true));
    }

    private Locator resolutionForm() {
        return resolutionNote().locator("xpath=..");
    }

    private Locator dialog(String name) {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    private Locator retryEventButton() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Retry Event").setExact(true));
    }

    private Locator.WaitForOptions visible() {
        return new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    }

    private Locator.WaitForOptions hidden() {
        return new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN);
    }
}
