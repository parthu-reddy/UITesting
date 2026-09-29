package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.*;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Admin Fleet Map, LiveOps pagination/refunds, and Operations DLQ tabs.
 * Covers: FLEET-01..06, LIVEOPS-01..09, OPS-TAB-01..07
 */
@Tag("admin-liveops")
public class AdminLiveOpsFleetTest extends TestBase {

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    private AdminLiveOpsPage openLoadedLiveOps() {
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/active-all")
                                && "GET".equals(r.request().method()),
                portal::openLiveOpsTab);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);
        AdminLiveOpsPage liveOps = new AdminLiveOpsPage(adminPage);
        assertThat(liveOps.isLiveOpsVisible()).isTrue();
        liveOps.waitForOrdersLoaded();
        return liveOps;
    }

    private AdminFleetMapPage openLoadedFleetMap() {
        portal.openLiveOpsTab();
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/delivery/drivers/all-with-location")
                                && "GET".equals(r.request().method()),
                portal::openFleetTab);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);
        AdminFleetMapPage fleet = new AdminFleetMapPage(adminPage);
        fleet.waitForFleetMap();
        return fleet;
    }

    // ── FLEET MAP SCENARIOS ─────────────────────────────────────────────

    @Test
    @DisplayName("FLEET-01: Fleet map visible")
    void fleetMapVisible() {
        AdminFleetMapPage fleet = openLoadedFleetMap();
        assertThat(fleet.isFleetMapVisible()).isTrue();
    }

    @Test
    @DisplayName("FLEET-02: Driver markers on fleet map")
    void driverMarkersOnFleetMap() {
        AdminFleetMapPage fleet = openLoadedFleetMap();
        int count = fleet.getDriverMarkerCount();
        Assumptions.assumeTrue(count > 0,
                "The shared Dev fleet currently has no rider with a plotted location");
        assertThat(count).isPositive();
        assertThat(fleet.getRiderMarkers()).allSatisfy(marker ->
                assertThat(marker.getAttribute("data-pin-tone")).matches("rider(-offline)?"));
    }

    @Test
    @DisplayName("FLEET-03: Select driver and view detail")
    void selectDriverViewDetail() {
        AdminFleetMapPage fleet = openLoadedFleetMap();
        Assumptions.assumeTrue(fleet.getDriverMarkerCount() > 0,
                "A rider map marker is required for the marker detail check");
        fleet.selectDriver(0);
        assertThat(fleet.isDriverDetailVisible()).isTrue();
        assertThat(fleet.getDriverName()).isNotBlank();
    }

    @Test
    @DisplayName("FLEET-06: Refresh fleet map")
    void refreshFleetMap() {
        AdminFleetMapPage fleet = openLoadedFleetMap();
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/delivery/drivers/all-with-location")
                                && "GET".equals(r.request().method()),
                fleet::refreshMap);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);
        assertThat(fleet.isFleetMapVisible()).isTrue();
    }

    // ── LIVEOPS SCENARIOS ───────────────────────────────────────────────

    @Test
    @DisplayName("LIVEOPS-01: Live operations tab visible")
    void liveOpsTabVisible() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        assertThat(liveOps.isLiveOpsVisible()).isTrue();
    }

    @Test
    @DisplayName("LIVEOPS-02: Active order count")
    void activeOrderCount() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        int count = liveOps.getActiveOrderCount();
        if (count == 0) {
            assertThat(liveOps.isEmptyStateVisible()).isTrue();
        } else {
            assertThat(count).isPositive();
            assertThat(liveOps.isEmptyStateVisible()).isFalse();
        }
    }

    @Test
    @DisplayName("LIVEOPS-04: Available driver count")
    void availableDriverCount() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        if (liveOps.getActiveOrderCount() == 0) {
            assertThat(liveOps.isEmptyStateVisible()).isTrue();
            return;
        }

        liveOps.selectOrder(0);
        assertThat(liveOps.isOrderSelected()).isTrue();
        String heading = liveOps.getAvailableDriverHeading();
        int declaredCount = Integer.parseInt(heading.replaceAll("[^0-9]", ""));
        int assignButtons = liveOps.getAvailableDriverCount();
        assertThat(assignButtons).isEqualTo(declaredCount);
        if (declaredCount == 0) {
            assertThat(liveOps.isNoAvailableDriverMessageVisible()).isTrue();
        } else {
            assertThat(adminPage.getByRole(AriaRole.BUTTON,
                    new com.microsoft.playwright.Page.GetByRoleOptions().setName("Assign").setExact(true)).count())
                    .isEqualTo(declaredCount);
        }
    }

    @Test
    @DisplayName("LIVEOPS-09: Zero amount keeps refund actions disabled")
    void zeroRefundAmountIsBlocked() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        Assumptions.assumeTrue(liveOps.getActiveOrderCount() > 0,
                "An active order is required to inspect refund validation");
        liveOps.selectOrder(0);
        assertThat(liveOps.isOrderSelected()).isTrue();

        adminPage.getByPlaceholder("Amount (₹)").fill("0");
        assertThat(adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Partial Refund").setExact(true))
                .isDisabled()).isTrue();
        assertThat(adminPage.getByRole(AriaRole.BUTTON,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName("Post-Delivery").setExact(true))
                .isDisabled()).isTrue();
    }

    @Test
    @DisplayName("LIVEOPS-07: Refresh live ops")
    void refreshLiveOps() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/active-all")
                                && "GET".equals(r.request().method()),
                liveOps::refresh);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);
        assertThat(liveOps.isLiveOpsVisible()).isTrue();
        liveOps.waitForOrdersLoaded();
    }

    @Test
    @DisplayName("LIVEOPS-08/09: LiveOps pagination")
    void liveOpsPagination() {
        AdminLiveOpsPage liveOps = openLoadedLiveOps();
        String initial = liveOps.getPageInfo();
        assertThat(initial).matches("Page 1 of [1-9][0-9]*");
        int totalPages = Integer.parseInt(initial.substring("Page 1 of ".length()));
        if (totalPages == 1) {
            assertThat(liveOps.canGoPreviousPage()).isFalse();
            assertThat(liveOps.canGoNextPage()).isFalse();
            return;
        }

        Response next = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/active-all")
                                && r.url().matches(".*[?&]page=1(?:&|$).*")
                                && "GET".equals(r.request().method()),
                liveOps::nextPage);
        assertThat(next.status()).isEqualTo(200);
        adminPage.getByText(Pattern.compile("^Page 2 of [0-9]+$"))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));

        Response previous = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/active-all")
                                && r.url().matches(".*[?&]page=0(?:&|$).*")
                                && "GET".equals(r.request().method()),
                liveOps::prevPage);
        assertThat(previous.status()).isEqualTo(200);
        adminPage.getByText(Pattern.compile("^Page 1 of [0-9]+$"))
                .waitFor(new Locator.WaitForOptions().setTimeout(15000));
    }

    @Test
    @DisplayName("ADMIN-OPS-06/10: Intervention queue is explicit and force assignment can be cancelled")
    void manualInterventionQueueAndSafeForceAssignConfirmation() {
        AtomicInteger assignmentRequests = new AtomicInteger();
        adminPage.onRequest(request -> {
            if ("POST".equals(request.method())
                    && request.url().contains("/orders/intervention/")
                    && request.url().contains("/assign-driver")) {
                assignmentRequests.incrementAndGet();
            }
        });

        Response response = adminPage.waitForResponse(r ->
                        r.url().contains("/api/v1/internal/admin/orders/intervention")
                                && r.url().contains("page=0")
                                && "GET".equals(r.request().method()),
                portal::openInterventionsTab);
        assertThat(response.status()).isEqualTo(200);
        adminPage.waitForLoadState(LoadState.NETWORKIDLE);

        AdminManualInterventionsPage interventions = new AdminManualInterventionsPage(adminPage);
        interventions.waitForQueue();
        assertThat(interventions.isInterventionsVisible()).isTrue();
        int count = interventions.getInterventionCount();
        if (count == 0) {
            assertThat(interventions.isEmptyStateVisible()).isTrue();
            assertThat(interventions.getPageInfo()).isEqualTo("Page 1 of 1");
            assertThat(interventions.canGoPreviousPage()).isFalse();
            assertThat(interventions.canGoNextPage()).isFalse();
            Assumptions.assumeTrue(false,
                    "The queue empty state passed, but no intervention exists for the confirmation check");
        }

        assertThat(count).isPositive();
        interventions.selectIntervention(0);
        assertThat(interventions.isDetailVisible()).isTrue();
        assertThat(adminPage.getByPlaceholder("Reason for cancellation...").isEditable()).isTrue();

        if (interventions.getForceAssignButtonCount() == 0) {
            assertThat(interventions.areDriversUnavailable()).isTrue();
        } else {
            interventions.openForceAssignConfirmation();
            assertThat(interventions.confirmationDialog().isVisible()).isTrue();
            assertThat(interventions.confirmationDialog().getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Force assign").setExact(true)).isVisible()).isTrue();
            interventions.cancelConfirmation();
            assertThat(interventions.confirmationDialog().isVisible()).isFalse();
            assertThat(assignmentRequests.get()).isZero();
        }
    }

    // ── OPERATIONS DLQ TABS SCENARIOS ────────────────────────────────────

    @Test
    @DisplayName("OPS-TAB-01: Operations panel visible")
    void operationsPanelVisible() {
        portal.openMoneyOperationsTab();
        AdminOperationsPage ops = new AdminOperationsPage(adminPage);
        assertThat(ops.isOperationsVisible()).isTrue();
    }

    @Test
    @DisplayName("OPS-TAB-02: Open rejections tab")
    void openRejectionsTab() {
        portal.openMoneyOperationsTab();
        AdminOperationsPage ops = new AdminOperationsPage(adminPage);
        ops.openRejectionsTab();
        assertOperationsTab("Ledger Rejections", "Rejected Ledger Movements", "No unresolved rejections.");
    }

    @Test
    @DisplayName("OPS-TAB-03: Open reconciliation tab")
    void openReconciliationTab() {
        portal.openMoneyOperationsTab();
        AdminOperationsPage ops = new AdminOperationsPage(adminPage);
        ops.openReconciliationTab();
        assertOperationsTab("Reconciliation Runs", "Recent Reconciliation Runs", "No runs found.");
    }

    @Test
    @DisplayName("OPS-TAB-05: Open payment DLQ tab")
    void openPaymentDlqTab() {
        portal.openMoneyOperationsTab();
        AdminOperationsPage ops = new AdminOperationsPage(adminPage);
        ops.openPaymentDlqTab();
        assertOperationsTab("Payment DLQ", "Failed Payment Webhooks", "No failed webhooks found.");
    }

    @Test
    @DisplayName("OPS-TAB-06: Open wallet DLQ tab")
    void openWalletDlqTab() {
        portal.openMoneyOperationsTab();
        AdminOperationsPage ops = new AdminOperationsPage(adminPage);
        ops.openWalletDlqTab();
        assertOperationsTab("Wallet DLQ", "Wallet Outbox DLQ", "No wallet outbox events in DLQ.");
    }

    private void assertOperationsTab(String tabName, String panelHeading, String emptyState) {
        Locator selectedTab = adminPage.getByRole(AriaRole.TAB,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName(tabName).setExact(true));
        assertThat(selectedTab.getAttribute("aria-selected")).isEqualTo("true");
        Locator heading = adminPage.getByRole(AriaRole.HEADING,
                new com.microsoft.playwright.Page.GetByRoleOptions().setName(panelHeading).setExact(true));
        heading.waitFor(new Locator.WaitForOptions().setTimeout(30000));
        assertThat(heading.isVisible()).isTrue();
        // A fetch failure currently leaves the panel body blank. Require its explicit empty state
        // or at least one rendered entry so a heading alone cannot pass as a healthy queue.
        Locator empty = adminPage.getByText(emptyState,
                new com.microsoft.playwright.Page.GetByTextOptions().setExact(true));
        if (!empty.isVisible()) {
            Locator panel = heading.locator("xpath=..");
            assertThat(panel.locator(":scope > div").count()).isPositive();
        }
    }
}
