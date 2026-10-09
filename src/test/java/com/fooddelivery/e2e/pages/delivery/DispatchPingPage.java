package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object for the Dispatch Ping Card (rider receives new order).
 * Maps to: {@code DispatchPingCard.tsx} — shows "New Dispatch" with countdown timer.
 */
public class DispatchPingPage {

    private final Page page;

    public DispatchPingPage(Page page) {
        this.page = page;
    }

    private Locator popup() {
        return page.getByRole(AriaRole.ALERT)
                .filter(new Locator.FilterOptions().setHasText("New Dispatch"));
    }

    /**
     * Waits for a new dispatch ping to appear.
     * The dispatch ping auto-expires after a timeout, so we wait with a generous timeout.
     */
    public void waitForPing() {
        popup()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(120000)); // 2 minutes: order dispatch may take time
    }

    public void waitForPing(String outletName) {
        popup().filter(new Locator.FilterOptions().setHasText(outletName))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(120000));
    }

    /**
     * Accepts the dispatch by clicking "Accept Order".
     */
    public void acceptDispatch() {
        acceptDispatch(null);
    }

    public void acceptDispatch(String orderId) {
        // The jobs board appears before the popup in the DOM. A broad "Accept" selector
        // clicks that covered card only after the popup expires, when the API returns 410.
        Locator accept = page.getByRole(AriaRole.ALERT)
                .filter(new Locator.FilterOptions().setHasText("New Dispatch"))
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Accept Order").setExact(true));
        accept.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        Response response = page.waitForResponse(
                r -> r.request().method().equals("POST")
                        && r.url().contains("/api/delivery/drivers/")
                        && r.url().endsWith("/accept"),
                new Page.WaitForResponseOptions().setTimeout(15000),
                () -> accept.click(new Locator.ClickOptions().setTimeout(5000)));
        org.assertj.core.api.Assertions.assertThat(response.ok())
                .as("Dispatch acceptance must succeed: HTTP %s %s", response.status(), response.url())
                .isTrue();
        if (orderId != null) {
            org.assertj.core.api.Assertions.assertThat(response.url())
                    .as("Accepted dispatch must belong to the test order")
                    .endsWith("/orders/" + orderId + "/accept");
        }
    }

    /**
     * Declines the dispatch.
     */
    public void declineDispatch() {
        popup().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Decline").setExact(true)).click();
        page.waitForTimeout(1000);
    }

    /**
     * Gets the remaining countdown time displayed on the ping.
     */
    public String getCountdownText() {
        return popup().getByRole(AriaRole.TIMER).innerText().trim();
    }

    /**
     * Gets the restaurant name from the dispatch ping.
     */
    public String getRestaurantName() {
        return popup().getByText("Pickup", new Locator.GetByTextOptions().setExact(true))
                .locator("..").locator("span").last().innerText().trim();
    }
}
