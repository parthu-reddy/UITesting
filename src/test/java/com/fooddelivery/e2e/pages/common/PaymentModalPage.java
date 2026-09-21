package com.fooddelivery.e2e.pages.common;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the PaymentModal used during checkout.
 * Maps to: {@code CustomerPaymentModal.tsx → PaymentModal.tsx}
 */
public class PaymentModalPage {

    private final Page page;

    public PaymentModalPage(Page page) {
        this.page = page;
    }

    public void waitForModalOpen() {
        page.locator("text=Delivering To, text=Order Summary, text=Pay Securely").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10000));
    }

    public boolean isOpen() {
        return page.locator("text=Delivering To, text=Pay Securely").first().isVisible();
    }

    public void selectCashOnDelivery() {
        page.locator("button:has-text('Cash on Delivery'), label:has-text('Cash on Delivery')").first().click();
        page.waitForTimeout(300);
    }

    public void selectWalletPayment() {
        page.locator("button:has-text('Wallet'), label:has-text('Wallet')").first().click();
        page.waitForTimeout(300);
    }

    public void clickPaySecurely() {
        page.locator("button:has-text('Pay Securely')").click();
        page.waitForTimeout(2000); // Wait for order processing
    }

    public String getOrderTotal() {
        return page.locator("text=Total").locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getDeliveryAddress() {
        Locator addr = page.locator("text=Delivering To").locator("xpath=..").locator("p").first();
        return addr.isVisible() ? addr.innerText().trim() : "";
    }

    public boolean hasError() {
        return page.locator(".text-rose-500, .text-red-500, text=Error, text=Failed").first().isVisible();
    }

    public String getErrorMessage() {
        return page.locator(".text-rose-500, .text-red-500").first().innerText().trim();
    }
}
