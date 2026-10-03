# 04 — Chat, Calls, and Live Tracking — All Scenarios

Uses: `ChatWidgetPage`, `CustomerOrderChatPage`, `RestaurantChatPage`, `CallOverlayPage`, `MapTrackingPage`. Requires an active order in progress.

## Batch 1 — Customer-to-Restaurant chat

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-01 | Chat widget appears during an active order | Customer opens the order-scoped chat launcher after restaurant acceptance. | The launcher opens the composer and an authenticated STOMP session. |
| CHAT-02 | Customer sends message to restaurant | Customer sends a unique message longer than 200 characters. | Full message appears in the customer chat. |
| CHAT-03 | Restaurant receives message | Restaurant opens chat for the active order. | Full customer message appears without a page reload. |
| CHAT-04 | Restaurant replies | Restaurant sends a unique reply. | Reply appears in the restaurant chat. |
| CHAT-05 | Customer sees reply | Keep the customer chat open while the restaurant replies. | Customer sees the reply in real time without a page reload. |
| CHAT-06 | Empty message not sent | Customer leaves the composer empty, then tries whitespace-only input. | Send remains disabled for both values. |
| CHAT-07 | Long message handling | Customer sends a message over 200 characters. | The UI has no 200-character cap; the full message is sent and displayed wrapped. The server controller currently rejects content over 10,000 characters. |
| CHAT-12 | Chat during delivered-order grace period | Customer reopens chat after delivery and sends a message within the grace period. | The existing order session remains available and the message is sent. |

## Batch 1A — Chat-window reliability and attachments

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-13 | Session initialization retry | Return one temporary session-creation failure, then select “Try again”. | The error is actionable and the second request restores the composer. |
| CHAT-14 | Stored history after reload | Send a unique text message, reload the customer application, and reopen that order’s chat. | The message is loaded from authorized server history. |
| CHAT-15 | Multiline composer | Enter a line break with Shift+Enter, then submit with Enter. | Shift+Enter keeps the newline; Enter sends one text message containing it. |
| CHAT-16 | Typing indicator | Customer begins composing while restaurant chat is open, then sends the text. | The restaurant sees the indicator and it clears after the message arrives. |
| CHAT-17 | Unread count | Close the customer chat and send one restaurant message. | The launcher shows one unread message; reopening clears it and displays the message. |
| CHAT-18 | Reconnect after a live socket closes | Close an already-connected customer chat WebSocket, wait for the next connection, then send a message. | Reconnect state disables the composer; the restored subscription delivers the new message to the restaurant. |
| CHAT-19 | Message size boundary | Send a 10,000-character message, then enter 10,001 characters. | The exact server limit is accepted; the over-limit value disables Send and explains the limit. |
| CHAT-20 | Image attachment contract | Upload a valid fixture image from the gallery control. | Upload succeeds, both participants render the attachment, and no upload-failed toast appears. |
| CHAT-21 | Image upload limits and failure | Reach the four-image per-user cap, then simulate a response without an image URL. | Both upload controls disable at the cap; an invalid response shows the upload error. |
| CHAT-22 | History longer than one page (2026-10-03) | `ChatHistoryPagingTest`, `-Dchat.history.order.id` (an owned order inside its chat window). Tops the chat up to 60 messages as its customer, opens it from History. | The newest page shows and the oldest text is absent; “Load earlier messages” brings the oldest in and disappears at the last page. Red on UI 41578ee (no control); expected green after the checkpoint27 UI deploy. |

## Batch 2 — Customer-to-Rider chat

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-08 | Chat with rider after dispatch | After rider accepts dispatch, wait until the customer's live order tracker shows the assigned rider; then customer and rider open their order-scoped chat launchers. | Both participants join the same authenticated order session after the order participant list includes the rider. |
| CHAT-09 | Customer sends message to rider | Customer sends a unique rider-directed message. | Message appears in customer chat. |
| CHAT-10 | Rider receives message | Keep the rider chat open while the customer sends. | Message appears in rider chat without a page reload. |
| CHAT-11 | Rider replies to customer | Rider sends a unique reply. | Customer sees the reply in real time without a page reload. |

