# chat-calls-and-live-tracking

Status: local messaging implementation is ready; deployed verification is pending the UI release.

Scope: Order communication, calling UI and live location.

Messaging implementation:

- `ChatCommunicationTest` covers customer-to-restaurant text chat, customer-to-rider text chat,
  post-delivery chat, empty and whitespace validation, long-message display, session retry,
  history after reload, typing indicators, unread state, reconnection, and image attachments.
- `ChatWindowRoutedUiTest` exercises retry, initial WebSocket recovery, and multiline submission
  with in-browser transport fixtures, without creating a live order.
- `RefundRequestTest` covers the delivered-order refund-quote modal without approving or paying a
  refund.
- `CustomerOrderChatPage` owns the customer chat-window selectors; the UI exposes stable
  `data-testid` hooks for the launcher, unread count, typing indicator, and message rows.

Validation:

- The deployed core text-chat lifecycle passed on 2026-09-29.
- The expanded chat-window run exposed an image-upload response-contract defect in the deployed
  UI: the service returns `data.url`, while the deployed client reads `data.imageUrl`. The local
  fix and focused UI tests are ready; redeploy the UI, then rerun the chat-window E2E before
  marking this messaging scope complete.
- Production sign-off also needs backend decisions for synchronous text durability at refresh,
  loading the newest history page after more than 50 messages, concurrent session creation, and
  server-side enforcement of the delivered-order chat window.

Calls and map tracking remain separate scenarios in `scenarios.md`; they are outside this chat
messaging pass.
