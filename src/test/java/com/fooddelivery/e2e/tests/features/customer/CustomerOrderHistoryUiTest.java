package com.fooddelivery.e2e.tests.features.customer;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderHistoryPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
@Tag("customer-history")
public class CustomerOrderHistoryUiTest extends TestBase {

    private CustomerOrderHistoryPage history;

    @BeforeEach
    void openHistory() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.getByTitle("Profile Settings",
                new Page.GetByTitleOptions().setExact(true)).click();
        customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        history = new CustomerOrderHistoryPage(customerPage);
        history.waitForHistoryLoad();
        System.out.println("[HISTORY] customer=" + testCustomerPhone
                + " rows=" + history.getOrderCount()
                + " empty=" + history.emptyState().isVisible());
    }

    @Test
    @DisplayName("HISTORY-01/06: History renders an explicit empty or populated state")
    void historyRendersDefinedState() {
        assertThat(customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true)))
                .hasAttribute("aria-selected", "true");
        assertThat(history.hasDefinedState())
                .as("History must render either order rows or its explicit empty state")
                .isTrue();
        assertThat(customerPage.locator("[data-screen='settings']").innerText())
                .doesNotContain("undefined", "null");
    }

    @Test
    @DisplayName("HISTORY-02/04: Populated history rows expose ID, restaurant, status, and total")
    void populatedRowsHaveRequiredSummaryFields() {
        if (history.getOrderCount() == 0) {
            assertThat(history.emptyState()).isVisible();
            return;
        }

        for (int index = 0; index < history.getOrderCount(); index++) {
            Locator row = history.historyCards().nth(index);
            assertThat(row.locator("span.font-mono"))
                    .hasText(Pattern.compile("^[0-9a-f]{8}$", Pattern.CASE_INSENSITIVE));
            String text = row.innerText();
            assertThat(text).contains("₹").doesNotContain("undefined", "null", "Unknown");
            assertThat(row.locator("h5").innerText()).isNotBlank();
            assertThat(row.locator("[data-variant], span").last().innerText()).isNotBlank();
        }
    }

    @Test
    @DisplayName("HISTORY-05: Selecting a history row opens that exact order tracker")
    void historyRowOpensExactOrderTrackerWhenHistoryExists() {
        if (history.getOrderCount() == 0) {
            assertThat(history.emptyState()).isVisible();
            return;
        }

        String shortId = history.historyCards().first().locator("span.font-mono").innerText().trim();
        history.clickOrderCard(0);

        assertThat(customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true))).isHidden();
        assertThat(customerPage.locator("span.font-mono",
                new Page.LocatorOptions().setHasText("#" + shortId)).first()).isVisible();
    }
}
