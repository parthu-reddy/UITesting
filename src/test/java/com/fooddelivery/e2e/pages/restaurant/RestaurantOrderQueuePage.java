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
        new RestaurantDashboardPage(page).waitForDashboard();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(
                page.getByRole(com.microsoft.playwright.options.AriaRole.REGION,
                        new Page.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Incoming, [0-9]+ orders?$")))).isVisible();
    }

    /**
     * Selects an outlet from the header dropdown so orders for that outlet are shown.
     * The dropdown is a custom {@code <Select>} with {@code aria-label="Outlet"}.
     *
     * @param outletName partial or full name of the outlet, e.g. "Brand 1 Outlet 1"
     */
    public void selectOutlet(String outletName) {
        new RestaurantDashboardPage(page).selectOutlet(outletName);
    }

    private Locator cards(String... statuses) {
        StringBuilder sel = new StringBuilder();
        for (String st : statuses) {
            if (sel.length() > 0) sel.append(", ");
            sel.append("[data-testid='restaurant-order-card'][data-status='").append(st).append("']");
        }
        return page.locator(sel.toString());
    }

    public int getNewOrderCount() {
        return cards("PENDING_ACCEPTANCE", "CREATED").count();
    }

    public int getCookingOrderCount() {
        return cards("ACCEPTED", "PREPARING").count();
    }

    public boolean hasOrders() {
        // Poll for up to 15 seconds — SSE-delivered orders may not appear immediately
        for (int i = 0; i < 15; i++) {
            if (page.locator("[data-testid='restaurant-order-card']").first().isVisible()) {
                return true;
            }
            page.waitForTimeout(1000);
        }
        return false;
    }

    /**
     * The board has no refresh control: it polls every 5 s ("Updates every 5 s",
     * useRestaurantOrders). A reload is the immediate refresh, and it keeps the selected outlet,
     * which the dashboard stores in localStorage.
     */
    public void refreshOrders() {
        page.reload();
        waitForQueueLoad();
    }
}

