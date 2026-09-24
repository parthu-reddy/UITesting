package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.OtpExtractor;

/**
 * Page Object for Restaurant Order Actions (Accept, Start cooking, Mark ready, Show pickup code).
 * Maps to: {@code RestaurantOrderActions.tsx, RestaurantOrderCard.tsx}
 */
public class RestaurantOrderActionsPage {

    private final Page page;

    public RestaurantOrderActionsPage(Page page) {
        this.page = page;
    }

    /**
     * The card by its data-order-id (prefix match, so a short id works). Classes are not an
     * interface. Public because flow tests scope their own assertions to one card.
     */
    public Locator orderCard(String orderId) {
        String shortId = orderId.substring(0, Math.min(8, orderId.length())).toLowerCase();
        return page.locator("[data-testid='restaurant-order-card'][data-order-id^='" + shortId + "']");
    }

    /**
     * The filled primary on an incoming card. It reads "Accept · 25 min" -- the promised prep
     * time follows the verb -- so match the start of the name, not the whole of it.
     */
    private static Locator acceptIn(Locator scope) {
        return scope.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("^Accept\\b"))).first();
    }

    public void openOrderDetails(String shortOrderId) {
        orderCard(shortOrderId).locator("button:has(svg.lucide-receipt)").click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG)
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
    }

    // ── Order lifecycle actions ──────────────────────────────────────────

    public void acceptOrder() {
        Locator btn = acceptIn(page.locator("[data-testid='restaurant-order-card']"));
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        btn.click();
        page.waitForTimeout(1000);
    }

    public void acceptOrder(String shortOrderId) {
        Locator btn = acceptIn(orderCard(shortOrderId));
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
        Locator btn = page.locator("button:has-text('Mark ready')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void markPrepared(String shortOrderId) {
        Locator btn = orderCard(shortOrderId).locator("button:has-text('Mark ready')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        btn.click();
        page.waitForTimeout(500);
    }

    public void cancelOrder() {
        // Step 1: "Reject" (incoming) or "Cancel" (accepted) opens the cancellation reason drawer
        Locator cancelBtn = page.locator("[data-testid='restaurant-order-card'] button:has-text('Reject'), [data-testid='restaurant-order-card'] button:has-text('Cancel')").first();
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
        // Step 1: "Reject" (incoming) or "Cancel" (accepted) opens the cancellation reason drawer
        Locator container = orderCard(shortOrderId);
        Locator cancelBtn = container.locator("button:has-text('Reject'), button:has-text('Cancel')").first();
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
     * Gets the pickup/handover OTP by clicking "Show pickup code" and extracting the value for a specific order.
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

    public void requestDelay(String shortOrderId, int minutes, String reason) {
        Locator card = orderCard(shortOrderId);
        card.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Ask the customer for more time").setExact(true)).click();

        Locator delayOption = card.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("+" + minutes + " Min").setExact(true));
        delayOption.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        delayOption.click();

        card.locator("input[placeholder*='High custom baking orders']").fill(reason);
        card.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit Delay").setExact(true)).click();
    }

    public boolean hasNewOrders() {
        return acceptIn(page.locator("[data-testid='restaurant-order-card']")).isVisible();
    }

    public boolean hasRefundRequest() {
        return page.locator("text=Refund requested").first().isVisible();
    }
}
