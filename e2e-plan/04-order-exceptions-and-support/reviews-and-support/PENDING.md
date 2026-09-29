# Validation failures and pending work

## My Reviews HTTP 403 — historical blocker, not reproduced

`CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState` is implemented as a strict read-only test. It logs in with a randomized seeded customer, opens Account Settings → My Reviews, and requires either at least one review article or the defined `You haven't reviewed anything yet` empty state.

Live validation on 2026-09-23 failed: `GET /api/v1/reviews/me?page=0&size=20` returned HTTP 403, and the tab rendered neither the review list nor its defined empty state. On 2026-09-28, the focused test passed on the deployed environment for the selected seeded customer. Treat the 403 as a historical observation; it is not a current confirmed failure.

Evidence: `target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.CustomerSettingsUiTest.xml` and `target/screenshots/myReviewsTabShowsReviewsOrDefinedEmptyState___customer.png` / `.html`.

The 2026-09-28 My Reviews test was read-only and submitted no review.

## UI fixes applied 2026-09-24 (not yet deployed or re-run)

**Backend cause identified for the wallet failure** (recorded here because the UI cannot fix it).

`GET /api/v1/money/customer/wallet` returns HTTP 400 with:

```
Failed to evaluate expression '@moneyAccessPolicy.canAccessMoney(authentication,
  T(...MoneyOwnerType).CUSTOMER, T(java.util.UUID).fromString(principal.name))'
```

`principal.name` is not a UUID, so `UUID.fromString` throws inside the `@PreAuthorize` SpEL and
the endpoint 400s before any handler runs. Observed live 2026-09-19 while attempting a Wallet
payment; the order was created and then auto-cancelled. UPI works.

Separately: `DeliveryPartnerUnavailableException` carries an error code, but both
`CustomerExceptionHandler` and `CustomerGlobalExceptionHandler` call `ApiResponse.error(message)`
and drop it, so `errorCode` is always null on the wire. Both files also register a handler for
the same exception type, so one is dead.

These changes are applied to `FoodDeliveryAppUI` source and pass typecheck, lint and the 339
unit tests. **They are not verified**: nothing here is proven until the UI is deployed and the
owning test is re-run live. Do not mark anything validated on the strength of this note.

## 2026-09-24 (evening) — the admin refund path, re-checked against the redesigned UI

**PRODUCT DEFECT — money.** The admin refund queue is built but not rendered.
`pages/admin/money/RefundQueue.tsx` (and `RefundTicketPanel.tsx`, which only it imports) has no
importer outside its own unit test; `git log -S RefundQueue -- src/pages/admin/AdminPortal.tsx`
is empty, so it was never wired in. The admin sidebar has no refunds item. Refund requests are
resolved from **Support Tickets** instead ("Review refund requests and support cases"), where
**"Resolve Ticket" approves in one tap with no confirmation and no amount shown** — only Reject
asks first (`AdminSupportTickets.handleResolveTicket`). The confirmation that names the amount
and the order (backlog E1, UI `a5094cd`) was added to `RefundTicketPanel`, which nobody can reach.

E2E consequence: REFUND-QUEUE-01..07 and REFUND-ADV-01..04 are `@Disabled` with this reason, and
`AdminPortalPage.openRefundsTab()` throws. They become valid again the day the queue is routed.

**2026-09-25 worktree update:** The above routing and confirmation defects are fixed in source.
AdminPortal now exposes Refund Queue at `/admin/refunds`; Support Tickets retains approval
with amount/order confirmation and uses the refund resolution endpoint for both outcomes.
The existing refund tests are re-enabled, `openRefundsTab()` navigates, and refund page-object
helpers match the current table, resolution controls, and confirmation dialog. No new E2E
tests were added. These changes have not been validated against a live deployment.

**Vacuous tests, not failures.** `ChatRefundMapTest` (CHAT-REFUND-01..04, MAP-SEARCH-01..05)
wraps every assertion in `if (chat.isChatOpen())` / `if (map.isMapContainerVisible())` and never
opens either widget first, so on the customer home it asserts nothing and passes. Its locators now
match the real widgets (the chat composer's "Type a message..." field; the place search named
"Search for a place") so the guards are at least truthful, but the scenarios still need a flow
that opens the chat from an order and the address map — new test code, out of this phase's scope.

