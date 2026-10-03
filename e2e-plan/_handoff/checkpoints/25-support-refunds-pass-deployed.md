# Checkpoint25: support refunds pass on deployed code; the post-delivery chat window

Updated 2026-10-03T04:40:00+05:30. The user deployed customer-service and asked to rerun the support refund test.

## Deployment verified (read-only)

customer-service `cc04ed7` ("Fix chat refund logic") is running and healthy. The commit holds exactly the 8 checkpoint24 files. The Dev data was **not** reset this time (0554f250 intact, 1 order before this run).

## Runs, in order

1. **SUPPORT-REFUND-03 alone on 0554f250: ERROR** after 97.9s. The "Request refund quote" dialog never opened. Cause, from source: `CustomerOrderChat` offers a finished order's chat only for **two hours after its `updatedAt`**. 0554f250 was 5h old, so the chat widget was unmounted, and "Something wrong with this order?" called `chatWidgetRef.current?.…` on null: a silent no-op. **Checkpoint24 wrongly said this rerun's precondition held**: it checked tickets and refunds, not the chat window. The backend has no such window; it is UI-only.
2. **Fresh `HappyDeliveryFlowTest`: ERROR** at `DispatchPingPage.acceptDispatch:64` (332.9s). The accept click was done and nginx shows the accept POST answered **200**; Playwright then waited for "scheduled navigations" past the 5s click timeout, though the rider page requested no document. DB: order **d3acfc93** ACCEPTED/ASSIGNED, rider ON_DELIVERY. An unexplained harness transient; the same code passed at checkpoint24. Not changed.
3. **Resume on the exact order** (`-Dresume.order.id=d3acfc93-7aa2-4aae-aad0-7a9711c31b8d '-Dresume.outlet=Brand 1 Outlet 3'`): **PASS** 1/1, 73.1s. DELIVERED at 22:59:03Z; 18 ledger entries balanced at 115.46; 0 rejections; rider OFFLINE.
4. **`SupportRefundResolutionFlowTest`, full class on d3acfc93: PASS 3/3**, 0 skip, 73.4s.
   - 02 denial: ticket 005c08a4 REJECTED, no refund.
   - 01 award: ticket a301b711 RESOLVED 12.00; refund d3e4b73c COMPLETED ORIGINAL_METHOD, RESTAURANT_FAULT; intent PARTIALLY_REFUNDED; CLAWBACK 4.28; 22 entries at 131.74; 0 rejections.
   - 03 refusal: **answered**. Customer outbox `CHAT_REFUND_ERROR {"error": "ITEM_ALREADY_REFUNDED"}` committed and the REFUND_ERROR chat message is shown. customer-service logs since the deploy: **0 DLT writes, 0 UnexpectedRollbackException**, the refusal logged once per request with no retries.
5. **New precondition guard** (below), SUPPORT-REFUND-03 only: the stale 0554f250 **fails in 13.9s** with "must be within the two-hour post-delivery chat window … 5H12M"; the fresh d3acfc93 **passes** (19.3s, second refusal also answered and committed).

Evidence: [deployed runs](../evidence/25-deployed-support-refunds.json), [local results](../evidence/25-local-results.json).

## Changes (local, uncommitted)

- **FoodDeliveryAppUI, the dead button.** `isOrderChatOffered(order, now)` in `model/orderStatus.ts` is now the one rule (active, or within `CHAT_AFTER_FINISH_MS` = 2h of `updatedAt`). `CustomerOrderChat` and `OrderDeliveredSummary`'s "Something wrong with this order?" both use it, so the button is never shown without a chat to open. Tests: +4 rule cases and +2 summary cases; the hide case was seen red with the guard removed. typecheck and `npm run lint` exit 0; vitest 121 files / 743 tests.
- **UITesting.** `SupportRefundResolutionFlowTest.openDeliveredOrder` asserts the two-hour window from the order's `updatedAt` before acting, and the Javadoc states the precondition.

## Open, for the user

- **Deploy the UI** for the dead-button fix (no backend change).
- **Product decision, not changed:** the two-hour support window is enforced only in the UI. Chat sessions, quotes and refund requests are accepted by the backend for any delivered order at any age. Enforce the window server-side too, or keep it as UI guidance?
- The rider accept-click navigation wait is unexplained. Watch for a recurrence before changing the page object.
