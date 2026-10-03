# Checkpoint29: isolation, history paging and admin refund retry pass on deployed code

Updated 2026-10-03T08:50:00+05:30. The user deployed the four services from checkpoints 27/28 and asked to run the tests. (Correction: checkpoint28's timestamps were first written as 08:40, ahead of real time; they now read 08:10, approximate.)

## Deployment verified (read-only)

customer `07ea84f`, chat `089e5eb`, payment-gateway `5220632` and UI `766b214` are healthy; each commit holds exactly the planned files. The Dev data was not reset.

## Runs, all real deployed flows

| Test | Order | Result |
|---|---|---|
| ChatAndRefundIsolationTest (CHAT-ISO-01..03) | d3acfc93 | **PASS** 1/1. The outsider's order read now returns 404; customer-service logged 0 unhandled exceptions |
| HappyDeliveryFlowTest, fresh | **cf608115-ee1e-468c-8a7f-1212e1cef902** | **PASS** 1/1, 421.7s |
| ChatHistoryPagingTest (CHAT-22) | cf608115 | **PASS** 1/1. "Load earlier messages" brings the oldest message in |
| AdminRefundRetryFlowTest (REFUND-RETRY-01) | cf608115 | **PASS** 1/1. Refund 67562478 was declined once, retried from Money Operations → Failed Refunds, and COMPLETED |

Money proof for the retry: the order has one customer refund (COMPLETED ₹1.13) and one gateway refund row (COMPLETED ₹1.13). Intent PARTIALLY_REFUNDED; REFUND 1.13 balanced, restaurant CLAWBACK 0.40; 22 entries balanced at 116.99; 0 ledger rejections. Gateway log: initiation, seam decline, admin-retry initiation, success. 0 dead letters, 0 rollback-only errors. [Evidence](../evidence/29-deployed-priority-runs.json).

## Still open

- SupportRefundResolutionFlowTest after its helper move: compile-checked, not re-run live (next natural run).
- The page-size/validation-handler task (separate chip).
- Next on the priority list: admin order-money views per outcome.
