package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Tests the post-delivery review submission flow for every entity connected to the order.
 */
@Tag("flow")
public class ReviewFlowTest extends TestBase {

    @Test
    @DisplayName("Review the restaurant, delivery partner, and dish after delivery")
    void submitReview() {
        CompletedDeliveryFixture.Result deliveredOrder = CompletedDeliveryFixture.completeOrder(
                customerPage, restaurantPage, riderPage,
                testCustomerPhone, testRestaurantPhone, testRiderPhone);

        Locator deliveredTracker = customerPage.locator(
                "[data-testid='order-tracker'][data-order-id='" + deliveredOrder.orderId() + "']");
        assertThat(deliveredTracker.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Order delivered").setExact(true))).isVisible();
        Locator rateOrder = deliveredTracker.getByTestId("rate-order-prompt");
        assertThat(rateOrder).isVisible();
        rateOrder.click();

        Locator reviewDialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Rate your order").setExact(true));
        assertThat(reviewDialog).isVisible();
        assertThat(reviewDialog.getByText(deliveredOrder.outletName(),
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        assertThat(reviewDialog.getByText("Delivery partner",
                new Locator.GetByTextOptions().setExact(true))).isVisible();

        // This fixture orders one dish and completes a rider delivery, so the order has exactly
        // three customer-reviewable targets: outlet, delivery partner, and dish.
        Locator ratingGroups = reviewDialog.locator("[role='radiogroup']");
        assertThat(ratingGroups).hasCount(3);
        for (int index = 0; index < ratingGroups.count(); index++) {
            Locator ratingGroup = ratingGroups.nth(index);
            assertThat(ratingGroup).isVisible();
            ratingGroup.getByRole(AriaRole.RADIO,
                    new Locator.GetByRoleOptions().setName("5 stars").setExact(true)).click();
        }
        // Mark the generated reviews so they are recognizable if the development review feed is inspected.
        reviewDialog.getByRole(AriaRole.TEXTBOX).first().fill("E2E automated review verification.");

        Locator submit = reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit 3 reviews").setExact(true));
        assertThat(submit).isEnabled();
        submit.click();
        assertThat(reviewDialog.getByText("Thanks for that", new Locator.GetByTextOptions().setExact(true)))
                .isVisible();
        assertThat(reviewDialog.getByText("Your review is in.", new Locator.GetByTextOptions().setExact(true)))
                .isVisible();
    }
}
