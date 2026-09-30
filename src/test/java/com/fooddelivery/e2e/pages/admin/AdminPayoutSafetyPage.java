package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page object for the fixture-backed admin payout lifecycle.
 *
 * <p>Each action is used only after the browser route has been installed by the associated test.
 * That keeps the rendered lifecycle coverage isolated from shared financial data while still
 * exercising the application's real controls and generated API client.</p>
 */
public final class AdminPayoutSafetyPage {

    private final Page page;

    public AdminPayoutSafetyPage(Page page) {
        this.page = page;
    }

    public void waitForPayouts() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Payouts").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void openHistory() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
    }

    public void waitForPendingPayee(String payeeDisplayName) {
        page.getByText(payeeDisplayName, new Page.GetByTextOptions().setExact(true))
                .first()
                .waitFor(visible());
    }

    public void openPendingPayee(String payeeDisplayName) {
        page.getByText(payeeDisplayName, new Page.GetByTextOptions().setExact(true))
                .first()
                .click();
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Create Payout for Balance").setExact(true))
                .waitFor(visible());
    }

    public void openCreatePayoutDialog() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Create Payout for Balance").setExact(true))
                .click();
        createPayoutDialog().waitFor(visible());
    }

    public void confirmCreateDraftPayout() {
        createPayoutDialog().getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Create Draft Payout").setExact(true))
                .click();
    }

    public void waitForCreatePayoutPayee(String payeeDisplayName) {
        createPayoutDialog().getByText(payeeDisplayName,
                        new Locator.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public void waitForUnverifiedBankOverride() {
        unverifiedBankOverride().waitFor(visible());
    }

    public boolean isUnverifiedBankOverrideChecked() {
        return unverifiedBankOverride().isChecked();
    }

    public void enableUnverifiedBankOverride() {
        unverifiedBankOverride().check();
    }

    public boolean isCreateDraftPayoutVisible() {
        Locator createButton = createPayoutDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Create Draft Payout").setExact(true));
        return createButton.count() > 0 && createButton.isVisible();
    }

    public void searchHistory(String payeeId) {
        page.getByPlaceholder("Enter UUID...").fill(payeeId);
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Search").setExact(true)).click();
    }

    public void openFirstHistoryPayout() {
        Locator firstRow = page.locator("table tbody tr").first();
        firstRow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        firstRow.click();
    }

    public void waitForDetails() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Payout Details").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void waitForPayoutStatus(String status) {
        page.getByText(status, new Page.GetByTextOptions().setExact(true))
                .first()
                .waitFor(visible());
    }

    public boolean isPayoutActionVisible(String action) {
        Locator button = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(action).setExact(true));
        return button.count() > 0 && button.isVisible();
    }

    public boolean isPayoutActionDisabled(String action) {
        Locator button = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(action).setExact(true));
        return button.count() == 1 && button.isDisabled();
    }

    public void waitForBankReference(String bankReference) {
        page.getByText(bankReference, new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public void waitForFailureReason(String reason) {
        page.getByText(reason, new Page.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    public void approveDraftPayout() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Approve").setExact(true))
                .click();
    }

    public void cancelDraftPayout() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Cancel").setExact(true))
                .click();
    }

    public void openMarkPaidDialog() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Mark Paid").setExact(true)).click();
        markPaidDialog().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillBankReference(String value) {
        markPaidDialog().getByPlaceholder("e.g. UTR-123456789").fill(value);
    }

    public boolean isConfirmPaymentDisabled() {
        return markPaidDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Confirm Payment").setExact(true)).isDisabled();
    }

    public boolean isConfirmPaymentEnabled() {
        return markPaidDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Confirm Payment").setExact(true)).isEnabled();
    }

    public void confirmMarkPaid() {
        markPaidDialog().getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Confirm Payment").setExact(true))
                .click();
    }

    public void cancelMarkPaidDialog() {
        Locator dialog = markPaidDialog();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public void openFailPayoutDialog() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Fail Payout").setExact(true)).click();
        failPayoutDialog().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void fillFailureReason(String value) {
        failPayoutDialog().getByPlaceholder("e.g. Invalid bank account").fill(value);
    }

    public boolean isMarkFailedDisabled() {
        return failPayoutDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Mark Failed").setExact(true)).isDisabled();
    }

    public void confirmFailPayout() {
        failPayoutDialog().getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Mark Failed").setExact(true))
                .click();
    }

    public boolean isFailPayoutDialogVisible() {
        return failPayoutDialog().isVisible();
    }

    public void waitForFailPayoutDialog() {
        failPayoutDialog().waitFor(visible());
    }

    public String failureReasonValue() {
        return failPayoutDialog().getByPlaceholder("e.g. Invalid bank account").inputValue();
    }

    public void waitForToast(String message) {
        page.getByRole(AriaRole.STATUS)
                .getByText(message, new Locator.GetByTextOptions().setExact(true))
                .waitFor(visible());
    }

    private Locator createPayoutDialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Create Payout").setExact(true));
    }

    private Locator unverifiedBankOverride() {
        return createPayoutDialog().getByLabel("Create payout despite unverified bank details",
                new Locator.GetByLabelOptions().setExact(true));
    }

    public void cancelFailPayoutDialog() {
        Locator dialog = failPayoutDialog();
        dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel").setExact(true)).click();
        dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private Locator markPaidDialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Mark Payout as Paid").setExact(true));
    }

    private Locator failPayoutDialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Fail Payout").setExact(true));
    }

    private Locator.WaitForOptions visible() {
        return new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE);
    }
}
