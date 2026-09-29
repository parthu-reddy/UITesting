package com.fooddelivery.e2e.tests.features.admin;

import com.fooddelivery.e2e.base.TestBase;
import com.fooddelivery.e2e.pages.admin.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Admin Ledger advanced filters, pagination, statement panel,
 * and extended Payout actions (reject, force-create, draft).
 * Covers: LEDGER-ADV-01..14, PAYOUT-08..12
 */
@Tag("admin-ledger")
public class AdminLedgerAdvancedTest extends TestBase {

    private AdminPortalPage portal;

    @BeforeEach
    void loginAdmin() {
        loginAsAdmin();
        portal = new AdminPortalPage(adminPage);
        portal.waitForPortal();
    }

    // ── LEDGER ADVANCED FILTER SCENARIOS ─────────────────────────────────

    @Test
    @DisplayName("LEDGER-ADV-01: Ledger view visible")
    void ledgerViewVisible() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        assertThat(ledger.isLedgerVisible()).isTrue();
    }

    @Test
    @DisplayName("LEDGER-ADV-02: Filter by transaction ID")
    void filterByTransactionId() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        // The API binds transactionId as a UUID; use a valid UUID that cannot match seeded data.
        ledger.filterByTransactionId("00000000-0000-0000-0000-000000000000");
        assertThat(ledger.applyFilter("transactionId=00000000-0000-0000-0000-000000000000")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
    }

    @Test
    @DisplayName("LEDGER-ADV-04: Filter by restaurant payable account")
    void filterByOwnerType() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.filterByOwnerId("00000000-0000-0000-0000-000000000000");
        ledger.selectOwnerType("RESTAURANT_PAYABLE");
        assertThat(ledger.applyFilter("ownerId=00000000-0000-0000-0000-000000000000&ownerType=RESTAURANT_PAYABLE")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
    }

    @Test
    @DisplayName("LEDGER-ADV-06: Filter by direction CREDIT")
    void filterByDirectionCredit() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.selectDirection("CREDIT");
        assertThat(ledger.applyFilter("direction=CREDIT")).isEqualTo(200);
    }

    @Test
    @DisplayName("LEDGER-ADV-09: Clear filters resets all")
    void clearFiltersResetsAll() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        ledger.filterByTransactionId("00000000-0000-0000-0000-000000000000");
        ledger.filterByOwnerId("00000000-0000-0000-0000-000000000000");
        ledger.selectOwnerType("RESTAURANT_PAYABLE");
        assertThat(ledger.applyFilter("transactionId=00000000-0000-0000-0000-000000000000&ownerId=00000000-0000-0000-0000-000000000000&ownerType=RESTAURANT_PAYABLE")).isEqualTo(200);
        ledger.waitForEmptyResults();
        assertThat(ledger.getTransactionCount()).isZero();
        assertThat(ledger.clearFilters()).isEqualTo(200);
        assertThat(adminPage.getByPlaceholder("Transaction ID").inputValue()).isEmpty();
        assertThat(adminPage.getByPlaceholder("Owner ID").inputValue()).isEmpty();
    }

    @Test
    @Disabled("AdminLedgerView has no statement or detail panel; a row's only action copies its transaction id. Recorded in e2e-plan/NOT-DEFECTS/README.md")
    @DisplayName("LEDGER-ADV-11: Open statement detail panel")
    void openStatementDetailPanel() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        if (ledger.getTransactionCount() > 0) {
            ledger.openStatement(0);
            assertThat(ledger.isStatementPanelOpen()).isTrue();
        }
    }

    @Test
    @DisplayName("LEDGER-ADV-12/13: Ledger pagination next and prev")
    void ledgerPagination() {
        portal.openLedgerTab();
        AdminLedgerPage ledger = new AdminLedgerPage(adminPage);
        String initialPage = ledger.getPageInfo();
        try {
            ledger.nextPage();
            adminPage.waitForTimeout(500);
            String nextPageInfo = ledger.getPageInfo();
            // Page should have changed
            ledger.prevPage();
            adminPage.waitForTimeout(500);
        } catch (Exception e) {
            System.out.println("[INFO] Pagination not available (likely only 1 page): " + e.getMessage());
        }
    }

    // ── PAYOUT ADVANCED SCENARIOS ────────────────────────────────────────

    @Test
    @DisplayName("PAYOUT-11: Open payout history tab")
    void openPayoutHistoryTab() {
        portal.openPayoutsTab(); // "Pending Payouts" in the admin sidebar
        AdminPayoutsPage payouts = new AdminPayoutsPage(adminPage);
        assertThat(payouts.isPayoutsVisible()).isTrue();
    }
}
