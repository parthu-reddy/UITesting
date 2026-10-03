# Checkpoint26: the support-button fix is verified on the deployed UI

Updated 2026-10-03T07:40:00+05:30. The user deployed the UI and asked to verify the button fix.

## Deployment verified

food-delivery-app-ui `41578ee` ("Fix UI refund logic") is running and healthy and holds exactly the five checkpoint25 files. customer-service is unchanged at `cc04ed7`.

## Both sides of the rule, live

- **Window closed: PASS.** New `tests/features/exceptions/ChatSupportWindowClosedTest` (CHAT-REFUND-05), `-Dsupport.closed.order.ids=0554f250…,d3acfc93…` (last updated 7h58m and 2h53m earlier): 2/2 invocations, 0 skip. The delivered summary, receipt and Tax invoice render; there is no "Something wrong with this order?" button and no chat launcher. The red state for this assertion was seen on the pre-fix UI at 04:19 (the same 0554f250 showed the button). The test asserts each order really is past the window before checking.
- **Window open: PASS.** A fresh lifecycle created and delivered **bb43e2a4-5e05-4a7c-9e0d-7f3d5e7c9fb4** (DELIVERED 01:59:01Z, assignment RELEASED, ledger 115.46 balanced, rider OFFLINE). Then the delivered follow-up on that same order (`-Dresume.delivered.order.id`) **passed**: `RefundQuoteChecks` clicked the button, the quote form opened and a real quote came back (`quotePassed=true`, `moneyChecked=true`, `newOrderCreated=false`; CHAT_REFUND_QUOTE_RESPONSE committed, no ticket or refund).

## The fresh lifecycle's own failure

It failed at `HappyDeliveryFlowTest:732`: after clicking the new order in History, the delivered summary was not visible within the 5s assertion timeout. The teardown screenshot and DOM show it rendered (tracker for bb43e2a4 with "Order delivered", the support button and the chat launcher), and no browser console errors were logged. The UI change is a synchronous render condition, and the same History → summary path rendered for both aged orders on this UI. **Unexplained timing miss, not attributed and not changed.** With checkpoint25's accept-navigation wait, two consecutive fresh lifecycles each hit a different harness timing miss after successful server actions. If a third occurs, investigate the lifecycle's waits before running more orders.

Evidence: [26-ui-button-fix-verified.json](../evidence/26-ui-button-fix-verified.json).

## Open

- The server-side two-hour support window is still a pending user decision.
- Owned orders now: 0554f250 and d3acfc93 (support outcomes), bb43e2a4 (delivered, quoted, no tickets/refunds).
