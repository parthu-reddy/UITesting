# Pending validation and environment failures

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
