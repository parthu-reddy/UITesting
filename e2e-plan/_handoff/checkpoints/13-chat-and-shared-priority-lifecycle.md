# Chat and shared priority lifecycle — 2026-10-02

Fresh seed: customer 8000000484, restaurant 9000000001, rider 7000000026, admin 1000000001. All verified active with their expected roles; 104 outlets, zero initial orders. One owned CARD order `b83c71bd-022c-439d-8586-aa4fe5b1c0d1`, total ₹43.35, Brand 1 Outlet 3.

The existing HappyDeliveryFlowTest executed acceptance, preparation, assignment, pickup, delivery, customer/restaurant history and rider payout. Cross-role chat evidence records customer/restaurant/rider messages and replies, delivered grace message, persistence after reload, blank/whitespace/length validation, typing, unread, reconnect and uploaded image receipt (HTTP 200). Chat helpers created no order. The complete test FAILED at the admin money GET (HTTP 500), before aggregate chat assertions and refund quote. These earlier executed assertions and captured evidence do not make the complete invocation green.

The order is HANDED_OVER / DELIVERED in the authoritative DB, and rider OFFLINE. Records are retained. SSE, duration and rate-limit cases remain deferred.

Seven affected browser-fixture tests passed with zero skips: three chat retry/loading cases, three admin recovery cases, one ledger pagination case. These do not mutate server money and do not replace integration proof.

## Retained continuation

`-Dresume.delivered.order.id=<owned UUID>` reuses the exact manifest/customer history and rider payout, with new order POST blocked. `-Dresume.delivered.quote.only=true` isolates the quote while admin money is deployment-blocked. This branch does not repeat delivery and is reported separately.

The first quote continuation accidentally inherited the happy chat helper’s injected first-session outage; isolated by moving helper construction after the retained branch. The corrected continuation sent a real refund quote request. The chat outbox row remained UNPROCESSED with zero retries, and no quote response reached the browser. The real chat startup context regression reproduced a missing OutboxProcessor; preserving Boot auto-configuration scan exclusions repaired it locally. Redeploy chat-service, then continue the same order. No support ticket approval or refund payment has occurred in this run.

## Remaining

- Verify deployed admin money response and exact-order monetary arithmetic/balanced ledger.
- Verify deployed quote response carries exact order/items/quantity/reason and can submit that scope.
- Real controlled support decisions and cancellation/rejection recovery remain unverified.
- Unrelated-user isolation and latest-page/large-history/concurrent chat sessions remain assigned follow-ups; do not infer them from same-order message receipt.

Strict optional startup diagnostic with bean overrides disabled exposes existing duplicate wallet-service Feign client specification. Deployment and platform defaults enable overrides. Normal-profile startup passed; stricter mode is a recorded separate follow-up. No workflow/config default was changed.

## Postdeployment checkpoint

The publisher event is PROCESSED and admin money now returns HTTP 200. The quote is blocked at the Kafka raw-payload/envelope mismatch. Capture-only balanced ledger rows concealed missing delivered payables; settlement is not passed. Distinct customer cancellation and restaurant rejection reached their terminal UI states but refunds stayed PROCESSING. Source fixes and owned IDs are recorded in 14-postdeployment-financial-contracts.md. No additional delivered lifecycle was created.
