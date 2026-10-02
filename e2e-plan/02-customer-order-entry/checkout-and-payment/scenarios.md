# 02 — Checkout and Payment — All Scenarios

All scenarios require: seeded customer logged in, "Home" address selected, Brand1 outlet < 5 km selected, at least one item in cart. Uses `CustomerCartDrawerPage`, `PaymentModalPage`. Rider MUST have fresh server ONLINE, live DUTY_STATUS/connected tracking socket, current browser location and no connection/location-loss warning near the restaurant. Keep its context open; never cycle an already-ONLINE rider. The restaurant must be online.

## Batch 1 — Checkout initiation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-01 | Checkout button appears with items | Add at least one item to cart → open cart drawer. | "Proceed to Checkout" (or equivalent) button is visible and enabled. |
| CHECKOUT-02 | Checkout button absent with empty cart | Open a nonempty drawer then remove its final item. | Checkout disappears; empty-cart state remains, and closing hides View Cart. |
| CHECKOUT-03 | Checkout screen shows delivery address | Tap "Proceed to Checkout". | Checkout screen displays the selected "Home" address; address is non-empty. |
| CHECKOUT-04 | Checkout screen shows order summary | On checkout screen, verify item names, quantities, and subtotal match the cart. | Item list matches; subtotal correct. |
| CHECKOUT-05 | Checkout screen shows price breakdown | Delivery fee, tax, and grand total lines are all visible on the checkout screen. | Three separate line items visible; no line shows "₹null" or "₹undefined". |

## Batch 2 — Payment method selection

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-06 | Payment options visible | On checkout/payment step, verify at least one payment method option is visible (e.g. UPI, Card, Wallet). | Payment option list renders; no blank section. |
| CHECKOUT-07 | Select UPI | Tap the UPI radio. | Choice accepts the click and the Place order action remains enabled. The current UI has no UPI detail form or QR step. |
| CHECKOUT-08 | Select Card | Tap Credit or debit card. | Choice accepts the click and the Place order action remains enabled. The current simulated payment UI has no card-entry fields. |
| CHECKOUT-09 | Select Wallet | Verify the Wallet option. | Wallet option renders; it may be disabled when the displayed balance is insufficient. |
| CHECKOUT-10 | Payment choice after closing | Select a method → close payment → re-enter checkout. | UPI selection persists when closing/reopening the same mounted checkout; explicitly asserted. Wallet authorization must independently wait for the newly fetched balance. |

## Batch 3 — Successful checkout (integration)

| ID | Description | Prerequisite | Action | Expected result |
|---|---|---|---|---|
| CHECKOUT-11 | Full order placement | Rider available near restaurant; restaurant online. | Add item → select Home → checkout → select any available payment → confirm payment. | POST returns matching UUID/owner/item/amount; exact order tracker reaches PENDING_ACCEPTANCE after Dev mock completion. Deployed exact-order paid-status check passed. |
| CHECKOUT-12 | Order ID is non-empty | After successful checkout. | Inspect order confirmation. | Order ID is a non-empty string/UUID. |
| CHECKOUT-13 | Order confirmation screen dismissal | Observe the success state transition. | Product transitions directly to the matching order tracker; no separate Track Order/Done action exists. Fresh matching-tracker/closed-checkout integration passed. |

## Batch 4 — Checkout abandonment

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-14 | Back from checkout preserves cart | Enter checkout → tap Back. | Cart items still present; no partial order created. |
| CHECKOUT-15 | Close payment modal | On payment modal, tap "X" or Cancel. | Checkout sheet closes; existing cart drawer is visible and exact item remains. |
| CHECKOUT-16 | Reload during checkout | Enter checkout → reload page. | Implemented and live-passed: the unsubmitted payment modal closes, the authenticated dashboard renders, and the same cart item remains with checkout enabled. |

