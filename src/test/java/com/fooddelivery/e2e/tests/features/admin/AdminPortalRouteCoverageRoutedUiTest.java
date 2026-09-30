package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.base.TestConfig;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser-routed coverage for the Admin Portal's route contract.
 *
 * <p>The administrator signs in normally against the deployed application. Once the session is
 * established, every API read used by this navigation pass is fulfilled in the browser and every
 * non-read request is blocked. This makes the route assertions independent of mutable Dev data
 * while still exercising the deployed bundle, sidebar controls, and client router.</p>
 */
@Tag("admin")
@Tag("admin-navigation")
@Tag("browser-routed")
public class AdminPortalRouteCoverageRoutedUiTest extends TestBase {

    private static final String ACTIVE_ORDERS_PATH = "/api/v1/internal/admin/orders/active-all";
    private static final String INTERVENTIONS_PATH = "/api/v1/internal/admin/orders/intervention";
    private static final String SUPPORT_TICKETS_PATH = INTERVENTIONS_PATH + "/support-tickets";
    private static final String REFUNDS_PATH = "/api/v1/internal/admin/refunds";
    private static final String USERS_PATH = "/api/v1/internal/admin/users/all";
    private static final String CATEGORIES_PATH = "/api/v1/categories";
    private static final String FLEET_RESTAURANTS_PATH =
            "/api/v1/internal/admin/restaurants/all-with-location";
    private static final String FLEET_DRIVERS_PATH =
            "/api/v1/internal/admin/delivery/drivers/all-with-location";
    private static final String FLEET_CUSTOMERS_PATH = "/api/v1/internal/admin/customers/addresses";
    private static final String LEDGER_PATH = "/api/v1/internal/admin/ledger/transactions";
    private static final String PENDING_PAYOUTS_PATH = "/api/v1/internal/admin/payouts/pending";
    private static final String LEDGER_REJECTIONS_PATH = "/api/v1/internal/admin/ledger/rejections";
    private static final String MAP_STYLE_PREFIX = "/olamaps/tiles/vector/v1/styles/";

    private static final List<SidebarDestination> SIDEBAR_DESTINATIONS = List.of(
            new SidebarDestination("Live Operations", "/admin/deliveries", List.of(ACTIVE_ORDERS_PATH)),
            new SidebarDestination("Support Tickets", "/admin/support_tickets", List.of(SUPPORT_TICKETS_PATH)),
            new SidebarDestination("Refund Queue", "/admin/refunds", List.of(REFUNDS_PATH)),
            new SidebarDestination("Manual Interventions", "/admin/interventions", List.of(INTERVENTIONS_PATH)),
            new SidebarDestination("User Management", "/admin/users", List.of(USERS_PATH)),
            new SidebarDestination("Categories", "/admin/categories", List.of(CATEGORIES_PATH)),
            new SidebarDestination("Fleet Map", "/admin/map", List.of(
                    FLEET_RESTAURANTS_PATH, FLEET_DRIVERS_PATH, FLEET_CUSTOMERS_PATH)),
            new SidebarDestination("Ledger Entries", "/admin/ledger", List.of(LEDGER_PATH)),
            new SidebarDestination("Pending Payouts", "/admin/payouts", List.of(PENDING_PAYOUTS_PATH)),
            new SidebarDestination("Money Operations", "/admin/money_ops", List.of(LEDGER_REJECTIONS_PATH)),
            // Review data is requested only after an operator enters a lookup value. This route
            // test deliberately performs no lookup, so it cannot accidentally query live review data.
            new SidebarDestination("Review Moderation", "/admin/reviews", List.of())
    );

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("ADMIN-ROUTES-01: every admin sidebar item reaches its client route and unknown admin routes redirect to Fleet Map")
    void sidebarDestinationsAndWildcardRedirectFollowTheAdminRouteContract() {
        NavigationFixture fixture = new NavigationFixture();
        registerFixtureRoutes(fixture);

        for (SidebarDestination destination : SIDEBAR_DESTINATIONS) {
            openSidebarDestination(destination);
            awaitFixtureReads(fixture, destination);
        }

        // This is a browser reload, rather than a history mutation, so it proves the deployed
        // SPA entry point and AdminPortal's nested wildcard route agree on the default location.
        adminPage.navigate(TestConfig.APP_URL.replaceAll("/$", "") + "/admin/not-a-real-admin-route");
        adminPage.waitForURL("**/admin/map");
        portal.waitForPortal();
        assertThat(pathOf(adminPage.url())).isEqualTo("/admin/map");

        assertThat(fixture.blockedWriteAttempts.get())
                .as("route navigation must never attempt an API mutation")
                .isZero();
        assertThat(fixture.unexpectedReadRequests.get())
                .as("every post-login route read must have a local fixture response")
                .isZero();
    }

