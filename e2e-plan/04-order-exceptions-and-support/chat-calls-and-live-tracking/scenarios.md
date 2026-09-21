# 04 — Chat, Calls, and Live Tracking — All Scenarios

Uses: `ChatWidgetPage`, `CustomerOrderChatPage`, `RestaurantChatPage`, `CallOverlayPage`, `MapTrackingPage`. Requires an active order in progress.

## Batch 1 — Customer-to-Restaurant chat

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-01 | Chat widget appears during active order | Customer opens active order tracker. | Chat icon / "Message Restaurant" button visible on `CustomerOrderChatPage`. |
| CHAT-02 | Customer sends message to restaurant | Customer types "Is the food spicy?" → tap Send. | Message appears in customer chat with sent status. |
| CHAT-03 | Restaurant receives message | On restaurant context, open chat for the active order. | Message "Is the food spicy?" appears on `RestaurantChatPage`. |
| CHAT-04 | Restaurant replies | Restaurant types "Yes, medium spicy" → tap Send. | Reply appears on restaurant side with sent status. |
| CHAT-05 | Customer sees reply | On customer context. | "Yes, medium spicy" appears in customer chat without page reload. |
| CHAT-06 | Empty message not sent | Customer taps Send with empty message box. | No message sent; send button disabled or validation message shown. |
| CHAT-07 | Long message handling | Customer sends a message > 200 characters. | Message is either truncated by the input (character limit) or sent and displayed wrapped. Document actual limit. |

## Batch 2 — Customer-to-Rider chat

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-08 | Chat with rider after dispatch | After rider accepts dispatch, customer opens tracker. | "Message Rider" button visible. |
| CHAT-09 | Customer sends message to rider | Type "I'm at the blue gate" → send. | Message appears in customer chat. |
| CHAT-10 | Rider receives message | On rider context, open chat for active job. | Message visible on rider's chat UI. |
| CHAT-11 | Rider replies to customer | Rider sends "Got it, I'll ring the doorbell". | Customer chat shows rider's reply in real-time. |

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

## Batch 5 — Chat-based Refund Request (`ChatWidgetPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CHAT-REFUND-01 | Open refund request from chat | During active chat, `ChatWidgetPage.openRefundRequest()`. | Refund request form appears within the chat widget. |
| CHAT-REFUND-02 | Fill refund reason | `fillRefundReason("Item missing")`. | Reason text accepted. |
| CHAT-REFUND-03 | Submit refund request | `submitRefundRequest()`. | Refund request submitted; success message shown; refund enters admin refund queue. |
| CHAT-REFUND-04 | Refund request without reason blocked | Leave reason blank → submit. | Validation error; refund request not submitted. |

## Batch 6 — Map Search and Place Autocomplete (`MapTrackingPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| MAP-SEARCH-01 | Map container visible | Open map-based view. | `MapTrackingPage.isMapContainerVisible()` returns true. |
| MAP-SEARCH-02 | Search place | `searchPlace("Indiranagar")`. | Search results appear below the search input. |
| MAP-SEARCH-03 | Search results visible | After searching. | `hasSearchResults()` returns true. |
| MAP-SEARCH-04 | Select first result | `selectFirstResult()`. | Map centers on the selected place; marker or pin placed. |
| MAP-SEARCH-05 | Fill coordinates manually | `fillCoordinates("12.9716,77.5946")`. | Map centers on the specified coordinates. |

