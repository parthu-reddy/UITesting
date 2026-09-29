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

## Batch 2 — Customer-to-Rider chat

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-08 | Chat with rider after dispatch | After rider accepts dispatch, customer opens the order-scoped chat launcher and rider opens the active-job launcher. | Both participants join the same authenticated order session. |
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
| CHAT-REFUND-04 | Missing reason blocked | Leave the reason blank after selecting an item. | The request stays disabled and no quote event is sent. |

## Batch 6 — Map Search and Place Autocomplete (`MapTrackingPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| MAP-SEARCH-01 | Map container visible | Open map-based view. | `MapTrackingPage.isMapContainerVisible()` returns true. |
| MAP-SEARCH-02 | Search place | `searchPlace("Indiranagar")`. | Search results appear below the search input. |
| MAP-SEARCH-03 | Search results visible | After searching. | `hasSearchResults()` returns true. |
| MAP-SEARCH-04 | Select first result | `selectFirstResult()`. | Map centers on the selected place; marker or pin placed. |
| MAP-SEARCH-05 | Fill coordinates manually | `fillCoordinates("12.9716,77.5946")`. | Map centers on the specified coordinates. |
