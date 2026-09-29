package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.CompletedDeliveryFixture;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Tests the post-delivery review submission flow for every entity connected to the order.
 */
@Tag("flow")
public class ReviewFlowTest extends TestBase {

    @Test
    @DisplayName("Review the restaurant, delivery partner, and dish after delivery")
    void submitReview() {
        String existingOrderId = System.getProperty("review.order.id");
        boolean createOrder = existingOrderId == null || existingOrderId.isBlank();
        boolean allowExistingRiderSubmission = !createOrder
                && Boolean.parseBoolean(System.getProperty("review.submit.rider.existing", "false"));
        String outletName = createOrder ? null : System.getProperty("review.outlet.name");
        if (!createOrder && (outletName == null || outletName.isBlank())) {
            throw new IllegalArgumentException("Set -Dreview.outlet.name when reusing -Dreview.order.id");
        }
        CompletedDeliveryFixture.Result deliveredOrder = createOrder
                ? CompletedDeliveryFixture.completeOrder(
                        customerPage, restaurantPage, riderPage,
                        testCustomerPhone, testRestaurantPhone, testRiderPhone)
                : new CompletedDeliveryFixture.Result(existingOrderId, outletName);

        if (createOrder) {
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

            // Reopen from Order History to verify the persisted review is read-only.
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
            waitForEligibilityResult(customerPage, reviewDialog);
            assertReadOnly(reviewDialog, 3);
        }

        // Reusing a delivered order is verification-only by default. Rider submission can be
        // explicitly enabled for a known order to finish the rider role's one-time review flow.
        reviewAsRestaurant(deliveredOrder, !createOrder, createOrder);
        reviewAsDeliveryPartner(deliveredOrder, !createOrder,
                createOrder || allowExistingRiderSubmission);
    }

    private void reviewAsRestaurant(CompletedDeliveryFixture.Result deliveredOrder, boolean login,
                                    boolean allowSubmission) {
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        AtomicReference<Integer> restaurantEarningsStatus = new AtomicReference<>();
        restaurantPage.onResponse(response -> {
            if (response.url().contains("/api/v1/money/restaurant/")
                    && response.url().contains("/orders/")) {
                restaurantEarningsStatus.set(response.status());
            }
            if (response.status() >= 500
                    && response.url().contains("/api/v1/money/restaurant/")
                    && response.url().contains("/orders/")) {
                try {
                    String body = response.text();
                    System.out.println("[RESTAURANT EARNINGS SERVER ERROR] "
                            + body.substring(0, Math.min(body.length(), 1200)));
                } catch (RuntimeException unreadableBody) {
                    System.out.println("[RESTAURANT EARNINGS SERVER ERROR] response body unavailable");
                }
            }
        });
        if (login) {
            restaurantPage.navigate(TestConfig.APP_URL);
            new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
            dashboard.waitForDashboard();
        }
        dashboard.selectOutlet(deliveredOrder.outletName());
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
        restaurantPage.waitForCondition(() -> restaurantEarningsStatus.get() != null,
                new Page.WaitForConditionOptions().setTimeout(15000));
        org.assertj.core.api.Assertions.assertThat(restaurantEarningsStatus.get())
                .as("restaurant order earnings must load instead of showing a zero-value fallback")
                .isBetween(200, 299);
        Locator reviewDialog = openPartnerReview(restaurantPage, orderDetails);
        submitAndVerifyReadOnly(restaurantPage, reviewDialog,
                List.of("Customer", "Delivery partner"),
                "E2E restaurant review verification.", deliveredOrder, allowSubmission);
    }

