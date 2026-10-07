# Durable audit status: support-and-refund-queues

## 2026-10-07T11:37:19+05:30 — Retained financial continuation passes; active UI gate running

Exact6fbe0289 retained money/quote passes1/1,0failures/errors/skips,18balanced lines and matching
posted payables through actual admin navigation. Original queue-name failure remains failed.
Changed active pickup/chat/swipe/pin gates have started one justified lifecycle, no final result yet.
[Checkpoint124](../../_handoff/checkpoints/124-deployed-receipt-owned-refund-and-active-rider-gate.md). Item3 active; M1/A5b paused.

## 2026-10-07T11:33:21+05:30 — item3 deployed follow-up

UI6eb4ef6 is confirmed live. Exact6fbe0289 delivered receipt/quote passes1/1 with0new order/refund
submission. One approved ticket47ffbccd was rejected200, then the writer errored in a request-body
observer. Same-ticket read-only resume passes1/1,0failures/errors/skips, persisted REJECTED audit,
0customer refunds and0new order/resolve writes. Keep the original writer error. Final audit capture
is inspected; no duplicate ticket. The actual rider CI-asset check also passes1/1.
Changed active mobile pin/hint/swipe/header-chat gate remains unverified; retained money through
normal payout drawer/reference controls is running. Item3 remains active, M1/A5b paused.
[Current checkpoint and exact evidence](../../_handoff/checkpoints/124-deployed-receipt-owned-refund-and-active-rider-gate.md).

## 2026-10-07T11:17:55+05:30 — checkpoint123: real active-state failure and local repair

One deployed HappyDeliveryFlowTest delivered retained6fbe0289 at Brand1Outlet5, then failed its
history-to-receipt assertion:1failure/0errors/skips,253.778s. Actual delivery,completed rider payout,
customer delivered history and old active canvas/contact/swipe checks passed before failure.
Mobile capture found a clipped pickup hint and floating chat overlap. Terminal history was hidden
by stale active cache. These three UI fixes pass1049local assertions/174files,focused34 and all
toolchain/Phase4gates; owner UI6eb4ef6 is publishing, not yet live-confirmed. Old canvas/sidebar/F12
fixes remain live-green. New pin/hint/obstruction/header-chat E2E is compiled but unexecuted.

All Tickets read passes1/1(GET200,0rows/0resolution writes). Populated refund detail/confirm/audit
is unverified. Owner approved an exact dummy support request/rejection for6fbe0289, no approval/pay;
the UI-only test is compiled and must wait for retained receipt proof after deployment.

[Checkpoint123](../../_handoff/checkpoints/123-active-visual-receipt-and-mobile-fixes.md),
[local gates](../../_handoff/evidence/123-active-ui-local-gates.json),
[original failure](../../_handoff/evidence/122-role-active-invocation1-failed-late.json),
[All queue](../../_handoff/evidence/123-admin-all-refunds-invocation1.json).
Keep the original manifest and execution failure; M1/A5b and explicit deferrals remain paused.
Older current/running claims below are their dated state.

## 2026-10-07T10:49+05:30 — UI deployment gate closed

Owner-published UI5a1e97c passes all3strengthened RoleVisualAuditUiTest methods in invocation5,
0failures/errors/skips/network/browser errors. Rider dark canvas/full admin labels/settled support
captures manually inspected. [Green evidence](../../_handoff/evidence/122-role-visual-invocation5-green.json).
Active rider and populated refund visual states remain open; the existing lifecycle is running
with its new opt-in visual helper. Later deployment-waiting entries below are dated history.


Updated 2026-10-07T10:35+05:30. Current reachable queue/detail source reviewed; empty-queue visual baseline passes. Sidebar fix local; owner UI deployment/final strengthened E2E pending. Prior financial fixtures are historical.

## Checkpoint122 — admin 1280×800 visual read-only audit

Initial RoleVisualAuditUiTest admin method passes refund navigation, Status Filter and Queue Empty,
then Support Tickets heading with no document overflow. Its first support capture caught loading;
final source now waits for rows/defined empty state, no load error and settled selection colour.
Strengthened admin invocation4 fails1/1 (0errors/skips) on a clipped Manual Interventions label.
SidebarNav wrapping fixes that locally; full UI1045/1045 and toolchain checks pass. Owner is
deploying UI. Final method has not passed live yet.

Reachable RefundQueue/RefundTicketPanel support per-ticket quote-capped decisions and a resolution
audit. The empty live OPEN queue does not prove populated rows, detail rail or confirmations.
Artboard bulk/search/GPS/customer-history timeline is not in this reachable queue; do not represent
it as already implemented or copy its dummy financial wording. No refund/support decision made.

