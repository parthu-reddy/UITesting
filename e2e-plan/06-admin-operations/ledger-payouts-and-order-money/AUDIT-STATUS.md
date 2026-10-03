# Durable audit status: ledger-payouts-and-order-money

Updated 2026-10-02T17:13:01+05:30. Review: Priority source/routed safety review. Implementation/next scope: Strict payee net checks; backend fixes deployed. Evidence: Admin money HTTP 200/arithmetic verified; posted earnings missing; no full settlement.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint 17 (2026-10-02T17:44:47+05:30): payment amount guard deployed/healthy at 0a0a227fa97e455d90aba2d7f6e9daa1bd558729. Earlier gate is closed. Three retained refunds remain FAILED; delivered status/ledger correction and exact-ID retry are open. Rider OFFLINE; no reset/cleanup/new lifecycle or financial mutation. Current continuation: [latest checkpoint](../../_handoff/checkpoints/17-deployment-confirmation-and-budget-handoff.md).

Checkpoint19 (2026-10-02T18:27:17+05:30): exact stored-confirmation capture recovery implemented locally in payment-gateway;58selected checks including committed concurrency and Spring method security passed. Deployment/live capture restoration pending; all3owned refunds stillFAILED. Source proof has3matching PROCESSED confirmations; no financial action performed. See [checkpoint19](../../_handoff/checkpoints/19-stored-confirmation-capture-recovery.md) and [deployment gates](../../_handoff/DEPLOYMENT-GATE.md).

## Checkpoint22 (2026-10-02)

Delivered earnings had never posted on PostgreSQL: the ledger's UNIQUE(transaction_id, account_id, direction) rejected compound distributions. The fix is a per-leg unique key plus migration V20261002210000, deployed. The user-approved DLT replay posted 7f7af6a5. A fresh lifecycle (c463191b) posts directly, balanced 93.40, restaurant net 9.49, rider net 18.86, and the strict admin money check PASSES. See [checkpoint22](../../_handoff/checkpoints/22-delivered-ledger-posting-fix.md).

## Checkpoint30 (2026-10-03T09:05:00+05:30)

The order-money panel lacked payment, refunds and booked amounts; fixed locally (UI). MONEY-05 (`AdminOrderMoneyOutcomesTest`) covers delivered, partial refund, cancelled and rejected owned orders; red on the deployed UI, rerun after deploy. See [checkpoint30](../../_handoff/checkpoints/30-admin-order-money-outcomes.md).

Checkpoint31 (2026-10-03T09:32:00+05:30): MONEY-05 passes 5/5 on UI 6eb1743 — see [checkpoint31](../../_handoff/checkpoints/31-priority-list-complete.md).
