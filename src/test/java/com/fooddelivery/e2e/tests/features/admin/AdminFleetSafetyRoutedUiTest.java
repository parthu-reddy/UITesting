package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminFleetSafetyPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic browser coverage for Fleet Map data rendering and recovery.
 *
 * <p>Authentication remains a normal admin login. The three Fleet Map reads and its map style
 * are fulfilled in the browser with disposable data, so this test needs neither a real rider
 * location nor a real customer address. Any assignment or cancellation write is blocked before
 * it could leave the browser.</p>
 */
@Tag("browser-routed")
@Tag("feature-admin-ops")
@Tag("feature-rider-delivery")
public class AdminFleetSafetyRoutedUiTest extends TestBase {

    private static final String RESTAURANTS_PATH =
            "/api/v1/internal/admin/restaurants/all-with-location";
    private static final String DRIVERS_PATH =
            "/api/v1/internal/admin/delivery/drivers/all-with-location";
    private static final String FLEET_CITIES_PATH =
            "/api/v1/internal/admin/delivery/fleet-cities";
    private static final String CUSTOMER_ADDRESSES_PATH =
            "/api/v1/internal/admin/customers/addresses";
    // AdminPortal polls this safe sidebar badge while Fleet Map is open. It is also routed so
    // the fixture never lets an unaccounted request reach the shared deployment.
    private static final String INTERVENTION_COUNT_PATH =
            "/api/v1/internal/admin/orders/intervention";
    private static final String MAP_STYLE_PREFIX = "/olamaps/tiles/vector/v1/styles/";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";
    private static final String BENGALURU_CITY_ID = "BLR";
    private static final String HYDERABAD_CITY_ID = "HYD";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("FLEET-SAFE-01: fixture layers render, rider details open, and a partial refresh recovers without dispatch writes")
    void fleetLayersRefreshSafelyAndRecoverFromOneUnavailableRead() {
        FleetFixture fixture = new FleetFixture();
        registerFixtureRoutes(fixture);

        portal.openFleetTab();
        AdminFleetSafetyPage fleet = new AdminFleetSafetyPage(adminPage);
        fleet.waitForFleetMap();
        waitForReadCount(fixture, 1, "initial Fleet Map reads");
        fleet.waitForLayerCounts(1, 1, 1);
        fleet.waitForFixtureMarkerCount(3);
        assertThat(fleet.mapViewportHeight())
                .as("the fixture map needs a usable layout before its pins can be trusted")
                .isGreaterThan(0.0);

        fleet.openOnlineRiderPopup();
        fleet.waitForRiderPopup();
        assertThat(fleet.riderPopup().innerText())
                .contains("Rider: Fixture online rider")
                .contains("Status: ONLINE");
        assertNoDispatchWrites(fixture);

        fixture.failCustomerReads();
        fleet.refresh();
        waitForReadCount(fixture, 2, "refresh Fleet Map reads");
        fleet.waitForPartialRefreshError();
        // The source deliberately retains the previous customer layer when only that layer fails.
        fleet.waitForLayerCounts(1, 1, 1);
        fleet.waitForFixtureMarkerCount(3);
        assertNoDispatchWrites(fixture);

        fixture.allowCustomerReads();
        fleet.refresh();
        waitForReadCount(fixture, 3, "recovery Fleet Map reads");
        fleet.waitForRefreshErrorToClear();
        // The recovered response has a second address, proving the final refresh changes both
        // the legend and the MapLibre marker collection rather than merely clearing an alert.
        fleet.waitForLayerCounts(1, 1, 2);
        fleet.waitForFixtureMarkerCount(4);

        assertThat(fixture.restaurantReads.get()).isGreaterThanOrEqualTo(3);
        assertThat(fixture.driverReads.get()).isGreaterThanOrEqualTo(3);
        assertThat(fixture.customerReads.get()).isGreaterThanOrEqualTo(3);
        assertThat(fixture.mapStyleReads.get()).isPositive();
        assertNoDispatchWrites(fixture);
    }