## 2026-09-25 — deployed refund queue smoke validation

Ran the existing `AdminSupportUserReviewTest#refundQueueVisible+refundCount` against
`https://gulf-strike-dark-extras.trycloudflare.com/` using admin `1000000001`:
**2 passed, 0 failures/errors/skips**. Report: `target/surefire-reports/com.fooddelivery.e2e.tests.features.admin.AdminSupportUserReviewTest.txt`.
The initial navigation attempt exposed Java `Pattern.quote` escaping in the shared sidebar
helper; `openRefundsTab()` now uses an exact accessible button name. TestBase now honors
`-Dadmin.phone` and defaults to the retained admin account. Working-tree locator audit passes.

`AdminSupportRefundQueueTest#testRefundQueueActions` was not executed: automatic approval
review blocked its real rejection of the first open ticket without explicit approval or an
isolated disposable fixture. Refund approval, rejection, and payment completion remain
unvalidated live. No refund data was changed by the completed read-only tests.


## 2026-09-28 deployed review E2E results

Focused checks against the deployed development environment:

- `ReviewFlowTest.submitReview`: 1 passed, 0 failures/errors/skips. The test completed a seeded order through the customer → restaurant → rider flow, submitted ratings for the restaurant, delivery partner, and dish, then reopened the order from History and verified that ratings are visible but cannot be edited or submitted again.
- `CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState`: 1 passed, 0 failures/errors/skips.
- `RestaurantNavigationUiTest.restaurantReviewsShowPublicFeedbackAndAggregate`: 1 passed, 0 failures/errors/skips. This is read-only and confirmed the posted restaurant comment, average rating, and review count for Brand 1 Outlet 6.

The delivery and review submissions are persistent test data in the development environment. Each successful review submission is immutable by design. The first two review-flow attempts also persisted delivered test orders and review submissions before failing on a test-only exact-copy assertion and a test-only assumption that a delivered tracker returns after reload; those assumptions were corrected, and the final History-based immutability check passed.

## 2026-09-28 expanded participant review flow — restaurant UI path blocked

The expanded `ReviewFlowTest.submitReview` completed delivery, submitted the customer's three reviews, and verified the customer read-only state. It then errored before restaurant submission while waiting for the restaurant's `Order History` tab. Latest result: 1 test, 0 assertion failures, 1 error, 0 skipped. The rider submission was not reached.

Source inspection found the product cause: the restaurant settings gear and profile button both navigated to `/restaurant/settings`, where `RestaurantDashboard` renders `SharedSettingsView`. The `OrderHistory` and `RestaurantOrderDetailsModal` review action are in `RestaurantSettingsShell`, which was never mounted for that route. The UI source now gives the management console its own `/restaurant/management` route while keeping account settings at `/restaurant/settings`. `npm run typecheck` passes. This route fix is not deployed yet, so the restaurant review submission flow cannot be validated against the current deployment.

This run added one delivered development order (`fb775a6b`) and three immutable customer reviews before reaching the blocked restaurant navigation. Across the four review-flow attempts so far, development test data contains four delivered orders and twelve customer reviews. The earlier three-order customer review, My Reviews, and restaurant public aggregate checks passed.

Remaining review E2E coverage after deployment: restaurant review submission and read-only reopening, delivery-partner review submission and read-only reopening, and explicit rejection of cross-order, unrelated-target, self-review, and role-disallowed submissions. Post-delivery support remains outside these review checks.

## 2026-09-29 earlier run — earnings and delivery blockers not yet revalidated

This was the status before the later same-day full delivery and review checks recorded below. The earnings HTTP 500 and `AT_RESTAURANT` failure were not reproduced by that later delivery; the current rider-review blocker is the off-duty history UI guard described in the latest section.

The previous route blocker is resolved in the deployed UI. Focused live checks against `https://gulf-strike-dark-extras.trycloudflare.com/` show:

