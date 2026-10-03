# Checkpoint31: order-money panel verified per outcome; priority list complete

Updated 2026-10-03T09:32:00+05:30. The user deployed the UI (`6eb1743`, holding exactly the AdminOrderMoney change).

`AdminOrderMoneyOutcomesTest` (MONEY-05): **PASS 5/5** — bb43e2a4 delivered, d3acfc93 and cf608115 partial (restaurant fault), 19359711 cancelled, bf109947 rejected. Red 5/5 on the previous UI. [Evidence](../evidence/31-order-money-outcomes-deployed.json).

## The priority list (chat, refunds, money, order admin), as proven live

| Item | Proof |
|---|---|
| Support refunds: denial, reduced award, refusal of an already-refunded item | SupportRefundResolutionFlowTest 3/3 (checkpoint25) |
| Admin retry of a provider-declined refund | AdminRefundRetryFlowTest (checkpoint29, Dev seam) |
| Chat isolation (HTTP, STOMP), refund and order reads | ChatAndRefundIsolationTest (checkpoint29) |
| Chat history beyond one page | ChatHistoryPagingTest (checkpoint29) |
| Support entry closes with the chat window | ChatSupportWindowClosedTest (checkpoint26) |
| Cancellation and rejection refunds | OrderCancellationFlowTest, RestaurantRejectFlowTest (checkpoint30) |
| Admin order money per outcome | AdminOrderMoneyOutcomesTest (checkpoint31) |

## Still open

- SupportRefundResolutionFlowTest after its helper move: compile-checked, re-prove at its next natural run.
- Consumer refund routing refusal (OrderEventConsumer/PaymentEventConsumer): local real-transaction proof only.
- Separate task: page-size bounds and validation-exception mapping.
- Remaining features in FEATURE-STATUS (reviews/support, partner/account management, live operations, resilience).
