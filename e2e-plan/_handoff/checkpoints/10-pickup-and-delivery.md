#10 — Pickup and delivery

User clarification on 2026-10-02: riders deliver assigned packages; itemized contents are not a rider requirement. The itemized UI was deployed, but its new browser assertion failed because driver reads omit items. This is not a required delivery defect. The item-exposure backend change was stopped before any product edit, and the item assertions were removed. The retained order `15619441-0198-4bae-84e5-abe156a07333` must be resumed using its owned fixture, without creating or cleaning up an order. The deployed itemized UI remains unchanged by this scope correction.

Earlier status below is historical and superseded where it describes item visibility as required.

Status: active source review/implementation. No blanket feature pass.

## Reviewed scope before execution

Read owning README/scenarios, all seven PickupDeliveryOtpTest methods, canonical HappyDeliveryFlowTest pickup/delivery/pair/resume, page objects, LiveOrderFixture, SeededRiderDuty, SwipeHelper, actual DeliveryActiveJob/ActiveDeliveryCard/useRiderJobActions and delivery OTP state strategies. Historical suite XML contains seven timeouts; no historical method pass is reused. Current source335methods/75classes.

| Scenarios | Existing defect/gap | Work and required proof |
|---|---|---|
|PICKUP01/04/05/07–09/11;DELIVERY01–05/09–12|Legacy checkout skips current quote/payment and sleeps; stops with active jobs; broad dispatch scope|Use canonical quoted fixture/current exact IDs, response-confirmed arrival/pickup/delivery, restored phases, exact receipt/history/positive payout; complete each retained flow in test body, idle OFFLINE only|
|PICKUP10;DELIVERY06|000000 can equal real OTP; exceptions swallowed and remaining phase alone can false-pass|Derive guaranteed distinct code from visible actor code, require exact owned POST400 and Invalid pickup/delivery OTP UI, unchanged active assignment through reload, then demonstrate correct code completes same order|
|PICKUP03;DELIVERY07|Old absent Navigate selector; catch-all/no assertions|Use actual accessible map popup directions button; verify newly opened navigation target's API/destination coordinates with external-content routing labelled boundary-only|
|PICKUP02/06|Active job has no View Details or item checklist; historical DeliveryOrderDetailsModal is earnings/history only|Real product gap: add accessible active-order itemized details after assessing current order payload, with local regression and deployed browser verification; do not call absent controls covered|
|DELIVERY08|Source exposes exact customer call button, not masked-number text|Cross-role calling/signalling and exact recipient belongs to chat/calls feature; role-correct call control can be asserted here without claiming live-call proof|
|DELIVERY13|Customer Unavailable appears after elapsed waitTimerSeconds>5; plan's conditional if-exists is a false-pass|Timing-gated unavailable/failure and financial effects assigned exception feature; duration run deferred, immediate fake-clock proof allowed|

## Execution policy

No cleanup, synthetic OTP/API status writes, expiry/quota waits or government/payment providers. Retain every manifest/order. Successful fixture completion is part of each flow's explicit delivery assertion, not teardown deletion/reset. A failed active delivery is preserved for exact-order continuation; do not replace it. Public SSE explicitly user-deferred; this feature does not require SSE events or fabricate tracking. Follow current TEST-DATA and read-only idle-rider preflight.


## Fast execution checkpoint

All seven repaired existing methods passed against the public Dev URL: wrong pickup54.369s, other six316.400s; zero errors/failures/skips. Wrong pickup/delivery each use a guaranteed-distinct visible-actor code and require owned POST400/explicit Invalid OTP, preserved phase after reload and successful correct-code completion. Short-code rejection requires visible validation and no status POST; numeric filtering and six-digit truncation exercised. Navigation proves the actual accessible restaurant direction popup and coordinate-valid Google Maps target, with external page content explicitly routed; no external provider success is claimed.

All seven normal quoted Dev orders are retained; read-only postconditions prove every order DELIVERED, assignment RELEASED, rider26OFFLINE,26customer orders/2addresses. Teardown does not cancel/abort/delete orders or revoke sessions. Public SSE events are explicitly user-deferred. Preparation readiness uses the ordinary authenticated active-order polling response instead of waiting for unsupported public SSE.

PICKUP02/06 product gap implemented locally: active View order details dialog shows exact assigned items/quantities, customer/destination/order total using existing authenticated payload; shared Modal and money formatting, no extra request or ledger disclosure. Closing/resource switching/absent items have3before-fix failures and13after local checks; typecheck/lint pass. The existing valid-pickup method now asserts these exact fields against its own creation response before and after pickup; Java compilation passes. Those new details assertions are deployment-dependent and were added after the seven passing browser methods; do not claim their live proof yet. User chose to deploy and confirm. No publishing/deployment by this audit after6AM. Handoff: outputs/pickup-handover-details-deployment.md in task workspace.

A local DeliveryOtpSourceTest success method caught arbitrary completion exceptions and only checked an outbox mock. Its fixture now supplies an ON_DELIVERY rider and Redis operation mocks; assertion requires no exception, riderONLINE and DELIVERED/RELEASED assignment with releasedAt. This is local strategy evidence, not database transaction atomicity or deployed release proof. Focused backend rerun passed19checks across DeliveryOtpSourceTest(8),DeliveryAssignmentAuthorizationTest(5),ConfirmedDeliveryProgressTest(6), with0errors/failures/skips. No deployed backend behavior was changed.


## Additional fast boundary and deployment handoff

DELIVERY14 was missing from the plan: selecting Go offline after delivery. Existing completeDeliveryWithOtp now checks the exact successful DELIVERED command's goOfflineAfter=true, immediate Offline UI, completed trip/history/customer receipt. Its rerun passed56.967s without skips/errors/failures. Read-only postconditions confirmed retained orderbf8cbbaa-29b1-4812-9e63-b5030e1d4e4b DELIVERED, assignmentRELEASED and rider26OFFLINE. Customer now has27orders/2addresses; no cleanup. This is an additional invocation, not an eighth source method.

Final source inventory remains75classes/335methods/28features, with method lines refreshed and existing review/evidence preserved.17curated issue/practice documents and74local links validated without issues. The active feature remains pickup-and-delivery until the user confirms the new UI deployment and the exact itemized-details assertions run. Full-suite completion is not claimed.


## Package-delivery continuation completed

Following the user’s rider scope clarification, the existing valid-pickup method resumed retained order `15619441-0198-4bae-84e5-abe156a07333` and passed in40.204s (1test;0failures/errors/skips). No replacement order was created. Pickup and delivery commands succeeded; read-only proof confirms DELIVERED, assignment RELEASED and rider OFFLINE, with all28customer orders/2addresses retained. Item-list assertions were removed; no backend item-exposure product edit was made.

Fast pickup/delivery review is complete. Customer-call signalling remains assigned to communication; unavailable/elapsed exception behavior and quota tests remain deferred/assigned to exceptions. Public SSE remains user-deferred. Proceed to feature11, restaurant acceptance and preparation. Evidence: [resumed browser report](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/10-package-resume-final-PickupDeliveryOtpTest.xml), [retained postconditions](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/10-package-resume-postconditions.json).
