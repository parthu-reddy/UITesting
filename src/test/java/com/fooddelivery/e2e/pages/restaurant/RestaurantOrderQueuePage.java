package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for Restaurant Order Queue (Kanban view).
 * Maps to: {@code RestaurantOrderQueue.tsx, OrderQueue.tsx, KanbanColumn.tsx}
 */
public class RestaurantOrderQueuePage {

    private final Page page;

    public RestaurantOrderQueuePage(Page page) {
        this.page = page;
    }

    public void waitForQueueLoad() {
        page.waitForTimeout(4000); // Wait for SSE / API to populate
    }

    /**
     * Selects an outlet from the header dropdown so orders for that outlet are shown.
     * The dropdown is a custom {@code <Select>} with {@code aria-label="Outlet"}.
     *
     * @param outletName partial or full name of the outlet, e.g. "Brand 1 Outlet 1"
     */
    public void selectOutlet(String outletName) {
        // Click the Select trigger button (aria-label="Outlet")
        Locator trigger = page.locator("button[aria-label='Outlet']").first();
        trigger.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        trigger.click();
        page.waitForTimeout(300);

        // Wait for the listbox to appear and click the matching option
        Locator listbox = page.locator("ul[role='listbox']").first();
        listbox.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));

        Locator option = listbox.locator("li[role='option']:has-text('" + outletName + "')").first();
        option.click();
        page.waitForTimeout(500);
    }

    public int getNewOrderCount() {
        return page.locator("button:has-text('Accept Order')").count();
    }

    public int getCookingOrderCount() {
        return page.locator("button:has-text('Mark Prepared')").count()
                + page.locator("button:has-text('Start Cook')").count();
    }

    public boolean hasOrders() {
        // Poll for up to 15 seconds — SSE-delivered orders may not appear immediately
        for (int i = 0; i < 15; i++) {
            if (page.locator("text=Accept Order").first().isVisible()
                    || page.locator("text=Order Value").first().isVisible()) {
                return true;
            }
            page.waitForTimeout(1000);
        }
        return false;
    }

    public void refreshOrders() {
        page.locator("button[title='Refresh Orders'], button:has(svg.lucide-refresh-cw)").first().click();
        page.waitForTimeout(1000);
    }
}

