# Checkpoint27: chat isolation proven live; order read 500 and long chat history fixed locally

Updated 2026-10-03T08:00:00+05:30. The user decided the two-hour support window stays UI-only and asked to continue the priority list.

## 1. Admin refund retry, live: not reachable in Dev

Every Dev payment mock (Razorpay, Cashfree, Vyapar) returns `true` from `initiateRefund` and always reports refund success. The failure branch (`PaymentEventConsumer:105`) and provider refund-failure webhooks (need the webhook secret) cannot fire. The retry stays proven by checkpoint20's local real-transaction tests only. **Decision asked of the user:** keep it that way, or add a Dev-mock failure seam.

## 2. Chat, refund and order isolation (live)

New `ChatAndRefundIsolationTest` on d3acfc93. The owner control (session, history, STOMP subscription) passed. An unrelated customer got 403 for history, session lookup, join and refunds, and a STOMP ERROR "Access Denied" for subscribe and send; another brand's owner and an unassigned rider got 403; the owner's history was unchanged. **One defect:** the unrelated customer's `GET /api/v1/orders/{id}` answered **500** (bare `RuntimeException` at `CustomerOrderService.java:102`, logged as an unhandled ERROR). Nothing leaked, but the status was wrong and it raised false error alerts.

Fix (CustomerApplication, local): `ResourceNotFoundException("Order not found")` → 404, the same for a missing order and someone else's. The unused `getOrderById` and its contract stub are deleted. `OrderReadOwnershipTest` (real service and handler) was red before (500) and is green after; full clean test 467/93.

## 3. History beyond 50 messages

The UI asked for page 0 only, so after a reload everything older than the newest 50 messages was unreachable, and the endpoint's `size` was unbounded.
- CommunicationService (local): `getMessages` answers 400 for `page < 0` or `size` outside 1..100, after the membership check (inline, like the controller's other 400s, because the shared handler maps no constraint violations). 63/20 clean test.
- FoodDeliveryAppUI (local): `useChatSession` follows the server's `last` flag and `loadOlderMessages` pages back, merging by id in time order; `ChatMessageList` shows "Load earlier messages". 750/121, typecheck, lint.
- **Live, before deploy:** new `ChatHistoryPagingTest` (CHAT-22) topped bb43e2a4's chat up to 60 and failed as expected on UI 41578ee: the oldest messages were unreachable, with no control.

## Flagged separately

26 of 34 paged endpoints across 9 services have no size bound, and the shared exception handler maps no validation exceptions, so existing `@Max` bounds may answer 500 (source reading, not run). Spawned as its own task.

## Pending

1. **Deploy customer-service, chat-service and the UI** (no migration or config). Then rerun `ChatAndRefundIsolationTest` (expect 1/1) and `ChatHistoryPagingTest` (expect 1/1; bb43e2a4's chat window closes about 09:29 IST, after that use a fresh in-window order). Commands are in NEXT-STEPS.
2. User decision on the admin retry seam.
3. Then: admin order-money views per outcome.

Evidence: [27-priority-chat-isolation-history.json](../evidence/27-priority-chat-isolation-history.json).
