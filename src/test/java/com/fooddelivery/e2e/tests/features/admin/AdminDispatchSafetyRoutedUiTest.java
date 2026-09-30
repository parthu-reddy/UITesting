package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.AdminDispatchSafetyPage;
import com.fooddelivery.e2e.pages.admin.AdminManualInterventionsPage;
import com.fooddelivery.e2e.pages.admin.AdminPortalPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
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
 * Browser-routed safety coverage for Live Operations dispatch.
 *
 * <p>The administrator logs in to the deployed application normally. The active-order,
 * restaurant-location, available-driver, and intervention-queue reads are fulfilled in the
 * browser with disposable fixtures. Guard scenarios abort mutations; explicit positive scenarios
 * accept only the expected fixture request and then remove the fixture order. This makes the
 * stale-result and audited-workflow handoff checks deterministic without touching a real order,
 * rider, or refund ticket.</p>
 */
@Tag("admin")
@Tag("admin-dispatch")
@Tag("browser-routed")
public class AdminDispatchSafetyRoutedUiTest extends TestBase {

    private static final String ORDER_A_ID = "a1000000-0000-4000-8000-000000000001";
    private static final String ORDER_B_ID = "b1000000-0000-4000-8000-000000000001";
    private static final String CUSTOMER_A_ID = "a1000000-0000-4000-8000-000000000002";
    private static final String CUSTOMER_B_ID = "b1000000-0000-4000-8000-000000000002";
    private static final String RESTAURANT_A_ID = "a1000000-0000-4000-8000-000000000003";
    private static final String RESTAURANT_B_ID = "b1000000-0000-4000-8000-000000000003";
    private static final String ORDER_A_ITEM_ID = "a1000000-0000-4000-8000-000000000004";
    private static final String ORDER_B_ITEM_ID = "b1000000-0000-4000-8000-000000000004";
    private static final String MENU_A_ITEM_ID = "a1000000-0000-4000-8000-000000000005";
    private static final String MENU_B_ITEM_ID = "b1000000-0000-4000-8000-000000000005";
    private static final String DRIVER_A_ID = "a1000000-0000-4000-8000-000000000006";
    private static final String DRIVER_B_ID = "b1000000-0000-4000-8000-000000000006";

    private static final String CITY_A = "fixture-city-a";
    private static final String CITY_B = "fixture-city-b";
    private static final String DRIVER_A_NAME = "Fixture stale driver A";
    private static final String DRIVER_B_NAME = "Fixture current driver B";
    private static final String FIXTURE_TIME = "2026-09-29T10:00:00Z";

    private static final String ACTIVE_ORDERS_PATH = "/api/v1/internal/admin/orders/active-all";
    private static final String INTERVENTION_QUEUE_PATH = "/api/v1/internal/admin/orders/intervention";
    private static final String AVAILABLE_DRIVERS_PATH = "/api/v1/internal/admin/delivery/drivers/available-with-location";

    private AdminPortalPage portal;

