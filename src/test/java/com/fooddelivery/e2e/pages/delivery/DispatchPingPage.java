package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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

    /**
     * Waits for a new dispatch ping to appear.
     * The dispatch ping auto-expires after a timeout, so we wait with a generous timeout.
     */
    public void waitForPing() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(120000)); // 2 minutes: order dispatch may take time
    }

    public boolean hasPing() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT).isVisible();
    }

    /**
     * Accepts the dispatch by clicking "Accept Order".
     */
    public void acceptDispatch() {
        Locator accept = page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
                .locator("button:has-text('Accept Order'), button:has-text('Accept')");
        accept.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        accept.click();
        page.waitForTimeout(2000);
    }

    /**
     * Declines the dispatch.
     */
    public void declineDispatch() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.ALERT)
                .locator("button:has-text('Decline')").click();
        page.waitForTimeout(1000);
    }

    /**
     * Gets the remaining countdown time displayed on the ping.
     */
    public String getCountdownText() {
        return page.locator(".countdown, text=s remaining, text=seconds").first().innerText().trim();
    }

    /**
     * Gets the restaurant name from the dispatch ping.
     */
    public String getRestaurantName() {
        return page.locator("text=from").locator("xpath=..").innerText().trim();
    }
}
