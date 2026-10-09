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

    /**
     * The history row of one order, clicking "Load More History" until it is rendered. History is newest first and
     * paged, so an older owned order is not on page 0. Fails when the list ends (no button) without the order.
     */
    public Locator pageToOrder(String orderId, int maxPages) {
        Locator card = page.locator("[data-testid='customer-history-order'][data-order-id='" + orderId + "']");
        Locator loadMore = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Load More History").setExact(true));
        waitForHistoryLoad();
        for (int loaded = 1; card.count() == 0; loaded++) {
            if (loaded > maxPages || loadMore.count() == 0) {
                throw new AssertionError("order " + orderId + " not in " + loaded + " history page(s); rows="
                        + getOrderCount() + ", more=" + (loadMore.count() > 0));
            }
            int before = getOrderCount();
            loadMore.click();
            page.waitForCondition(() -> getOrderCount() > before, new Page.WaitForConditionOptions().setTimeout(30000));
            waitForHistoryLoad();
        }
        return card;
    }

    public Locator emptyState() {
        return page.getByText("No order history found.",
                new Page.GetByTextOptions().setExact(true));
    }

    public boolean hasDefinedState() {
        return emptyState().isVisible() || getOrderCount() > 0;
    }
}
