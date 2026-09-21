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
            page.locator("text=Select Outlet Location, text=Select Outlet").first()
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
     * Adds the first item with preparation time less than 15 minutes to the cart.
     * This ensures the rider gets assigned immediately during E2E tests.
     */
    public void addQuickPrepItemToCart() {
        // Try up to 5 times (checking different outlets if needed)
        for (int attempt = 0; attempt < 5; attempt++) {
            Locator dishControls = page.locator("[data-menu-item]");
            try {
                dishControls.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
            } catch (Exception e) {
                System.out.println("[CUSTOMER] No menu items visible on attempt " + attempt);
            }

            boolean foundAnyOrderable = false;
            for (int i = 0; i < dishControls.count(); i++) {
                Locator control = dishControls.nth(i);
                
                // Skip items that are not orderable (no ADD button)
                if (control.locator("[data-testid='add-to-cart-button']").count() == 0) {
                    continue;
                }
                
                foundAnyOrderable = true;
                String text = control.textContent();
                if (text != null && text.contains("min")) {
                    Matcher m = Pattern.compile("(\\d+)\\s*min").matcher(text);
                    if (m.find()) {
                        int mins = Integer.parseInt(m.group(1));
                        if (mins < 15) {
                            System.out.println("[CUSTOMER] Found item with prep time: " + mins + " min");
                            control.locator("[data-testid='add-to-cart-button']").first().click();
                            page.waitForTimeout(500);
                            return;
                        }
                    }
                }
            }

            if (foundAnyOrderable) {
                // We found orderable items, but none with < 15 min. Just add the first available.
                System.out.println("[CUSTOMER] No item with <15 min prep time found, but items are available. Adding first available.");
                page.locator("[data-testid='add-to-cart-button']").first().click();
                page.waitForTimeout(500);
                return;
            }

            System.out.println("[CUSTOMER] No orderable items at this outlet. Trying another outlet...");
            try {
                page.locator("#outlet-select").first().click();
                page.locator("role=dialog").first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                Locator options = page.locator("role=dialog").first().locator("button:has(p)");
                options.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                
                // Pick the next outlet < 4km that we haven't tried (we can just pick randomly or use attempt index)
                // For simplicity, pick the (attempt + 1)th outlet < 4km
                int validOutletsFound = 0;
                boolean selectedNew = false;
                for (int i = 0; i < options.count(); i++) {
                    String text = options.nth(i).innerText();
                    if (text != null) {
                        Matcher m = Pattern.compile("([0-9.]+)\\s*(km|kms)").matcher(text.toLowerCase());
                        if (m.find()) {
                            double distance = Double.parseDouble(m.group(1));
                            if (distance < 4.0) {
                                if (validOutletsFound == attempt + 1) { // skip the ones we already tried
                                    System.out.println("[CUSTOMER] Selecting alternative outlet: " + distance + " km");
                                    options.nth(i).click();
                                    selectedNew = true;
                                    break;
                                }
                                validOutletsFound++;
                            }
                        }
                    }
                }
                
                if (!selectedNew) {
                    System.out.println("[CUSTOMER] No more alternative outlets < 4km found.");
                    break; // stop trying
                }
                page.waitForTimeout(2000); // wait for new menu to load
            } catch (Exception e) {
                System.out.println("[CUSTOMER] Failed to switch outlet during retry: " + e.getMessage());
                break;
            }
        }

        throw new RuntimeException("Could not find any orderable items across available outlets.");
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

    public boolean isCartPopupVisible() {
        return page.locator("text=Items Added, text=View Cart").first().isVisible();
    }

    public void clickViewCart() {
        page.locator("text=View Cart").click();
        page.waitForTimeout(1000);
    }
}
