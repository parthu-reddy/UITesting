package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Customer Cart Drawer.
 * Maps to: {@code CustomerCartDrawer.tsx}
 */
public class CustomerCartDrawerPage {

    private final Page page;

    public CustomerCartDrawerPage(Page page) {
        this.page = page;
    }

    public void waitForCartOpen() {
        page.getByText("Your Cart").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
    }

    public boolean isCartOpen() {
        return page.getByText("Your Cart").first().isVisible();
    }

    public String getFirstItemName() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                        new Page.GetByRoleOptions().setName("Your cart"))
                .locator("div.space-y-3 span.font-semibold").first().innerText().trim();
    }

    public boolean isCheckoutEnabled() {
        Locator checkout = page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                        new Page.GetByRoleOptions().setName("Your cart"))
                .locator("button:has-text('Checkout')").first();
        return checkout.isVisible() && checkout.isEnabled();
    }

    public void clickPlaceOrder() {
        page.locator("button:has-text('Checkout')").first().click();
        new PaymentModalPage(page).waitForOpen();
    }

    /** Pays by card on the checkout sheet -- the method every test account can use. */
    public void clickPaySecurely() {
        new PaymentModalPage(page).placeOrder("Credit or debit card");
    }

    /**
     * Performs the complete checkout flow: Checkout → choose card → Place order.
     */
    public void checkout() {
        clickPlaceOrder();
        clickPaySecurely();
    }

    public String getCartTotal() {
        return page.locator("text=Total").locator("xpath=..").locator("span").last().innerText().trim();
    }

    private Locator drawer() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart"));
    }

    /** One row per dish: each row's Stepper has an "Add one <dish>" button (Stepper.tsx). */
    public int getItemCount() {
        return drawer().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Add one "))).count();
    }

    /** "Remove one <dish>"; at quantity 1 it removes the row (the Stepper's min is 0). */
    public void removeItem(int index) {
        drawer().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Remove one "))).nth(index).click();
        page.waitForTimeout(300);
    }

    public void incrementItem(int index) {
        drawer().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Add one "))).nth(index).click();
        page.waitForTimeout(300);
    }

    /** The drawer's close control is an unlabelled X icon button (CustomerCartDrawer.tsx). */
    public void closeCart() {
        drawer().locator("button:has(svg.lucide-x)").first().click();
        drawer().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
