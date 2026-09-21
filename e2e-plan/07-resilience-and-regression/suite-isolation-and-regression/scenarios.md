# 07 — Suite Isolation and Regression — All Scenarios

Ensures test suite hygiene: no state bleed between tests, no hardcoded waits causing flakiness, no data mutation between test cases.

## Batch 1 — Context isolation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ISOLATION-01 | Customer and restaurant in separate contexts | Run customer login (Context A) and restaurant login (Context B) simultaneously. | Context A has customer session; Context B has restaurant session; no cross-contamination. |
| ISOLATION-02 | Customer and rider in separate contexts | Same for customer (A) and rider (C). | Independent sessions; each sees only its own role UI. |
| ISOLATION-03 | All three roles in separate contexts | Customer (A), Restaurant (B), Rider (C) simultaneously. | All three dashboards independent; no shared localStorage or cookies. |
| ISOLATION-04 | Context close cleans up | After test, context.close() called. | Next test opens a fresh context with no residual auth state. |
| ISOLATION-05 | Incognito-equivalent fresh context | Open a new BrowserContext with no stored state. | Role selector shown; no dashboard visible without logging in. |

## Batch 2 — Test data isolation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ISOLATION-06 | No permanent data mutation | Cart tests do not place actual orders. | No orders in history from cart-only tests (CART-01 through CART-20). |
| ISOLATION-07 | Reviews test requires completed order | REVIEW-01 requires a completed order; test must verify it exists before asserting. | If no completed order exists, test FAILS with a clear message — not a silent skip. |
| ISOLATION-08 | Stock toggles reverted | STOCK-04 and STOCK-05 revert the toggle after asserting. | Item availability restored to original state after the test. |
| ISOLATION-09 | Campaign drafts discarded | CAMPAIGN-06/07 cancel the draft. | No "Unsaved E2E Draft" campaign persists in the system. |
| ISOLATION-10 | Refund tests require specific ticket | REFUND-02 finds a ticket by order ID. | If ticket not found, test FAILS explicitly — no assumption of ticket existence. |

## Batch 3 — No hardcoded waits (flakiness prevention)

| ID | Description | Action | Expected result |
|---|---|---|---|
| FLAKY-01 | No `Thread.sleep()` in test code | Run `grep -r "Thread.sleep" src/test/` | Zero occurrences. All waits use Playwright's `waitForSelector`, `waitForResponse`, or `Locator.waitFor()`. |
| FLAKY-02 | No fixed-time waits > 500 ms | Run `grep -r "waitForTimeout" src/test/` | Any `waitForTimeout` calls use ≤ 500 ms; document every occurrence. |
| FLAKY-03 | Timeouts tuned per scenario | Long-poll scenarios (e.g. waiting for incoming order) use explicit long timeouts (e.g. 90 s). | No implicit 30 s timeouts for real-time scenarios that may take longer. |

## Batch 4 — Regression smoke (post-deployment gate)

| ID | Description | Action | Expected result |
|---|---|---|---|
| REGR-01 | Login smoke (all 4 roles) | Run `LoginSmokeTest` after each deployment. | 8/8 tests pass. |
| REGR-02 | Menu loads (customer) | Run `MenuCartUiTest.menuDisplaysItemsWithoutRestaurantEditingControls`. | Menu items visible; no restaurant editing controls. |
| REGR-03 | Restaurant dashboard loads | Run `RestaurantUiTest`. | Restaurant dashboard renders; all tabs accessible. |
| REGR-04 | Rider dashboard loads | Run `RiderUiTest`. | Rider dashboard renders; earnings visible. |
| REGR-05 | Admin portal loads | Run `AdminUiTest`. | Admin portal renders; all admin tabs accessible. |
| REGR-06 | Session persists on reload | Run `SessionUiTest`. | All 4 sessions persist through page reload. |
