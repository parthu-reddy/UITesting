# Checkpoint 144 — restaurant-applicant reruns after the ApplicationOutletFactory redeploy

2026-10-10 ~07:00 IST. Owner: "deployed, go ahead with the checks". Nothing running afterwards.

Deploy verified: restaurant-service `168853c` (ApplicationOutletFactory equal ends = all day + ApplicationOutletFactoryTest)
— DEPLOY_LOG 2026-10-10T01:17:50Z == `Deployment/env_deployments/dev/restaurant-service.env` pin == local HEAD ==
origin/main (clean tree) == running image (`docker compose ps`: healthy, created 01:17:05Z).

| Run | Result | Dev records |
|---|---|---|
| `run_partner_applications_o3_e2e.py --only restaurant` (RestaurantApplicationApiTest#restaurantApplicationLifecycle) | 1/1, 56.6 s | org 51ec0d3e, application 600388de (changes requested → resubmitted → approved); approved storefront opens for the customer |
| `run_registration_e2e.py --only restaurant` (RegistrationUiTest#restaurantRegistrationFlow) | 1/1, 25.1 s | org 735e00ef, application submitted; `POST .../application/outlets` 200 (was 400 before the fix) |

Quote 401 after sign-out — investigated, NOT a defect (owner asked, 2026-10-10):
- Order, from Dev api-gateway logs: storefront GETs 01:21:20.608–.624 (delivery-availability passed auth at .624);
  customer `auth/logout` .632; restaurant logout .799; admin logouts 21.233/.335. Teardown
  (`PartnerApplicationsUiTestBase.tearDownContexts`) signs out customer → restaurant → rider → admin and closes contexts
  only afterwards, so the customer storefront stays live after its session is revoked.
- The quote is `useCustomerCart`'s 500 ms debounce after the storefront selects the outlet (it quotes the selected
  restaurant even with an empty cart), so it fired after the logout. Neither gateway nor customer-service logged it:
  the gateway rejects a revoked session in `GlobalJwtAuthFilter.handleUnauthorized` (plain 401, no log, no
  `X-Auth-Reason`); customer-service has no request line for it. (The quote timing comes from the debounce plus the
  missing log lines; there's no timestamp for the quote itself.)
- No refresh is by design: `authFetch` refreshes only on `X-Auth-Reason: ENTITLEMENTS_CHANGED`. Any other 401 calls
  `expire()`. The refreshes elsewhere in these runs were all entitlement changes (org created / approved).
- The 409 `delivery-availability` = `DeliveryPartnerUnavailableException` (customer-service 01:21:20.713, "No delivery
  partner near that restaurant"): the new applicant outlet has no rider online nearby. Expected.

## Follow-up (owner: "make both changes") — local, uncommitted

1. **UITesting teardown** (`TestBase.tearDownContexts`, `PartnerApplicationsUiTestBase.tearDownContexts`): each page is
   signed out and its context closed at once (customer → restaurant → rider → admin), so no page keeps running on a
   revoked session while the others sign out. TestBase still swallows close failures; the partner base still fails
   the test on one. A page's own calls in the few ms between its logout response and its close can still 401.
   Verified: test-compile green; live `--only queues` (AdminPartnerApprovalsUiTest#reviewQueuesNavigation, read-only,
   admin 002) 1/1 in 9.0 s, teardown `admin signed out [admin=200, everyday=200]`. TestBase's path is compiled, not
   yet run live.
2. **ApiGateway** `GlobalJwtAuthFilter`: every 401 now logs
   `GlobalJwtAuthFilter REJECTED: path=… method=… reason=…` (INFO, same logger as the SUCCESS lines). Reasons:
   no-token, invalid-token:<ExceptionClass>, malformed-claims, session-revoked, session-not-active,
   session-state-unavailable, bad-entitlements-version, entitlements-changed. Never the token, query string or session
   id. Tests: 4 new in GlobalJwtAuthFilterRevocationTest; proofs red (no-op log → 4 fail; entitlements line removed →
   1 fail; Authorization header appended → 4 fail); restored file byte-identical; `mvn -o clean test` 76/0.
   Role 403s are still silent (not a session rejection; out of scope).

Owner: commit ApiGateway + redeploy api-gateway; commit UITesting.
