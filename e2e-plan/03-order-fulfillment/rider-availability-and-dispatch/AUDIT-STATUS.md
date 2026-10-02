# Durable audit status: rider-availability-and-dispatch

Updated 2026-10-02T17:13:01+05:30. Review: Pending. Implementation/next scope: Pending. Evidence: Not run.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

## Checkpoint21 (2026-10-02T20:05:00+05:30)

Live defect: right after the rider accepted, their trip disappeared from the app (the server still had it ASSIGNED and the rider ON_DELIVERY). `/orders/active` read only the customer service's copy, which trails DRIVER_ASSIGNED; the UI replaces its list on each poll. The fix is local (the delivery service sends held assignments; the customer service includes them only when no driver is recorded there). See [checkpoint21](../../_handoff/checkpoints/21-fresh-flows-and-rider-active-gap.md). Next: deploy, resume 7f7af6a5, then run one fresh lifecycle to prove the gap is closed live.
