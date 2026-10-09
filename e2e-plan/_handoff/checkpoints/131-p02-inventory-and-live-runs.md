# Checkpoint 131: P0-2 measured inventory and safe live runs (2026-10-08 00:15 IST)

Plan, tools, evidence: `RandomDocuments/PendingWork_2026-10-07/P02_E2EInventory/README.md`.

## How to run E2E now
Multi-class runs only through `e2e-plan/_handoff/tools/run_e2e_batch.py` (see e2e-plan/AGENTS.md): deploy lock,
single-run lock, canary per role, admin step-up pacing (Identity: 5 verifies/5 min per phone; runner uses 4/5, 8/10),
fail-fast on a repeated failure, per-invocation redacted logs. Guards: `test_run_e2e_batch.py` (10/10).

## Results on the current release (customer-service 4cf2d7c, 13:36Z)
- Inventory PASS_CURRENT 210 after run 2, plus run 3/4 confirmations (see Phase3 checklist).
- Batch 1 (19:11–20:40) was invalid: admin step-up 429 + an address-prompt race burned ~80 min; fixed.

## UI defects found and fixed locally (deploy food-delivery-app-ui to make the E2E pass live)
1. AdminFleetMap: manual refresh wiped every layer when one failed (city-tagged layers now).
2. ChatWidget on Support Tickets floated over "Reject Request" (new `placement="inline"`).
3. AdminPortal: unknown admin route looped (`<Navigate to="/admin/map">`).
UI gates: lint, typecheck, vitest all green (counts in the P02 Phase3 checklist).

## Test-side changes (UITesting, uncommitted)
SavedDeliveryAddressPage (address-prompt race), LoginPage (fail fast on step-up 429), NearbyOutletPage.openBrandCard
(Reorder strip ambiguity), MenuCartUiTest reload, OutletSelectionTest, SavedAddressOutletUiTest, NavigationSmokeTest,
ResponsiveAccessibilityTest (ACCESS-04, NAV-03), AdminSupportTicketResolution, AdminPortalRouteCoverage,
AdminFleetSafetyPage, AdminUserCatalogModeration (+SafetyPage), DeliveryDashboardPage (backToJobs; openEarningsTab and
openActiveTab deleted), util/FixtureShell (new).

## Open
- AdminSupportChatIsolationRoutedUiTest ×4: browser never opens /ws/chat under the fixture (diagnosis in progress).
- Gated: reorder ×2 (category hours 07:00–20:00 IST), history pagination (needs >1 page), cancelled-history, earnings,
  review history, owned refund visual, sponsored listing (fresh seed).
- Impact C (record-creating) tests: waiting for the owner's go-ahead per batch (Phase4 plan).
