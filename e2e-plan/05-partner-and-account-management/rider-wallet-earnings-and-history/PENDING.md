# Validation and pending work

## Rider history filter

Five focused read-only rider tests pass. Completed Deliveries opens, date selection is reflected, Clear resets it, and the 2000-01-01 filter displays the defined empty-state title and guidance. This checks filter UI behavior, not historical data completeness.

`RiderUiTest` also passed History navigation and reload persistence with randomized approved riders on 2026-09-23.

Trip-row detail and pagination checks remain data-dependent. The randomized seeded rider used by the current read-only runs exposes the defined empty history state after filtering, but no guaranteed completed-trip row or pagination control. Tests will require those controls to exist and fail clearly when that prerequisite is absent; they will not silently skip or fabricate history through backend access.

`riderCompletedTripShowsRestaurantPayoutAndDate` is now implemented as that strict check. Its first live run failed because the randomized seeded rider exposed no `ORDER #...` history row. When a completed row exists, the test requires a restaurant name, readable date, nonnegative INR payout text and Delivered status. Pagination remains unavailable until a rider has enough completed rows for more than one page.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

## Rider verification/wallet rendering

PartnerReadOnlyUiTest.riderVerificationAndWalletSectionsRender passed: Document Verification, explicit Documents/Bank Approved-or-Pending statuses, exact disabled rider phone, Earnings Wallet, an INR-formatted nonnegative wallet balance, and Sign Out are visible. `riderTodayEarningsIsNonNegativeCurrency` also passed for the dashboard value. These checks validate UI formatting and nonnegative values; they do not establish ledger accuracy.

The focused run logged unrelated driver-review HTTP 403 responses. They did not block the rider dashboard, history, settings, verification, or wallet assertions.


## 2026-09-28 focused five-class rerun

`PartnerReadOnlyUiTest` ran 11 cases: 10 passed and 1 skipped. `riderCompletedTripShowsRestaurantPayoutAndDate` had no completed trip for its randomized rider, so it skipped before checking payout, restaurant, or date. Seeded riders guarantee approved accounts, not completed trips or ledger entries. Make this deterministic by using a completed order produced by the normal UI lifecycle for the same rider, as in `HappyDeliveryFlowTest`, or an isolated test database fixture that includes the matching completed order and ledger data. Do not fabricate payout rows or alter rider duty state to create a history entry. This case remains data-blocked.
