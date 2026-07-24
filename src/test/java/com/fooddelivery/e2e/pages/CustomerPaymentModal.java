package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class CustomerPaymentModal {
    private final Page page;

    public CustomerPaymentModal(Page page) {
        this.page = page;
    }

    public void clickPaySecurely() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Pay Securely")).click();
    }

    public void clickCancel() {
        page.locator("button:has-text('Cancel')").click();
    }
}
