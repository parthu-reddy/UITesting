# 123 — Active visual audit finds receipt and mobile defects

2026-10-07T11:12+05:30. Item3 remains the only active item; items1/2 are done and M1/A5b are paused.
The earlier canvas/sidebar release `5a1e97c` passed its final3 live visual methods. Those fixes and
F12 are fixed; the results below concern additional active-state behavior.

## Actual deployed results

One `HappyDeliveryFlowTest#completeOrderLifecycle` execution created retained order
**6fbe0289-645e-4f1c-ae0d-caab21689070**, Brand1Outlet5, customer8000000001,
owner9000000001, rider7000000001. Preflight, exact assignment, arrival/pickup/delivery, rider
completed trip/payout and customer delivered history passed before the method failed at its
history-to-receipt assertion. Result **1 failure / 0 errors / 0 skips**,253.778s, not a full pass.
[Original bounded evidence](../evidence/122-role-active-invocation1-failed-late.json),
[exact manifest](../fixtures/122-visual-owned-6fbe0289-645e-4f1c-ae0d-caab21689070.json).
The single401/resource error occurred immediately after customer sign-out in teardown, not before
the receipt assertion. Preserve it; do not claim a clean whole invocation or a failed order write.

Assigned/out-for-delivery canvas,48px contact/swipe and overflow assertions passed and saved
four390×844 images. Inspection found a clipped pickup hint and chat obscuring the delivery swipe.
The first map frame was captured too early; source/text had actual customer/restaurant pins, and
later tiles loaded. New checks wait for actual pins; no SSE/provider/outage acceptance is claimed.

Fresh `RoleVisualAuditUiTest#adminAllRefundHistoryRead` passes **1/1,0 failures/errors/skips**,10.670s,
UI-generated GET200, **0 rows / 0 resolution writes**,0 network/browser errors. Its settled All Tickets
capture is inspected. [Disposition](../evidence/123-admin-all-refunds/admin-refunds-all-disposition.json).
Empty state is verified; populated action/detail/audit branches remain unverified, not green.

## Local fixes and checks

`useTrackedOrder` keeps a terminal history/poller record over a stale active projection, preventing
the live rail from hiding a delivered receipt. New regressions first fail2of3on old code; current
focused receipt/dashboard tests pass10. Rider pickup hint has normal placeholder spacing, fits a
mobile input and has an associated label/error. Rider chat's48px launcher is hosted in the header,
while the open window remains outside header glass/stacking/containing-block constraints.

Full UI **174 files / 1,049 assertions**, focused **34**, typecheck,lint,build and Phase4 source12/12
pass. Generated clients have no residual diff. Owner committed the eight tested UI files as6eb4ef6;
GitHub37577846909 is publishing, with no agent commit/push. Current
visual locators28PASS/0FAIL/5DYNAMIC; rider helper7PASS/0FAIL/3DYNAMIC. The helper now hit-tests
the swipe, measures the complete hint, verifies header chat size and waits for actual known pins.
Those new active assertions are compiled, not yet executed on the new UI.

Initial harness compile failed because this test-only project has no Jackson dependency. It now
uses the established pure JSON.parse evaluation for the observed UI response; the subsequent
Maven invocation recompiles and passes the read-only method. No dependency was added.

## Exact next gates

1. Owner builds/deploys **FoodDeliveryAppUI only** for these additional fixes. No backend, schema,
   reset, agent commit/push or direct-state E2E. [Concrete handoff and retained command](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ACTIVE-DEPLOYMENT.md).
2. After confirmed live, use the existing retained-delivered branch on6fbe0289 to verify the exact
   receipt/quote without another order. Keep the original failure; quote is not refund submission.
3. The delivered fixture cannot reproduce active pickup/swipe state. One fresh lifecycle is justified
   only after deployment to test the changed active UX with full current preflight and a new manifest.
   Preserve/resume that new identity on failure. Do not replace the original failed receipt fixture.
4. Owner explicitly approved the exact owned dummy support request/rejection to exercise populated
   admin detail/confirm/audit. [Reviewable fixture plan](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ADMIN-FIXTURE.md).
   OwnedRefundVisualUiTest compiles, with13PASS/0FAIL/9DYNAMIC locator sites. No ticket has yet been
   created/resolved for this batch. Run only after the repaired UI is live and the retained receipt passes.
5. Close item3 only with exact dispositions and deployed proof, then proceed to item4 M1 and item5
   A5b sequentially. C7/B10/B11/F11 deletion and timing/CSP/SSE/rate/duration/provider/load deferrals remain.

[Local gates](../evidence/123-active-ui-local-gates.json),
[All queue invocation](../evidence/123-admin-all-refunds-invocation1.json).
