package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.fooddelivery.e2e.util.OtpExtractor;
import com.fooddelivery.e2e.util.WaitHelpers;

/**
 * Page Object for the Customer Order Tracker.
 * Maps to: {@code OrderTrackerLive.tsx, OrderTrackerSettled.tsx, OrderTrackerSteps.tsx}
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

    public void cancelOrder() {
        page.locator("button:has-text('Cancel Order')").first().click();
        page.waitForTimeout(1000);
    }

    public void approveDelay() {
        page.locator("button:has-text('Accept Delay'), button:has-text('Approve')").first().click();
        page.waitForTimeout(1000);
    }

    public void rejectDelay() {
        page.locator("button:has-text('Cancel Order')").first().click();
        page.waitForTimeout(1000);
    }

    public void dismissFailedOrder() {
        page.locator("button:has-text('Dismiss')").first().click();
        page.waitForTimeout(500);
    }

    // ── Order details ────────────────────────────────────────────────────

    public String getTotalPaid() {
        return page.locator("text=Total Paid").locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getPaymentMethod() {
        return page.locator("text=Paid via").first().innerText().trim();
    }

    public String getOrderId() {
        Locator selectLocator = page.locator("[role='combobox'][aria-label='Which order to track']").first();
        if (selectLocator.isVisible()) {
            String text = selectLocator.innerText().trim();
            if (text.startsWith("#")) {
                text = text.substring(1);
            }
            int spaceIndex = text.indexOf(" ");
            if (spaceIndex != -1) {
                return text.substring(0, spaceIndex);
            }
            return text;
        }

        String idText = page.locator("h3 span.font-mono").first().innerText().trim();
        if (idText.startsWith("#")) {
            return idText.substring(1);
        }
        return idText;
    }

    public boolean hasRiderAssigned() {
        try {
            page.locator("text=Rider Assigned").waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRiderAssigned(String orderId) {
        try {
            Locator trackerContainer = page.locator("div:has(span:has-text('#" + orderId + "'))").last();
            trackerContainer.locator("text=Rider Assigned").waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
