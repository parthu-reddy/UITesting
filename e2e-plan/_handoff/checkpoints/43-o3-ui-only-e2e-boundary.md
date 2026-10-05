# Checkpoint 43 — O3 UI-only E2E boundary

## 2026-10-04T07:03:09+05:30

The owner requires all O3 E2E setup, actions and assertions to use the deployed public UI only.
The decision is recorded in [USER-INSTRUCTIONS.md](../USER-INSTRUCTIONS.md),
[DECISIONS.md](../DECISIONS.md), and the Business Platform
[DECISIONS.md](../../../../RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md).

### Current state

- O3 is local, uncommitted, unpublished and undeployed.
- No rewritten UI-only O3 browser test has been compiled or run. No O3 application, upload, admin
  decision, database write, wipe, seed, publication or deployment was performed by this checkpoint.
- Prior local embedded-H2 outcomes remain service-level evidence only: Delivery 5/0/0/0 and
  Restaurant 15/0/0/0. They do not prove an Oracle browser journey or PostgreSQL/PostGIS behavior.
- Docker and every external database fixture are prohibited. The earlier Oracle/external-database
  approach is historical and must not be restored.

### Required browser boundary

Use normal visible login and Dev Autofill, then visible applicant wizards, upload controls, admin
approval controls, customer discovery/search, and rider duty controls. A click-generated
request/response may be observed. Do not use `GatewayApi`, browser `fetch`, direct URLs or API
clients, DB/Redis/SSH access, fixture-side calls, local-storage/cookie/indexed-storage injection,
or a Docker/external database test fixture.

### Deferred coverage

The four non-UI categories are recorded in
[DEFERRED-UI-ONLY-TESTS.md](../../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-UI-ONLY-TESTS.md):

1. Direct IDOR and retired-route probes.
2. Hidden-outlet stale-cart/by-id/catalog/quote behavior that requires injected state.
3. The direct thirty-request measurement loop.
4. Outbox/audit/Redis allocation reads.

Review-queue duration and rate-limit exhaustion are separately deferred in the feature's
[DEFERRED-WAIT-TESTS.md](../../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-WAIT-TESTS.md)
and
[DEFERRED-RATE-LIMIT-TESTS.md](../../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-RATE-LIMIT-TESTS.md).
Deferred cases are neither executed nor passes.

### Next sequence

1. Complete the four O3 browser classes using the visible UI only; retain the legacy `*ApiTest`
   filenames solely for validator compatibility.
2. Compile and source-audit the rewritten E2E without Docker, external DB, direct APIs or state
   injection. Record the exact invocation separately.
3. Finish current local service/UI gates. Publish through the approved GitHub workflow path before
   any deployment.
4. After the authorised clean Dev deployment/seed, run only the non-deferred deployed UI journeys
   and record their evidence. Do not infer completion from this documentation checkpoint.

Previous: [checkpoint42](42-business-platform-partner-applications.md).
