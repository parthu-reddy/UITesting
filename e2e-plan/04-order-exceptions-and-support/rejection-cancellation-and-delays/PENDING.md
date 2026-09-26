# Pending and observed failures

## CANCEL-07 / CANCEL-11 — customer cancellation before restaurant acceptance

`OrderCancellationFlowTest` now creates and scopes assertions to the exact new order, but live validation is not yet passing consistently.

- With seeded rider `7000000001`, order `8aa62e83-4a3b-448b-bee1-8e3d3bace4e3` reached `PENDING_ACCEPTANCE`, the Cancel Order button was clicked, and `POST /api/v1/orders/{id}/cancel` returned HTTP 409. The optimistic UI was reverted, so the expected terminal message never appeared.
- After introducing randomized seeded accounts, rider `7000000030` logged in and was put Online, but the selected Brand 1 outlet returned HTTP 409 from `GET /api/v1/restaurants/{id}/delivery-availability`. The payment dialog therefore did not open and no order was created.

These are deployed-environment state or behavior failures observed entirely through the UI and its browser network log. The test remains strict and active. Re-run with random seeded accounts; use the phone overrides only when reproducing one account's state.

`RestaurantRejectFlowTest` was rewritten to use randomized seeded customer, restaurant and rider accounts, the restaurant owner's matching brand, an outlet below 5 km, and exact-order cancellation assertions. Its first live run selected restaurant `9000000002`, rider `7000000017`, and Brand 2 Outlet 9 at 1.2 km. The delivery-availability request returned HTTP 409, so the payment modal did not open and the test could not create the order to reject. This is also left active rather than bypassing the availability rule.

The long-wait restaurant timeout scenarios CANCEL-15 and CANCEL-16 remain deferred by project decision.

## DELAY-01..07 — restaurant delay decision

`DelayApprovalFlowTest` now contains strict approval and rejection paths. Both register the randomized seeded rider through the UI, place an order against the randomized restaurant's matching nearby outlet, target the exact order card, submit a 15-minute delay with a reason, and verify the result in both customer and restaurant UIs.

**2026-09-24 (evening), re-checked against the redesigned tracker.** The customer's prompt is
now "The kitchen asked for N more minutes" with the kitchen's reason, and the buttons are
**"I’ll wait"** (a typographic apostrophe) and **"Cancel order"**, which confirms first
("Cancel instead of waiting?"). The restaurant card's status is read from its `data-status`.

**UI finding — FIXED 2026-09-25 (UI `useLiveOrderActions`, backlog A9): the screen now sets
CANCELLED_BY_RESTAURANT, the status the server settles on. Kept for the record:** the tracker
misstated the outcome of a declined delay until it refreshed.
Declining publishes `ORDER_DELAY_REJECTED`; `AwaitingDelayApprovalState.handleDelayRejected` settles
the order as **CANCELLED_BY_RESTAURANT**, reason "Customer rejected delay". But
`useLiveOrderActions.answerDelay(false)` sets **CANCELLED** locally, so the screen says "This order
was cancelled." until the next poll or reload says "The restaurant could not fulfil this order."
The test therefore reloads and asserts the server's status, not the optimistic one. Traced in
source, not yet observed live.

Live validation is pending while the deployed rider verification endpoint returns HTTP 503 and prevents the prerequisite rider duty control from rendering. No backend shortcut was added.
