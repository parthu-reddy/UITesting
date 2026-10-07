# Durable audit status: rider-wallet-earnings-and-history

## 2026-10-07T12:40+05:30 — False ₹0.00 today tiles and fee-as-earnings fixed locally

Live evidence on the deployed 6eb4ef6: one capture's PNG shows ₹0.00 / 0 orders and its text dump
seconds later shows ₹21.16 / 1 order. A pending, failed or other-day history read rendered as zero.
Fixed locally: `todayFigures` drives "—" plus a reason. The history details modal showed the
customer delivery fee as "Total Earnings" and now shows the server net payout or "Not confirmed yet".
Red-first tests and the full UI gates pass. Not deployed.

RIDER-EARN-03 is **unproven** by invocation 5: its baseline was read while the tile still showed
the unloaded ₹0.00. After deployment, the `₹`-only E2E locator waits for the real figure.
[Checkpoint125](../../_handoff/checkpoints/125-retained-resume-false-zero-and-gps-pins.md).

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


Updated 2026-10-07T10:35+05:30. Idle dashboard/header/stats and shell source reviewed; baseline visible-read checks pass. Canvas fix local; owner UI deploy/final strengthened E2E pending. Wider earnings/history outcomes unverified by this batch.

## Checkpoint122 — current idle rider visual proof

Seeded rider7000000001 signs in through normal Dev Autofill and opens Delivery at390×844. The UI's
verification GET returns200, duty control is ≥48px, Today’s earnings/Paid today and Trips Completed
render, and the document does not overflow horizontally. Initial visual method passes. This closes
F12's verification503, not active delivery or nonzero payout/history proof.

Source DeliveryShell forces dark text tokens; transparent RoleShell exposed the light App canvas.
Strengthened live invocation3 fails1/1 (0errors/skips) on that actual computed transparency. Local
RoleShell now paints dark background/foreground/control scheme; focused13/full UI1045 plus
typecheck/lint/build/source gate pass. Owner is deploying UI. Final paint assertion not live-green yet.

Actual RiderStatsBar exposes earnings, completed trips and a DriverRatingCard. Artboard kilometres
are dummy content with no total-distance field here; do not fabricate them or override parked B10.
Idle capture does not prove active stop/contact/swipe/map. No duty/order/financial action was added.
[Comparison](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-VISUAL-AUDIT.md),
[baseline](../../_handoff/evidence/122-role-visual-baseline.json),
[live negative assertions](../../_handoff/evidence/122-role-visual-red-regressions.json).

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).
