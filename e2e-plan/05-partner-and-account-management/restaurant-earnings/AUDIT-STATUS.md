# Durable audit status: restaurant-earnings

Updated 2026-10-07T10:13:20+05:30. Named-owner harness follow-up: complete locally and verified on deployed Dev UI. Wider earnings scenarios remain pending; historical evidence is labeled below.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint33 (2026-10-03T09:55:00+05:30): see [checkpoint33](../../_handoff/checkpoints/33-restaurant-earnings-and-campaigns.md).


## 2026-10-07 — Named outlet fixture uses its explicit owner; live empty-state regression green

`RestaurantEarningsLiveTest#earningsMatchTheLedger` now requires `-Drestaurant.phone` whenever `-Dearnings.outlet` enables the test. A named fixture no longer signs in as an unrelated random owner. Before selection, `RestaurantDashboardPage.availableOutlet` opens the actual **Outlet** combobox and reads its rendered listbox options; the named fixture must match exactly one available option for that signed-in person. This is UI-only validation, with no direct outlet API lookup or state injection.

Live invocation2 passed **1/1, 0 failures/errors/skips**, using normal Dev Autofill Code on `9000000001`, `Brand 1 Outlet 3`. All three figures were ₹0.00 and the actual statement was empty. The same focused invocation passed both navigation methods (3/3 total); no browser network errors. Invocation1 compiled successfully but both classes failed at Chromium macOS MachPortRendezvous startup in the sandbox: **0 business methods executed**, not an earnings failure. Invocation2 used the approved browser process outside the sandbox. Standard teardown signed out the test session successfully.

Evidence: [live invocation2 and exact command](../../_handoff/evidence/122-harness-live-invocation2.json), [startup blocker](../../_handoff/evidence/122-harness-live-invocation1-startup-blocked.json), [earnings locator audit](../../_handoff/evidence/122-harness-earnings-locators.json), [dashboard locator audit](../../_handoff/evidence/122-harness-dashboard-locators.json). The earnings audit has 3 PASS/0 FAIL/4 DYNAMIC; dashboard 12 PASS/0 FAIL. Source changes remain local/uncommitted; no product deployment was required.

**Boundary:** this closes the pending named-owner harness follow-up and re-proves the zero/empty statement only. Nonzero delivery payout, clawback, settlement and statement arithmetic fixtures remain unverified, as do the wider scenarios in `scenarios.md`. No order was placed and no data was seeded/reset. New work is paused at the owner's 2026-10-07 one-work-at-a-time instruction while the root finishes the paperwork item.

## Historical live result — 2026-10-03T17:36:31+05:30

Existing RestaurantEarningsLiveTest#earningsMatchTheLedger passed1/0/0/0 using actual Oracle
zero balances and empty statement for9000000001/Brand1Outlet3. Baseline SQL36-earnings-baseline.json.
Test's explicit earnings.expected.statement.empty=true checks all three figures are0, empty label
"No transactions found.", no signed rows. No conditional pass for a missing table. First attempt
failed on wrong empty label; corrected from actual LedgerStatementPanel source, rerun green.
Old nonzero retained fixtures absent before this seed. Nonzero payout/clawback/signed-row proof remains
open; do not claim the empty-state pass covers it.