    @BeforeEach
    void authenticateLiveAdmin() {
        adminPage.setViewportSize(1440, 1000);
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-01: stale candidates stay hidden and Live Ops routes manual dispatch to the audited workflow")
    void staleCandidatesAreDiscardedAndLiveOpsCannotDirectlyAssign() {
        DispatchFixture fixture = new DispatchFixture();
        registerFixtureRoutes(fixture);
        AdminDispatchSafetyPage dispatch = openFixtureLiveOps();

        // Keep both candidate responses pending. Releasing A only after B is selected makes A a
        // true stale result, not merely a slower initial load.
        dispatch.selectOrder(ORDER_A_ID);
        waitFor(() -> fixture.staleCandidateRoute.get() != null,
                "the selected A order must request location-scoped driver candidates");

        dispatch.selectOrder(ORDER_B_ID);
        waitFor(() -> fixture.currentCandidateRoute.get() != null,
                "the selected B order must request its own location-scoped driver candidates");

        Response staleResponse = adminPage.waitForResponse(
                response -> isCandidateResponse(response, CITY_A),
                fixture::releaseStaleCandidates);
        assertThat(staleResponse.status()).isEqualTo(200);
        waitForBrowserPaint();

        assertThat(dispatch.isDriverVisible(DRIVER_A_NAME))
                .as("the A candidate must not be rendered after B becomes the selected order")
                .isFalse();
        assertThat(dispatch.availableDriverHeading()).isEqualTo("Nearby Ready Drivers (0)");
        assertThat(dispatch.hasDirectAssignmentAction())
                .as("Live Operations must never expose the retired direct assignment action")
                .isFalse();

        Response currentResponse = adminPage.waitForResponse(
                response -> isCandidateResponse(response, CITY_B),
                fixture::releaseCurrentCandidates);
        assertThat(currentResponse.status()).isEqualTo(200);

        dispatch.waitForNearbyDriver(DRIVER_B_NAME);
        assertThat(dispatch.availableDriverHeading()).isEqualTo("Nearby Ready Drivers (1)");
        assertThat(dispatch.isDriverVisible(DRIVER_A_NAME))
                .as("only the B-scoped rider can remain visible")
                .isFalse();
        assertThat(dispatch.hasDirectAssignmentAction()).isFalse();
        assertThat(adminPage.getByText(
                "Direct refunds are unavailable here until the server provides an authenticated, idempotent admin refund command.")
                .isVisible()).isTrue();
        assertThat(adminPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Open Refund Queue").setExact(true)).isVisible()).isTrue();
        assertThat(adminPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Partial Refund").setExact(true)).count()).isZero();
        assertThat(adminPage.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Post-Delivery").setExact(true)).count()).isZero();
        assertNoUnsafeWrites(fixture);

        dispatch.openManualInterventions();
        dispatch.waitForManualInterventions();
        assertThat(adminPage.url()).contains("/admin/interventions");
        assertNoUnsafeWrites(fixture);

        assertThat(fixture.activeOrderReads.get()).isGreaterThanOrEqualTo(1);
        assertThat(fixture.restaurantReads.get()).isGreaterThanOrEqualTo(2);
        assertThat(fixture.staleCandidateReads.get()).isEqualTo(1);
        assertThat(fixture.currentCandidateReads.get()).isEqualTo(1);
        assertThat(fixture.unexpectedCandidateReads.get()).isZero();
        assertThat(fixture.unexpectedApiRequests.get())
                .as("every post-login API request must be an explicitly routed fixture")
                .isZero();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-02: Manual assignment and cancellation require reasons and confirmations without writes")
    void manualInterventionGuardsAreDeterministicAndDoNotWrite() {
        ManualInterventionFixture fixture = new ManualInterventionFixture();
        registerFixtureRoutes(fixture);

        Response queueResponse = adminPage.waitForResponse(
                response -> "GET".equals(response.request().method())
                        && INTERVENTION_QUEUE_PATH.equals(path(response.url())),
                portal::openInterventionsTab);
        assertThat(queueResponse.status()).isEqualTo(200);

        AdminManualInterventionsPage interventions = new AdminManualInterventionsPage(adminPage);
        interventions.waitForQueue();
        assertThat(interventions.getInterventionCount()).isEqualTo(1);
        interventions.selectIntervention(0);
        interventions.waitForDriverCandidates();

        assertThat(interventions.getForceAssignButtonCount()).isEqualTo(1);
        assertThat(interventions.isForceAssignEnabled())
                .as("an audited manual assignment must have an operator reason")
                .isFalse();
        assertThat(interventions.isCancellationRequestEnabled())
                .as("an audited cancellation must have an operator reason")
                .isFalse();

        interventions.fillAssignmentReason("Rider verified pickup access");
        assertThat(interventions.isForceAssignEnabled()).isTrue();
        interventions.openForceAssignConfirmation();
        assertThat(interventions.confirmationDialog().isVisible()).isTrue();
        assertThat(interventions.confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Force assign").setExact(true)).isVisible()).isTrue();
        interventions.cancelConfirmation();

        interventions.fillCancellationReason("No safe rider is available");
        assertThat(interventions.isCancellationRequestEnabled()).isTrue();
        interventions.openCancellationConfirmation();
        assertThat(interventions.cancellationConfirmationDialog().isVisible()).isTrue();
        assertThat(interventions.cancellationConfirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Request cancellation").setExact(true)).isVisible()).isTrue();
        interventions.cancelCancellationConfirmation();

        assertNoUnsafeWrites(fixture);
        assertThat(fixture.interventionReads.get()).isPositive();
        assertThat(fixture.restaurantReads.get()).isPositive();
        assertThat(fixture.driverReads.get()).isPositive();
        assertThat(fixture.unexpectedApiRequests.get())
                .as("every post-login API request must be an explicitly routed fixture")
                .isZero();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-03: A durable manual-assignment rejection is visible and permits a new audited attempt")
    void manualAssignmentFailureBecomesVisibleAndReenablesTheNextAttempt() {
        ManualInterventionFixture fixture = ManualInterventionFixture.rejectAssignmentAfterAcceptance();
        registerFixtureRoutes(fixture);

        Response queueResponse = adminPage.waitForResponse(
                response -> "GET".equals(response.request().method())
                        && INTERVENTION_QUEUE_PATH.equals(path(response.url())),
                portal::openInterventionsTab);
        assertThat(queueResponse.status()).isEqualTo(200);

        AdminManualInterventionsPage interventions = new AdminManualInterventionsPage(adminPage);
        interventions.waitForQueue();
        interventions.selectIntervention(0);
        interventions.waitForDriverCandidates();
        String initialReason = "Rider confirmed pickup readiness";
        interventions.fillAssignmentReason(initialReason);
        assertThat(interventions.isForceAssignEnabled()).isTrue();

        interventions.openForceAssignConfirmation();
        interventions.confirmationDialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Force assign").setExact(true)).click();

        waitFor(() -> fixture.assignmentWrites.get() == 1,
                "the browser-local fixture receives the confirmed manual assignment");
        assertThat(fixture.assignmentRequestBody.get())
                .contains("\"deliveryExecutiveId\":\"" + DRIVER_B_ID + "\"")
                .contains("\"reason\":\"" + initialReason + "\"");
        assertThat(fixture.assignmentIdempotencyKey.get())
                .as("manual assignment retains an API-valid idempotency key")
                .matches("^[A-Za-z0-9][A-Za-z0-9._:-]{7,79}$");

        adminPage.getByText(
                        "Manual assignment is being validated. The controls will reopen if dispatch rejects it.",
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        waitFor(() -> fixture.pendingFailureQueueRoute.get() != null,
                "the post-command queue refresh is held until the durable dispatch result is available");

        Response durableFailure = adminPage.waitForResponse(
                response -> "GET".equals(response.request().method())
                        && INTERVENTION_QUEUE_PATH.equals(path(response.url())),
                fixture::releaseDurableFailure);
        assertThat(durableFailure.status()).isEqualTo(200);
        waitForBrowserPaint();

        adminPage.getByText(
                        "The previous manual assignment was not applied: The rider is no longer online.",
                        new Page.GetByTextOptions().setExact(true))
                .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        interventions.fillAssignmentReason("Replacement rider verified at restaurant");
        assertThat(interventions.isForceAssignEnabled())
                .as("a new reason starts a new audited operation after the durable rejection")
                .isTrue();
        assertThat(fixture.blockedUnsafeWrites.get()).isZero();
        assertThat(fixture.cancellationWrites.get()).isZero();
        assertThat(fixture.unexpectedApiRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-04: confirmed manual assignment is submitted once and the resolved intervention leaves the queue")
    void acceptedManualAssignmentRemovesTheResolvedIntervention() {
        ManualInterventionFixture fixture = ManualInterventionFixture.acceptAssignmentAndRemove();
        registerFixtureRoutes(fixture);

        AdminManualInterventionsPage interventions = openManualInterventionQueue();
        String reason = "Rider confirmed at the pickup location";
        interventions.fillAssignmentReason(reason);
        interventions.openForceAssignConfirmation();
        interventions.confirmForceAssignment();

        waitFor(() -> fixture.assignmentWrites.get() == 1,
                "the confirmed assignment is sent exactly once to the audited endpoint");
        assertThat(fixture.assignmentRequestBody.get())
                .contains("\"deliveryExecutiveId\":\"" + DRIVER_B_ID + "\"")
                .contains("\"reason\":\"" + reason + "\"");
        assertThat(fixture.assignmentIdempotencyKey.get())
                .matches("^[A-Za-z0-9][A-Za-z0-9._:-]{7,79}$");
        interventions.waitForEmptyQueue();
        assertThat(interventions.getInterventionCount()).isZero();
        assertThat(fixture.cancellationWrites.get()).isZero();
        assertThat(fixture.blockedUnsafeWrites.get()).isZero();
        assertThat(fixture.unexpectedApiRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-05: confirmed cancellation is submitted once and the resolved intervention leaves the queue")
    void acceptedCancellationRemovesTheResolvedIntervention() {
        ManualInterventionFixture fixture = ManualInterventionFixture.acceptCancellationAndRemove();
        registerFixtureRoutes(fixture);

        AdminManualInterventionsPage interventions = openManualInterventionQueue();
        String reason = "No eligible rider remains in this delivery city";
        interventions.fillCancellationReason(reason);
        interventions.openCancellationConfirmation();
        interventions.confirmCancellation();

        waitFor(() -> fixture.cancellationWrites.get() == 1,
                "the confirmed cancellation is sent exactly once to the audited endpoint");
        assertThat(fixture.cancellationRequestBody.get())
                .contains("\"reason\":\"" + reason + "\"");
        assertThat(fixture.cancellationIdempotencyKey.get())
                .matches("^[A-Za-z0-9][A-Za-z0-9._:-]{7,79}$");
        interventions.waitForEmptyQueue();
        assertThat(interventions.getInterventionCount()).isZero();
        assertThat(fixture.assignmentWrites.get()).isZero();
        assertThat(fixture.blockedUnsafeWrites.get()).isZero();
        assertThat(fixture.unexpectedApiRequests.get()).isZero();
    }

    @Test
    @DisplayName("ADMIN-DISPATCH-SAFE-06: a rejected cancellation keeps the intervention and reason visible without a false success")
    void rejectedCancellationKeepsTheInterventionCorrectableWithoutFalseSuccess() {
        ManualInterventionFixture fixture = ManualInterventionFixture.rejectCancellation();
        registerFixtureRoutes(fixture);

        AdminManualInterventionsPage interventions = openManualInterventionQueue();
        String reason = "The restaurant cannot safely fulfill this order";
        interventions.fillCancellationReason(reason);
        interventions.openCancellationConfirmation();
        interventions.confirmCancellation();

        waitFor(() -> fixture.cancellationWrites.get() == 1,
                "the rejected cancellation reaches only the browser fixture");
        assertThat(fixture.cancellationRequestBody.get()).contains("\"reason\":\"" + reason + "\"");
        assertThat(fixture.cancellationIdempotencyKey.get())
                .matches("^[A-Za-z0-9][A-Za-z0-9._:-]{7,79}$");
        interventions.waitForToast("Fixture cancellation rejected");
        assertThat(interventions.isDetailVisible())
                .as("the failed request must keep the selected intervention visible")
                .isTrue();
        assertThat(interventions.cancellationReasonValue())
                .as("the operator must be able to correct or retry the audited reason")
                .isEqualTo(reason);
        assertThat(interventions.isCancellationRequestEnabled())
                .as("the cancellation control must recover after a rejected request")
                .isTrue();
        assertThat(interventions.isToastVisible("Cancellation requested. The queue will update when processing finishes."))
                .as("a rejected request must never report a successful cancellation")
                .isFalse();
        assertThat(interventions.getInterventionCount())
                .as("the intervention remains until the server accepts the cancellation")
                .isEqualTo(1);
        assertThat(fixture.assignmentWrites.get()).isZero();
        assertThat(fixture.blockedUnsafeWrites.get()).isZero();
        assertThat(fixture.unexpectedApiRequests.get()).isZero();
    }

    private AdminManualInterventionsPage openManualInterventionQueue() {
        Response queueResponse = adminPage.waitForResponse(
                response -> "GET".equals(response.request().method())
                        && INTERVENTION_QUEUE_PATH.equals(path(response.url())),
                portal::openInterventionsTab);
        assertThat(queueResponse.status()).isEqualTo(200);

        AdminManualInterventionsPage interventions = new AdminManualInterventionsPage(adminPage);
        interventions.waitForQueue();
        interventions.selectIntervention(0);
        interventions.waitForDriverCandidates();
        return interventions;
    }

    private AdminDispatchSafetyPage openFixtureLiveOps() {
        portal.openLiveOpsTab();
        AdminDispatchSafetyPage dispatch = new AdminDispatchSafetyPage(adminPage);
        dispatch.waitForFixtureOrders(ORDER_A_ID, ORDER_B_ID);
        return dispatch;
    }

    private void registerFixtureRoutes(DispatchFixture fixture) {
        // Login precedes this registration. Authentication remains a real deployment check;
        // only the reads that drive this dispatch test become browser-local fixtures.
        adminPage.route(url -> url.contains("/api/v1/"), fixture::handle);
    }

    private void registerFixtureRoutes(ManualInterventionFixture fixture) {
        // This route is installed after a real administrator login. The action buttons are
        // exercised against disposable browser responses, so cancelling a confirmation cannot
        // alter a shared Dev order or initiate a refund.
        adminPage.route(url -> url.contains("/api/v1/"), fixture::handle);
    }

    private void waitFor(java.util.function.BooleanSupplier condition, String expectation) {
        adminPage.waitForCondition(condition, new Page.WaitForConditionOptions().setTimeout(10_000));
        assertThat(condition.getAsBoolean()).as(expectation).isTrue();
    }

    private void waitForBrowserPaint() {
        // A routed response is observable before React's promise continuation renders. Two frames
        // let that continuation commit without relying on a wall-clock sleep.
        adminPage.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
    }

    private static void assertNoUnsafeWrites(DispatchFixture fixture) {
        assertThat(fixture.assignmentWrites.get())
                .as("the test must not send a driver-assignment mutation")
                .isZero();
        assertThat(fixture.cancellationWrites.get())
                .as("the test must not send a cancellation mutation")
                .isZero();
        assertThat(fixture.blockedUnsafeWrites.get())
                .as("no blocked mutation should be attempted before the confirmation is accepted")
                .isZero();
    }

    private static boolean isCandidateResponse(Response response, String cityId) {
        return "GET".equals(response.request().method())
                && AVAILABLE_DRIVERS_PATH.equals(path(response.url()))
                && cityId.equals(queryValue(response.url(), "cityId"));
    }

    private static String path(String url) {
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

    private static final class DispatchFixture {
        private final AtomicInteger activeOrderReads = new AtomicInteger();
        private final AtomicInteger interventionCountReads = new AtomicInteger();
        private final AtomicInteger restaurantReads = new AtomicInteger();
        private final AtomicInteger staleCandidateReads = new AtomicInteger();
        private final AtomicInteger currentCandidateReads = new AtomicInteger();
        private final AtomicInteger unexpectedCandidateReads = new AtomicInteger();
        private final AtomicInteger assignmentWrites = new AtomicInteger();
        private final AtomicInteger cancellationWrites = new AtomicInteger();
        private final AtomicInteger blockedUnsafeWrites = new AtomicInteger();
        private final AtomicInteger unexpectedApiRequests = new AtomicInteger();

        private final AtomicReference<Route> staleCandidateRoute = new AtomicReference<>();
        private final AtomicReference<Route> currentCandidateRoute = new AtomicReference<>();

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String requestPath = path(request.url());

            if (isUnsafeDispatchWrite(method, requestPath)) {
                blockedUnsafeWrites.incrementAndGet();
                if (requestPath.contains("/assign")) assignmentWrites.incrementAndGet();
                if (requestPath.contains("/cancel")) cancellationWrites.incrementAndGet();
                route.abort();
                return;
            }

            if ("GET".equals(method) && ACTIVE_ORDERS_PATH.equals(requestPath)) {
                activeOrderReads.incrementAndGet();
                fulfillJson(route, activeOrdersResponse());
                return;
            }

            // The AdminPortal sidebar maintains this badge while Live Operations is open. It is
            // not part of the dispatch assertion, but it must stay local once the API route is
            // installed so an incidental poll cannot escape to the deployment.
            if ("GET".equals(method) && INTERVENTION_QUEUE_PATH.equals(requestPath)) {
                interventionCountReads.incrementAndGet();
                fulfillJson(route, "[]");
                return;
            }

            if ("GET".equals(method) && requestPath.startsWith("/api/v1/restaurants/")) {
                restaurantReads.incrementAndGet();
                fulfillJson(route, restaurantResponse(requestPath));
                return;
            }

            if ("GET".equals(method) && AVAILABLE_DRIVERS_PATH.equals(requestPath)) {
                String cityId = queryValue(request.url(), "cityId");
                if (CITY_A.equals(cityId)) {
                    staleCandidateReads.incrementAndGet();
                    captureOrFulfillEmpty(route, staleCandidateRoute);
                    return;
                }
                if (CITY_B.equals(cityId)) {
                    currentCandidateReads.incrementAndGet();
                    captureOrFulfillEmpty(route, currentCandidateRoute);
                    return;
                }
                unexpectedCandidateReads.incrementAndGet();
                fulfillJson(route, "[]");
                return;
            }

            unexpectedApiRequests.incrementAndGet();
            fulfillJson(route, 405, """
                    {
                      "message": "Unexpected API request in admin dispatch fixture"
                    }
                    """);
        }

        private void captureOrFulfillEmpty(Route route, AtomicReference<Route> target) {
            // Playwright intentionally leaves a route pending if the handler does not resolve it.
            // The test later resolves that exact response after the other order has been selected.
            if (target.compareAndSet(null, route)) return;
            fulfillJson(route, "[]");
        }

        private void releaseStaleCandidates() {
            Route route = staleCandidateRoute.getAndSet(null);
            if (route == null) throw new IllegalStateException("No delayed A candidate route was captured");
            fulfillJson(route, driverResponse(DRIVER_A_ID, DRIVER_A_NAME, 12.9720, 77.5950));
        }

        private void releaseCurrentCandidates() {
            Route route = currentCandidateRoute.getAndSet(null);
            if (route == null) throw new IllegalStateException("No delayed B candidate route was captured");
            fulfillJson(route, driverResponse(DRIVER_B_ID, DRIVER_B_NAME, 12.9810, 77.6050));
        }

        private static boolean isUnsafeDispatchWrite(String method, String requestPath) {
            if (!("POST".equals(method) || "PUT".equals(method)
                    || "PATCH".equals(method) || "DELETE".equals(method))) {
                return false;
            }
            if (!requestPath.startsWith("/api/v1/internal/admin/")) return false;
            return requestPath.contains("/assign") || requestPath.contains("/cancel");
        }
    }

    /**
     * A single intervention with one scoped, ready rider. It is deliberately browser-local so
     * the confirmation guard can be tested every run without depending on the shared queue.
     */
    private static final class ManualInterventionFixture {
        private enum AssignmentOutcome { BLOCK, ACCEPT_THEN_DURABLY_REJECT, ACCEPT_AND_REMOVE }
        private enum CancellationOutcome { BLOCK, ACCEPT_AND_REMOVE, REJECT }

        private final AssignmentOutcome assignmentOutcome;
        private final CancellationOutcome cancellationOutcome;
        private final AtomicInteger interventionReads = new AtomicInteger();
        private final AtomicInteger restaurantReads = new AtomicInteger();
        private final AtomicInteger driverReads = new AtomicInteger();
        private final AtomicInteger assignmentWrites = new AtomicInteger();
        private final AtomicInteger cancellationWrites = new AtomicInteger();
        private final AtomicInteger blockedUnsafeWrites = new AtomicInteger();
        private final AtomicInteger unexpectedApiRequests = new AtomicInteger();
        private final AtomicBoolean assignmentAccepted = new AtomicBoolean(false);
        private final AtomicBoolean cancellationAccepted = new AtomicBoolean(false);
        private final AtomicBoolean durableFailureReleased = new AtomicBoolean(false);
        private final AtomicReference<Route> pendingFailureQueueRoute = new AtomicReference<>();
        private final AtomicReference<String> assignmentRequestBody = new AtomicReference<>();
        private final AtomicReference<String> assignmentIdempotencyKey = new AtomicReference<>();
        private final AtomicReference<String> cancellationRequestBody = new AtomicReference<>();
        private final AtomicReference<String> cancellationIdempotencyKey = new AtomicReference<>();

        private ManualInterventionFixture() {
            this(AssignmentOutcome.BLOCK, CancellationOutcome.BLOCK);
        }

        private ManualInterventionFixture(AssignmentOutcome assignmentOutcome) {
            this(assignmentOutcome, CancellationOutcome.BLOCK);
        }

        private ManualInterventionFixture(AssignmentOutcome assignmentOutcome,
                                          CancellationOutcome cancellationOutcome) {
            this.assignmentOutcome = assignmentOutcome;
            this.cancellationOutcome = cancellationOutcome;
        }

        private static ManualInterventionFixture rejectAssignmentAfterAcceptance() {
            return new ManualInterventionFixture(AssignmentOutcome.ACCEPT_THEN_DURABLY_REJECT);
        }

        private static ManualInterventionFixture acceptAssignmentAndRemove() {
            return new ManualInterventionFixture(AssignmentOutcome.ACCEPT_AND_REMOVE);
        }

        private static ManualInterventionFixture acceptCancellationAndRemove() {
            return new ManualInterventionFixture(AssignmentOutcome.BLOCK, CancellationOutcome.ACCEPT_AND_REMOVE);
        }

        private static ManualInterventionFixture rejectCancellation() {
            return new ManualInterventionFixture(AssignmentOutcome.BLOCK, CancellationOutcome.REJECT);
        }

        private void handle(Route route) {
            Request request = route.request();
            String method = request.method();
            String requestPath = path(request.url());

            if (isManualInterventionWrite(method, requestPath)) {
                if (requestPath.endsWith("/assign-driver")) {
                    assignmentWrites.incrementAndGet();
                    if (assignmentOutcome != AssignmentOutcome.BLOCK) {
                        assignmentRequestBody.set(request.postData());
                        assignmentIdempotencyKey.set(request.headerValue("Idempotency-Key"));
                        assignmentAccepted.set(true);
                        fulfillJson(route, """
                                {
                                  "success": true,
                                  "message": "fixture manual assignment accepted",
                                  "data": "queued",
                                  "timestamp": "%s"
                                }
                                """.formatted(FIXTURE_TIME));
                        return;
                    }
                }
                if (requestPath.endsWith("/cancel")) {
                    cancellationWrites.incrementAndGet();
                    cancellationRequestBody.set(request.postData());
                    cancellationIdempotencyKey.set(request.headerValue("Idempotency-Key"));
                    if (cancellationOutcome == CancellationOutcome.ACCEPT_AND_REMOVE) {
                        cancellationAccepted.set(true);
                        fulfillJson(route, """
                                {
                                  "success": true,
                                  "message": "fixture cancellation accepted",
                                  "data": "queued",
                                  "timestamp": "%s"
                                }
                                """.formatted(FIXTURE_TIME));
                        return;
                    }
                    if (cancellationOutcome == CancellationOutcome.REJECT) {
                        fulfillJson(route, 409, "{\"message\":\"Fixture cancellation rejected\"}");
                        return;
                    }
                }
                blockedUnsafeWrites.incrementAndGet();
                route.abort();
                return;
            }

            if ("GET".equals(method) && INTERVENTION_QUEUE_PATH.equals(requestPath)) {
                interventionReads.incrementAndGet();
                if ((assignmentOutcome == AssignmentOutcome.ACCEPT_AND_REMOVE && assignmentAccepted.get())
                        || (cancellationOutcome == CancellationOutcome.ACCEPT_AND_REMOVE && cancellationAccepted.get())) {
                    fulfillJson(route, emptyManualInterventionQueueResponse());
                    return;
                }
                if (assignmentOutcome == AssignmentOutcome.ACCEPT_THEN_DURABLY_REJECT
                        && assignmentAccepted.get()
                        && !durableFailureReleased.get()
                        && pendingFailureQueueRoute.compareAndSet(null, route)) {
                    return;
                }
                fulfillJson(route, manualInterventionQueueResponse(durableFailureReleased.get()));
                return;
            }
            if ("GET".equals(method) && requestPath.startsWith("/api/v1/restaurants/")) {
                restaurantReads.incrementAndGet();
                fulfillJson(route, restaurantResponse(requestPath));
                return;
            }
            if ("GET".equals(method) && AVAILABLE_DRIVERS_PATH.equals(requestPath)) {
                driverReads.incrementAndGet();
                fulfillJson(route, driverResponse(DRIVER_B_ID, DRIVER_B_NAME, 12.9810, 77.6050));
                return;
            }

            unexpectedApiRequests.incrementAndGet();
            fulfillJson(route, 405, "{\"message\":\"Unexpected API request in manual intervention fixture\"}");
        }

        private void releaseDurableFailure() {
            Route route = pendingFailureQueueRoute.getAndSet(null);
            if (route == null) {
                throw new IllegalStateException("No pending manual-intervention refresh is available to release");
            }
            durableFailureReleased.set(true);
            fulfillJson(route, manualInterventionQueueResponse(true));
        }
    }

    private static boolean isManualInterventionWrite(String method, String requestPath) {
        if (!("POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method))) {
            return false;
        }
        if (!requestPath.startsWith("/api/v1/internal/admin/orders/intervention/")) return false;
        return requestPath.endsWith("/assign-driver")
                || requestPath.endsWith("/cancel")
                || requestPath.endsWith("/force-cancel");
    }

    private static void assertNoUnsafeWrites(ManualInterventionFixture fixture) {
        assertThat(fixture.assignmentWrites.get())
                .as("cancelling the force-assignment confirmation must not post an assignment")
                .isZero();
        assertThat(fixture.cancellationWrites.get())
                .as("cancelling the cancellation confirmation must not post a cancellation")
                .isZero();
        assertThat(fixture.blockedUnsafeWrites.get())
                .as("no manual-intervention mutation should be attempted before confirmation is accepted")
                .isZero();
    }

    private static void fulfillJson(Route route, String body) {
        fulfillJson(route, 200, body);
    }

    private static void fulfillJson(Route route, int status, String body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setBody(body));
    }

    private static String activeOrdersResponse() {
        return """
                {
                  "success": true,
                  "message": "fixture",
                  "data": {
                    "content": [%s, %s],
                    "totalElements": 2,
                    "totalPages": 1,
                    "last": true,
                    "size": 50,
                    "number": 0,
                    "first": true,
                    "numberOfElements": 2,
                    "empty": false
                  },
                  "timestamp": "%s"
                }
                """.formatted(orderResponse(
                ORDER_A_ID, CUSTOMER_A_ID, RESTAURANT_A_ID, ORDER_A_ITEM_ID, MENU_A_ITEM_ID,
                "Fixture Restaurant A", CITY_A), orderResponse(
                ORDER_B_ID, CUSTOMER_B_ID, RESTAURANT_B_ID, ORDER_B_ITEM_ID, MENU_B_ITEM_ID,
                "Fixture Restaurant B", CITY_B), FIXTURE_TIME);
    }

    private static String manualInterventionQueueResponse(boolean durableFailure) {
        String failureFields = durableFailure
                ? """
                          ,"manualInterventionFailureCode": "DRIVER_NOT_ONLINE",
                          "manualInterventionFailedAt": "2026-09-29T10:01:00Z"
                        """
                : "";
        return """
                {
                  "content": [{
                    "id": "%s",
                    "customerId": "%s",
                    "restaurantId": "%s",
                    "restaurantName": "Manual Intervention Fixture Kitchen",
                    "status": "ACCEPTED",
                    "deliveryStatus": "MANUAL_INTERVENTION_REQUIRED",
                    "totalAmount": 199.0,
                    "itemTotal": 159.0,
                    "customerPlatformFee": 0.0,
                    "sgst": 0.0,
                    "cgst": 0.0,
                    "deliveryFee": 40.0,
                    "deliveryAddress": "Manual intervention fixture address",
                    "items": [{
                      "id": "%s",
                      "menuItemId": "%s",
                      "name": "Manual intervention fixture dish",
                      "quantity": 1,
                      "price": 159.0
                    }],
                    "createdAt": "%s",
                    "updatedAt": "%s",
                    "dispatchCityId": "%s",
                    "fleetSearchRadiusKm": 5.0%s
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
                """.formatted(ORDER_B_ID, CUSTOMER_B_ID, RESTAURANT_B_ID, ORDER_B_ITEM_ID,
                MENU_B_ITEM_ID, FIXTURE_TIME, FIXTURE_TIME, CITY_B, failureFields);
    }

    private static String emptyManualInterventionQueueResponse() {
        return """
                {
                  "content": [],
                  "totalElements": 0,
                  "totalPages": 0,
                  "last": true,
                  "size": 20,
                  "number": 0,
                  "first": true,
                  "numberOfElements": 0,
                  "empty": true
                }
                """;
    }

    private static String orderResponse(String orderId, String customerId, String restaurantId,
                                        String orderItemId, String menuItemId, String restaurantName,
                                        String cityId) {
        return """
                {
                  "id": "%s",
                  "customerId": "%s",
                  "restaurantId": "%s",
                  "restaurantName": "%s",
                  "status": "ACCEPTED",
                  "deliveryStatus": "SEARCHING_FOR_DRIVER",
                  "totalAmount": 199.0,
                  "itemTotal": 159.0,
                  "customerPlatformFee": 0.0,
                  "sgst": 0.0,
                  "cgst": 0.0,
                  "deliveryFee": 40.0,
                  "deliveryAddress": "Dispatch fixture address",
                  "items": [{
                    "id": "%s",
                    "menuItemId": "%s",
                    "name": "Dispatch fixture dish",
                    "quantity": 1,
                    "price": 159.0
                  }],
                  "createdAt": "%s",
                  "updatedAt": "%s",
                  "dispatchCityId": "%s",
                  "fleetSearchRadiusKm": 5.0
                }
                """.formatted(orderId, customerId, restaurantId, restaurantName, orderItemId,
                menuItemId, FIXTURE_TIME, FIXTURE_TIME, cityId);
    }

    private static String restaurantResponse(String requestPath) {
        String restaurantId = requestPath.endsWith(RESTAURANT_A_ID) ? RESTAURANT_A_ID : RESTAURANT_B_ID;
        return """
                {
                  "success": true,
                  "message": "fixture",
                  "data": {
                    "id": "%s",
                    "lat": 12.9716,
                    "lng": 77.5946
                  },
                  "timestamp": "%s"
                }
                """.formatted(restaurantId, FIXTURE_TIME);
    }

    private static String driverResponse(String driverId, String driverName, double lat, double lng) {
        return """
                [{
                  "id": "%s",
                  "fullName": "%s",
                  "lat": %s,
                  "lng": %s,
                  "status": "ONLINE"
                }]
                """.formatted(driverId, driverName, lat, lng);
    }

}
