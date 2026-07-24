package com.fooddelivery.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.regex.Pattern;

public class CustomerCartDrawer {
    private final Page page;

    public CustomerCartDrawer(Page page) {
        this.page = page;
    }

    public void clickCloseCart() {
        page.locator("button:has(svg.lucide-x)").first().click();
    }

    public void clickIncrementItem(String itemName) {
        Locator itemRow = page.locator("div.flex.justify-between.items-center").filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + itemName + ".*")));
        itemRow.locator("button:has-text('+')").click();
    }

    public void clickDecrementItem(String itemName) {
        Locator itemRow = page.locator("div.flex.justify-between.items-center").filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + itemName + ".*")));
        itemRow.locator("button:has-text('-')").click();
    }

    public void fillDeliveryAddress(String address) {
        page.locator("input.bg-transparent").last().fill(address);
    }

    public void clickPlaceCashOnDeliveryOrder() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Place Cash-on-Delivery Order", Pattern.CASE_INSENSITIVE))).click();
    }
}
