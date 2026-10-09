# Checkpoint 139: owner-gated list done under the fast defaults (2026-10-08 21:03–21:30 IST)

Owner: "continue with what's remaining". Details: `RandomDocuments/PendingWork_2026-10-07/FastE2E_2026-10-08/Phase3_MeasureOnDev/mistakes_and_improvements.md`.

| Item | Result |
|---|---|
| Canaries ×4 (fast defaults) | pass (7.6/3.4/4.1/8.7 s) |
| CustomerOrderPlacementTest#tippedOrderMatchesMockPaidTotal | PASS 21.3 s (was 30.0 s) |
| ChatSupportWindowClosedTest (8a7114cb) | PASS 10.6 s after the history-paging fix |
| DelayApprovalFlowTest#customerRejectsRestaurantDelay | PASS 32.2 s after 3 fixes (first recorded pass) |
| HappyDeliveryFlowTest#overlappingOrdersRemainIndependent | PASS 155.9 s (resumed pair after the chatChecks NPE fix) |

## Test-harness fixes (all hidden by the 400 ms slow-mo or pre-existing)
1. `StateSetupHelper.placeOrder` takes the id from its own `POST /api/v1/orders` response. Reading the tracker
   returned an older active order's id. Guard: `validate_fast_e2e.py --phase 4` P4.1.
2. History lookups of possibly-older orders page through `pageToOrder` (7 sites). Guard: P4.2.
3. `rejectDelay()` waits for AWAITING_DELAY_APPROVAL. The test waits for the `/delay-approval` response, then
   verifies the server's record via History.
4. The overlapping test builds `OrderChatChecks` per customer.
5. EntitlementRevocationTest suspension method tagged `slow` (Phase 6 V3, 75.2 s).

## Mistake
I ran the delay test while an order from the previous test was active, although checkpoint 137 recorded "no active
orders". Effect: retained order 3a001a9d got a delay request (the sweeper settled it), and the test's own 9dce13ba
was cancelled. Read-only DB checks before and after; no manual cleanup.

## Gates
Fast E2E phases 1 5/5, 2 6/6, 3 3/3, 4 2/2; break tests 11/11. Locator audit PASS, feature tags 6/6, Phase 6 green,
p02 6/6 (inventory rebuilt), Phase 4 10/10 (dossier rebuilt), fixture guard PASS, redaction guard PASS (run from
UITesting), runner tests OK, test-compile rc 0. Dev: no active order.