[Comparison](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-VISUAL-AUDIT.md),
[baseline](../../_handoff/evidence/122-role-visual-baseline.json),
[live regression failures](../../_handoff/evidence/122-role-visual-red-regressions.json),
[owner UI gate](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-DEPLOYMENT.md).
Earlier approval/retry/money results below describe their dated fixtures and proof policy, not this
UI-only batch or a refreshed live financial assertion.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint 17 (2026-10-02T17:44:47+05:30): payment amount guard deployed/healthy at 0a0a227fa97e455d90aba2d7f6e9daa1bd558729. Earlier gate is closed. Three retained refunds remain FAILED; delivered status/ledger correction and exact-ID retry are open. Rider OFFLINE; no reset/cleanup/new lifecycle or financial mutation. Current continuation: [latest checkpoint](../../_handoff/checkpoints/17-deployment-confirmation-and-budget-handoff.md).

Checkpoint18 (2026-10-02T18:13:07+05:30): local exact-refund queueing, UI refund read/retry/refresh and newest chat window fixes complete; 120customer/22chat/30UI final checks plus typecheck/lint passed. Customer/chat/UI deployment pending. No new live record/action/E2E lifecycle; historical refund/capture/ledger recovery is still open. See [checkpoint18](../../_handoff/checkpoints/18-refund-retry-ui-chat-history.md) and [deployment gate](../../_handoff/DEPLOYMENT-GATE.md).

## Checkpoint24 (2026-10-02T23:58:00+05:30): Batch 5 run live

`SupportRefundResolutionFlowTest` now runs the three decisions on **one** delivered order (`-Dsupport.order.id`); see scenarios.md Batch 5 and DECISIONS.md. Run on fresh order 0554f250 (delivered by a passing full lifecycle after the Dev reset): 3 tests, 2 pass, 1 fail.

| Scenario | Result | Proof |
|---|---|---|
| SUPPORT-REFUND-02 denial (runs 1st) | **PASS** | real deployed UI + DB: ticket 054a652b REJECTED, notes, resolvedBy; no refund; payment status and ledger lines unchanged |
| SUPPORT-REFUND-01 reduced award, restaurant fault (runs 2nd) | **PASS** | real deployed UI + DB: ticket 25ac7a74 RESOLVED 12.00; refund fe60ce55 COMPLETED ORIGINAL_METHOD, completedAt; intent PARTIALLY_REFUNDED; REFUND 12.00/12.00; CLAWBACK 4.28 = 19.11 × round4(12/53.53); earlier REJECTED ticket unchanged |
| SUPPORT-REFUND-03 already-refunded item (runs 3rd) | **FAIL** (product defect) | no chat reply in 20s; the ITEM_ALREADY_REFUNDED refusal rolled back with its reply and dead-lettered (chat-events.DLT p0 o0) |

Defect and fix: [checkpoint24](../../_handoff/checkpoints/24-support-refunds-live-and-refusal-rollback.md). Fixed locally in CustomerApplication (real-proxy test seen red, then green; full clean test 465/92). **Next:** after the user deploys customer-service, rerun only SUPPORT-REFUND-03 on 0554f250 (command in NEXT-STEPS). Still open in this feature: the remaining-cap at approval is unit-tested only (no customer UI path since checkpoint23's deletion); admin retry live; concurrent decisions on one ticket.

## Checkpoint25 (2026-10-03T04:40:00+05:30): Batch 5 passes on deployed code

On customer-service `cc04ed7`, `SupportRefundResolutionFlowTest` **PASS 3/3** on fresh order d3acfc93: denial (005c08a4 REJECTED), reduced award (a301b711 RESOLVED, refund d3e4b73c COMPLETED ₹12.00, CLAWBACK 4.28, ledger 131.74 balanced), and refusal (CHAT_REFUND_ERROR ITEM_ALREADY_REFUNDED committed; 0 DLT, 0 rollback-only). Proof: real deployed UI + DB + outbox + logs ([evidence](../../_handoff/evidence/25-deployed-support-refunds.json)).

Precondition, now asserted by the test: the order's `updatedAt` is less than two hours old, because the customer UI offers the order chat (the only support entry) only for that long. A 03-only rerun on 5h-old 0554f250 errored on this before the guard existed. Still open: the remaining-cap at approval (unit-tested only), admin retry live, concurrent decisions on one ticket, and the user's decision on a server-side support window.

## Checkpoint28 (2026-10-03T08:10:00+05:30)

The admin UI had no failed-refund screen; Money Operations now has a Failed Refunds tab with a confirmed Retry (local). With the user-approved Dev seam (₹1.13 declined once), REFUND-RETRY-01 can run live after the four-service deploy (Batch 6). Support helpers moved to `util/SupportRefundSteps`.

Checkpoint29 (2026-10-03T08:50:00+05:30): passed live on deployed code — see [checkpoint29](../../_handoff/checkpoints/29-deployed-priority-runs.md) (isolation on d3acfc93; CHAT-22 and REFUND-RETRY-01 on cf608115).
