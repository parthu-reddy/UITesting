package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Observe the rendered exact-order tracker; never reload, poll APIs manually or sleep. */
public final class AutomaticCustomerProgress {
    private final Locator tracker;
    private final List<Map<String, String>> observations = new ArrayList<>();
    public AutomaticCustomerProgress(Page page, String orderId) {
        tracker = page.locator("[data-testid='order-tracker'][data-order-id='" + orderId + "']");
    }
    public void orderStatus(String status) {
        assertThat(tracker).hasAttribute("data-status", status,
                new com.microsoft.playwright.assertions.LocatorAssertions.HasAttributeOptions().setTimeout(90000));
        observations.add(Map.of("kind", "order", "state", status));
    }
    public void deliveryLabel(String label) {
        assertThat(tracker.getByText(label, new Locator.GetByTextOptions().setExact(true))).isVisible(
                new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(90000));
        observations.add(Map.of("kind", "delivery", "state", label));
    }
    public List<Map<String, String>> observations() { return List.copyOf(observations); }
}
