# Customer tracking and history

Status: active feature audit. History/reorder passed13deployed browser methods; map/carousel strengthening is locally verified and deployment/live reruns are in progress. Do not call the whole feature or suite green yet.

The mounted history view is Account → History (`SettingsHistoryTab`), with dated/full-ID summaries, pagination and loading/error/empty recovery. Home's `ReorderStrip` now restores validated current-catalogue items through an owned completed order/address-bound quote; it does not submit an order. The standalone old history overlay is not the tested route.

Use customer8000000484's retained completed/cancelled orders. History/reorder tests block all order POSTs. Real happy/concurrent lifecycle methods prove automatic status convergence and independently owned records; the map and carousel assertions are being rerun after UI816c6f6. Delay approvals/rejections are cross-referenced to `04-order-exceptions-and-support/rejection-cancellation-and-delays` and must receive real lifecycle verification there.

See [scenario matrix](scenarios.md), [remaining work](PENDING.md) and [audit09](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/09-customer-tracking-and-history.md). Duration/rate tests remain deferred and excluded. No automatic data cleanup; only idle riders becomeOFFLINE.
