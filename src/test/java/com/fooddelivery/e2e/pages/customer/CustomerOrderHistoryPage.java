package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object for Customer Order History.
 * Maps to the reachable account-settings {@code SettingsHistoryTab.tsx}
 */
public class CustomerOrderHistoryPage {

    private final Page page;

    public CustomerOrderHistoryPage(Page page) {
        this.page = page;
    }

    public void waitForHistoryLoad() {
        // Loading or an error is never successful history readiness. Do not swallow timeouts.
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                page.getByTestId("customer-history-state")).hasAttribute("data-state",
                java.util.regex.Pattern.compile("^(empty|populated)$"),
                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(30000));
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
        return page.getByTestId("customer-history-order");
    }

    public Locator emptyState() {
        return page.getByText("No order history found.",
                new Page.GetByTextOptions().setExact(true));
    }

    public boolean hasDefinedState() {
        return emptyState().isVisible() || getOrderCount() > 0;
    }
}
