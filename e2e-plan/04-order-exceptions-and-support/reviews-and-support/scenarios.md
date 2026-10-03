# 04 — Reviews and Support — All Scenarios

Prerequisite: at least one completed order in the customer's history. Uses (2026-10-03): the real dialogs by accessible name (`rate-order-prompt`, "Rate {target} out of 5 stars"); `RateOrderModalPage`'s old text/SVG selectors are not used. `PostDeliverySupportModalPage` was deleted with its component (checkpoint23).

## Batch 1 — Participants review order-related targets

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-01 | Customer sees eligible targets | Customer opens a delivered order's review action. | The shared review dialog lists only that order's restaurant, assigned delivery partner, and purchased dishes. |
| REVIEW-02 | Customer submits ratings | Rate one or more targets and optionally comment on each rated target. | Only rated targets are submitted; success is shown. |
| REVIEW-03 | Customer review is immutable | Reopen the same order's review action. | Saved ratings/comments are visible without rating controls, text fields, or submit action. |
| REVIEW-04 | Restaurant reviews participants | Restaurant opens a delivered order and its review action. | Only Customer and Delivery partner targets are available; submit and reopen as read-only. |
| REVIEW-05 | Delivery partner reviews participants | Assigned rider opens a delivered order from Completed Deliveries while Off duty, then opens its review action. | Rider history loads while Offline; only Customer and Restaurant targets are available; submit once and reopen as read-only. Live rider UI validation is pending deployment of the off-duty history fix. |
| REVIEW-06 | Empty submission blocked | Open an eligible review dialog without selecting stars. | Submit stays disabled; a review cannot be saved without a rating. The browser E2E uses an isolated intercepted order fixture and blocks the write request. |
| REVIEW-07 | Ineligible target rejected | Attempt self-review, review of an unrelated dish, a target outside the actor's role matrix, or an order the actor did not participate in. | Backend rejects the request; no review is created. Covered by service/controller tests; not posted to the live immutable review endpoint. |

## Batch 2 — Restaurant sees review

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-09 | Restaurant reviews tab | Login as restaurant → open Reviews tab. | Review list and aggregate requests succeed; public feedback and aggregate render, or the defined empty state renders when the outlet has no reviews. Populated Outlet 9 and empty Outlet 9 states were verified on 2026-09-29 in separate focused runs. |
| REVIEW-10 | Review shows customer name or "Anonymous" | On the review entry. | Customer identifier is non-empty ("Customer" or name); not `null`. |
| REVIEW-11 | Review timestamp | Each review entry shows a date/time. | Date is non-empty and in a readable format. |
| REVIEW-12 | Multiple reviews visible | If more than one review exists. | List shows all reviews; pagination works if many reviews exist. |

## Batches 3 and 4 — retired 2026-10-03

SUPPORT-01..11 described a post-delivery "Help" button, `PostDeliverySupportModal` and its category form. That UI and its endpoint were deleted by user decision at checkpoint23 (dead code: rendered nowhere). Customer support now runs through the order chat (item quote → Submit Refund Request) and is decided in the admin Refund Queue; it is covered live by `06-admin-operations/support-and-refund-queues` Batches 5 and 6 (SUPPORT-REFUND-01..03, REFUND-RETRY-01) and CHAT-REFUND-01..05. Do not reimplement these IDs.

## Batch 5 — Customer review history

| ID | Description | Action | Expected result |
|---|---|---|---|
| REVIEW-13 | My Reviews resolves | Customer opens Account Settings → My Reviews. | At least one review article or the defined empty state renders. Focused deployed check passed on 2026-09-29; older HTTP 403 is historical. |

## Live status (2026-10-03, order bb43e2a4)

| ID | Proof |
|---|---|
| REVIEW-01, 02, 03, 06 (customer) | `OrderReviewsFlowTest` PASS: targets = eligibility (RESTAURANT, DRIVER, PRODUCT); "Pick a rating to continue" disabled until rated; restaurant 4 + comment, rider 5 submitted; reopen shows them under "Already reviewed", the dish still rateable |
| REVIEW-04 (restaurant) | same test: targets CUSTOMER, DRIVER only; customer 5; read-only on reopen |
| REVIEW-05 (rider, off duty) | same test from Completed Deliveries with duty OFFLINE: targets CUSTOMER, RESTAURANT only; restaurant 4; plus `RiderReviewHistoryApiTest` PASS |
| REVIEW-07 | backend tests (not posted live by design) |
| REVIEW-09/10/11 | `RestaurantNavigationUiTest` REVIEW-AGG-01 PASS with `-Dreview.outlet.name="Brand 1 Outlet 3" -Dreview.customer.comment=...`: the exact public review appears |
| REVIEW-12 | not exercised (single review on the outlet) |
| REVIEW-13 | `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState` PASS with `-Dreview.customer.comment` (now required to find that review; seen red with an unwritten comment) |

DB: four reviews (customer→restaurant PUBLIC 4 with comment; customer→driver PRIVATE 5; restaurant→customer PRIVATE 5; rider→restaurant PRIVATE 4). Aggregates: RESTAURANT 1 review 4.00 (public only), DRIVER 1 review 5.00 (driver reviews count by design); one REVIEW_CREATED event PROCESSED; Brand 1 Outlet 3 rating 4 from 1 review in restaurant_db.
