package com.fooddelivery.e2e.pages.delivery;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.SwipeHelper;

/**
 * Page Object for the Active Delivery Job (pickup → delivery flow).
 * Maps to: {@code DeliveryActiveJob.tsx, ActiveDeliveryCard.tsx}
 * <p>
 * Key difference from old tests: Pickup and delivery confirmations now use
 * {@code <SwipeAction>} (role="slider") instead of button clicks.
 * </p>
 */
public class DeliveryActiveJobPage {

    private final Page page;

    public DeliveryActiveJobPage(Page page) {
        this.page = page;
    }

    // ── Pre-pickup ───────────────────────────────────────────────────────

    /**
     * Clicks "Arrived at Restaurant" to signal the rider is at the pickup location.
     */
    public void markArrivedAtRestaurant() {
        Locator btn = page.locator("button:has-text('Arrived at Restaurant'), button:has-text('Arrived')").first();
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));
        btn.click();
        page.waitForTimeout(1000);
    }

    // ── Pickup OTP + SwipeAction ─────────────────────────────────────────

    /**
     * Enters the 6-digit pickup OTP from the restaurant.
     */
    public void enterPickupOtp(String otp) {
        Locator input = page.getByPlaceholder("Enter 6-digit pickup OTP");
        input.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        input.fill(otp);
    }

    /**
     * Swipes to confirm pickup (replaces the old "Verify & Pick Up" button).
     */
    public void swipeToConfirmPickup() {
        SwipeHelper.swipeToConfirm(page, "Slide to confirm pickup");
    }

    // ── Delivery OTP + SwipeAction ───────────────────────────────────────

    /**
     * Enters the 6-digit delivery OTP from the customer.
     */
    public void enterDeliveryOtp(String otp) {
        Locator input = page.getByPlaceholder("Ask customer for 6-digit OTP");
        input.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30000));
        input.fill(otp);
    }

    /**
     * Swipes to confirm delivery (replaces the old "Verify & Deliver" button).
     */
    public void swipeToConfirmDelivery() {
        // The delivery swipe label includes the payout amount, so we match partial text
        Locator slider = page.locator("div[role='slider']")
                .filter(new Locator.FilterOptions().setHasText("Slide to deliver"));
        slider.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        slider.first().focus();
        slider.first().press("End");
        page.waitForTimeout(500);
    }

    // ── Status checks ────────────────────────────────────────────────────

    /** The rider's job panel: "Active Contract" (DeliveryActiveJob.tsx). */
    private Locator activeContract() {
        return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Active Contract").setExact(true));
    }

    public boolean isActiveJobVisible() {
        return activeContract().isVisible();
    }

    /** Before pickup the panel asks for the restaurant's code (ActiveDeliveryCard.tsx). */
    public boolean isPickupPhase() {
        return page.getByPlaceholder("Enter 6-digit pickup OTP").isVisible();
    }

    /** After pickup it asks for the customer's code instead. */
    public boolean isDeliveryPhase() {
        return page.getByPlaceholder("Ask customer for 6-digit OTP").isVisible();
    }

    /**
     * A confirmed delivery closes the job panel (useRiderJobActions: setActiveJobId(null)); there
     * is no toast. A rejected code keeps the panel open with an error, so "closed" is the signal.
     */
    public boolean hasCompletedDelivery() {
        try {
            activeContract().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(15000));
            return true;
        } catch (com.microsoft.playwright.PlaywrightException e) {
            return false;
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────

    public void openNavigationMap() {
        page.locator("button:has-text('Navigate'), button:has(svg.lucide-navigation)").first().click();
        page.waitForTimeout(500);
    }

    public void callCustomer() {
        page.locator("button:has(svg.lucide-phone)").first().click();
        page.waitForTimeout(500);
    }
}
