package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.OtpExtractor;
import com.fooddelivery.e2e.util.WaitHelpers;

/**
 * Page Object for the Customer Order Tracker.
 * Maps to: {@code OrderTrackerLive.tsx, OrderTrackerSettled.tsx, OrderDeliveredSummary.tsx}
 * <p>
 * Each of the three renders {@code [data-testid='order-tracker']} with the full order id in
 * {@code data-order-id}. At the default 1280 px viewport the live tracker is in the desktop
 * right rail, not the main column -- locate it by test id, never by layout.
 * </p>
 * <p>
 * Also covers sub-components: {@code OrderStatusTimeline.tsx}, {@code OrderMoneyBreakdown.tsx},
 * {@code OrderItemList.tsx}, {@code CustomerOrderPlacedToast.tsx}, {@code CustomerOrderTracker.tsx}
 * </p>
 */
public class CustomerOrderTrackerPage {

    private final Page page;

    public CustomerOrderTrackerPage(Page page) {
        this.page = page;
    }

    // ── Status verification ──────────────────────────────────────────────

    /** The tracker for the order in view (or the first one). */
    public Locator tracker() {
        return page.locator("[data-testid='order-tracker']").first();
    }

    /** The tracker is on screen: an order was placed and the customer is watching it. */
    public void waitForTracker() {
        tracker().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30000));
    }

    public void verifyOrderStatus(String expectedStatus) {
        WaitHelpers.waitForOrderStatus(page, expectedStatus, 30000);
    }

    public void verifyOrderStatusWithReload(String expectedStatus) {
        try {
            WaitHelpers.waitForOrderStatus(page, expectedStatus, 20000);
        } catch (Exception e) {
            // SSE may have dropped — reload and try again
            page.reload();
            page.waitForTimeout(2000);
            WaitHelpers.waitForText(page, expectedStatus, 15000);
        }
    }

    public boolean isStatusVisible(String status) {
        return page.locator("text=" + status).isVisible();
    }

    // ── OTP extraction ───────────────────────────────────────────────────

    public String getDeliveryOtp() {
        return OtpExtractor.getCustomerDeliveryOtp(page);
    }

    // ── Order actions ────────────────────────────────────────────────────

    /** Cancel now asks first (a danger confirm); this answers it. */
    private void confirmCancel() {
        page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG)
                .getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Cancel order").setExact(true))
                .click();
    }

    public void cancelOrder() {
        tracker().locator("button:has-text('Cancel order')").first().click();
        confirmCancel();
        page.waitForTimeout(1000);
    }

    public void approveDelay() {
        tracker().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("ll wait$"))).click();
        page.waitForTimeout(1000);
    }

    public void rejectDelay() {
        tracker().locator("button:has-text('Cancel order')").first().click();
        confirmCancel();
        page.waitForTimeout(1000);
    }

    public void dismissFailedOrder() {
        page.locator("button:has-text('Dismiss')").first().click();
        page.waitForTimeout(500);
    }

    // ── Order details ────────────────────────────────────────────────────

    public String getTotalPaid() {
        // The live tracker keeps the bill in a collapsed <details>; open it first.
        Locator summary = tracker().locator("details > summary").first();
        if (summary.count() > 0 && tracker().locator("details[open]").count() == 0) summary.click();
        return tracker().locator("text=Total paid").first()
                .locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getPaymentMethod() {
        return page.locator("text=Paid via").first().innerText().trim();
    }

    public String getOrderId() {
        waitForTracker();
        return tracker().getAttribute("data-order-id");
    }

    public boolean hasRiderAssigned() {
        try {
            page.locator("[data-testid='rider-card']").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRiderAssigned(String orderId) {
        try {
            page.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "'] [data-testid='rider-card']")
                    .first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
