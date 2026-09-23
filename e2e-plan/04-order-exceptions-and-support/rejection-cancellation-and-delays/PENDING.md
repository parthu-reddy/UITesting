# Pending and observed failures

## CANCEL-07 / CANCEL-11 — customer cancellation before restaurant acceptance

`OrderCancellationFlowTest` now creates and scopes assertions to the exact new order, but live validation is not yet passing consistently.

- With seeded rider `7000000001`, order `8aa62e83-4a3b-448b-bee1-8e3d3bace4e3` reached `PENDING_ACCEPTANCE`, the Cancel Order button was clicked, and `POST /api/v1/orders/{id}/cancel` returned HTTP 409. The optimistic UI was reverted, so the expected terminal message never appeared.
- After introducing randomized seeded accounts, rider `7000000030` logged in and was put Online, but the selected Brand 1 outlet returned HTTP 409 from `GET /api/v1/restaurants/{id}/delivery-availability`. The payment dialog therefore did not open and no order was created.

These are deployed-environment state or behavior failures observed entirely through the UI and its browser network log. The test remains strict and active. Re-run with random seeded accounts; use the phone overrides only when reproducing one account's state.

`RestaurantRejectFlowTest` was rewritten to use randomized seeded customer, restaurant and rider accounts, the restaurant owner's matching brand, an outlet below 5 km, and exact-order cancellation assertions. Its first live run selected restaurant `9000000002`, rider `7000000017`, and Brand 2 Outlet 9 at 1.2 km. The delivery-availability request returned HTTP 409, so the payment modal did not open and the test could not create the order to reject. This is also left active rather than bypassing the availability rule.

The long-wait restaurant timeout scenarios CANCEL-15 and CANCEL-16 remain deferred by project decision.
