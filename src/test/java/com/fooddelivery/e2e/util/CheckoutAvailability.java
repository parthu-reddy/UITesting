package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;

/**
 * Starts checkout through the cart and distinguishes unavailable delivery from checkout defects.
 */
public final class CheckoutAvailability {

    private CheckoutAvailability() {}

    public static Response clickCheckoutAndWaitForAvailability(Page page) {
        Locator cart = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Your cart").setExact(true));
        Locator checkout = cart.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Checkout").setExact(true));
        return page.waitForResponse(
                response -> response.request().method().equals("GET")
                        && response.url().contains("/api/v1/restaurants/")
                        && response.url().contains("/delivery-availability"),
                checkout::click);
    }

    public static void requireDeliveryAvailable(Response response) {
        String body = response.text().trim();
        boolean explicitUnavailable = body.equalsIgnoreCase("false")
                || body.matches("(?s).*\"data\"\\s*:\\s*false.*");
        Assumptions.assumeTrue(response.status() != 409 && !explicitUnavailable,
                "Checkout is unavailable in the seeded environment (HTTP "
                        + response.status() + "): " + body.substring(0, Math.min(body.length(), 240)));
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("delivery availability request should succeed")
                .isBetween(200, 299);
    }
}
