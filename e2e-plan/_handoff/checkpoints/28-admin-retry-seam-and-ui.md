# Checkpoint28: Dev refund failure seam and the missing admin retry screen

Updated 2026-10-03T08:10:00+05:30. The user chose to add a Dev-mock failure seam so the admin refund retry can be proven live.

## Found first

The admin UI had **no screen to list or retry failed refunds**. `AdminDlqController` (`GET /api/v1/internal/admin/orders/dlq/refunds`, `POST …/refunds/{id}/retry`) and the generated `customerApi.adminDlq` client existed, but nothing in FoodDeliveryAppUI called them. An operator could not retry a failed refund without calling the API by hand.

## Changes (local, uncommitted)

- **PaymentGatewayIntegration:** `MockRefundFailureSeam`. A refund of exactly ₹1.13 is declined on its first initiation and accepted on the retry, remembered per refund id in Redis. It is registered only on the mocks' profiles (never prod) and keys only on the amount, so no production event gains a test flag. All three mocks use it. Tests: 3 new (mutation red); full clean test 93/28.
- **FoodDeliveryAppUI:** Money Operations has a **Failed Refunds** tab: amount, refund, order and reason, plus Retry Refund behind a "Retry refund of ₹X?" confirmation that says queued is not returned money. 4 new tests (red when the tab is removed); vitest 754/121, typecheck, lint.
- **UITesting:** `AdminRefundRetryFlowTest` (REFUND-RETRY-01) creates the ₹1.13 refund through the real support path, watches it fail, retries it from Money Operations, and checks the same refund completes with PARTIALLY_REFUNDED and a balanced REFUND ledger. The support helpers moved unchanged into `util/SupportRefundSteps`; SupportRefundResolutionFlowTest uses them (compile-checked, not yet re-run live).

## Pending: one deployment, then one run order

Deploy **customer-service, chat-service, payment-gateway and food-delivery-app-ui** (checkpoint27 and checkpoint28 together; no migration or config). Then, per NEXT-STEPS: one fresh delivered order serves CHAT-22 and REFUND-RETRY-01 inside its two-hour chat window; ChatAndRefundIsolationTest can run on d3acfc93 at any time.

Evidence: [28-admin-retry-seam-local.json](../evidence/28-admin-retry-seam-local.json).
