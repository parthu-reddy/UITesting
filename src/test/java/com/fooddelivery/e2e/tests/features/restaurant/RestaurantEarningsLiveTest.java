package com.fooddelivery.e2e.tests.features.restaurant;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * EARNINGS-01..04, 09: the outlet's Earnings tab shows the money the platform actually owes it.
 *
 * <p>{@code -Dearnings.outlet}, plus the figures read from the databases for the current month:
 * {@code -Dearnings.expected.net} (quoted payouts of orders delivered this month),
 * {@code -Dearnings.expected.clawbacks} (restaurant-fault clawbacks booked this month) and
 * {@code -Dearnings.expected.pending} (the outlet's unsettled ledger balance). Read-only.
 * Independently of those inputs: with no payout yet, Pending = Net - Clawbacks, and the statement's
 * signed lines add up to Pending.
 */
@Tag("money")
public class RestaurantEarningsLiveTest extends TestBase {

    @Test
    @DisplayName("EARNINGS-01..04/09: the Earnings tab shows the ledger's figures and a statement that adds up")
    void earningsMatchTheLedger() {
        String outlet = System.getProperty("earnings.outlet", "").trim();
        Assumptions.assumeFalse(outlet.isEmpty(), "needs -Dearnings.outlet and the expected figures");
        BigDecimal net = new BigDecimal(System.getProperty("earnings.expected.net"));
        BigDecimal clawbacks = new BigDecimal(System.getProperty("earnings.expected.clawbacks"));
        BigDecimal pending = new BigDecimal(System.getProperty("earnings.expected.pending"));

        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        dashboard.selectOutlet(outlet);
        dashboard.openEarningsTab();

        // EARNINGS-01..04: the tab stays open and every card shows a real rupee figure, never "—" or a placeholder.
        assertThat(card("Net Earnings")).hasText(inr(net));
        assertThat(card("Clawbacks")).hasText(inr(clawbacks));
        assertThat(card("Pending Balance")).hasText(inr(pending));
        assertThat(restaurantPage.getByText("Net Earnings", new Page.GetByTextOptions().setExact(true))).isVisible();
        org.assertj.core.api.Assertions.assertThat(net.subtract(clawbacks))
                .as("with no payout yet, pending = net - clawbacks").isEqualByComparingTo(pending);

        // EARNINGS-09: the statement is the ledger, line by line, and its signed lines add up to the balance.
        Locator rows = restaurantPage.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions()
                .setHasText(java.util.regex.Pattern.compile("[+-]₹[0-9,]+\\.[0-9]{2}")));
        rows.first().waitFor();
        BigDecimal sum = BigDecimal.ZERO;
        for (String text : rows.allInnerTexts()) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("([+-])₹([0-9,]+\\.[0-9]{2})").matcher(text);
            org.assertj.core.api.Assertions.assertThat(m.find()).as("signed amount in %s", text).isTrue();
            BigDecimal amount = new BigDecimal(m.group(2).replace(",", ""));
            sum = "-".equals(m.group(1)) ? sum.subtract(amount) : sum.add(amount);
        }
        org.assertj.core.api.Assertions.assertThat(rows.count()).as("one page holds the whole statement for this fixture").isLessThanOrEqualTo(20);
        org.assertj.core.api.Assertions.assertThat(sum).as("statement lines add up to the pending balance").isEqualByComparingTo(pending);
    }

    /** The value under a summary card's label. */
    private Locator card(String label) {
        return restaurantPage.getByText(label, new Page.GetByTextOptions().setExact(true)).locator("..").locator("h3");
    }

    private static String inr(BigDecimal amount) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("₹#,##0.00");
        return format.format(amount);
    }
}
