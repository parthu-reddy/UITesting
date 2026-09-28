package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Tests the post-delivery review submission flow.
 */
@Tag("flow")
public class ReviewFlowTest extends TestBase {

    @Test
    @DisplayName("Submit star rating and comment after delivery")
    void submitReview() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();

        CustomerDashboardPage.openProfileSettings(customerPage);
        Locator historyTab = customerPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("History").setExact(true));
        historyTab.click();

        Locator loading = customerPage.getByText("Loading history...",
                new Page.GetByTextOptions().setExact(true));
        Locator empty = customerPage.getByText("No order history found.",
                new Page.GetByTextOptions().setExact(true));
        Locator historyOrders = customerPage.locator("[data-testid='customer-history-order']");
        customerPage.waitForCondition(
                () -> historyOrders.count() > 0 || empty.isVisible() || loading.isVisible(),
                new Page.WaitForConditionOptions().setTimeout(15000));
        if (loading.isVisible()) {
            loading.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN)
                    .setTimeout(20000));
        }

        Assumptions.assumeTrue(historyOrders.count() > 0,
                "The seeded customer has no delivered order history to review");
        Locator rateOrder = customerPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Rate this order").setExact(true));
        Assumptions.assumeTrue(rateOrder.count() > 0,
                "The seeded customer has no delivered order eligible for a review");
        rateOrder.first().click();

        Locator reviewDialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Rate your order").setExact(true));
        assertThat(reviewDialog).isVisible();
        Locator ratingGroup = reviewDialog.locator("[role='radiogroup']").first();
        assertThat(ratingGroup).isVisible();
        ratingGroup.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName("5 stars").setExact(true)).click();
        reviewDialog.getByRole(AriaRole.TEXTBOX).first().fill("Great food and fast delivery!");

        Locator submit = reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Submit \\d+ reviews?$")));
        assertThat(submit).isEnabled();
        submit.click();
        assertThat(reviewDialog.getByText("Thanks for that", new Locator.GetByTextOptions().setExact(true)))
                .isVisible();
        assertThat(reviewDialog.getByText("Your review is in.", new Locator.GetByTextOptions().setExact(true)))
                .isVisible();
    }
}
