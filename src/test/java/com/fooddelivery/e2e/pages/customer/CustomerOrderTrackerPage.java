package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.OtpExtractor;

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
    private final String orderId;

    public CustomerOrderTrackerPage(Page page) {
        this(page, null);
    }

    public CustomerOrderTrackerPage(Page page, String orderId) {
        this.page = page;
        this.orderId = orderId;
    }

    // ── Status verification ──────────────────────────────────────────────

    /** The tracker for the order in view (or the first one). */
    public Locator tracker() {
        if (orderId != null) return page.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
        return page.locator("[data-testid='order-tracker']").first();
    }

    /** The tracker is on screen: an order was placed and the customer is watching it. */
    public void waitForTracker() {
        tracker().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30000));
    }

    /**
     * Waits until the tracker reports one of these ORDER statuses -- the backend enum the tracker
     * carries in {@code data-status} (PREPARING, READY_FOR_PICKUP, CANCELLED ...).
     *
     * <p>Not the headline: its wording is design copy ("Cooking now"), and while an arrival
     * estimate exists the headline is replaced by "ARRIVING IN N min" altogether. Reloads once
     * midway, because a dropped live stream leaves the tracker on its last state.</p>
     */
    public void waitForStatus(String... orderStatuses) {
        Locator match = page.locator(java.util.Arrays.stream(orderStatuses)
                .map(s -> "[data-testid='order-tracker'][data-status='" + s + "']")
                .collect(java.util.stream.Collectors.joining(", "))).first();
        try {
            match.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(20000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            page.reload();
            match.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));
        }
    }

    public boolean hasStatus(String orderStatus) {
        try {
            waitForStatus(orderStatus);
            return true;
        } catch (com.microsoft.playwright.PlaywrightException e) {
            return false;
        }
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
        // "I’ll wait" -- the UI writes a typographic apostrophe (&rsquo;), so match the end of
        // the name rather than typing the character (OrderTrackerLive.tsx).
        Locator approve = tracker().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(java.util.regex.Pattern.compile("ll wait$")));
        approve.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(20000));
        approve.click();
    }

    /** Declining the delay asks first ("Cancel instead of waiting?"), like any other cancel. */
    public void rejectDelay() {
        // The delay prompt's "Cancel order" answers the delay (POST .../delay-approval). While the screen still shows
        // CREATED/PENDING_ACCEPTANCE the same name belongs to the plain cancel, so wait for the prompt first
        // (2026-10-08: without the 400 ms slow-mo the plain cancel was clicked; the order ended CANCELLED).
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(tracker())
                .hasAttribute("data-status", "AWAITING_DELAY_APPROVAL");
        Locator reject = tracker().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Cancel order").setExact(true));
        reject.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(20000));
        reject.click();
        confirmCancel();
    }

    public void dismissFailedOrder() {
        // Failed history is rendered by CustomerOrderTracker + OrderTrackerSettled.
        // Its header's Back button dismisses the selected details, retaining the record.
        tracker().locator("..").getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Back").setExact(true)).click();
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
        return tracker().getByText(java.util.regex.Pattern.compile("^Paid via ")).innerText().trim();
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
