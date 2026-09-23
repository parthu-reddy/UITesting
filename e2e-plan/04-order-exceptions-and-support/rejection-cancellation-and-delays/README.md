# rejection-cancellation-and-delays

Status: in progress.

Scope: Restaurant rejection, cancellation and preparation delay workflows.

Implemented first UI-only batch in `OrderCancellationFlowTest` (live validation currently blocked by the failures recorded in `PENDING.md`):

- logs the seeded rider in and makes the rider available before checkout;
- places an order from a Brand 1 outlet reachable from Home;
- proves the cancellation control is available before restaurant acceptance;
- cancels the exact newly created order and verifies its terminal customer message;
- proves the cancellation control is removed after cancellation.

`RestaurantRejectFlowTest` now prepares all three randomized seeded roles, selects the restaurant owner's matching brand and outlet, rejects only the exact new order with a reason, and expects the customer-facing restaurant cancellation source and reason. Live checkout is currently blocked by the delivery-availability failure in `PENDING.md`.

Validation results and deployed-environment limitations belong in this folder's `PENDING.md`.
