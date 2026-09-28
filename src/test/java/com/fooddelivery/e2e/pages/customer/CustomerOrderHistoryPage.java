package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object for Customer Order History.
 * Maps to: {@code CustomerOrderHistory.tsx, OrderCard.tsx}
 */
public class CustomerOrderHistoryPage {

    private final Page page;

    public CustomerOrderHistoryPage(Page page) {
        this.page = page;
    }

    public void waitForHistoryLoad() {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true))
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(10000));
        // First wait for either loading, empty state, or data to appear
        Locator loading = page.locator("[data-screen='settings']")
                .getByText("Loading history...", new Locator.GetByTextOptions().setExact(true));
        Locator anyContent = loading
                .or(emptyState())
                .or(historyCards().first());
        anyContent.first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(20000));
        // Then wait for Loading to disappear (i.e. data actually loaded)
        try {
            loading.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(15000));
        } catch (com.microsoft.playwright.TimeoutError ignored) { }
    }

    public int getOrderCount() {
        return historyCards().count();
    }

    public boolean hasOrderWithStatus(String status) {
        return page.locator("text=" + status).isVisible();
    }

    public void clickOrderCard(int index) {
        historyCards().nth(index).click();
    }

    public boolean hasReorderButton() {
        return page.locator("button:has-text('Reorder')").first().isVisible();
    }

    public void clickReorder() {
        page.locator("button:has-text('Reorder')").first().click();
        page.waitForTimeout(500);
    }

    public Locator historyCards() {
        return page.locator("[data-screen='settings']")
                .locator("div.flex-1.overflow-y-auto.overscroll-none button[type='button']")
                .filter(new Locator.FilterOptions().setHas(page.locator("span.font-mono")));
    }

    public Locator emptyState() {
        return page.getByText("No order history found.",
                new Page.GetByTextOptions().setExact(true));
    }

    public boolean hasDefinedState() {
        return emptyState().isVisible() || getOrderCount() > 0;
    }
}
