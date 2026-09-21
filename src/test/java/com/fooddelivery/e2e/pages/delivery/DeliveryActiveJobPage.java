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
        Locator input = page.getByPlaceholder("Enter 6-digit delivery OTP");
        input.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
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

    public boolean isActiveJobVisible() {
        return page.locator("text=Active Delivery, text=Pickup, text=Arrived at Restaurant").first().isVisible();
    }

    public boolean isPickupPhase() {
        return page.locator("text=Enter 6-digit pickup OTP, text=Slide to confirm pickup").first().isVisible();
    }

    public boolean isDeliveryPhase() {
        return page.locator("text=Enter 6-digit delivery OTP, text=Slide to deliver").first().isVisible();
    }

    public boolean hasCompletedDelivery() {
        return page.locator("text=Delivered, text=Delivery Complete, text=Completed").first().isVisible();
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
