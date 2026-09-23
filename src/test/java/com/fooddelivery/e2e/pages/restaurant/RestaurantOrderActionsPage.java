package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.OtpExtractor;

/**
 * Page Object for Restaurant Order Actions (Accept, Cook, Mark Prepared, Show OTP).
 * Maps to: {@code RestaurantOrderActions.tsx, RestaurantOrderCard.tsx}
 */
public class RestaurantOrderActionsPage {

    private final Page page;

    public RestaurantOrderActionsPage(Page page) {
        this.page = page;
    }

    private Locator orderCard(String orderId) {
        String shortId = orderId.substring(0, Math.min(8, orderId.length()));
        return page.locator("div.p-4.space-y-3\\.5").filter(
                new Locator.FilterOptions().setHas(page.locator("span.font-mono", new Page.LocatorOptions().setHasText("#" + shortId))));
    }

    // ── Order lifecycle actions ──────────────────────────────────────────

    public void acceptOrder() {
        Locator btn = page.locator("button:has-text('Accept Order')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        btn.click();
        page.waitForTimeout(1000);
    }

    public void acceptOrder(String shortOrderId) {
        Locator btn = orderCard(shortOrderId).locator("button:has-text('Accept Order')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        btn.click();
        page.waitForTimeout(1000);
    }

    public void startCooking() {
        Locator btn = page.locator("button:has-text('Start Cook')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void startCooking(String shortOrderId) {
        Locator btn = orderCard(shortOrderId).locator("button:has-text('Start Cook')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void markPrepared() {
        Locator btn = page.locator("button:has-text('Mark Prepared')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void markPrepared(String shortOrderId) {
        Locator btn = orderCard(shortOrderId).locator("button:has-text('Mark Prepared')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void cancelOrder() {
        // Step 1: Click "Cancel" to open the cancellation reason drawer
        Locator cancelBtn = page.locator("button:has-text('Cancel')").first();
        cancelBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        cancelBtn.click();
        page.waitForTimeout(500);

        // Step 1.5: Fill in a cancellation reason
        Locator reasonInput = page.locator("input[placeholder*='Out of stock']").first();
        if (reasonInput.isVisible()) {
            reasonInput.fill("Item out of stock");
        }

        // Step 2: Click "Confirm Cancel" in the drawer to actually cancel
        Locator confirmBtn = page.locator("button:has-text('Confirm Cancel')").first();
        confirmBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        confirmBtn.click();
        page.waitForTimeout(1000);
    }

    public void cancelOrder(String shortOrderId) {
        // Step 1: Click "Cancel" to open the cancellation reason drawer
        Locator container = orderCard(shortOrderId);
        Locator cancelBtn = container.locator("button:has-text('Cancel')").first();
        cancelBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        cancelBtn.click();
        page.waitForTimeout(500);

        // Step 1.5: Fill in a cancellation reason
        Locator reasonInput = page.locator("input[placeholder*='Out of stock']").first();
        if (reasonInput.isVisible()) {
            reasonInput.fill("Item out of stock");
        }

        // Step 2: Click "Confirm Cancel" in the drawer to actually cancel
        Locator confirmBtn = page.locator("button:has-text('Confirm Cancel')").first();
        confirmBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        confirmBtn.click();
        page.waitForTimeout(1000);
    }

    // ── OTP extraction ───────────────────────────────────────────────────

    /**
     * Gets the pickup/handover OTP by clicking "Show Handover OTP" and extracting the value for a specific order.
     */
    public String getPickupOtp(String shortOrderId) {
        return OtpExtractor.getRestaurantPickupOtp(page, shortOrderId);
    }

    // ── Chat ─────────────────────────────────────────────────────────────

    public void openChat() {
        // The chat button is the MessageSquare icon button
        page.locator("button:has(svg.lucide-message-square)").first().click();
        page.waitForTimeout(500);
    }

    // ── Order details ────────────────────────────────────────────────────

    public void showOrderDetails() {
        // The receipt icon button opens details
        page.locator("button:has(svg.lucide-receipt)").first().click();
        page.waitForTimeout(500);
    }

    public String getOrderValue() {
        return page.locator("text=Order Value").locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getPayoutAmount() {
        return page.locator("text=Your Payout").locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Delay request ────────────────────────────────────────────────────

    public void requestDelay() {
        // The clock icon button opens the delay modal
        page.locator("button:has(svg.lucide-clock)").first().click();
        page.waitForTimeout(500);
    }

    public boolean hasNewOrders() {
        return page.locator("button:has-text('Accept Order')").first().isVisible();
    }

    public boolean hasRefundRequest() {
        return page.locator("text=Action Required: Refund").isVisible();
    }
}
