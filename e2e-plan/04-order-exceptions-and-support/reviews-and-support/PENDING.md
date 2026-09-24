# Validation failures and pending work

## My Reviews returns HTTP 403

`CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState` is implemented as a strict read-only test. It logs in with a randomized seeded customer, opens Account Settings → My Reviews, and requires either at least one review article or the defined `You haven't reviewed anything yet` empty state.

Live validation on 2026-09-23 fails. `GET /api/v1/reviews/me?page=0&size=20` returns HTTP 403, and the tab renders neither the review list nor its defined empty state. The test remains active and failing. No review was submitted, edited, or deleted.

Evidence: `target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.CustomerSettingsUiTest.xml` and `target/screenshots/myReviewsTabShowsReviewsOrDefinedEmptyState___customer.png` / `.html`.

Review submission and post-delivery support still require a suitable completed order and remain pending.

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
