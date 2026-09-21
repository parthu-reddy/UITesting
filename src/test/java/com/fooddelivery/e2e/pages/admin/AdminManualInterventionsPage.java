package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Page;

/**
 * Page Object for the Admin Manual Interventions panel.
 * Maps to: {@code AdminManualInterventions.tsx}
 * <p>
 * Shows orders stuck in dispatch or requiring manual intervention.
 * </p>
 */
public class AdminManualInterventionsPage {

    private final Page page;

    public AdminManualInterventionsPage(Page page) {
        this.page = page;
    }

    public boolean isInterventionsVisible() {
        return page.locator("text=Manual Intervention, text=Intervention, text=Dispatch").first().isVisible();
    }

    public int getInterventionCount() {
        return page.locator("[data-testid='intervention-row'], .intervention-card").count();
    }

    public void selectIntervention(int index) {
        page.locator("[data-testid='intervention-row'], .intervention-card").nth(index).click();
        page.waitForTimeout(500);
    }

    public void fillCancelReason(String reason) {
        page.locator("textarea").first().fill(reason);
    }

    public void forceCancel() {
        page.locator("button:has-text('Force Cancel'), button:has-text('Cancel Order')").first().click();
        page.waitForTimeout(2000);
    }

    public void forceRedispatch() {
        page.locator("button:has-text('Redispatch'), button:has-text('Force Dispatch')").first().click();
        page.waitForTimeout(2000);
    }
}
