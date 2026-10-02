# Feature11: Restaurant acceptance and preparation

Started2026-10-02 after retained pickup/delivery completion. Source review before execution; preserve all data and failed active orders. No publishing/deployment authorization is active.

## Review checklist

- [x] Inspect owning plan, two existing feature classes, shared page objects and duplicate rejection flow.
- [x] Inspect restaurant queue/card/actions/drawers, navigation and backend fulfillment states/dispatch scheduling.
- [ ] Repair queue-only setup so it creates no order and does not log in a rider.
- [ ] Require full-ID and owned command success; remove conditional acceptance/cooking passes and fixed sleeps.
- [ ] Verify incoming card/items, exact details/dismissal, acceptance/customer polling, reload uniqueness, preparation/ready/code, rider dispatch and successful retained completion.
- [ ] Verify blank/whitespace rejection reason cannot submit, Back dismissal, exact valid rejection and customer terminal reason.
- [ ] Verify current six Kanban regions/counts and empty states without a new order.
- [ ] Verify outlet switching, settings and five section transitions/rapid switching; no stale panels.
- [ ] Run focused backend fulfillment/tenant authorization and dispatch boundary checks where applicable.
- [ ] Record precise results, retained final states and remaining assigned/deferred coverage.

## Source findings

Legacy RestaurantFulfillmentTest setup uses StateSetupHelper and creates an order for queue-only checks. Its acceptance/start-cooking branches are conditional, its queue regex uses browser-incompatible Pattern.quote, and page objects contain fixed sleeps and old button/selector assumptions. Duplicate RestaurantRejectFlowTest skips canonical quote/payment setup. Correct these tests before execution; the 2026-09-27 report’s three fulfillment errors occurred in obsolete rider setup, not proof of restaurant behavior.

Current UI source has disabled blank-reason confirmation and direct tab mounting; historical navigation and reason-gap notes must be revalidated. The queue has six Kanban columns and no Completed tab. Completed orders belong to settings Order History. Prep displays ready-by time; marking ready is allowed immediately. The15minute threshold schedules dispatch from acceptance, not a prohibition on ready. Real scheduled-duration checks are deferred; source/local boundary proof is separate from deployed threshold timing.

Restaurant review aggregation belongs to the later review feature; no populated-feedback proof will be inferred from a natural empty state. Public SSE is explicitly user-deferred; ordinary authenticated polling remains eligible for browser proof.
