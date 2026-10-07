# 124 — UI deployed; retained receipt and owned rejection verified

## 2026-10-07T12:40+05:30 — Correction: invocations 3 and 4 finished unrecorded

Invocation 3 did not stay "running". It failed 1/1 in 27.18s before checkout, with no order and the
rider restored Offline ([evidence](../evidence/124-active-invocation3.json)). Invocation 4 created
ce254f3a and failed at the out-for-delivery courier pin
([evidence](../evidence/124-active-invocation4.json)). The order was resumed and verified in
[checkpoint125](125-retained-resume-false-zero-and-gps-pins.md).

## 2026-10-07T11:39:32+05:30 — Active attempt2 stopped before checkout; corrected3 running

Invocation2 errors1/1,0failures/skips,51.090s while locating the Brand1 heading. The captured settled
home feed visibly contains Brand1 inside its actual button card. It created no order or manifest;
current ONLINE/live-location preflight passed and the idle rider was returned OFFLINE through UI.
The helper now clicks the brand button containing exact visible brand text and demands one match.
The prior screenshot alone cannot distinguish accessibility matching from response timing, so do
not claim a product availability fault or an independently proven heading-role cause.
Invocation3 runs the corrected helper with unchanged proximity/duty/checkout/identity/finance
requirements. No result yet; any created124owned identity must be retained/resumed.
[Original error and capture](../evidence/124-active-invocation2-precheckout-error.json).


## 2026-10-07T11:37:19+05:30 — Retained finance gate passes; active mobile run started

The first retained money continuation failed1/1,0errors/skips,20.470s because the admin outlet name
is decorated by OwnerNameResolver with its brand. The corrected harness selects that actual display
name while demanding one match, exact owned order reference and unchanged financial assertions.
Second invocation passes1/1,0failures/errors/skips/network/browser errors,26.000s, on6fbe0289.
Actual CARD/SUCCESS total₹43.02, restaurant net₹6.54, rider net₹21.16;18ledger lines balance
₹92.72credit/debit and exact posted restaurant/rider payables agree. Quote/no new order/no refund
submission passes. [Original name failure](../evidence/124-retained-money-invocation1.json),
[corrected green](../evidence/124-retained-money-invocation2.json).

One fresh active lifecycle has started with the same seeded actors and new390×844 visual gates.
Current authoritative DUTY_STATUS ONLINE/live location preflight is verified; no completion result
yet. It writes a durable124-visual-owned manifest immediately upon order creation. Preserve/resume
any new identity on failure. This active regression is justified by the changed pickup/chat/swipe UI;
the original delivered order and rejected ticket remain retained. Item3 still active; M1/A5b paused.


2026-10-07T11:33:21+05:30. Items1/2 remain done; item3 alone is active before M1/A5b.
Owner confirmed “I've deployed UI alone for now”. UI6eb4ef6/GitHub37577846909 is served:
the normal rider login observes the CI-built DeliveryDashboard-CubehF8y.js and passes1/1,
0failures/errors/skips,7.762s. The actual rider screenshot is inspected. No infrastructure health claim.

The exact retained delivered order **6fbe0289-645e-4f1c-ae0d-caab21689070** now opens its delivered CARD receipt and
selected-item refund quote through History. Retained quote-only continuation passes1/1,
0failures/errors/skips,15.980s, **no new order and no refund submission**. The original active
lifecycle's late receipt failure remains preserved, not relabeled as a pass.

The one authorized request created ticket **47ffbccd-1f4a-42f1-93dd-f610f07bd923**, quote₹14.26. Its approval confirmation
was opened/cancelled with0resolution writes, then only rejection was submitted and returned200.
The writer method subsequently errored while parsing null Response.request().postData(),24.270s:
1error/0failures/skips. Its failure-time OPEN snapshot is stale, not a current server result.
The same-ticket resume passes1/1,0failures/errors/skips,20.380s, **0new orders/0resolution writes**.
Actual ALL read, detail, resolution audit, exact note and persisted actor/timestamp verify REJECTED;
customer refund reads before/after are empty. No refund approval or payout. The settled final audit
image is inspected; initial confirmation PNGs caught fade-in and are functional text/action evidence,
not settled visual proof. No second ticket/resolve is needed. Original artifacts are retained.

The harness captures future resolve bodies at request time and persists a validated response state
before optional observer assertions. It waits for dialog opacity before future captures.
The complete lifecycle's money helper had typed a direct order-money URL. It now follows normal
Pending Payouts → exact outlet drawer → actual order reference, retaining all financial assertions.
Compile passes; the exact retained money continuation is running and has no result yet.

Remaining item3 gate: verify retained money through that UI route, then one justified fresh lifecycle
for changed active pickup-hint/header-chat/swipe/pin checks. The delivered retained fixture cannot
re-enter pickup. Full current preflight and durable new identity are mandatory; resume it on failure.
No weakening of default money/chat/quote assertions, backend deploy, reset or direct-state fixture.
M1/A5b and explicit duration/rate/SSE/provider/load/CSP/timing/design/deletion deferrals remain paused.

[Served release](../evidence/123-ui-release-read-invocation1.json),
[retained receipt](../evidence/123-retained-receipt-invocation1.json),
[original writer error](../evidence/123-owned-refund-invocation1-error-after-rejection.json),
[same-ticket green resume](../evidence/123-owned-refund-invocation2-readonly-green.json),
[retained ticket](../fixtures/123-owned-refund-47ffbccd-1f4a-42f1-93dd-f610f07bd923.json).
