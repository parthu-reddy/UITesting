package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/**
 * Page object for the customer checkout sheet ({@code CustomerCheckout.tsx}).
 *
 * <p>Rebuilt 2026-09-24 against the redesign: the dialog is named "Checkout", payment methods
 * are a radio group (Wallet / UPI / card), and the single primary action is "Place order",
 * which stays disabled until the server quote has returned.</p>
 */
public class PaymentModalPage {

    private final Page page;

    public PaymentModalPage(Page page) {
        this.page = page;
    }

    private Locator dialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Checkout").setExact(true));
    }

    public void waitForOpen() {
        dialog().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
    }

    public boolean isOpen() {
        return dialog().isVisible();
    }

    /** The address as words. The sheet it replaced printed the address row's UUID here. */
    public String getDeliveryAddress() {
        return dialog().getByText("DELIVER TO", new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::span[1]").innerText().trim();
    }

    public boolean hasItem(String itemName) {
        return dialog().getByText(itemName, new Locator.GetByTextOptions().setExact(true)).isVisible();
    }

    public boolean hasQuantity(int quantity) {
        return dialog().getByText("×" + quantity, new Locator.GetByTextOptions().setExact(true)).first().isVisible();
    }

    /**
     * A bill line with a real amount. Labels: "Item total", "Delivery fee", "Platform fee",
     * "GST & restaurant charges", "Total".
     */
    public boolean hasTotalLine(String label) {
        Locator line = dialog().getByText(label, new Locator.GetByTextOptions().setExact(true)).first()
                .locator("xpath=ancestor::div[contains(., '₹') or contains(., 'FREE')][1]");
        if (!line.isVisible()) return false;
        String text = line.innerText();
        return !text.contains("null") && !text.contains("undefined") && !text.contains("NaN");
    }

    /** Radio names: "La Bouffe Wallet …", "UPI", "Credit or debit card". Matched as a substring. */
    public boolean hasPaymentMethod(String name) {
        return dialog().getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)))
                .first().isVisible();
    }

    public void selectPaymentMethod(String name) {
        dialog().getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)))
                .first().click();
    }

    private Locator placeOrder() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("Place order")));
    }

    /** Waits for the quote: Place order is disabled while taxes are still being calculated. */
    public boolean isPayEnabled() {
        Locator pay = placeOrder();
        for (int i = 0; i < 20 && pay.isVisible() && !pay.isEnabled(); i++) {
            page.waitForTimeout(500);
        }
        return pay.isVisible() && pay.isEnabled();
    }

    public void placeOrder(String method) {
        selectPaymentMethod(method);
        if (!isPayEnabled()) {
            throw new IllegalStateException("Place order stayed disabled -- quote never arrived or no method is selectable");
        }
        placeOrder().click();
    }

    public void close() {
        dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close dialog")).click();
        dialog().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