- Customer review submission and immutable reopening succeeded for delivered order `5d82315a-1084-4dbd-ba6c-da302b4fb7c1`; a later reuse run submitted the restaurant's and delivery partner's reviews and verified their read-only reopening.
- `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState`: **1 passed, 0 failures/errors/skips**.
- `RestaurantNavigationUiTest#restaurantReviewsShowPublicFeedbackAndAggregate`: **1 passed, 0 failures/errors/skips** for Brand 1 Outlet 9.
- The latest stricter, read-only `ReviewFlowTest#submitReview` run is **1 failure, 0 errors/skips**: the restaurant earnings request returned HTTP 500. It used the existing order, created no order or review data, and now fails instead of accepting the UI's ₹0.00 fallback.

Backend verification:

- ReviewsService: full Maven suite **94 passed, 0 failures/errors/skips**.
- CustomerApplication: review eligibility/participant authorization, restaurant earnings controller, and JPA earnings query checks **24 passed, 0 failures/errors/skips**.
- DeliveryExecutiveApplication: focused assignment authorization, OTP, and delivery-progress checks **19 passed, 0 failures/errors/skips**.
- FoodDeliveryAppUI: review modal and star-rating unit tests **16 passed**; `npm run typecheck` passed.
- UITesting E2E sources compile with the new strict earnings-response assertion and status-response diagnostics.

### Historical restaurant earnings HTTP 500 — later live request returned 2xx

Opening restaurant order details for order `5d82315a-1084-4dbd-ba6c-da302b4fb7c1` repeatedly returned HTTP 500 from `/api/v1/money/restaurant/{outletId}/orders/{orderId}`. The UI silently displayed ₹0.00 for the breakdown. Source inspection found that the controller used the normal `findById` lookup and then iterated lazy `orderItems`, while Open Session in View is off; unlike the other order queries used outside a transaction, this lookup did not fetch `orderItems`.

The CustomerApplication source now uses a dedicated `@EntityGraph(orderItems)` query, with a repository test that clears the persistence context before checking the returned collection. The E2E now fails if the endpoint does not return a 2xx response instead of allowing the zero-value fallback to look successful. The later 2026-09-29 full review flow received 2xx from the earnings request, so this earlier 500 is not currently reproduced.

### Historical delivery lifecycle HTTP 500 — not reproduced by the later full delivery

An attempt to create another full delivery failed on the rider `/status` request for `AT_RESTAURANT` with HTTP 500, leaving order `7166ba95-a3cc-4475-81f8-f745294fba35` at `READY_FOR_PICKUP`. A later same-day order completed delivery, so this status failure is not currently reproduced. This failure is separate from review submission.

The source path validates the active assignment, records `AT_RESTAURANT`, and writes an outbox event, but no server-side exception details were available from that run, so the exact cause remains unconfirmed. `CompletedDeliveryFixture.confirmStatus` now includes the HTTP response body in a failed assertion, and `OrderExecutionService` now logs unexpected status-transition exceptions with driver, order, and status context. These diagnostics did not reproduce an error in the later delivery.

### Scope and persistent test data

The successful customer review submission for order `5d82315a-1084-4dbd-ba6c-da302b4fb7c1` created immutable restaurant, delivery-partner, and dish reviews. The later participant run added immutable restaurant-authored and rider-authored reviews for that order. The customer history and public restaurant feed checks are read-only. Live negative POST cases remain intentionally unprobed because a validation defect could create irreversible invalid review rows; the backend authorization tests cover those denials.

At the time of this earlier run, status was **not production-ready** pending deployment and retest of the earnings fix. That blocker was cleared by the later live 2xx response recorded below. Post-delivery support scenarios above are not part of this review validation and remain unverified.

## 2026-09-29 — latest deployed review and history validation

Focused checks against the currently deployed development environment (`https://gulf-strike-dark-extras.trycloudflare.com/`) show:

