# Pending validation and environment failures


## 2026-10-05T11:57:12+05:30 — O4/O5 scoped regression acceptance

The required current methods for this feature pass on the retained Dev fixtures. Canonical60631296 is delivered with exact receipt/posted earnings/18balanced ledger lines, selected-item quote without refund submission and four immutable reviews; actual participant chat round trips are retained from the same lifecycle. Settings15/15 distinct methods,partner10/10,restaurant earnings1/1,navigation2/2,admin-money1/1 and exact beneficiary queue1/1 pass. Original failed invocations remain separately recorded; no duplicate lifecycle or server cleanup. See the checkpoint113 release evidence and feature-specific artifacts. Unselected/outcome/provider/internal/routed/duration/rate/SSE cases remain outside this acceptance.
## 2026-10-05T06:59:57+05:30 — checkpoint106: canonical delivered; existing money/quote branch running

Happy assigned resume invocation9 passes1/1,0failures/errors/skips in53.834s on UI509d084/harnessafe12f1. Pickup and delivery return actual200; the exact rider Delivered history has positive payout and today earnings increase by it; the exact customer Order delivered receipt and real post-delivery chat assertion pass. Evidence55 and the separate retained state manifest record DELIVERED. Original creation manifest and both older cancelled orders remain preserved. The canonical checkout/accept/dispatch/both actual chat round trips were observed in invocation6 and Ready/saved arrival in invocation8; their original later failures remain failures, not a fabricated single clean full run.

The existing retained-delivered branch now runs money/quote invocation1 on the same60631296. It reads the normal rendered History/receipt, verifies the exact admin money page and balanced posted payouts, then asks for a selected-item quote without submitting a refund. Staff1000000001 is used to spread ordinary admin verification quota before the final O4/O5 gates. No second checkout, direct business API, DB/Redis or cleanup is used.

Next: successful money/quote, unrated-dialog guard before review writes, required settings/partner/restaurant earnings/navigation/admin-money and immutable participant reviews using this one order, final O4/O5 image gates and measured histograms/checklists. Existing static O4 22/22,O5 7/7 and GitHub941/941 remain valid. Stop after O4/O5; explicit internal/performance-baseline/CSP-mutation/SSE/rate/duration/provider deferrals stay unverified and are now linked from the shared DEFERRED index.

## Current audit checkpoint — 2026-10-01

Fast CROSS01–15 verified in corrected deployed invocations. Backend/config SSE pool fix and UI acceptance/restored receipt fixes deployed. Intentional expiry, long scheduled preparation and quota exhaustion remain explicitly deferred. The 28-feature suite is unfinished.

All original retained orders are terminal now: ced55d8c delivered after deployment; the first failed concurrency pair has one delivered and one naturally timed-out restaurant cancellation. Corrected fresh CROSS15 and full CROSS09 invocations passed. See RandomDocuments/E2ECoverageAudit_2026-10-01/08-cross-role-order-lifecycle.md for exact evidence and explicit deferred coverage. Historical failures below do not describe current assigned rider state.

## Expanded lifecycle validation

`HappyDeliveryFlowTest` now asserts dispatch pickup/drop-off details, offered payout, countdown and both actions; exact active-contract identity and addresses; accepted-job persistence across reload; delivery-phase persistence across reload; return to the online/available state; exact completed-history details and payout; and the corresponding increase in today's rider earnings.

The first validation run of this expanded flow did not create an order. With a randomized seeded restaurant/rider/customer set, Brand 1 Outlet 7 was selected at 0.5 km and exposed an orderable 14-minute item, but `GET /api/v1/restaurants/{id}/delivery-availability` returned HTTP 409. The Complete Your Order dialog therefore never opened. This is an environment/prerequisite failure before the newly added lifecycle assertions; the strict test remains active and randomized retries are allowed.

A second randomized run completed order `7d5c7607-f41b-4b3e-9635-9cd637c32e37` through delivery. Dispatch details/actions, accepted-job reload, pickup, delivery-phase reload, return to available duty and the exact Delivered history row all passed. The first payout rule failed because the dispatch presented **Est. Delivery Fee ₹20.44** while completed history presented **net payout ₹18.86**. Source inspection confirms these are different concepts (`deliveryFee` versus `earnings.netPayout`). The test now requires the completed net payout to be positive and no greater than the gross offer, and uses the net payout for the Today's Earnings increase assertion.

A later run created and prepared order `df38362b-e9b3-46c9-a9b1-d1de2f1c296d`, but dispatch acceptance returned HTTP 410 and no Active Contract appeared. The ping had arrived late in its server acceptance window. The test now captures the rendered dispatch evidence and clicks Accept before evaluating those captured values, minimizing UI-side delay without weakening the assertions. The prepared order was not assigned by this failed attempt.

The next randomized validation attempt created no order because rider login received HTTP 503 twice from `/api/delivery/verification/status`; the Online/Offline control never rendered. This is a deployed-service availability failure before lifecycle setup.

The optimized dispatch run used customer `8000000092`, restaurant `9000000010`, rider `7000000013`, and order `e6b39e43-f1c9-4d0e-bb63-e5d6333b2cf8` at Brand 10 Outlet 3. Accept succeeded with 59 seconds remaining, proving immediate acceptance works; a case-sensitive test assertion then failed because styled `innerText()` returned `NEW DISPATCH`. That assertion now ignores case. The UI-only resume path subsequently restored the exact rider Active Contract and completed pickup, but a fresh customer login and reload did not restore the `Secure Delivery Verification` panel. Consequently the delivery OTP is no longer reachable through the customer UI and the rider remains in delivery phase for this exact order. No backend lookup or state mutation was used. The resume mode remains available through `-Dresume.order.id` and `-Dresume.outlet` if the customer tracker restoration defect is fixed.

The two confirmed causes now have source fixes: delivery commit `ef34907` preserves an accepted rider's Redis assignment locks across OFFLINE/disconnect state, and UI commit `fa844ff` restores the customer's active tracker and OTP after login/reload. Both commits are pushed. Deployment and a clean live `HappyDeliveryFlowTest` run remain pending.

## Latest deployed-flow validation (2026-09-24)

Order `f5a010f8-542d-44a9-9a57-196f48223e70` completed customer checkout, restaurant acceptance/preparation, dispatch acceptance, arrival and pickup. Its delivery-phase reload capture showed only the application loading spinner; the assertion had used Playwright's five-second default. The test now waits up to 60 seconds for the server-backed Active Contract after reload.

The immediate resume attempt was blocked before reaching the order because the rider verification endpoint returned HTTP 503. The order remains the preferred resume fixture with customer `8000000362`, restaurant `9000000006`, rider `7000000016`, and outlet `Brand 6 Outlet 9`.