    @Test
    @DisplayName("FLEET-SAFE-02: changing the Fleet Map city scopes every layer request to that city")
    void fleetCitySelectionScopesEveryLayerRequest() {
        FleetFixture fixture = new FleetFixture();
        registerFixtureRoutes(fixture);

        portal.openFleetTab();
        AdminFleetSafetyPage fleet = new AdminFleetSafetyPage(adminPage);
        fleet.waitForFleetMap();
        fleet.waitForFleetCity(BENGALURU_CITY_ID);
        waitForScopedReadCount(fixture, BENGALURU_CITY_ID, 1, "initial city scope");

        fleet.selectFleetCity(HYDERABAD_CITY_ID);
        fleet.waitForFleetCity(HYDERABAD_CITY_ID);
        waitForScopedReadCount(fixture, HYDERABAD_CITY_ID, 1, "selected city scope");

        assertThat(fixture.fleetCityReads.get())
                .as("the canonical fleet city list must be loaded before layer reads")
                .isPositive();
        assertThat(fixture.lastRestaurantCity.get()).isEqualTo(HYDERABAD_CITY_ID);
        assertThat(fixture.lastDriverCity.get()).isEqualTo(HYDERABAD_CITY_ID);
        assertThat(fixture.lastCustomerCity.get()).isEqualTo(HYDERABAD_CITY_ID);
        assertNoDispatchWrites(fixture);
    }

    private void registerFixtureRoutes(FleetFixture fixture) {
        // Route registration occurs after login. This preserves normal deployed authentication,
        // while making only Fleet Map data and map styling deterministic.
        adminPage.route(com.fooddelivery.e2e.util.FixtureShell::isFixturedApi, fixture::handleApiRequest);
        adminPage.route(url -> pathOf(url).startsWith(MAP_STYLE_PREFIX), fixture::handleMapStyleRequest);
    }

