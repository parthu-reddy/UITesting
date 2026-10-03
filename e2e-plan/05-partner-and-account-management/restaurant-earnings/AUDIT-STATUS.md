# Durable audit status: restaurant-earnings

Updated 2026-10-02T17:13:01+05:30. Review: Pending. Implementation/next scope: Pending. Evidence: Not run.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint33 (2026-10-03T09:55:00+05:30): see [checkpoint33](../../_handoff/checkpoints/33-restaurant-earnings-and-campaigns.md).

## Current live result — 2026-10-03T17:36:31+05:30

Existing RestaurantEarningsLiveTest#earningsMatchTheLedger passed1/0/0/0 using actual Oracle
zero balances and empty statement for9000000001/Brand1Outlet3. Baseline SQL36-earnings-baseline.json.
Test's explicit earnings.expected.statement.empty=true checks all three figures are0, empty label
"No transactions found.", no signed rows. No conditional pass for a missing table. First attempt
failed on wrong empty label; corrected from actual LedgerStatementPanel source, rerun green.
Old nonzero retained fixtures absent before this seed. Nonzero payout/clawback/signed-row proof remains
open; do not claim the empty-state pass covers it.
