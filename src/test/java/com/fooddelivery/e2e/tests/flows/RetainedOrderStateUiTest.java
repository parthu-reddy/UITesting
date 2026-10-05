package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.*;
import com.fooddelivery.e2e.pages.common.*;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import java.nio.file.*;
import java.util.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Inspect only the rendered owned history card and tracker before any further checkout. */
@Tag("ui-only")
public class RetainedOrderStateUiTest extends TestBase {
    @Test void historyAndTrackerExposeTheSameOwnedOrderState() throws Exception {
        String id = System.getProperty("retained.order.id", "");
        org.assertj.core.api.Assertions.assertThat(id).matches("[a-f0-9-]{36}");
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", id + ".json")))
                .contains(id).contains(testCustomerPhone);
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        var card = customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + id + "']");
        assertThat(card).isVisible(); card.click();
        var tracker = new CustomerOrderTrackerPage(customerPage, id).tracker();
        assertThat(tracker).isVisible();
        String status = tracker.getAttribute("data-status");
        org.assertj.core.api.Assertions.assertThat(status).isNotBlank();
        Map<String,Object> safe = Map.of("orderId", id, "status", status,
                "historyVisible", true, "trackerVisible", true, "observedAt", java.time.Instant.now().toString(),
                "proof", "rendered owned history card and tracker; no response body inspection");
        Files.writeString(Path.of("target/lifecycle", id + "-state.json"),
                (String) customerPage.evaluate("data => JSON.stringify(data,null,2)", safe));
        System.out.println("[RETAINED ORDER] id=" + id + " renderedStatus=" + status);
    }
}