## Batch 5 — Payment failure handling

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-17 | Payment gateway timeout feedback | If the payment gateway returns an error/timeout, the UI shows an error message. | Routed UI proof passed: provider message visible, retained cart, enabled explicit retry; exactly one intercepted POST per click. Genuine timeout/compensation/server duplicate protection remains separate pending proof. |
| CHECKOUT-18 | Declined payment feedback | If the gateway declines the payment, a specific decline message (not a generic crash) is shown. | Routed UI proof passed: decline text remains visible and explicit retry changes UPI to CARD. Genuine provider decline is unverified. |

## Added source-grounded recovery/boundary scenarios

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-19 | Rejected stock items | Backend-shaped HTTP400 response with one of two item IDs. | Only the rejected item disappears; remaining item/quantity stay in the browser cart, rejection is visible and no successful-payment state is fabricated. Deployed; routed UI passed. |
| CHECKOUT-20 | Unread/insufficient wallet | Hold the wallet request until checkout renders, then return zero balance. | Wallet is disabled/unselected, Place order cannot pay from it, and CARD remains selectable. Routed UI passed. |
| CHECKOUT-21 | Wallet balance refresh on reopening | First open with sufficient balance, close/reopen while the new response is held, then return zero. | Previous balance never enables payment; new insufficient balance remains disabled, while UPI can be selected. Deployed; routed UI passed. |
| CHECKOUT-22 | Tip-inclusive payment | Customer opts into a tip; compare bill/button amount and outgoing order payload. | Tip starts at zero, adds to charged total and changes wallet sufficiency; local component proof passed. Routed payload and deployed mock-settled tip-inclusive order amount passed; final rider payout belongs to fulfillment/earnings. |

Long-duration and rate-limit execution is excluded. Current results are in 06-current-results.json; ten non-submitting invocations passed. Three exact-order Dev mock checkout checks passed. Full creation/provider/concurrency coverage remains open.

## Dev payment-status recovery

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHECKOUT-23 | Payment completes after creation | Dev mock succeeds while the UI leaves its success state. | Matching tracker uses a fresh server order and promptly reaches PENDING_ACCEPTANCE; no real money is charged. Deployed exact-order paid-status check passed. |
| CHECKOUT-24 | Tracking refresh fails after successful creation | Fail only the follow-up order read. | Preserve the successful order, close checkout, retain tracking and never submit another order POST. Fake-clock local regression passed; deployed forced-read-failure recovery passed with one successful order creation. |

HappyDeliveryFlowTest supplies the same card-checkout reference and owns later restaurant/rider fulfillment; its initial ID assertion alone does not prove paid-status freshness.

## Next required fast checkout proof

CHECKOUT-22: exercise tip 0 -> 20 against the actual quoted bill, make the wallet sufficient for the base bill but insufficient after the tip, and assert the exact tip/payment method in an intercepted order POST. Keep the real cart after an explicit failure. No actual order is submitted.

CHECKOUT-14-16: parameterized close, browser Back and reload must preserve the exact item/quantity while an order-POST interception counter remains zero. This replaces inference from merely retaining a cart with direct no-submission evidence. No duration or rate-limit tests are executed.

## Quote authorization and basket validation

CHECKOUT-25: missing or foreign-owner/restaurant/address quotes must fail before any menu/provider/dispatch/saga call or atomic quote claim. CHECKOUT-26: changing quoted item IDs or quantities must require a fresh quote without payment side effects. CHECKOUT-27: an already consumed quote must fail immediately; expired-timestamp local fixtures can prove the guard without elapsed-time waits. Concurrent database redemption remains a separate required transactional proof. These are local service-contract checks, separate from deployed browser evidence.

CHECKOUT-28: payment-intent or wallet settlement failure after order creation must cancel only a still-CREATED order and publish one ORDER_CANCELLED outbox event. A progressed order must not be overwritten. Successful orchestration must return the persisted order/intent without compensation. Local service tests verify behavior; real transaction/outbox atomicity remains with the backend concurrency review.

Additional required deployed checks reuse CustomerOrderPlacementTest's seeded preflight and retained manifest: a tipped CARD order must return/store quoted base + 20 with tipAmount=20, and an intercepted read failure after successful creation must still open the exact paid tracker via active-order reconciliation with only one successful creation. Dev mock payments only; retain both new orders. CHECKOUT-22/24 deployed executions both passed.
