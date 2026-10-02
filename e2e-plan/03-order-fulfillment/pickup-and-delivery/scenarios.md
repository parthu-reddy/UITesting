# 03 — Pickup and Delivery — All Scenarios

Prerequisite: Rider has accepted a dispatch ping and is in the active job phase. Uses `DeliveryActiveJobPage`, `DeliveryOrderDetailsModalPage`, `MapTrackingPage`.

## Batch 1 — Navigate to restaurant (pickup phase)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-01 | Active job shows pickup phase | After accepting dispatch. | Implemented and live-validated: exact Active Contract order ID, selected outlet, customer address and pickup controls render. |
| PICKUP-02 | Verify assigned delivery identity | Match the full assigned order ID, pickup location and delivery address. | Required package-delivery information; item names, quantities and prices are outside the rider scope per user clarification on 2026-10-02. |
| PICKUP-03 | Restaurant directions | Click actual accessible Open directions to Restaurant popup. | Passed actual new Google Maps API target with valid destination coordinates; external content is routed boundary-only. |
| PICKUP-04 | "Arrived at Restaurant" button | While in pickup phase, `DeliveryActiveJobPage` shows "Arrived at Restaurant" button. | Implemented and live-validated before clicking. |
| PICKUP-05 | Mark arrived at restaurant | Tap "Arrived at Restaurant". | Implemented and live-validated through the following pickup OTP interaction. |

## Batch 2 — Pickup OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterPickupOtp()` and `DeliveryActiveJobPage.swipeToConfirmPickup()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| PICKUP-06 | Verify package handover | Verify the assigned order and restaurant pickup OTP before confirming pickup. | The rider delivers the assigned package; inspecting its itemized contents is not required. |
| PICKUP-07 | Pickup OTP input appears | After arriving at restaurant. | Implemented and live-validated, including restoration after accepted-job reload. |
| PICKUP-08 | Enter valid pickup OTP | Enter the 6-digit pickup OTP provided by the restaurant via `enterPickupOtp()`. | Implemented and live-validated with the restaurant-rendered six-digit OTP. |
| PICKUP-09 | Swipe to confirm pickup | After entering valid pickup OTP, swipe the slider via `swipeToConfirmPickup()`. | Implemented and live-validated: Step 2, Package Picked Up, customer address and delivery OTP input render. |
| PICKUP-10 | Wrong pickup code and recovery | Enter guaranteed-distinct code, confirm, reload, then use correct code on same order. | Passed owned HTTP400, explicit Invalid pickup OTP, preserved pickup and successful completion. |
| PICKUP-11 | Customer address shown after pickup | After confirming pickup via swipe. | Implemented and live-validated against the exact address captured from the dispatch card. |

## Batch 3 — Delivery OTP and SwipeAction confirmation

Uses `DeliveryActiveJobPage.enterDeliveryOtp()` and `DeliveryActiveJobPage.swipeToConfirmDelivery()`.

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-01 | Drop-off phase indicators | After pickup confirmed. | Implemented and live-validated with Step 2 and Package Picked Up indicators. |
| DELIVERY-02 | Delivery OTP input appears | When at customer's location. | Implemented and live-validated before and after delivery-phase reload. |
| DELIVERY-03 | Customer provides delivery OTP | On customer tracker, `getDeliveryOtp()` returns a 6-digit code. | Implemented and live-validated from the customer context. |
| DELIVERY-04 | Enter valid delivery OTP | Rider enters the customer's OTP via `enterDeliveryOtp()`. | Implemented and live-validated. |
| DELIVERY-05 | Confirm exact delivery | Submit owned correct code using pointer swipe. | Passed exact DELIVERED command/retained receipt/history, positive payout and released assignment. Canonical earnings comparison passed in features08/09. |
| DELIVERY-06 | Wrong delivery code and recovery | Reject short code locally and guaranteed-distinct code on server, reload then complete same order. | Passed no POST for short code, owned HTTP400/explicit Invalid delivery OTP, restored delivery phase and successful completion. |

## Batch 4 — Rider navigation and communication during active job

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-07 | Navigation during active job | Open actual map restaurant directions popup. | Passed actual target proof; no Google Maps provider success claimed. |
| DELIVERY-08 | Call exact customer | Use named customer call button in destination block. | Recipient-specific UI control exists; live calling/signalling assigned chat/calls feature, not counted passed here. |

## Batch 5 — Post-delivery

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-09 | Rider returns to available state | After delivery complete. | Implemented and live-validated: Active Contract disappears and Online Duty renders. |
| DELIVERY-10 | Completed trip in history | Open History tab after delivering. | Implemented and live-validated for the exact order, selected restaurant, Delivered status and positive net payout. |
| DELIVERY-11 | Earnings update | Compare dashboard earnings before/after with exact trip payout. | Reused canonical features08/09 real positive payout/earnings comparison; broader earnings audit remains its owning feature. |

## Batch 6 — Edge cases

| ID | Description | Action | Expected result |
|---|---|---|---|
| DELIVERY-12 | Reload during active delivery | While in drop-off phase, reload the page. | Implemented and live-validated: exact order ID, Step 2 and delivery OTP input are restored rather than reset to pickup. |
| DELIVERY-13 | Customer unavailable | Timing-gated Customer Unavailable (Mark Failed) control. | Elapsed eligibility and exception/financial behavior assigned exceptions; intentional duration run deferred. No conditional no-op pass. |


## Audit corrections and additional fast boundary —2026-10-02

Seven existing methods were repaired and passed; historical implemented claims above must be read with [current pending notes](PENDING.md). PICKUP02/06 were corrected to package assignment and handover verification; itemized contents are outside the rider scope. Real customer-call signalling belongs to chat/calls, not a generic button-click pass. Customer Unavailable is timing-gated, so DELIVERY13 is explicitly deferred/assigned exceptions, not conditional if-exists coverage.

|ID|Description|Action|Expected result|
|---|---|---|---|
|DELIVERY-14|Finish delivery with offline-after selection|Check Go offline after delivery and submit valid owned customer code|Exact DELIVERED command has goOfflineAfter=true; active job closes; authoritative rider is OFFLINE and delivered history remains accessible. Existing completeDeliveryWithOtp passed56.967s; delivered/released assignment and OFFLINE confirmed read-only.|