## Batch 3 — Call (masked)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CALL-01 | Call rider button visible | Customer on active order tracker. | "Call Rider" button visible after rider is assigned. |
| CALL-02 | Call overlay appears | Customer taps "Call Rider". | `CallOverlayPage` opens showing masked number or "Calling..." state. |
| CALL-03 | Call overlay dismissal | Tap "Cancel" / "Hang Up" on call overlay. | Overlay closes; returns to tracker screen. |
| CALL-04 | Rider call customer button | On rider active job. | "Call Customer" button visible. |
| CALL-05 | Rider call overlay | Rider taps "Call Customer". | Call overlay on rider side opens with masked number. |

## Batch 4 — Live map tracking

| ID | Description | Action | Expected result |
|---|---|---|---|
| LIVE-01 | Map visible while rider in transit | Customer's active order tracker after rider picks up. | Map with rider marker visible on `MapTrackingPage`. |
| LIVE-02 | Rider marker coordinates are valid | Inspect rider marker on map. | Latitude/longitude non-zero; marker is in a plausible geographic location (not at 0,0). |
| LIVE-03 | Map updates as rider moves | Rider location updates (simulated or real). | Map marker shifts position without page reload. |
| LIVE-04 | Rider offline during delivery | Rider toggles Offline while in active delivery. | Customer map shows "Rider temporarily offline" or last known position; no crash. |
| LIVE-05 | Map hidden before rider dispatch | Customer tracker before rider assignment. | No map / no rider marker shown (only status text). |

## Batch 5 — Chat-based Refund Quote Request (`ChatWidgetPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-REFUND-01 | Open refund quote form | Complete a test-created order and use “Something wrong with this order?” on its delivered summary. | The refund quote modal opens for that exact order and loads its items. |
| CHAT-REFUND-02 | Item, reason, and chat connection are required | Select an item and leave the reason blank, then provide a reason while the chat WebSocket is disconnected. | “Request Quote” stays disabled until the form is valid and chat reconnects; it then enables. |
| CHAT-REFUND-03 | Submit a refund quote request | Submit a reasoned quote request for the test-created order. | The accepted `REFUND_QUOTE_REQUEST` appears in chat as “Requesting quote...”; no refund decision or payout is made. |
| CHAT-REFUND-04 | Missing reason blocked on connected chat | Leave the reason blank after selecting an item while chat is connected. | The request stays disabled and no quote message appears. |
| CHAT-REFUND-05 | Support entry closes with the chat window (2026-10-03) | Open, from History, an owned delivered order last updated two or more hours ago (`ChatSupportWindowClosedTest`, `-Dsupport.closed.order.ids`). Uses orders that aged naturally; no waiting. | The delivered summary and receipt render; there is no “Something wrong with this order?” button and no chat launcher. Inside the window the button opens the quote form (CHAT-REFUND-01). |

## Batch 6 — Map Search and Place Autocomplete (`MapTrackingPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| MAP-SEARCH-01 | Map container visible | Open map-based view. | `MapTrackingPage.isMapContainerVisible()` returns true. |
| MAP-SEARCH-02 | Search place | `searchPlace("Indiranagar")`. | Search results appear below the search input. |
| MAP-SEARCH-03 | Search results visible | After searching. | `hasSearchResults()` returns true. |
| MAP-SEARCH-04 | Select first result | `selectFirstResult()`. | Map centers on the selected place; marker or pin placed. |
| MAP-SEARCH-05 | Fill coordinates manually | `fillCoordinates("12.9716,77.5946")`. | Map centers on the specified coordinates. |

## Batch 7 — Isolation (2026-10-03)

`ChatAndRefundIsolationTest`: the order's customer (`-Dcustomer.phone`) reads the chat first as the control; intruders are `-Disolation.customer.phone`, `-Drestaurant.phone` (another brand's owner) and `-Drider.phone` (never assigned), on `-Disolation.order.id`.

| ID | Actor | Action | Expected result |
|---|---|---|---|
| CHAT-ISO-01 | Unrelated customer | Read history, look up and join the session, read refunds and the order; STOMP subscribe and send. | 403 for history, lookup, join and refunds; 404 for the order; STOMP ERROR "Access Denied" for subscribe and send; owner history unchanged. |
| CHAT-ISO-02 | Another brand's owner | Read history. | 403. |
| CHAT-ISO-03 | Rider never assigned | Read history. | 403. |

Run 2026-10-03 on d3acfc93: all pass except the order read (500, defect; fixed locally to 404, customer-service deploy pending).