    private void waitForReadCount(FleetFixture fixture, int expectedCount, String expectation) {
        adminPage.waitForCondition(() -> fixture.restaurantReads.get() >= expectedCount
                        && fixture.driverReads.get() >= expectedCount
                        && fixture.customerReads.get() >= expectedCount,
                new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(fixture.restaurantReads.get()).as(expectation + " restaurants").isGreaterThanOrEqualTo(expectedCount);
        assertThat(fixture.driverReads.get()).as(expectation + " riders").isGreaterThanOrEqualTo(expectedCount);
        assertThat(fixture.customerReads.get()).as(expectation + " customers").isGreaterThanOrEqualTo(expectedCount);
    }

    private void waitForScopedReadCount(FleetFixture fixture, String cityId, int expectedCount,
                                        String expectation) {
        adminPage.waitForCondition(() -> fixture.restaurantReadsFor(cityId) >= expectedCount
                        && fixture.driverReadsFor(cityId) >= expectedCount
                        && fixture.customerReadsFor(cityId) >= expectedCount,
                new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(fixture.restaurantReadsFor(cityId))
                .as(expectation + " restaurant reads for " + cityId)
                .isGreaterThanOrEqualTo(expectedCount);
        assertThat(fixture.driverReadsFor(cityId))
                .as(expectation + " rider reads for " + cityId)
                .isGreaterThanOrEqualTo(expectedCount);
        assertThat(fixture.customerReadsFor(cityId))
                .as(expectation + " customer reads for " + cityId)
                .isGreaterThanOrEqualTo(expectedCount);
    }

    private static void assertNoDispatchWrites(FleetFixture fixture) {
        assertThat(fixture.assignmentWrites.get())
                .as("Fleet Map inspection must not send a driver assignment")
                .isZero();
        assertThat(fixture.cancellationWrites.get())
                .as("Fleet Map inspection must not send a cancellation")
                .isZero();
        assertThat(fixture.blockedDispatchWrites.get())
                .as("no blocked dispatch write should be attempted")
                .isZero();
        assertThat(fixture.unexpectedApiRequests.get())
                .as("every API request after authentication must be explicitly fixture-routed")
                .isZero();
        assertThat(fixture.invalidCityScopedReads.get())
                .as("each Fleet Map layer read must carry an allowed cityId")
                .isZero();
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static String queryValue(String url, String name) {
        String rawQuery = URI.create(url).getRawQuery();
        if (rawQuery == null) return null;
        for (String parameter : rawQuery.split("&")) {
            int separator = parameter.indexOf('=');
            String encodedName = separator < 0 ? parameter : parameter.substring(0, separator);
            if (!name.equals(URLDecoder.decode(encodedName, StandardCharsets.UTF_8))) continue;
            String encodedValue = separator < 0 ? "" : parameter.substring(separator + 1);
            return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
        }
        return null;
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private static final class FleetFixture {
        private final AtomicInteger restaurantReads = new AtomicInteger();
        private final AtomicInteger driverReads = new AtomicInteger();
        private final AtomicInteger customerReads = new AtomicInteger();
        private final AtomicInteger fleetCityReads = new AtomicInteger();
        private final AtomicInteger bengaluruRestaurantReads = new AtomicInteger();
        private final AtomicInteger hyderabadRestaurantReads = new AtomicInteger();
        private final AtomicInteger bengaluruDriverReads = new AtomicInteger();
        private final AtomicInteger hyderabadDriverReads = new AtomicInteger();
        private final AtomicInteger bengaluruCustomerReads = new AtomicInteger();
        private final AtomicInteger hyderabadCustomerReads = new AtomicInteger();
        private final AtomicInteger interventionCountReads = new AtomicInteger();
        private final AtomicInteger mapStyleReads = new AtomicInteger();
        private final AtomicInteger assignmentWrites = new AtomicInteger();
        private final AtomicInteger cancellationWrites = new AtomicInteger();
        private final AtomicInteger blockedDispatchWrites = new AtomicInteger();
        private final AtomicInteger unexpectedApiRequests = new AtomicInteger();
        private final AtomicInteger invalidCityScopedReads = new AtomicInteger();
        private final AtomicReference<String> lastRestaurantCity = new AtomicReference<>();
        private final AtomicReference<String> lastDriverCity = new AtomicReference<>();
        private final AtomicReference<String> lastCustomerCity = new AtomicReference<>();
        private final AtomicBoolean customerReadsFail = new AtomicBoolean(false);
        private final AtomicBoolean recoveredCustomerLayer = new AtomicBoolean(false);

        private void failCustomerReads() {
            customerReadsFail.set(true);
        }

        private void allowCustomerReads() {
            customerReadsFail.set(false);
            recoveredCustomerLayer.set(true);
        }

        private void handleApiRequest(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if (isDispatchWrite(method, path)) {
                blockedDispatchWrites.incrementAndGet();
                if (path.contains("/assign")) assignmentWrites.incrementAndGet();
                if (path.contains("/cancel")) cancellationWrites.incrementAndGet();
                route.abort();
                return;
            }

            if ("GET".equals(method) && FLEET_CITIES_PATH.equals(path)) {
                fleetCityReads.incrementAndGet();
                fulfillJson(route, 200, "[\"" + BENGALURU_CITY_ID + "\",\"" + HYDERABAD_CITY_ID + "\"]");
                return;
            }

            if ("GET".equals(method) && RESTAURANTS_PATH.equals(path)) {
                restaurantReads.incrementAndGet();
                recordCityScopedRead(request, bengaluruRestaurantReads, hyderabadRestaurantReads,
                        lastRestaurantCity);
                fulfillJson(route, 200, restaurantResponse());
                return;
            }
            if ("GET".equals(method) && DRIVERS_PATH.equals(path)) {
                driverReads.incrementAndGet();
                recordCityScopedRead(request, bengaluruDriverReads, hyderabadDriverReads,
                        lastDriverCity);
                fulfillJson(route, 200, driverResponse());
                return;
            }
            if ("GET".equals(method) && CUSTOMER_ADDRESSES_PATH.equals(path)) {
                customerReads.incrementAndGet();
                recordCityScopedRead(request, bengaluruCustomerReads, hyderabadCustomerReads,
                        lastCustomerCity);
                if (customerReadsFail.get()) {
                    fulfillJson(route, 503, "{\"message\":\"Fixture customer layer unavailable\"}");
                } else {
                    fulfillJson(route, 200, customerAddressResponse(recoveredCustomerLayer.get(),
                            queryValue(request.url(), "cityId")));
                }
                return;
            }
            if ("GET".equals(method) && INTERVENTION_COUNT_PATH.equals(path)) {
                interventionCountReads.incrementAndGet();
                fulfillJson(route, 200, "{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"last\":true,"
                        + "\"size\":20,\"number\":0,\"first\":true,\"numberOfElements\":0,\"empty\":true}");
                return;
            }

            unexpectedApiRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected API request blocked by Fleet Map fixture\"}");
        }

        private void recordCityScopedRead(Request request, AtomicInteger bengaluruReads,
                                          AtomicInteger hyderabadReads,
                                          AtomicReference<String> lastCity) {
            String cityId = queryValue(request.url(), "cityId");
            lastCity.set(cityId);
            if (BENGALURU_CITY_ID.equals(cityId)) {
                bengaluruReads.incrementAndGet();
                return;
            }
            if (HYDERABAD_CITY_ID.equals(cityId)) {
                hyderabadReads.incrementAndGet();
                return;
            }
            invalidCityScopedReads.incrementAndGet();
        }

        private int restaurantReadsFor(String cityId) {
            return cityReadCount(cityId, bengaluruRestaurantReads, hyderabadRestaurantReads);
        }

        private int driverReadsFor(String cityId) {
            return cityReadCount(cityId, bengaluruDriverReads, hyderabadDriverReads);
        }

        private int customerReadsFor(String cityId) {
            return cityReadCount(cityId, bengaluruCustomerReads, hyderabadCustomerReads);
        }

        private static int cityReadCount(String cityId, AtomicInteger bengaluruReads,
                                         AtomicInteger hyderabadReads) {
            if (BENGALURU_CITY_ID.equals(cityId)) return bengaluruReads.get();
            if (HYDERABAD_CITY_ID.equals(cityId)) return hyderabadReads.get();
            throw new IllegalArgumentException("Unexpected fleet city " + cityId);
        }

        private void handleMapStyleRequest(Route route) {
            mapStyleReads.incrementAndGet();
            // A minimal valid MapLibre style makes pin rendering deterministic without calling
            // the real tile provider. Live map-proxy availability is covered by the read-only run.
            fulfillJson(route, 200, "{\"version\":8,\"name\":\"fleet-fixture\",\"sources\":{},\"layers\":[]}");
        }

        private static boolean isDispatchWrite(String method, String path) {
            if (!("POST".equals(method) || "PUT".equals(method)
                    || "PATCH".equals(method) || "DELETE".equals(method))) {
                return false;
            }
            if (!path.startsWith("/api/v1/internal/admin/")) return false;
            return path.contains("/assign") || path.contains("/cancel");
        }

        private static String restaurantResponse() {
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [{
                          "id": "e1000000-0000-4000-8000-000000000001",
                          "name": "Fixture Fleet Kitchen",
                          "isActive": true,
                          "lat": 12.9716,
                          "lng": 77.5946
                        }],
                        "totalElements": 1,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": 1,
                        "empty": false
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(FIXTURE_TIME);
        }

        private static String driverResponse() {
            return """
                    {
                      "content": [{
                        "id": "e1000000-0000-4000-8000-000000000002",
                        "fullName": "Fixture online rider",
                        "phoneNumber": "7000000999",
                        "lat": 12.9780,
                        "lng": 77.6020,
                        "status": "ONLINE"
                      }],
                      "totalElements": 1,
                      "totalPages": 1,
                      "last": true,
                      "size": 20,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 1,
                      "empty": false
                    }
                    """;
        }

        private static String customerAddressResponse(boolean includeRecoveredAddress, String cityId) {
            String recoveredAddress = includeRecoveredAddress ? """
                    ,{
                      "id": "e1000000-0000-4000-8000-000000000005",
                      "customerId": "e1000000-0000-4000-8000-000000000006",
                      "label": "Fixture Work",
                      "addressLine1": "2 Fixture Street",
                      "city": "Bengaluru",
                      "cityId": "%s",
                      "state": "Karnataka",
                      "zipCode": "560001",
                      "latitude": 12.9890,
                      "longitude": 77.6100,
                      "isDefault": false
                    }""".formatted(cityId) : "";
            int count = includeRecoveredAddress ? 2 : 1;
            return """
                    {
                      "success": true,
                      "message": "fixture",
                      "data": {
                        "content": [{
                          "id": "e1000000-0000-4000-8000-000000000003",
                          "customerId": "e1000000-0000-4000-8000-000000000004",
                          "label": "Fixture Home",
                          "addressLine1": "1 Fixture Street",
                          "city": "Bengaluru",
                          "cityId": "%s",
                          "state": "Karnataka",
                          "zipCode": "560001",
                          "latitude": 12.9650,
                          "longitude": 77.5900,
                          "isDefault": true
                        }%s],
                        "totalElements": %d,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": %d,
                        "empty": false
                      },
                      "timestamp": "%s"
                    }
                    """.formatted(cityId, recoveredAddress, count, count, FIXTURE_TIME);
        }
    }
}
