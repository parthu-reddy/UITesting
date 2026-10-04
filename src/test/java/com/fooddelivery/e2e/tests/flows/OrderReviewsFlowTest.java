package com.fooddelivery.e2e.tests.flows;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.customer.CustomerDashboardPage;
import com.fooddelivery.e2e.pages.customer.CustomerOrderTrackerPage;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryDashboardPage;
import com.fooddelivery.e2e.pages.delivery.DeliveryOnlineTogglePage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.fooddelivery.e2e.util.RefundRecoveryChecks;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

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
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']").click();
        assertThat(new CustomerOrderTrackerPage(customerPage, ORDER).tracker()).isVisible();
        review(customerPage, new CustomerOrderTrackerPage(customerPage, ORDER).tracker(), "CUSTOMER", "Rate your order", Set.of("RESTAURANT", "DRIVER", "PRODUCT"),
                Map.of("RESTAURANT", new Rating(4, restaurantComment), "DRIVER", new Rating(5, null)));

        // Restaurant: the customer 5, from the outlet's order history.
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
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
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
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

    /** A retained order's previously unrated dish proves a committed write replaces an empty cache. */
    @Test
    @Disabled("O4-INT-002: direct aggregate requests require a visible UI replacement")
    @DisplayName("REVIEW-CACHE-01: a remaining dish review immediately refreshes its warmed aggregate")
    void remainingDishReviewRefreshesTheCachedAggregate() throws java.io.IOException {
        Assumptions.assumeFalse(ORDER.isEmpty(), "needs -Dreview.order.id");
        org.assertj.core.api.Assertions.assertThat(Files.readString(Path.of("target/lifecycle", ORDER + ".json")))
                .contains("\"" + ORDER + "\"").contains(testCustomerPhone);
        Path evidence = Path.of("target/lifecycle", ORDER + "-dish-review-cache.json");
        if (Files.exists(evidence)) {
            Map<?, ?> existing = (Map<?, ?>) customerPage.evaluate("json => JSON.parse(json)", Files.readString(evidence));
            org.assertj.core.api.Assertions.assertThat(existing.get("submitted"))
                    .as("an already submitted immutable dish review must be audited, never submitted again").isNotEqualTo(true);
            org.assertj.core.api.Assertions.assertThat(existing.get("submissionAttempted"))
                    .as("an ambiguous prior submission must be audited before another write").isNotEqualTo(true);
        }
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']").click();
        Locator tracker = new CustomerOrderTrackerPage(customerPage, ORDER).tracker();
        assertThat(tracker).isVisible();

        List<Map<?, ?>> before = targets(customerPage, "CUSTOMER");
        var previous = before.stream().filter(target -> Boolean.TRUE.equals(target.get("alreadyReviewed"))).toList();
        org.assertj.core.api.Assertions.assertThat(previous).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(previous.stream().map(target -> (String) target.get("entityType")))
                .containsExactlyInAnyOrder("RESTAURANT", "DRIVER");
        Map<?, ?> dish = before.stream().filter(target -> "PRODUCT".equals(target.get("entityType"))
                        && Boolean.FALSE.equals(target.get("alreadyReviewed")))
                .findFirst().orElseThrow(() -> new AssertionError("needs a previously unrated owned dish"));
        String dishId = (String) dish.get("entityId"), name = uiName(dish);
        String aggregatePath = "/api/v1/reviews/aggregate?entityType=PRODUCT&entityId=" + dishId;
        // This fresh owned dish has no reviews. Two reads populate and then exercise its empty cache.
        for (int i = 0; i < 2; i++) {
            Map<?, ?> aggregate = aggregate(aggregatePath);
            org.assertj.core.api.Assertions.assertThat(((Number) aggregate.get("totalReviews")).intValue()).isZero();
        }
        saveCacheReview(evidence, dishId, false, false);
        tracker.getByTestId("rate-order-prompt").click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Rate your order"));
        for (var old : previous) assertThat(dialog.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + uiName(old) + " out of 5 stars"))).hasCount(0);
        Locator group = dialog.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"));
        group.getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName("5 stars").setExact(true)).click();
        String comment = "E2E cache review " + ORDER.substring(0, 8);
        dialog.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Comment about " + name)).fill(comment);
        Files.writeString(evidence, (String) customerPage.evaluate("json => { const m=JSON.parse(json);"
                + "m.submissionAttempted=true; return JSON.stringify(m); }", Files.readString(evidence)));
        var submitted = customerPage.waitForResponse(response -> "/api/v1/reviews".equals(java.net.URI.create(response.url()).getPath())
                        && "actorRole=CUSTOMER".equals(java.net.URI.create(response.url()).getQuery())
                        && "POST".equals(response.request().method()), () -> dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit 1 review").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(submitted.status()).isEqualTo(201);
        // Persist before the next assertion so a later failure cannot duplicate an immutable write.
        saveCacheReview(evidence, dishId, true, false);
        Map<?, ?> refreshed = aggregate(aggregatePath);
        org.assertj.core.api.Assertions.assertThat(((Number) refreshed.get("totalReviews")).intValue()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(new java.math.BigDecimal(refreshed.get("averageRating").toString()))
                .isEqualByComparingTo("5.00");
        assertThat(dialog.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Thanks for that"))).isVisible();
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Done").setExact(true)).click();
        List<Map<?, ?>> after = targets(customerPage, "CUSTOMER");
        for (var old : previous) {
            var same = after.stream().filter(target -> old.get("entityType").equals(target.get("entityType"))
                    && old.get("entityId").equals(target.get("entityId"))).findFirst().orElseThrow();
            org.assertj.core.api.Assertions.assertThat(same.get("alreadyReviewed")).isEqualTo(true);
            org.assertj.core.api.Assertions.assertThat(same.get("existingRating")).isEqualTo(old.get("existingRating"));
            org.assertj.core.api.Assertions.assertThat(same.get("existingComment")).isEqualTo(old.get("existingComment"));
        }
        tracker.getByTestId("rate-order-prompt").click();
        Locator reopened = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Rate your order"));
        assertThat(reopened.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"))).hasCount(0);
        assertThat(reopened.getByText(comment)).isVisible();
        customerPage.keyboard().press("Escape");
        saveCacheReview(evidence, dishId, true, true);
    }

    @Test
    @Disabled("O4-INT-002: historical direct eligibility fixture; primary journey proves rendered read-only state")
    @DisplayName("REVIEW-CACHE-02: the retained submitted dish review and earlier reviews stay read-only")
    void submittedDishReviewRemainsReadOnly() throws java.io.IOException {
        org.assertj.core.api.Assertions.assertThat(ORDER).isNotEmpty();
        Path evidence = Path.of("target/lifecycle", ORDER + "-dish-review-cache.json");
        Map<?, ?> manifest = (Map<?, ?>) customerPage.evaluate("json => JSON.parse(json)", Files.readString(evidence));
        org.assertj.core.api.Assertions.assertThat(manifest.get("submitted")).isEqualTo(true);
        Locator tracker = openCustomerHistoryTracker();
        List<Map<?, ?>> current = targets(customerPage, "CUSTOMER");
        Map<?, ?> dish = current.stream().filter(target -> manifest.get("dishId").equals(target.get("entityId")))
                .findFirst().orElseThrow();
        org.assertj.core.api.Assertions.assertThat(dish.get("alreadyReviewed")).isEqualTo(true);
        org.assertj.core.api.Assertions.assertThat(dish.get("existingRating")).isEqualTo(5);
        String comment = "E2E cache review " + ORDER.substring(0, 8);
        org.assertj.core.api.Assertions.assertThat(dish.get("existingComment")).isEqualTo(comment);
        for (var old : current) {
            if ("RESTAURANT".equals(old.get("entityType"))) {
                org.assertj.core.api.Assertions.assertThat(old.get("existingRating")).isEqualTo(4);
                org.assertj.core.api.Assertions.assertThat(old.get("existingComment"))
                        .isEqualTo("E2E review " + ORDER.substring(0, 8) + ": hot and on time");
            } else if ("DRIVER".equals(old.get("entityType"))) {
                org.assertj.core.api.Assertions.assertThat(old.get("existingRating")).isEqualTo(5);
            }
        }
        for (int i = 0; i < 2; i++) {
            Map<?, ?> value = aggregate("/api/v1/reviews/aggregate?entityType=PRODUCT&entityId=" + manifest.get("dishId"));
            assertAggregate(value, 1, "5.00");
        }
        tracker.getByTestId("rate-order-prompt").click();
        Locator dialog = customerPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Rate your order"));
        assertThat(dialog.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + uiName(dish) + " out of 5 stars"))).hasCount(0);
        assertThat(dialog.getByText(comment)).isVisible();
        customerPage.keyboard().press("Escape");
        Files.writeString(evidence, (String) customerPage.evaluate("json => { const m=JSON.parse(json);"
                + "m.readOnlyVerified=true; return JSON.stringify(m); }", Files.readString(evidence)));
    }

    @Test
    @Disabled("O4-INT-002: direct aggregate requests require a visible UI replacement")
    @DisplayName("REVIEW-CACHE-03: a remaining restaurant driver review immediately refreshes the driver's own warmed aggregate")
    void remainingDriverReviewRefreshesTheCachedAggregate() throws java.io.IOException {
        org.assertj.core.api.Assertions.assertThat(ORDER).isNotEmpty();
        String manifest = Files.readString(Path.of("target/lifecycle", ORDER + ".json"));
        org.assertj.core.api.Assertions.assertThat(manifest).contains(testRestaurantPhone).contains(testRiderPhone);
        Map<?, ?> actors = (Map<?, ?>) customerPage.evaluate("json => JSON.parse(json)", manifest);
        Path evidence = Path.of("target/lifecycle", ORDER + "-driver-review-cache.json");
        org.assertj.core.api.Assertions.assertThat(Files.exists(evidence))
                .as("an attempted immutable review must be inspected, never repeated").isFalse();
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        RestaurantDashboardPage restaurant = new RestaurantDashboardPage(restaurantPage);
        restaurant.waitForDashboard();
        restaurant.selectOutlet((String) actors.get("outlet"));
        restaurant.openSettingsTab();
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Order History").setExact(true)).click();
        restaurantPage.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(ORDER.substring(0, 8) + "..."))
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Details")).click();
        List<Map<?, ?>> before = targets(restaurantPage, "RESTAURANT");
        Map<?, ?> customer = before.stream().filter(target -> "CUSTOMER".equals(target.get("entityType"))).findFirst().orElseThrow();
        org.assertj.core.api.Assertions.assertThat(customer.get("alreadyReviewed")).isEqualTo(true);
        org.assertj.core.api.Assertions.assertThat(customer.get("existingRating")).isEqualTo(5);
        Map<?, ?> driver = before.stream().filter(target -> "DRIVER".equals(target.get("entityType"))).findFirst().orElseThrow();
        org.assertj.core.api.Assertions.assertThat(driver.get("alreadyReviewed")).isEqualTo(false);
        riderPage.navigate(TestConfig.APP_URL);
        new LoginPage(riderPage).login(testRiderPhone).openPortal(Portal.DELIVERY);
        new DeliveryDashboardPage(riderPage).waitForDashboard();
        org.assertj.core.api.Assertions.assertThat(new DeliveryOnlineTogglePage(riderPage).isOffline()).isTrue();
        String aggregatePath = "/api/v1/reviews/aggregate?entityType=DRIVER&entityId=" + driver.get("entityId");
        // Private driver aggregates are read only by the assigned rider, using that rider's normal login.
        for (int i = 0; i < 2; i++) assertAggregate(aggregate(riderPage, aggregatePath), 1, "5.00");
        detailsDialog(restaurantPage, ORDER.substring(0, 8)).getByTestId("rate-order-prompt").click();
        Locator dialog = restaurantPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Review this delivery"));
        String name = uiName(driver), comment = "E2E driver cache review " + ORDER.substring(0, 8);
        dialog.getByRole(AriaRole.RADIOGROUP, new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"))
                .getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName("4 stars").setExact(true)).click();
        dialog.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Comment about " + name)).fill(comment);
        saveDriverCacheReview(evidence, (String) driver.get("entityId"), false, false);
        var submitted = restaurantPage.waitForResponse(response -> "/api/v1/reviews".equals(java.net.URI.create(response.url()).getPath())
                && "actorRole=RESTAURANT".equals(java.net.URI.create(response.url()).getQuery())
                && "POST".equals(response.request().method()), () -> dialog.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Submit 1 review").setExact(true)).click());
        org.assertj.core.api.Assertions.assertThat(submitted.status()).isEqualTo(201);
        saveDriverCacheReview(evidence, (String) driver.get("entityId"), true, false);
        assertAggregate(aggregate(riderPage, aggregatePath), 2, "4.50");
        assertThat(dialog.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Thanks for that"))).isVisible();
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Done").setExact(true)).click();
        var after = targets(restaurantPage, "RESTAURANT");
        var previousCustomer = after.stream().filter(target -> "CUSTOMER".equals(target.get("entityType"))).findFirst().orElseThrow();
        org.assertj.core.api.Assertions.assertThat(previousCustomer.get("existingRating")).isEqualTo(5);
        detailsDialog(restaurantPage, ORDER.substring(0, 8)).getByTestId("rate-order-prompt").click();
        Locator reopened = restaurantPage.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Review this delivery"));
        assertThat(reopened.getByRole(AriaRole.RADIOGROUP,
                new Locator.GetByRoleOptions().setName("Rate " + name + " out of 5 stars"))).hasCount(0);
        assertThat(reopened.getByText(comment)).isVisible();
        restaurantPage.keyboard().press("Escape");
        saveDriverCacheReview(evidence, (String) driver.get("entityId"), true, true);
    }

    private Locator openCustomerHistoryTracker() {
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        CustomerDashboardPage.openProfileSettings(customerPage);
        customerPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("History").setExact(true)).click();
        customerPage.locator("[data-testid='customer-history-order'][data-order-id='" + ORDER + "']").click();
        Locator tracker = new CustomerOrderTrackerPage(customerPage, ORDER).tracker();
        assertThat(tracker).isVisible();
        return tracker;
    }

    private static void assertAggregate(Map<?, ?> value, int expectedCount, String expectedAverage) {
        org.assertj.core.api.Assertions.assertThat(((Number) value.get("totalReviews")).intValue()).isEqualTo(expectedCount);
        org.assertj.core.api.Assertions.assertThat(new java.math.BigDecimal(value.get("averageRating").toString()))
                .isEqualByComparingTo(expectedAverage);
    }

    private void saveDriverCacheReview(Path path, String driverId, boolean submitted, boolean complete) throws java.io.IOException {
        Files.writeString(path, (String) customerPage.evaluate("value => JSON.stringify(value)",
                Map.of("orderId", ORDER, "driverId", driverId, "rating", 4, "submissionAttempted", true,
                        "submitted", submitted, "complete", complete, "dataPolicy", "retain", "newOrderCreated", false)));
    }

    private Map<?, ?> aggregate(String path) {
        return aggregate(customerPage, path);
    }

    private Map<?, ?> aggregate(Page page, String path) {
        Map<?, ?> response = RefundRecoveryChecks.read(page, path);
        org.assertj.core.api.Assertions.assertThat(response.get("status")).isEqualTo(200);
        return (Map<?, ?>) ((Map<?, ?>) response.get("body")).get("data");
    }

    private void saveCacheReview(Path path, String dishId, boolean submitted, boolean complete) throws java.io.IOException {
        Files.writeString(path, (String) customerPage.evaluate("value => JSON.stringify(value)",
                Map.of("orderId", ORDER, "dishId", dishId, "rating", 5, "submitted", submitted,
                        "complete", complete, "submissionAttempted", submitted, "dataPolicy", "retain", "newOrderCreated", false)));
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
        List<Map<?, ?>> before = targetsFromVisibleOpen(page, actorRole, () -> scope.getByTestId("rate-order-prompt").click());
        org.assertj.core.api.Assertions.assertThat(before).as("%s has something to review", actorRole).isNotEmpty();
        org.assertj.core.api.Assertions.assertThat(before.stream().map(t -> (String) t.get("entityType")).collect(Collectors.toSet()))
                .as("%s's role matrix", actorRole).isEqualTo(allowedTypes);
        org.assertj.core.api.Assertions.assertThat(before).allMatch(t -> Boolean.FALSE.equals(t.get("alreadyReviewed")));

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
        List<Map<?, ?>> after = targetsFromVisibleOpen(page, actorRole, () -> scope.getByTestId("rate-order-prompt").click());
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

    /** Observe the exact request made by opening the real review dialog; never create a request. */
    @SuppressWarnings("unchecked")
    private List<Map<?, ?>> targetsFromVisibleOpen(Page page, String actorRole, Runnable open) {
        var response = page.waitForResponse(r -> r.request().method().equals("GET")
                && java.net.URI.create(r.url()).getPath().equals("/api/v1/reviews/orders/" + ORDER + "/eligibility")
                && java.net.URI.create(r.url()).getQuery().contains("actorRole=" + actorRole), open);
        org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo(200);
        Map<?, ?> envelope = (Map<?, ?>) page.evaluate("text => JSON.parse(text)", response.text());
        Map<?, ?> data = (Map<?, ?>) envelope.get("data");
        org.assertj.core.api.Assertions.assertThat(data.get("reviewable")).as("rendered review eligibility").isEqualTo(true);
        return (List<Map<?, ?>>) data.get("targets");
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
