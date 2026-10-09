package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Page Object for the Admin Order Money view.
 * Maps to: {@code AdminOrderMoney.tsx}
 * <p>
 * Read-only breakdown of order money: customer paid, restaurant payout, rider payout.
 * Used to verify financial integrity in E2E tests.
 * </p>
 */
public class AdminOrderMoneyPage {

    private final Page page;

    public AdminOrderMoneyPage(Page page) {
        this.page = page;
    }

    // ── Visibility ───────────────────────────────────────────────────────

    public boolean isOrderMoneyVisible() {
        return customerPaidHeading().isVisible()
                && restaurantPayoutHeading().isVisible()
                && riderPayoutHeading().isVisible();
    }

    public void waitForOrderMoney() {
        customerPaidHeading().waitFor();
        restaurantPayoutHeading().waitFor();
        riderPayoutHeading().waitFor();
    }

    private com.microsoft.playwright.Locator customerPaidHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Customer Paid").setExact(true));
    }

    private com.microsoft.playwright.Locator restaurantPayoutHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Restaurant Payout").setExact(true));
    }

    private com.microsoft.playwright.Locator riderPayoutHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Rider Payout").setExact(true));
    }

    // ── Customer section ─────────────────────────────────────────────────

    public String getCustomerTotal() {
        return customerPaidHeading().locator("xpath=..").getByText("Total", new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))
                .locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getFoodCost() {
        return page.locator("text=Food Cost").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getDeliveryFee() {
        return page.locator("text=Delivery Fee").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    public String getPlatformFee() {
        return page.locator("text=Platform Fee").first().locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Restaurant section ───────────────────────────────────────────────

    public String getRestaurantNetPayout() {
        return restaurantPayoutHeading().locator("xpath=..").getByText("Net Payout", new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))
                .locator("xpath=..").locator("span").last().innerText().trim();
    }

    // ── Rider section ────────────────────────────────────────────────────

    public String getRiderNetPayout() {
        return riderPayoutHeading().locator("xpath=..").getByText("Net Payout", new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))
                .locator("xpath=..").locator("span").last().innerText().trim();
    }
}