    private void reviewAsDeliveryPartner(CompletedDeliveryFixture.Result deliveredOrder, boolean login,
                                         boolean allowSubmission) {
        if (login) {
            riderPage.navigate(TestConfig.APP_URL);
            new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
            new DeliveryDashboardPage(riderPage).waitForDashboard();
        }
        if (Boolean.parseBoolean(System.getProperty("review.require-rider-offline", "false"))) {
            org.assertj.core.api.Assertions.assertThat(
                            new DeliveryOnlineTogglePage(riderPage).isOffline())
                    .as("review history is available while the rider is off duty")
                    .isTrue();
        }
        AtomicReference<Integer> historyResponseStatus = new AtomicReference<>();
        AtomicReference<String> historyResponseBody = new AtomicReference<>();
        riderPage.onResponse(response -> {
            if ("GET".equals(response.request().method())
                    && response.url().contains("/api/v1/delivery/orders/history")) {
                historyResponseStatus.set(response.status());
                try {
                    historyResponseBody.set(response.text());
                } catch (RuntimeException unreadableBody) {
                    historyResponseBody.set("<unreadable response body>");
                }
            }
        });
        new DeliveryDashboardPage(riderPage).openHistoryTab();
        riderPage.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Completed Deliveries").setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));

        riderPage.waitForCondition(() -> historyResponseStatus.get() != null,
                new Page.WaitForConditionOptions().setTimeout(15000));
        org.assertj.core.api.Assertions.assertThat(historyResponseStatus.get())
                .as("rider history API response for delivered order " + deliveredOrder.orderId())
                .isBetween(200, 299);
        org.assertj.core.api.Assertions.assertThat(historyResponseBody.get())
                .as("rider history response must include the completed order")
                .contains(deliveredOrder.orderId());

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
                "E2E delivery-partner review verification.", deliveredOrder, allowSubmission);
    }

    private Locator restaurantOrderDetails(CompletedDeliveryFixture.Result deliveredOrder) {
        return restaurantPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(orderDialogName(deliveredOrder)).setExact(true));
    }

    private Locator riderOrderDetails(CompletedDeliveryFixture.Result deliveredOrder) {
        return riderPage.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName(orderDialogName(deliveredOrder)).setExact(true));
    }

    private String orderDialogName(CompletedDeliveryFixture.Result deliveredOrder) {
        return "Order #" + deliveredOrder.shortOrderId();
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
                                         List<String> expectedTargets, String comment,
                                         CompletedDeliveryFixture.Result deliveredOrder,
                                         boolean allowSubmission) {
        waitForEligibilityResult(page, reviewDialog);
        if (!allowSubmission) {
            // Existing deployed fixtures are shared and review rows are immutable. Reused-order
            // runs prove the saved state only; they must not submit if any target is still pending.
            assertReadOnly(reviewDialog, expectedTargets.size());
            return;
        }
        Locator ratingGroups = reviewDialog.locator("[role='radiogroup']");
        if (ratingGroups.count() == 0) {
            assertReadOnly(reviewDialog, expectedTargets.size());
            return;
        }
        assertThat(ratingGroups).hasCount(expectedTargets.size());
        List<String> visibleTargets = new java.util.ArrayList<>();
        for (int index = 0; index < ratingGroups.count(); index++) {
            Locator targetCard = ratingGroups.nth(index).locator("xpath=../..");
            String category = targetCard.locator("span.inline-flex").first().textContent();
            visibleTargets.add(category == null ? "" : category.trim());
        }
        org.assertj.core.api.Assertions.assertThat(visibleTargets)
                .containsExactlyInAnyOrderElementsOf(expectedTargets);
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
                new Page.GetByRoleOptions().setName(orderDialogName(deliveredOrder)).setExact(true));
        orderDetails.getByTestId("rate-order-prompt").click();
        reviewDialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Review this delivery").setExact(true));

        waitForEligibilityResult(page, reviewDialog);
        assertReadOnly(reviewDialog, expectedTargets.size());
    }

    private void waitForEligibilityResult(Page page, Locator reviewDialog) {
        Locator alreadyReviewed = reviewDialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Already reviewed").setExact(true));
        Locator retry = reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Try again").setExact(true));
        Locator refusalClose = reviewDialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true));
        Locator emptyState = reviewDialog.getByText("There's nothing to review for this order yet.",
                new Locator.GetByTextOptions().setExact(true));
        page.waitForCondition(() -> reviewDialog.locator("[role='radiogroup']").count() > 0
                        || reviewDialog.locator("[role='img']").count() > 0
                        || alreadyReviewed.count() > 0
                        || retry.count() > 0
                        || refusalClose.count() > 0
                        || emptyState.count() > 0,
                new Page.WaitForConditionOptions().setTimeout(15000));
    }

    private void assertReadOnly(Locator reviewDialog, int expectedTargets) {
        assertThat(reviewDialog.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Already reviewed").setExact(true))).isVisible();
        assertThat(reviewDialog.locator("[role='img']")).hasCount(expectedTargets);
        assertThat(reviewDialog.locator("[role='radiogroup']")).hasCount(0);
        assertThat(reviewDialog.getByRole(AriaRole.TEXTBOX)).hasCount(0);
        assertThat(reviewDialog.locator("button:has-text('Submit')")).hasCount(0);
    }
}
