package com.fooddelivery.e2e.pages.customer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object for the Customer Menu View (inside a restaurant).
 * Maps to: {@code CustomerMenuView.tsx, CustomerOutletSelectorModal.tsx}
 * <p>
 * Also covers sub-components rendered within:
 * {@code MenuList.tsx}, {@code MenuItemRow.tsx}, {@code MenuCategoryGroup.tsx},
 * {@code MasterItemForm.tsx}, {@code CategoryTimingPanel.tsx},
 * {@code RestaurantHeader.tsx}, {@code RestaurantStatsBar.tsx}, {@code VegMarker.tsx},
 * {@code CustomerRestaurantCard.tsx}, {@code AddToCartControl.tsx}
 * </p>
 */
public class CustomerMenuViewPage {

    private final Page page;

    public CustomerMenuViewPage(Page page) {
        this.page = page;
    }

    // ── Outlet selection ─────────────────────────────────────────────────

    public void selectOutlet(String outletNamePartial) {
        try {
            page.locator("button#outlet-select, button:has-text('Select Outlet')").first().click();
            // A Modal titled "Select Outlet Location" (CustomerOutletSelectorModal.tsx).
            page.getByRole(com.microsoft.playwright.options.AriaRole.DIALOG,
                            new Page.GetByRoleOptions().setName("Select Outlet Location").setExact(true))
                    .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            page.locator("p:has-text('" + outletNamePartial + "')").first().click();
            page.waitForTimeout(500);
        } catch (Exception e) {
            System.err.println("[CUSTOMER] Failed to select outlet: " + outletNamePartial);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("target/screenshots/select-outlet-timeout.png")));
            throw e;
        }
    }

    /** Current outlet after addQuickPrepItemToCart may have moved to another eligible location. */
    public String getSelectedOutletName() {
        Locator outletHeading = page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Brand \\d+ Outlet \\d+$"))).first();
        outletHeading.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return outletHeading.innerText().trim();
    }

    public String selectNearestOutlet() {
        try {
            // Click change outlet
            page.locator("#outlet-select").first().click();
            page.locator("role=dialog").first()
                    .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            
            // Find all options in the modal
            Locator options = page.locator("role=dialog").first().locator("button:has(p)");
            options.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            
            boolean found = false;
            String selectedOutletName = "";
            for (int i = 0; i < options.count(); i++) {
                String text = options.nth(i).innerText();
                if (text != null) {
                    Matcher m = Pattern.compile("([0-9.]+)\\s*(km|kms)").matcher(text.toLowerCase());
                    if (m.find()) {
                        double distance = Double.parseDouble(m.group(1));
                        if (distance < 4.0) {
                            System.out.println("[CUSTOMER] Selecting outlet with actual distance: " + distance + " km");
                            // The option text contains the outlet name followed by the distance.
                            // We can get just the name by looking at the first <p> which contains it.
                            selectedOutletName = options.nth(i).locator("p").first().innerText().trim();
                            options.nth(i).click();
                            found = true;
                            break;
                        }
                    }
                }
            }
            
            if (!found) {
                System.out.println("[CUSTOMER] No outlet < 4km found, selecting first available.");
                selectedOutletName = options.first().locator("p").first().innerText().trim();
                options.first().click();
            }
            
            page.waitForTimeout(1000);

            System.out.println("[CUSTOMER] Selected outlet: " + selectedOutletName);
            return selectedOutletName;
        } catch (Exception e) {
            System.err.println("[CUSTOMER] Failed to select nearest outlet: " + e.getMessage());
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("target/screenshots/select-nearest-outlet-timeout.png")));
            throw e;
        }
    }

    // ── Adding items to cart ─────────────────────────────────────────────

    /**
     * Adds the first orderable dish whose card shows a prep time under 15 minutes, at the outlet already
     * selected, so the rider sees the trip as soon as the restaurant accepts.
     *
     * <p>No other outlet is tried and no slower dish is taken instead. This used to wander through up to five
     * outlets and then fall back to any dish, so the same test ordered from different outlets at different
     * hours, often away from the waiting rider, and failed much later with an unrelated timeout. Every caller
     * orders from Brand 1 or Brand 2, which the Dev seed makes orderable at every minute at every outlet
     * (RandomDocuments/TimeIndependentOrdering_2026-10-09, Phase 4). A missing dish is therefore a fixture
     * defect, reported here with the outlet and the instant.
     */
    public void addQuickPrepItemToCart() {
        // The menu's heading names the outlet; #outlet-select is the "Change outlet" button, not a name.
        Locator heading = page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Brand \\d+ Outlet \\d+$"))).first();
        String outlet = heading.count() > 0 ? heading.innerText().trim() : "the selected outlet";
        Locator orderable = page.locator("[data-menu-item]:has([data-testid='add-to-cart-button'])");
        try {
            orderable.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        } catch (com.microsoft.playwright.TimeoutError e) {
            throw new AssertionError(noDish(outlet, "no orderable dish"), e);
        }
        Pattern prep = Pattern.compile("(\\d+)\\s*min");
        for (int i = 0; i < orderable.count(); i++) {
            Matcher m = prep.matcher(String.valueOf(orderable.nth(i).textContent()));
            if (m.find() && Integer.parseInt(m.group(1)) < 15) {
                System.out.println("[CUSTOMER] Adding a " + m.group(1) + "-min dish at " + outlet);
                orderable.nth(i).locator("[data-testid='add-to-cart-button']").first().click();
                return;
            }
        }
        throw new AssertionError(noDish(outlet, "no orderable dish under 15 min"));
    }

    private static String noDish(String outlet, String what) {
        return what + " at " + outlet + " at " + java.time.Instant.now() + ". Brand 1 and Brand 2 must be orderable at"
                + " every minute: run RandomDocuments/TimeIndependentOrdering_2026-10-09/tools/validate_time_independence.py"
                + " --phase 4 --live";
    }

    /**
     * Adds a specific item by name.
     */
    public void addItemByName(String itemName) {
        Locator item = page.locator("text=" + itemName).locator("xpath=../..")
                .locator("button:has-text('Add')").first();
        item.click();
        page.waitForTimeout(500);
    }

    public void clickViewCart() {
        page.locator("text=View Cart").click();
        page.waitForTimeout(1000);
    }
}