- `CustomerSettingsUiTest#myReviewsTabShowsReviewsOrDefinedEmptyState`: **1 passed, 0 failures/errors/skips** for seeded customer `8000000005`.
- `CustomerSettingsUiTest#deliveredOrderReviewRequiresAtLeastOneRating`: **1 passed, 0 failures/errors/skips**. This uses an isolated intercepted delivered-order fixture, renders restaurant/driver/dish targets, confirms submission stays disabled before a rating, and blocks the review POST. It creates no review row.
- `RestaurantNavigationUiTest#restaurantReviewsShowPublicFeedbackAndAggregate`: **1 passed, 0 failures/errors/skips** for Brand 1 Outlet 9 with the comment `E2E automated review verification.` This is the populated list/aggregate case; the earlier empty-state case also passed.
- `RiderReviewHistoryApiTest#riderCanReadCompletedHistoryWhileOffline`: **1 passed, 0 failures/errors/skips** for delivered order `cf6a8d4d-bfe6-4a97-b402-8bd3f038d45f`. The seeded rider remained Offline; an authenticated GET returned HTTP 2xx and the response contained the order.
- `ReviewFlowTest#submitReview`: **1 test, 0 assertion failures, 1 error, 0 skipped** against the deployed UI. Delivery, customer review, restaurant order details/earnings, and the restaurant review step completed. It then failed waiting for the delivered order in the rider's Completed Deliveries list. The screenshot showed the rider Offline and an empty history list.

### Confirmed UI defect; backend history read is healthy

The deployed frontend's `useDeliveryOrders` history effect returned early when `isOnline` was false. This made the rider's read-only completed-delivery history unavailable off duty, even though the UI allowed opening that panel. The direct authenticated history E2E above confirms that the backend returned this exact order while the rider was Offline, so no backend history-query fix is indicated.

The UI source now fetches the rider's read-only history independently of duty state. `useDeliveryOrders.reping.test.ts` adds an offline-history regression assertion; the focused Vitest file passed **3 tests**, and `npm run typecheck` passed. `ReviewFlowTest` now records the history endpoint response and includes a guarded opt-in (`-Dreview.submit.rider.existing=true`) to finish only the rider's still-pending reviews on an explicitly supplied order. `-Dreview.require-rider-offline=true` makes the follow-up prove off-duty access without toggling rider duty. Java E2E `test-compile` passed.

**Pending deployment:** the source change is not in the deployed UI yet. After deploying it, reuse the known delivered order rather than creating another order:

```text
-Dtest=ReviewFlowTest#submitReview
-Dreview.order.id=cf6a8d4d-bfe6-4a97-b402-8bd3f038d45f
-Dreview.outlet.name=Brand 1 Outlet 9
-Dreview.submit.rider.existing=true
-Dreview.require-rider-offline=true
-Drestaurant.phone=9000000001
-Drider.phone=7000000001
```

That flow is expected to create at most the rider's two immutable reviews (Customer and Restaurant), then reopen them read-only. The current order already has the customer reviews and restaurant-authored participant reviews from the earlier run. Do not rerun the full create-order flow for this check.

### Backend status and separate stream observation

No backend change is proposed for rider history: the direct API check passed. The restaurant order earnings request for the new order also returned 2xx during the latest full flow, so the earlier 500 is no longer reproduced on this deployment. The existing local backend suite counts above were run earlier; no backend files were changed in this validation.

The browser logged one `403` for `/api/delivery/drivers/{driverId}/orders/{orderId}/restaurant-status-stream` during the same delivery, but pickup and delivery still completed. The endpoint intentionally denies streams when `OrderAssignment.authorises(driverId)` is false, including after release. The exact timing/state behind this single 403 is not established. If investigating that independent issue, first add a response-level check during an active assignment and correlate it with the assignment row/state; only propose a backend change if that proves a live-assignment request is rejected. Preserve the existing cross-rider authorization check.

Live negative review POST cases (self-review, unrelated target, wrong role, nonparticipant, duplicate) remain unprobed to avoid creating invalid or irreversible data. ReviewsService and CustomerApplication tests cover these denial cases. Post-delivery support scenarios remain outside this review validation and unverified.
