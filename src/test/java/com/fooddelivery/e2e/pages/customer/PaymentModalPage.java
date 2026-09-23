package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/** Page object for the customer checkout payment modal. */
public class PaymentModalPage {

    private final Page page;

    public PaymentModalPage(Page page) {
        this.page = page;
    }

    private Locator dialog() {
        return page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Complete Your Order"));
    }

    public void waitForOpen() {
        dialog().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
    }

    public boolean isOpen() {
        return dialog().isVisible();
    }

    public String getDeliveryAddress() {
        return dialog().getByText("Delivering To", new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::p").innerText().trim();
    }

    public boolean hasItem(String itemName) {
        return dialog().getByText(itemName, new Locator.GetByTextOptions().setExact(true)).isVisible();
    }

    public boolean hasQuantity(int quantity) {
        return dialog().getByText(quantity + "x", new Locator.GetByTextOptions().setExact(true)).isVisible();
    }

    public boolean hasTotalLine(String label) {
        Locator line = dialog().getByText(label, new Locator.GetByTextOptions().setExact(true)).locator("xpath=..");
        String text = line.innerText();
        boolean hasAmount = text.contains("₹") || (label.equals("Delivery Fee") && text.contains("FREE"));
        return line.isVisible() && hasAmount && !text.contains("null") && !text.contains("undefined");
    }

    public boolean hasPaymentMethod(String accessibleName) {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(accessibleName).setExact(true)).isVisible();
    }

    public void selectPaymentMethod(String accessibleName) {
        dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(accessibleName).setExact(true)).click();
    }

    public boolean isPayEnabled() {
        Locator pay = dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("Pay .* Now")));
        return pay.isVisible() && pay.isEnabled();
    }

    public void close() {
        dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close dialog")).click();
        dialog().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
