package com.fooddelivery.e2e.pages.admin;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

/**
 * Narrow page object for the browser-routed Live Operations dispatch safety fixture.
 *
 * <p>Live Operations only presents scoped rider telemetry. It must not provide a direct
 * assignment mutation; an operator starts any manual dispatch exception in Manual Interventions.</p>
 */
public final class AdminDispatchSafetyPage {

    private final Page page;

    public AdminDispatchSafetyPage(Page page) {
        this.page = page;
    }

    public void waitForFixtureOrders(String firstOrderId, String secondOrderId) {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Active Orders").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        orderButton(firstOrderId).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        orderButton(secondOrderId).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void selectOrder(String orderId) {
        orderButton(orderId).click();
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions()
                                .setName("Order #" + shortId(orderId))
                                .setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public String availableDriverHeading() {
        return availableDriversHeading().innerText().trim();
    }

    public boolean isDriverVisible(String driverName) {
        Locator driver = page.getByText(driverName, new Page.GetByTextOptions().setExact(true));
        return driver.count() > 0 && driver.first().isVisible();
    }

    public boolean hasDirectAssignmentAction() {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Assign").setExact(true)).count() > 0;
    }

    /** Waits for the current order's location-scoped rider to appear in the read-only panel. */
    public void waitForNearbyDriver(String driverName) {
        page.getByText(driverName, new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void openManualInterventions() {
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Open Manual Interventions").setExact(true))
                .click();
    }

    public void waitForManualInterventions() {
        page.getByRole(AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName("Manual Interventions").setExact(true))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    private Locator orderButton(String orderId) {
        return page.locator("button:has(p:has-text('#" + shortId(orderId) + "'))").first();
    }

    private Locator availableDriversHeading() {
        return page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Nearby Ready Drivers \\(\\d+\\)$")));
    }

    private static String shortId(String id) {
        return id.substring(0, Math.min(8, id.length()));
    }
}
