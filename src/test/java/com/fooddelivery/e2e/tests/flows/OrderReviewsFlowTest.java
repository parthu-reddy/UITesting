package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * REVIEW-01..05: the three participants of one delivered order review each other through the real
 * dialogs. Each sees exactly the targets its role may review (customer: restaurant, delivery partner,
 * dishes; restaurant: customer, delivery partner; rider: customer, restaurant), submits, and on reopening
 * finds its reviews read-only. Every dialog is checked against the server's eligibility for that actor.
 *
 * <p>{@code -Dreview.order.id}: an owned delivered order inside the 14-day review window with no
 * reviews yet; manifest in target/lifecycle. Reviews are immutable, so this runs once per order. The
 * customer's restaurant comment is written to target/lifecycle/{order}-reviews.json for REVIEW-AGG-01.
 */
@Tag("flow") @Tag("review")
public class OrderReviewsFlowTest extends TestBase {
    private static final String ORDER = System.getProperty("review.order.id", "").trim();

    @Test
    @DisplayName("REVIEW-01..05: customer, restaurant and rider review each other once, then see it read-only")
    void participantsReviewEachOther() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "needs -Dreview.order.id");
        String manifest = Files.readString(Path.of("target/lifecycle", ORDER + ".json"));
        org.assertj.core.api.Assertions.assertThat(manifest).contains("\"" + ORDER + "\"")
                .contains(testCustomerPhone).contains(testRestaurantPhone).contains(testRiderPhone);
        java.util.regex.Matcher outletValue = Pattern.compile("\"outlet\"\\s*:\\s*\"([^\"]+)\"").matcher(manifest);
        org.assertj.core.api.Assertions.assertThat(outletValue.find()).as("manifest names the outlet").isTrue();
        String outlet = outletValue.group(1);
        String short8 = ORDER.substring(0, 8);
        String restaurantComment = "E2E review " + short8 + ": hot and on time";

        // Customer: restaurant 4 with a comment, delivery partner 5, dishes left unrated (they stay open).
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).loginAs("Order Food", testCustomerPhone);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']").click();
        assertThat(new CustomerOrderTrackerPage(customerPage, ORDER).tracker()).isVisible();
        review(customerPage, new CustomerOrderTrackerPage(customerPage, ORDER).tracker(), "CUSTOMER", "Rate your order", Set.of("RESTAURANT", "DRIVER", "PRODUCT"),
                Map.of("RESTAURANT", new Rating(4, restaurantComment), "DRIVER", new Rating(5, null)));

        // Restaurant: the customer 5, from the outlet's order history.
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).loginAs("Restaurant Partner", testRestaurantPhone);
        RestaurantDashboardPage restaurant = new RestaurantDashboardPage(restaurantPage);
        restaurant.waitForDashboard();
        restaurant.selectOutlet(outlet);
        restaurant.openSettingsTab();
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Order History").setExact(true)).click();
        restaurantPage.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(short8 + "..."))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Details")).click();
        review(restaurantPage, detailsDialog(restaurantPage, short8), "RESTAURANT", "Review this delivery", Set.of("CUSTOMER", "DRIVER"),
                Map.of("CUSTOMER", new Rating(5, null)));

        // Rider, off duty: the restaurant 4, from Completed Deliveries.
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).loginAs("Delivery Executive", testRiderPhone);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        org.assertj.core.api.Assertions.assertThat(new DeliveryOnlineTogglePage(riderPage).isOffline())
                .as("the rider reviews from history while off duty; duty is not changed").isTrue();
        riderPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Trips Completed"))).click();
        riderPage.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("ORDER #" + short8)).first().click();
        review(riderPage, detailsDialog(riderPage, short8), "DELIVERY", "Review this delivery", Set.of("CUSTOMER", "RESTAURANT"),
                Map.of("RESTAURANT", new Rating(4, null)));

        Files.writeString(Path.of("target/lifecycle", ORDER + "-reviews.json"),
                "{\"orderId\":\"" + ORDER + "\",\"outlet\":\"" + outlet + "\",\"restaurantComment\":\"" + restaurantComment
                        + "\",\"reviews\":4,\"newOrderCreated\":false}");
    }

    private record Rating(int stars, String comment) {}

    /** Opens the actor's review dialog, checks it against eligibility, submits, and checks the read-only reopen. */
    /** The open order-details dialog for this order; its review prompt belongs to this order only. */
    private static Locator detailsDialog(Page page, String short8) {
        Locator dialog = page.getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions().setHasText(Pattern.compile(short8, Pattern.CASE_INSENSITIVE)));
        assertThat(dialog).hasCount(1);
        return dialog;
    }

    private void review(Page page, Locator scope, String actorRole, String title, Set<String> allowedTypes, Map<String, Rating> ratings) {
        List<Map<?, ?>> before = targets(page, actorRole);
        org.assertj.core.api.Assertions.assertThat(before).as("%s has something to review", actorRole).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(before.stream().map(t -> (String) t.get("entityType")).collect(Collectors.toSet()))
                .as("%s's role matrix", actorRole).isEqualTo(allowedTypes);
        org.assertj.core.api.Assertions.assertThat(before).allMatch(t -> Boolean.FALSE.equals(t.get("alreadyReviewed")));

        scope.getByTestId("rate-order-prompt").click();
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName(title));
        Locator groups = dialog.getByRole(AriaRole.RADIOGROUP);
        assertThat(groups).hasCount(before.size());
        Map<String, String> nameByType = new LinkedHashMap<>();
        for (Map<?, ?> target : before) {
            String name = uiName(target);
            assertThat(dialog.getByRole(AriaRole.RADIOGROUP, new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"))).isVisible();
            nameByType.putIfAbsent((String) target.get("entityType"), name);
        }
        // REVIEW-06 on live data: nothing can be submitted until something is rated.
        assertThat(dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Pick a rating to continue").setExact(true))).isDisabled();

        ratings.forEach((type, rating) -> {
            String name = nameByType.get(type);
            dialog.getByRole(AriaRole.RADIOGROUP, new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"))
                    .getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName(rating.stars() + " star" + (rating.stars() == 1 ? "" : "s")).setExact(true))
                    .click();
            if (rating.comment() != null) {
                dialog.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Comment about " + name)).fill(rating.comment());
            }
        });
        String submitName = "Submit " + ratings.size() + " review" + (ratings.size() == 1 ? "" : "s");
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(submitName).setExact(true)).click();
        assertThat(dialog.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Thanks for that"))).isVisible();
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Done").setExact(true)).click();

        // The server recorded exactly what was submitted, and nothing else.
        List<Map<?, ?>> after = targets(page, actorRole);
        for (Map<?, ?> target : after) {
            Rating expected = ratings.get((String) target.get("entityType"));
            boolean reviewedNow = expected != null && uiName(target).equals(nameByType.get(target.get("entityType")));
            org.assertj.core.api.Assertions.assertThat(target.get("alreadyReviewed")).as("%s reviewed", target.get("displayName")).isEqualTo(reviewedNow);
            if (reviewedNow) {
                org.assertj.core.api.Assertions.assertThat(((Number) target.get("existingRating")).intValue()).isEqualTo(expected.stars());
                if (expected.comment() != null) org.assertj.core.api.Assertions.assertThat(target.get("existingComment")).isEqualTo(expected.comment());
            }
        }

        // Reopened, the submitted reviews are read-only; the unrated targets are still offered.
        scope.getByTestId("rate-order-prompt").click();
        Locator reopened = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName(title));
        assertThat(reopened.getByText("Already reviewed", new Locator.GetByTextOptions().setExact(true))).isVisible();
        ratings.keySet().forEach(type -> assertThat(reopened.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + nameByType.get(type) + " out of 5 stars"))).hasCount(0));
        assertThat(reopened.getByRole(AriaRole.RADIOGROUP)).hasCount(before.size() - ratings.size());
        ratings.values().stream().filter(r -> r.comment() != null)
                .forEach(r -> assertThat(reopened.getByText(r.comment())).isVisible());
        page.keyboard().press("Escape");
    }

    /** The dialog's label for a target: a driver the server names only generically is "Your delivery partner" (RateOrderModal.targetDisplayName). */
    private static String uiName(Map<?, ?> target) {
        String name = (String) target.get("displayName");
        return "DRIVER".equals(target.get("entityType")) && "Delivery partner".equals(name) ? "Your delivery partner" : name;
    }

    @SuppressWarnings("unchecked")
    private List<Map<?, ?>> targets(Page page, String actorRole) {
        Map<?, ?> result = (Map<?, ?>) page.evaluate("""
                async ([orderId, role]) => {
                  const r = await fetch(`/api/v1/reviews/orders/${orderId}/eligibility?actorRole=${role}`,
                      {headers: {Authorization: 'Bearer ' + localStorage.getItem('auth_token')}});
                  return {status: r.status, body: await r.json()};
                }
                """, List.of(ORDER, actorRole));
        org.assertj.core.api.Assertions.assertThat(result.get("status")).as("%s eligibility", actorRole).isEqualTo(200);
        Map<?, ?> data = (Map<?, ?>) ((Map<?, ?>) result.get("body")).get("data");
        org.assertj.core.api.Assertions.assertThat(data.get("reviewable")).as("%s may review this order (%s)", actorRole, data.get("reason")).isEqualTo(true);
        return (List<Map<?, ?>>) data.get("targets");
    }
}
