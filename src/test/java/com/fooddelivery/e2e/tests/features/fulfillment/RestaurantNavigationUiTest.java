package com.fooddelivery.e2e.tests.features.fulfillment;

import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.restaurant.RestaurantDashboardPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui-only")
public class RestaurantNavigationUiTest extends TestBase {

    @Test
    @DisplayName("REST-NAV-01-05/07: Restaurant sections render without state bleed")
    void restaurantSectionsRender() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        new RestaurantDashboardPage(restaurantPage).waitForDashboard();

        RestaurantDashboardPage dashboard=new RestaurantDashboardPage(restaurantPage);
        String outlet=System.getProperty("restaurant.outlet.name","Brand 1 Outlet 3");
        dashboard.selectOutlet(outlet);
        restaurantPage.reload();dashboard.waitForDashboard();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(AriaRole.COMBOBOX,new Page.GetByRoleOptions().setName("Outlet").setExact(true))).hasText(outlet);
        String alternate=System.getProperty("restaurant.alternate.outlet.name","Brand 1 Outlet 6");
        dashboard.selectOutlet(alternate);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(AriaRole.COMBOBOX,new Page.GetByRoleOptions().setName("Outlet").setExact(true))).hasText(alternate);
        dashboard.selectOutlet(outlet);

        clickTab(Pattern.compile("^Orders.*"));
        waitVisible(restaurantPage.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName(Pattern.compile("^Incoming, [0-9]+ orders?$"))));
        clickTab(Pattern.compile("^Menu$"));
        waitVisible(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName(Pattern.compile("Today.s menu"))));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByText("Updates every 5 s",new Page.GetByTextOptions().setExact(true))).isHidden();
        clickTab(Pattern.compile("^Campaigns$"));
        waitVisible(dashboard.campaignsScreen());
        clickTab(Pattern.compile("^Earnings$"));
        waitVisible(restaurantPage.getByText("Net Earnings", new Page.GetByTextOptions().setExact(true)));
        clickTab(Pattern.compile("^Reviews$"));
        waitVisible(restaurantPage.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName("What customers said").setExact(true)));
        dashboard.openSettingsTab();
        waitVisible(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName("Restaurant Console Settings").setExact(true)));
        String completed=System.getProperty("restaurant.completed.order.id", "").trim();
        if(!completed.isEmpty()){
            assertThat(completed).matches("[0-9a-fA-F-]{36}");
            Response history = restaurantPage.waitForResponse(response ->
                    response.url().contains("/fulfillment/orders/history")
                            && "GET".equals(response.request().method()), () ->
                    restaurantPage.getByRole(AriaRole.TAB,new Page.GetByRoleOptions().setName("Order History").setExact(true)).click());
            assertThat(history.status()).as("the selected outlet's order history loads").isEqualTo(200);
            Locator row=restaurantPage.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText(completed.substring(0,8)));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(row).hasCount(1,new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions().setTimeout(30000));
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(row).containsText(Pattern.compile("Delivered",Pattern.CASE_INSENSITIVE));
            String expectedRider = System.getProperty("restaurant.completed.rider.name", "").trim();
            if (!expectedRider.isEmpty()) {
                Map<?, ?> envelope = (Map<?, ?>) restaurantPage.evaluate("json => JSON.parse(json)", history.text());
                List<?> orders = (List<?>) ((Map<?, ?>) envelope.get("data")).get("content");
                List<?> matching = orders.stream().map(value -> (Map<?, ?>) value)
                        .filter(order -> completed.equals(order.get("orderId"))).toList();
                assertThat(matching).as("history response contains the retained order once").hasSize(1);
                assertThat(((Map<?, ?>) matching.get(0)).get("deliveryExecutiveName"))
                        .as("the real signed SERVICE summary enriches the completed order")
                        .isEqualTo(expectedRider);
            }
        }
        restaurantPage.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Back to Kitchen Feed").setExact(true)).click();
        for(String name:new String[]{"Menu","Campaigns","Earnings","Reviews"})clickTab(Pattern.compile("^"+name+"$"));
        clickTab(Pattern.compile("^Orders.*"));
        waitVisible(restaurantPage.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName(Pattern.compile("^In the kitchen, [0-9]+ orders?$"))));
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(AriaRole.REGION,new Page.GetByRoleOptions().setName("What customers said").setExact(true))).isHidden();
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(restaurantPage.getByRole(AriaRole.HEADING,new Page.GetByRoleOptions().setName(Pattern.compile("Today.s menu")))).isHidden();

    }

    @Test
    @DisplayName("REVIEW-AGG-01: Restaurant sees its review aggregate or the defined empty state")
    void restaurantReviewsShowPublicFeedbackAndAggregate() {
        restaurantPage.navigate(TestConfig.APP_URL);
        new LoginPage(restaurantPage).login(testRestaurantPhone).openPortal(Portal.RESTAURANT);
        RestaurantDashboardPage dashboard = new RestaurantDashboardPage(restaurantPage);
        dashboard.waitForDashboard();
        String outletName = System.getProperty("review.outlet.name", "Brand 1 Outlet 6");
        String expectedComment = System.getProperty("review.customer.comment");
        dashboard.selectOutlet(outletName);

        AtomicInteger reviewReadResponses = new AtomicInteger();
        AtomicReference<Integer> failedReadStatus = new AtomicReference<>();
        restaurantPage.onResponse(response -> {
            String url = response.url();
            boolean reviewList = url.contains("/api/v1/reviews?");
            boolean aggregate = url.contains("/api/v1/reviews/aggregate?");
            if ("GET".equals(response.request().method()) && (reviewList || aggregate)) {
                reviewReadResponses.incrementAndGet();
                if (response.status() < 200 || response.status() >= 300) {
                    failedReadStatus.set(response.status());
                }
            }
        });
        dashboard.openReviewsTab();

        Locator reviewsPanel = restaurantPage.getByRole(AriaRole.REGION,
                new Page.GetByRoleOptions().setName("What customers said").setExact(true));
        restaurantPage.waitForCondition(() -> reviewReadResponses.get() >= 2,
                new Page.WaitForConditionOptions().setTimeout(20000));
        assertThat(failedReadStatus.get())
                .as("review list and aggregate requests must load successfully")
                .isNull();

        Locator e2eReview = expectedComment == null || expectedComment.isBlank()
                ? null
                : reviewsPanel.getByText(expectedComment,
                        new Locator.GetByTextOptions().setExact(true));
        Locator reviewCards = reviewsPanel.locator("article");
        Locator noReviews = reviewsPanel.getByText("No reviews yet",
                new Locator.GetByTextOptions().setExact(true));

        if (expectedComment != null && !expectedComment.isBlank()) {
            e2eReview.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        } else if (reviewCards.count() == 0) {
            // Fresh deployments can start without review fixtures. Validate the real, successful
            // empty state instead of failing on a comment created in an older database snapshot.
            com.microsoft.playwright.assertions.PlaywrightAssertions
                    .assertThat(noReviews.first()).isVisible();
            com.microsoft.playwright.assertions.PlaywrightAssertions
                    .assertThat(reviewsPanel.locator("span.text-3xl.font-black")).hasCount(0);
            return;
        }

        assertThat(reviewCards.count()).isGreaterThan(0);
        if (expectedComment != null && !expectedComment.isBlank()) {
            assertThat(e2eReview.count()).isGreaterThan(0);
        }

        com.microsoft.playwright.assertions.PlaywrightAssertions
                .assertThat(reviewsPanel.locator("span.text-3xl.font-black"))
                .hasText(Pattern.compile("^[1-5]\\.\\d$"));
        com.microsoft.playwright.assertions.PlaywrightAssertions
                .assertThat(reviewsPanel.getByText(Pattern.compile("\\d+ reviews?"))).isVisible();
    }

    private void clickTab(Pattern name) {
        restaurantPage.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(name)).click();
    }

    private void waitVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10000));
        assertThat(locator.isVisible()).isTrue();
    }
}
