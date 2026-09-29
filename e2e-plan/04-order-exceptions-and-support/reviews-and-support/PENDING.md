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
