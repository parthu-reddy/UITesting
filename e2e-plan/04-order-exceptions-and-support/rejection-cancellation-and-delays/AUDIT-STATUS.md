# Durable audit status: rejection-cancellation-and-delays

Updated 2026-10-02T17:13:01+05:30. Review: Canonical fast terminal UI reviewed. Implementation/next scope: Shared retained money helper; duplicate methods removed. Evidence: Cancellation/rejection UI passed, whole flows failed money completion; resume owned IDs.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint 17 (2026-10-02T17:44:47+05:30): payment amount guard deployed/healthy at 0a0a227fa97e455d90aba2d7f6e9daa1bd558729. Earlier gate is closed. Three retained refunds remain FAILED; delivered status/ledger correction and exact-ID retry are open. Rider OFFLINE; no reset/cleanup/new lifecycle or financial mutation. Current continuation: [latest checkpoint](../../_handoff/checkpoints/17-deployment-confirmation-and-budget-handoff.md).
