# Checkpoint 141 — P0-2 owner-approved batch green; time-independent ordering built (deploy pending)

2026-10-09T22:45+05:30. Nothing running.

## P0-2 owner-approved batch (owner: "go ahead with the first three as one batch")

| Test | Runner | Result | Dev records created |
|---|---|---|---|
| AdminPartnerApprovalsUiTest#reviewQueuesNavigation | run_e2e_batch.py (canaries 4/4) | 1/1 | none (read-only) |
| OneLoginEntitlementsTest (4 methods) | scripts/run_business_platform_o45_e2e.py --only login | 4/4 | 1 fresh customer (8999xxxxxx, allocation-cb52e1296ff4ee36) |
| EntitlementRevocationTest#acceptedStaffApprovalAndRemovalRefreshOnNormalUiRequests | O4/O5 runner --only revocation, admin 1000000002 | 1/1 | 1 applicant + approved brand + 1 invited/removed staff (allocation-36279ac634250959) |

Evidence: `evidence/p02-close-2026-10-09/`. Admin step-up recorded in ADMIN-STEP-UPS.json. Inventory rebuilt at cutoff
2026-10-09T10:06:46Z: PASS_CURRENT 37, PASS_OLDER 289, LAST_FAILED 2 (RiderAvailabilityUiTest — owner-gated, cycles rider
duty; SSEReconnectTest — parked), NEVER_RUN 14 (helper unit tests + 2 measurement classes, which never run live).

## Time-independent ordering (owner: no test failures that depend on the time of day; production ready)

Plan + gate: `RandomDocuments/TimeIndependentOrdering_2026-10-09/` (README has the deploy order). Built locally, all
gates green, nothing deployed:
- one opening-hours rule (half-open; `00:00–00:00` = all day) in `OpeningHours`; SQL copy removed; listings and the
  menu judge outlet hours on the injected clock; Customer no longer caches clock-derived reads;
- UI editors write "Open 24 hours" as `00:00–00:00`;
- seed: Brand 1 and Brand 2 orderable at every minute at every outlet (additive reload, no wipe);
- `CustomerMenuViewPage.addQuickPrepItemToCart` no longer wanders between outlets; the application page object ticks
  "Shift 1: open 24 hours".

## Waiting on the owner

Deploy CommonLibrary (publish), restaurant-service, customer-service, food-delivery-app-ui; commit Deployment and run
`run_remote_dummy_data.sh` (all mode); commit UITesting. Then the agent runs `--phase 4 --live` and, with approval,
the order/catalog features.
