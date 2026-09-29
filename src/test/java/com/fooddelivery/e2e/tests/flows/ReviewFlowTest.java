package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.regex.Pattern;

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
        assertThat(reviewDialog.getByText(
                "Your review is in. Reviews can't be changed once submitted, so this is final.",
                new Locator.GetByTextOptions().setExact(true))).isVisible();

        // Reopen from Order History to verify the ratings came back from ReviewsService and are no
        // longer editable, rather than merely leaving the modal in its local success state.
        reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Done").setExact(true)).click();
        customerPage.getByRole(AriaRole.COMPLEMENTARY,
                        new Page.GetByRoleOptions().setName("Customer navigation").setExact(true))
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Orders").setExact(true)).click();
        Locator historyOrder = customerPage.getByTestId("customer-history-order")
                .filter(new Locator.FilterOptions().setHasText(deliveredOrder.shortOrderId()));
        historyOrder.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        historyOrder.locator("..").getByTestId("rate-order-prompt").click();

        reviewDialog = customerPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Rate your order").setExact(true));
        assertThat(reviewDialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Already reviewed").setExact(true))).isVisible();
        assertThat(reviewDialog.locator("[role='img']")).hasCount(3);
        assertThat(reviewDialog.locator("[role='radiogroup']")).hasCount(0);
        assertThat(reviewDialog.getByRole(AriaRole.TEXTBOX)).hasCount(0);
        assertThat(reviewDialog.locator("button:has-text('Submit')")).hasCount(0);

        reviewAsRestaurant(deliveredOrder);
        reviewAsDeliveryPartner(deliveredOrder);
    }

    private void reviewAsRestaurant(CompletedDeliveryFixture.Result deliveredOrder) {
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.openSettingsTab();
        restaurantPage.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Order History").setExact(true)).click();

        Locator historyRow = restaurantPage.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(deliveredOrder.shortOrderId()));
        historyRow.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        historyRow.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Details").setExact(true)).click();

        Locator orderDetails = restaurantOrderDetails(deliveredOrder);
        orderDetails.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        Locator reviewDialog = openPartnerReview(restaurantPage, orderDetails);
        submitAndVerifyReadOnly(restaurantPage, reviewDialog,
                List.of("Customer", "Delivery partner"),
                "E2E restaurant review verification.");
    }

    private void reviewAsDeliveryPartner(CompletedDeliveryFixture.Result deliveredOrder) {
        new DeliveryDashboardPage(riderPage).openHistoryTab();
        riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));

        Locator historyCard = riderPage.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions()
                        .setHasText("ORDER #" + deliveredOrder.shortOrderId().toUpperCase()));
        historyCard.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        historyCard.click();

        Locator orderDetails = riderOrderDetails(deliveredOrder);
        orderDetails.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        Locator reviewDialog = openPartnerReview(riderPage, orderDetails);
        submitAndVerifyReadOnly(riderPage, reviewDialog,
                List.of("Customer", "Restaurant"),
                "E2E delivery-partner review verification.");
    }

    private Locator restaurantOrderDetails(CompletedDeliveryFixture.Result deliveredOrder) {
        return restaurantPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(orderDialogName(deliveredOrder)));
    }

    private Locator riderOrderDetails(CompletedDeliveryFixture.Result deliveredOrder) {
        return riderPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(orderDialogName(deliveredOrder)));
    }

    private Pattern orderDialogName(CompletedDeliveryFixture.Result deliveredOrder) {
        return Pattern.compile("^Order #" + Pattern.quote(deliveredOrder.shortOrderId()) + "$",
                Pattern.CASE_INSENSITIVE);
    }

    private Locator openPartnerReview(Page page, Locator orderDetails) {
        Locator reviewPrompt = orderDetails.getByTestId("rate-order-prompt");
        assertThat(reviewPrompt).isVisible();
        reviewPrompt.click();
        Locator reviewDialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Review this delivery").setExact(true));
        reviewDialog.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return reviewDialog;
    }

    private void submitAndVerifyReadOnly(Page page, Locator reviewDialog,
                                         List<String> expectedTargets, String comment) {
        Locator ratingGroups = reviewDialog.locator("[role='radiogroup']");
        assertThat(ratingGroups).hasCount(expectedTargets.size());
        for (String target : expectedTargets) {
            assertThat(reviewDialog.getByText(target,
                    new Locator.GetByTextOptions().setExact(true))).isVisible();
        }
        for (int index = 0; index < ratingGroups.count(); index++) {
            ratingGroups.nth(index).getByRole(AriaRole.RADIO,
                    new Locator.GetByRoleOptions().setName("5 stars").setExact(true)).click();
        }
        reviewDialog.getByRole(AriaRole.TEXTBOX).first().fill(comment);
        reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Submit " + expectedTargets.size() + " reviews").setExact(true)).click();
        assertThat(reviewDialog.getByText(
                "Your review is in. Reviews can't be changed once submitted, so this is final.",
                new Locator.GetByTextOptions().setExact(true))).isVisible();

        reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Done").setExact(true)).click();
        Locator orderDetails = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Order #[0-9a-f]{8}$",
                        Pattern.CASE_INSENSITIVE)));
        orderDetails.getByTestId("rate-order-prompt").click();
        reviewDialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Review this delivery").setExact(true));

        assertThat(reviewDialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Already reviewed").setExact(true))).isVisible();
        assertThat(reviewDialog.locator("[role='img']")).hasCount(expectedTargets.size());
        assertThat(reviewDialog.locator("[role='radiogroup']")).hasCount(0);
        assertThat(reviewDialog.getByRole(AriaRole.TEXTBOX)).hasCount(0);
        assertThat(reviewDialog.locator("button:has-text('Submit')")).hasCount(0);
    }
}
