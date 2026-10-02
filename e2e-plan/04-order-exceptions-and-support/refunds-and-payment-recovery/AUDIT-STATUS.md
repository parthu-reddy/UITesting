# Durable audit status: refunds-and-payment-recovery

Updated 2026-10-02T17:13:01+05:30. Review: Current active priority; exact owned recovery review. Implementation/next scope: Latest completion/capture/source fixes deployed. Evidence: 81 affected local passes; all 3 owned refunds FAILED; no live completion.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Checkpoint 16 (2026-10-02T17:26:45+05:30): callback amount guard passed 32 affected local payment checks; deployment pending. Existing local fixture extended, no new E2E class/order. [Pending details](PENDING.md). Current retained financial recovery remains open; no reset/reseed required.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint 17 (2026-10-02T17:44:47+05:30): payment amount guard deployed/healthy at 0a0a227fa97e455d90aba2d7f6e9daa1bd558729. Earlier gate is closed. Three retained refunds remain FAILED; delivered status/ledger correction and exact-ID retry are open. Rider OFFLINE; no reset/cleanup/new lifecycle or financial mutation. Current continuation: [latest checkpoint](../../_handoff/checkpoints/17-deployment-confirmation-and-budget-handoff.md).

Checkpoint18 (2026-10-02T18:13:07+05:30): local exact-refund queueing, UI refund read/retry/refresh and newest chat window fixes complete; 120customer/22chat/30UI final checks plus typecheck/lint passed. Customer/chat/UI deployment pending. No new live record/action/E2E lifecycle; historical refund/capture/ledger recovery is still open. See [checkpoint18](../../_handoff/checkpoints/18-refund-retry-ui-chat-history.md) and [deployment gate](../../_handoff/DEPLOYMENT-GATE.md).

Checkpoint19 (2026-10-02T18:27:17+05:30): exact stored-confirmation capture recovery implemented locally in payment-gateway;58selected checks including committed concurrency and Spring method security passed. Deployment/live capture restoration pending; all3owned refunds stillFAILED. Source proof has3matching PROCESSED confirmations; no financial action performed. See [checkpoint19](../../_handoff/checkpoints/19-stored-confirmation-capture-recovery.md) and [deployment gates](../../_handoff/DEPLOYMENT-GATE.md).

## Checkpoint20 (2026-10-02T19:10:00+05:30)

Review result: the checkpoint19 recovery endpoint is deleted, and the checkpoint18 retry is fixed so it can actually complete (the gateway releases its held key on definitive failure; customer service has a separate `sweep_attempts` budget and routes by destination). Refund UI polling stops once refunds settle. Local proof only, with every guard seen red; see [checkpoint20](../../_handoff/checkpoints/20-review-and-refund-retry-fix.md). The three retained fixtures will be erased by the user's data reset. Next: after deployment and reset, run cancellation and rejection without resume properties and assert actual completion (refund COMPLETED, payment REFUNDED, balanced REFUND ledger, UI state).

