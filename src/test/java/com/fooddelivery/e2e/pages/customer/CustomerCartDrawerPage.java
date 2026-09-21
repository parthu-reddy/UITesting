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

    public void clickPlaceOrder() {
        page.locator("button:has-text('Place Cash-on-Delivery Order'), button:has-text('Checkout'), button:has-text('Place Order')").first().click();
        page.waitForTimeout(1000);
    }

    public void clickPaySecurely() {
        page.locator("button", new Page.LocatorOptions().setHasText("Pay ")).last()
            .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        page.waitForTimeout(2000);
    }

    /**
     * Performs the complete checkout flow: View Cart → Place Order → Pay.
     */
    public void checkout() {
        clickPlaceOrder();
        clickPaySecurely();
    }

    public String getCartTotal() {
        return page.locator("text=Total").locator("xpath=..").locator("span").last().innerText().trim();
    }

    public int getItemCount() {
        return page.locator("[data-testid='cart-item'], .cart-item").count();
    }

    public void removeItem(int index) {
        page.locator("button:has(svg.lucide-minus), button:has(svg.lucide-trash)").nth(index).click();
        page.waitForTimeout(300);
    }

    public void incrementItem(int index) {
        page.locator("button:has(svg.lucide-plus)").nth(index).click();
        page.waitForTimeout(300);
    }

    public void closeCart() {
        page.locator("button:has(svg.lucide-x)").first().click();
        page.waitForTimeout(300);
    }
}