    private void registerFixtureRoutes(NavigationFixture fixture) {
        // Registration follows real authentication. It prevents every admin screen read in this
        // test from depending on shared data, and terminates an unexpected write in the browser.
        adminPage.route(url -> url.contains("/api/v1/"), fixture::handleApiRequest);
        adminPage.route(url -> pathOf(url).startsWith(MAP_STYLE_PREFIX), fixture::handleMapStyleRequest);
    }

    private void openSidebarDestination(SidebarDestination destination) {
        Locator sidebar = adminPage.getByRole(AriaRole.NAVIGATION).first();
        Locator button = sidebar.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(destination.label()));

        button.click();
        adminPage.waitForURL("**" + destination.clientPath());
        waitForBrowserPaint();

        assertThat(pathOf(adminPage.url())).isEqualTo(destination.clientPath());
        assertThat(button.getAttribute("aria-current")).isEqualTo("page");
    }

    private void awaitFixtureReads(NavigationFixture fixture, SidebarDestination destination) {
        if (destination.expectedReadPaths().isEmpty()) return;

        adminPage.waitForCondition(
                () -> fixture.servedAll(destination.expectedReadPaths()),
                new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(fixture.servedAll(destination.expectedReadPaths()))
                .as(destination.label() + " should render from its browser fixture")
                .isTrue();
    }

    private void waitForBrowserPaint() {
        adminPage.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private record SidebarDestination(String label, String clientPath, List<String> expectedReadPaths) { }

    private static final class NavigationFixture {
        private static final Set<String> EXPECTED_READ_PATHS = Set.of(
                ACTIVE_ORDERS_PATH,
                INTERVENTIONS_PATH,
                SUPPORT_TICKETS_PATH,
                REFUNDS_PATH,
                USERS_PATH,
                CATEGORIES_PATH,
                FLEET_RESTAURANTS_PATH,
                FLEET_DRIVERS_PATH,
                FLEET_CUSTOMERS_PATH,
                LEDGER_PATH,
                PENDING_PAYOUTS_PATH,
                LEDGER_REJECTIONS_PATH
        );

        private final Set<String> servedReadPaths = ConcurrentHashMap.newKeySet();
        private final AtomicInteger blockedWriteAttempts = new AtomicInteger();
        private final AtomicInteger unexpectedReadRequests = new AtomicInteger();

        private void handleApiRequest(Route route) {
            Request request = route.request();
            String method = request.method();
            String path = pathOf(request.url());

            if (!("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method))) {
                blockedWriteAttempts.incrementAndGet();
                fulfillJson(route, 405, "{\"message\":\"Navigation fixture blocked a mutation\"}");
                return;
            }

            if (!EXPECTED_READ_PATHS.contains(path)) {
                unexpectedReadRequests.incrementAndGet();
                fulfillJson(route, 404, "{\"message\":\"Navigation fixture has no response for this read\"}");
                return;
            }

            servedReadPaths.add(path);
            fulfillJson(route, 200, responseFor(path));
        }

        private void handleMapStyleRequest(Route route) {
            // A minimal valid MapLibre style prevents this navigation test from reaching the
            // external tile provider. Fleet-map behavior itself has dedicated fixture coverage.
            fulfillJson(route, 200, "{\"version\":8,\"name\":\"admin-navigation-fixture\",\"sources\":{},\"layers\":[]}");
        }

        private boolean servedAll(List<String> expectedPaths) {
            return servedReadPaths.containsAll(expectedPaths);
        }

        private static String responseFor(String path) {
            if (PENDING_PAYOUTS_PATH.equals(path)) {
                // PayoutQueue consumes a direct array rather than a page envelope.
                return "[]";
            }
            if (CATEGORIES_PATH.equals(path)) {
                // AdminCategories accepts the legacy { data: Category[] } response shape.
                return "{\"data\":[]}";
            }
            return emptyPageEnvelope();
        }

        private static String emptyPageEnvelope() {
            // Some admin views consume a Page at the response root while others consume an
            // ApiResponse<Page>. Supplying both documented shapes lets this navigation test stay
            // focused on routing while each screen renders its ordinary empty state.
            return """
                    {
                      "success": true,
                      "message": "admin navigation fixture",
                      "data": {
                        "content": [],
                        "totalElements": 0,
                        "totalPages": 1,
                        "last": true,
                        "size": 20,
                        "number": 0,
                        "first": true,
                        "numberOfElements": 0,
                        "empty": true
                      },
                      "content": [],
                      "totalElements": 0,
                      "totalPages": 1,
                      "last": true,
                      "size": 20,
                      "number": 0,
                      "first": true,
                      "numberOfElements": 0,
                      "empty": true,
                      "timestamp": "2026-09-29T10:00:00Z"
                    }
                    """;
        }
    }
}
