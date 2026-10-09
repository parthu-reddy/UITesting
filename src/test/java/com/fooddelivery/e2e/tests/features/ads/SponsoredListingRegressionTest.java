package com.fooddelivery.e2e.tests.features.ads;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.common.LoginPage;
import com.fooddelivery.e2e.pages.common.Portal;
import com.fooddelivery.e2e.pages.customer.SavedDeliveryAddressPage;
import com.fooddelivery.e2e.util.UrlPaths;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A2 regression gate: before A2 the customer listing put an empty "Sponsored" card first whenever the ad
 * service answered anything. With no ACTIVE campaign in Dev (activation needs a moderated creative, A3),
 * the listing the customer UI itself requested has no sponsored entry, every entry is a real outlet with
 * its city (the city the listing asks ads for), and Home renders no "Sponsored" badge. Read-only.
 */
@Tag("feature-ads")
@Tag("feature-catalog")
public class SponsoredListingRegressionTest extends TestBase {

    @Test void listingHasNoAdOnlyCard() {
        assertThat(System.getProperty("bp.a2.preflight")).as("Run only after the A2 release and fresh seeds").isEqualTo("true");
        Response[] nearby = new Response[1];
        customerPage.onResponse(r -> {
            if ("GET".equals(r.request().method()) && "/api/v1/restaurants/nearby".equals(UrlPaths.path(r.url()))) nearby[0] = r;
        });
        customerPage.navigate(TestConfig.APP_URL);
        new LoginPage(customerPage).login(testCustomerPhone).openPortal(Portal.CUSTOMER);
        new SavedDeliveryAddressPage(customerPage).selectHomeFromOpenDialog();
        customerPage.waitForCondition(() -> nearby[0] != null);
        assertThat(nearby[0].status()).isEqualTo(200);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> listing = (List<Map<String, Object>>) customerPage.evaluate("text => JSON.parse(text).data", nearby[0].text());
        assertThat(listing).as("Home at the seeded address lists outlets").isNotEmpty();
        assertThat(listing).noneMatch(r -> Boolean.TRUE.equals(r.get("isSponsored")));
        assertThat(listing).allSatisfy(r -> {
            assertThat(r.get("id")).isNotNull();
            assertThat(r.get("name")).isNotNull();
            assertThat(r.get("cityId")).as("the nearest outlet's city is what ads are requested for").isNotNull();
        });

        customerPage.getByText(String.valueOf(listing.get(0).get("brandName") != null ? listing.get(0).get("brandName") : listing.get(0).get("name")))
                .first().waitFor();
        assertThat(customerPage.getByText("Sponsored", new com.microsoft.playwright.Page.GetByTextOptions().setExact(true)).count()).isZero();
        System.out.println("A2 listing regression: " + listing.size() + " outlets, none sponsored, all with a city");
    }
}
