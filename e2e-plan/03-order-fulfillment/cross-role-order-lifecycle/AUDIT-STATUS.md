# Durable audit status: cross-role-order-lifecycle

## 2026-10-07T14:15+05:30 — Post-deploy active lifecycle verified (after Dev wipe)

Post-deploy invocations 1–3 failed: a login during startup, the ageing emulated GPS fix, and my
harness's viewport ordering. Each failure is recorded separately. Both owned orders were resumed and
delivered (1/1 each), and the retained money check on f01c1e92 passes (18 lines, ₹92.72). The
missing courier pin is a Playwright emulation artifact; the harness refreshes the fix and waits for
pins inside the map. [Checkpoint127](../../_handoff/checkpoints/127-postdeploy-active-rider-verified.md).

## 2026-10-07T12:40+05:30 — ce254f3a resumed and verified; active rider fixes pending deploy

`HappyDeliveryFlowTest#completeOrderLifecycle` invocations, counted separately:
- **Invocation 3:** failed before checkout, no order.
- **Invocation 4:** created ce254f3a and failed at the out-for-delivery courier-pin capture (0 markers).
- **Invocation 5:** retained resume, passes 1/1, delivered through the UI, with out-for-delivery
  visual checks after a fresh load.
- **Retained money:** passes 1/1. CARD ₹43.02, rider net ₹21.16, restaurant ₹6.54, 18 lines
  balancing ₹92.72, 0 refunds.

Local UI fixes are not deployed: rider pins no longer wait for GPS, and the slider no longer
labels the customer fee as rider credit. Next: an owner UI deploy, then one fresh lifecycle to
verify the active-only changes. [Checkpoint125](../../_handoff/checkpoints/125-retained-resume-false-zero-and-gps-pins.md).

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

## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T06:59:57+05:30 — checkpoint106: canonical delivered; existing money/quote branch running

Happy assigned resume invocation9 passes1/1,0failures/errors/skips in53.834s on UI509d084/harnessafe12f1. Pickup and delivery return actual200; the exact rider Delivered history has positive payout and today earnings increase by it; the exact customer Order delivered receipt and real post-delivery chat assertion pass. Evidence55 and the separate retained state manifest record DELIVERED. Original creation manifest and both older cancelled orders remain preserved. The canonical checkout/accept/dispatch/both actual chat round trips were observed in invocation6 and Ready/saved arrival in invocation8; their original later failures remain failures, not a fabricated single clean full run.

The existing retained-delivered branch now runs money/quote invocation1 on the same60631296. It reads the normal rendered History/receipt, verifies the exact admin money page and balanced posted payouts, then asks for a selected-item quote without submitting a refund. Staff1000000001 is used to spread ordinary admin verification quota before the final O4/O5 gates. No second checkout, direct business API, DB/Redis or cleanup is used.

Next: successful money/quote, unrated-dialog guard before review writes, required settings/partner/restaurant earnings/navigation/admin-money and immutable participant reviews using this one order, final O4/O5 image gates and measured histograms/checklists. Existing static O4 22/22,O5 7/7 and GitHub941/941 remain valid. Stop after O4/O5; explicit internal/performance-baseline/CSP-mutation/SSE/rate/duration/provider deferrals stay unverified and are now linked from the shared DEFERRED index.

## 2026-10-05T06:56:59+05:30 — checkpoint105: Ready and arrival committed; same order resumes pickup

Happy invocation8 on UI509d084 executes1/0pass/0fail/1error/0skip (78.587s), at the arrival-response matcher. Real UI reaches Ready, then the arrival button disappears and pickup form remains for exact60631296. Separate native operator timeline confirms ORDER_READY and completed AT_RESTAURANT plus outbox publication. This verifies the kitchen correction's real effect but is not a full lifecycle pass. Evidence54 retains the original timeout and bounded observations. No checkout or duty reset occurs; owned order remains READY_FOR_PICKUP/AT_RESTAURANT.

UITestingafe12f1 is pushed, compile31 passes. Response matching now uses POST plus the owned URI path and subsequent visible phase; it does not depend on optional request-body metadata or a query-free raw URL. The exact reason the former predicate missed the response is not claimed beyond that overly restrictive predicate. The two-line extra teardown was removed after source verification that TestBase already invokes the safe shared idle-rider helper. Reuse existing code instead of a duplicate helper. Assigned Happy invocation9 is running for the same order's pickup/delivery/chat/history/payout.

Current deployment remains UI509d084/Customer0849ee6; GitHub941/941, hardening15/15 and reconciliation29/0drift are unchanged. Final phase gates, same-order money/quote/review/read-only regressions and measurements/checklists remain open. Stop after O4/O5.

Updated 2026-10-02T17:13:01+05:30. Review: Reviewed fast CROSS01–15. Implementation/next scope: All fast checks implemented; product fixes deployed. Evidence: Corrected single-order + overlapping-pair invocations passed;3 orders delivered; 17 backend + 16 restored UI local; slow/quota deferred.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 08-cross-role-order-lifecycle.md.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint26 (2026-10-03T07:40:00+05:30): a fresh HappyDeliveryFlowTest delivered bb43e2a4 (DB: DELIVERED, RELEASED, ledger balanced) but failed at :732: the delivered summary was not visible within 5s after the History click; teardown DOM shows it rendered, no console errors. Unexplained timing miss. Together with checkpoint25's accept-navigation wait, two consecutive fresh runs hit harness timing misses after successful server actions; investigate the waits if a third occurs. The delivered follow-up (`-Dresume.delivered.order.id`) passed on the same order.
