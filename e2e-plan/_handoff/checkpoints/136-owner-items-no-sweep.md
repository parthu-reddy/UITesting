# Checkpoint 136: checkpoint-135 list worked under the owner's "no sweep, no slow" rule (2026-10-08 17:35–18:00 IST)

Owner (DECISIONS.md 17:40): no full sweep (no backend change since the runs); live runs only for tests added or changed
in this session, never `slow`. Then (17:50): add feature tags so a change runs only its features' tests (Phase7).

## Done
| Item | Result | Proof |
|---|---|---|
| 6 unrecorded 15:39 order | `fac23c59` CANCELLED_BY_RESTAURANT by the 10-min sweeper at 15:49 | read-only customer_db query |
| 6 D1 refund ticket | D1 `8a7114cb` holds OPEN ticket `b8389997` (₹14.26, 15:41). Reported, not repaired | read-only customer_db query |
| 5 RetainedOrderStateUiTest | new `CustomerOrderHistoryPage.pageToOrder(id, maxPages)` clicks "Load More History" until the row renders; 1/1 on D1 via history (`historyVisible: true`, HANDED_OVER) | evidence/owner-items-2026-10-08 (P02) |
| 4 EntitlementRevocation suspension | 1/1 in 75 s (approve, suspend, hidden from search, reinstate, visible). `searchRestaurant` now uses the one textbox by accessible name. The 16:08 failure ran beside lane A on the same customer (its screenshot shows lane A's live order e33c94c1); which change fixed it is NOT established | same evidence dir |
| ws-token print path | `CustomerDashboardPage` added its own console listener printing `msg.text()` raw (one more per instance; the source of the duplicated console lines). Deleted; `[BROWSER DIALOG]` now redacted too. Guard `tools/check_log_redaction.py`: PASS 210 files; red on both reinserted leaks; STALE on an empty tree. One leaked value redacted in P02 evidence/run3 (mtime kept) | guard output |
| postData()==null root cause | authFetch sends `fetch(new Request(request.clone(), …))`; the clone carries the body as a stream and Chromium reports no post data. Probe `RequestBodyVisibilityTest` (no writes: 404 path): init/plain and uncloned Request visible, cloned null, cloned + pass-through route visible. Not a product defect | evidence/owner-items-2026-10-08-probe2 |
| Inventory scanner | `build_inventory.py` read a method name out of a DisplayName string ("postData()"); signatures now matched on string-blanked source. P1.1 red on the real input, then green | validate_p02 6/6 |

Dev writes this session: 1 fresh owner 9999696084 + organisation + approved brand "E2E O45 Restaurant 9999696084"
(suspended, then reinstated: APPROVED). Logins/logouts. Three 404 POSTs to /api/v1/e2e-request-body-probe.

## Not run, by the owner's rule
CustomerOrderPlacementTest#tippedOrderMatchesMockPaidTotal (auto-cancel, unchanged), ChatSupportWindowClosedTest
(unchanged; the closed-window order id is D1), DelayApproval#customerRejectsRestaurantDelay and
HappyDeliveryFlow#overlappingOrdersRemainIndependent (slow), F13 RiderAvailabilityUiTest / REST-01 (unchanged).
No active orders on Dev at 17:40, so delay-reject's precondition holds whenever it is run.

## Gates (quiet tree)
locator audit FAIL 0 (DEAD 27); validate_p02 6/6 (inventory rebuilt at cutoff 00:46:06Z: PASS_CURRENT 229, PASS_OLDER 73,
LAST_FAILED 15, NEVER_RUN 14); Phase 4 10/10 (dossier rebuilt); Phase 6 green; fixture-schema guard PASS 80/DRIFT 0;
runner unit tests OK; check_log_redaction PASS.
