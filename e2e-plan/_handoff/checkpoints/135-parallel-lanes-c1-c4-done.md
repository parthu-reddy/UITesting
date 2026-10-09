# Checkpoint 135: C1–C4 executed on two parallel lanes (2026-10-08 15:30–17:00 IST)

Owner decisions (DECISIONS.md, 15:40 + 15:58): two live lanes, deploy-scoped re-checks instead of the 80 remaining
admin re-runs (a full sweep still precedes P0-2 close), overlap reduction, no deploys, Mac kept awake.

## Results (surefire XML under P02_E2EInventory/Phase4_OwnerApprovedRuns/evidence/, lane B under evidence/laneB/)
| Batch | Result | Notes |
|---|---|---|
| C1 fresh people | 6/6 | 3 registrations + 3 session cases (manifests: evidence/laneB/manifests/registration) |
| C2 partner applications | 9/10 | suspension (`ownBrandSuspension…`, never run before) times out in CustomerHomePage.searchRestaurant; revocation failed once on `net::ERR_NETWORK_IO_SUSPENDED`, passed on retry |
| C3 wallets + ads | 9/9 | fresh owners in evidence/C3/fresh-phones-manifest.json |
| C4 orders | all steps pass except DelayApproval#customerRejectsRestaurantDelay (setup: selectNearestOutlet after the approve order was active) and CustomerOrderPlacement tip + quote-reuse (postData null; fixed, rerun needs owner OK: +2 orders) |
| C4-gated | 3/3 | cancelled history (R1), history pagination, rider trip payout |
| F classes | 4/4 + 1 fail | money outcomes 4/4; RetainedOrderState fails: D1 no longer on page 1 of history |
| Fixed-test reruns (lane B) | 25/25 | every test fixed today, incl. lane 2's fixtures and SessionUiTest admin case |

Order ids: D1 8a7114cb-2f9b-47b7-b40f-f5b71e9d5968, D2 8fa0ef02-9f00-481e-8889-ee55f1607afe, D3 49387edd-6436-48be-87cd-1512f3ac3fb1,
OTP 4ea76408-7f78-4f43-8473-37d6abd30b82 (resumed, delivered), cancel d836ca50-f7b3-4369-904e-7b64625e391f,
R1 727e2a1e-722e-4f8d-8f24-d1069064d9d4, 3 placement orders (auto-cancel), 1 delay-approved order (ACCEPTED until the
60-min sweeper). Manifests: UITesting/target/lifecycle and target/checkout (NOT durable: copy before any `mvn clean`).

## Dev changes beyond BATCHES.md (report to owner)
1. Unrecorded order at Brand 1 Outlet 5 (15:39), from the LiveOrderFixture crash; expected auto-cancelled, NOT verified.
2. D1 probably holds 1 OPEN refund ticket (first SupportRefund run submitted it, then 403'd on admin reads).
3. 1 extra fresh 9999 owner + organisation + SUBMITTED, unapproved restaurant application (revocation's first attempt).
4. Suspension left its brand APPROVED (never suspended). 2 extra delivered orders replaced the overlapping-order pair.
5. MenuCartUiTest's retained out-of-stock item (60c6e41e… at 36342aa6…) from the morning sweep.

## Code changes today (all local, uncommitted) — see CURRENT-STATE 16:10 entry, plus:
CustomerOrderPlacementTest + PickupDeliveryOtpTest capture request bodies with a pass-through route; CustomerHomePage.openRestaurant
delegates to NearbyOutletPage.openBrandCard; validate_p02 P1.5 counts executions by source (break-tested); one leaked ws
token redacted in evidence/sweep-77f29d7 OutletSelectionTest XML (a harness print path does not redact ws URLs).

## Gates (quiet tree, 16:59): inventory PASS_CURRENT 229 / PASS_OLDER 75 / LAST_FAILED 18 / NEVER_RUN 14;
validate_p02 6/6; Phase 4 gate 10/10 (88); runner tests 14/14; locator audit FAIL 0; fixture-schema guard PASS 80/DRIFT 0.
