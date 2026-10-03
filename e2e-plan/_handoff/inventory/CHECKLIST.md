> Historical checklist snapshot. For current deployment/fixture state and active next steps, read [CURRENT-STATE](../CURRENT-STATE.md) and [checkpoint 17](../checkpoints/17-deployment-confirmation-and-budget-handoff.md). The old pending deployment below is now completed; financial recovery remains open.

# E2E coverage and implementation audit — 2026-10-01

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


| `03-order-fulfillment/pickup-and-delivery` | Fast review complete | Seven repaired methods; package scope clarified |7methods plus offline-after and retained-order resume passed;9retained delivered orders; riderOFFLINE and release verified;19local backend passed; communication/slow/SSE gaps assigned or deferred |

Scope: reconcile all current executable E2E tests with UITesting/e2e-plan, the historical E2EFullSuite_2026-09-27 evidence and BackendIssues_2026-09-29. Repair tests, product behavior and plan gaps where source evidence supports the change. The current user request authorizes missing coverage implementation and this central progress checklist. Historical failure evidence remains unchanged.

Inventory: 28 feature folders, 71 current test classes, 325 source test methods after shared-flow consolidation; parameterized invocation totals must come from fresh reports. The old report has 103 failed cases. Forty backend report documents are indexed. These are inventory counts, not passing or coverage claims.

## Workflow

For each feature: inspect only its scenarios/pending notes, relevant tests/page objects, UI/backend rules and linked issue evidence; write an explicit scenario-to-test mapping; audit assertions, data isolation, negative/boundary cases and retained-state repeatability; implement defects/gaps; run focused tests; capture durable reports; record remaining blockers before advancing. Shared seeded flows run serially. Test-created records remain retained and identified in manifests. Mocked browser routing is UI-contract evidence only. Dev government verification uses mock approval. Do not count skipped, conditional no-op or environment-blocked paths as passed coverage.

## Feature checklist

| Feature | Source/scenario review | Implementation | Fresh execution |
|---|---|---|---|
| `01-foundation-and-access/environment-and-test-data` | Reviewed | Setup fixed; dependencies assigned | 16 invocations passed; ENV-05 in owning features |
| `01-foundation-and-access/login-and-otp` | Reviewed | Fast checks implemented; slow/rate deferred | 39 fast invocations passed; deferred cases excluded |
| `01-foundation-and-access/sessions-and-role-access` | Fast core reviewed; security follow-ups open | 21 core cases implemented; collision fixed/deployed | 21 cases pass under retention policy + 13 local backend; security follow-ups open |
| `02-customer-order-entry/cart-and-pricing` | Reviewed | Stronger price/quantity/isolation checks; plan corrected | 13 live cases + 5 local boundaries passed; duration/rate deferred |
| `02-customer-order-entry/checkout-and-payment` | Fast reviewed; transactional follow-ups assigned | Stock/wallet/tracker deployed; tip/fallback/abandonment covered | 17 browser passed; 28 UI + 30 backend local; no deployment pending |
| `02-customer-order-entry/restaurant-discovery-and-menu` | Reviewed | Recovery fix deployed; additional coverage implemented | 23 cases passed; 34 local menu checks passed |
| `02-customer-order-entry/saved-address-and-outlet-selection` | In progress | Fast scope verified; positive alternative fixture follow-up assigned | 21 feature-owned + 1 reused browser passed; 15 local passed; no deployment pending |
| `03-order-fulfillment/cross-role-order-lifecycle` | Reviewed fast CROSS01–15 | All fast checks implemented; product fixes deployed | Corrected single-order + overlapping-pair invocations passed;3orders delivered;17backend +16restored UI local; slow/quota deferred |
| `03-order-fulfillment/customer-tracking-and-history` | Fast source reviewed; follow-ups assigned | History/reorder/map/proxy corrections deployed |13history/reorder + dismissal passed; Oracle-origin map44.067s + public carousel135.077s passed; public SSE user-deferred; real delays assigned |
| `03-order-fulfillment/pickup-and-delivery` | Fast review complete | Seven repaired methods; package scope clarified |7methods plus offline-after and retained-order resume passed;9retained delivered orders; riderOFFLINE and release verified;19local backend passed; communication/slow/SSE gaps assigned or deferred |
| `03-order-fulfillment/restaurant-acceptance-and-preparation` | Active source review | Queue, acceptance, preparation, rejection and navigation |Repair legacy fixtures/conditional assertions, then validate focused deployed flows |
| `03-order-fulfillment/rider-availability-and-dispatch` | Pending | Pending | Not run |
| `04-order-exceptions-and-support/chat-calls-and-live-tracking` | Priority source review active | Shared helper/loading/publisher deployed; raw Kafka contract fix ready | 3 fixtures pass; live cross-role evidence retained; quote blocked by raw Kafka listener mismatch |
| `04-order-exceptions-and-support/refunds-and-payment-recovery` | Priority source review active; plan state/quote caps corrected | Quote scope/award/customer status deployed; completion-contract fixes ready | Live quote/cancel/reject money invocations fail; 129 local backend checks across current fixes pass; see feature14 |
| `04-order-exceptions-and-support/rejection-cancellation-and-delays` | Canonical fast cancellation/rejection reviewed | Shared owned refund helper; duplicate tests removed | Both terminal UI outcomes pass; whole invocations fail at mock refund completion; owned manifests retained |
| `04-order-exceptions-and-support/reviews-and-support` | Pending | Pending | Not run |
| `05-partner-and-account-management/campaigns-and-promotions` | Pending | Pending | Not run |
| `05-partner-and-account-management/profiles-settings-and-kyc` | Pending | Pending | Not run |
| `05-partner-and-account-management/restaurant-earnings` | Pending | Pending | Not run |
| `05-partner-and-account-management/restaurant-outlets-and-menu-management` | Pending | Pending | Not run |
| `05-partner-and-account-management/rider-wallet-earnings-and-history` | Pending | Pending | Not run |
| `06-admin-operations/ledger-payouts-and-order-money` | Priority money safety/source reviewed | Routed safety checks; strict posted payout check; ledger fixes ready | Prior routed checks pass; real money GET 200 but delivered payables absent; settlement blocked |
| `06-admin-operations/live-operations-fleet-and-interventions` | Pending | Pending | Not run |
| `06-admin-operations/support-and-refund-queues` | Pending | Pending | Not run |
| `06-admin-operations/users-categories-and-moderation` | Pending | Pending | Not run |
| `07-resilience-and-regression/reload-reconnect-and-recovery` | Pending | Pending | Not run |
| `07-resilience-and-regression/responsive-accessibility-and-navigation` | Pending | Pending | Not run |
| `07-resilience-and-regression/suite-isolation-and-regression` | Pending | Pending | Not run |

## Deferred execution policy

Intentional long-wait tests and rate-limit tests are excluded from current runs at the user's request. Record them in DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md in each relevant feature folder; keep execution opt-in and do not count deferred cases as passed. Final separate execution is reserved for the end.

## Evidence index

- `inventory.json`: source methods, plan references, historical failed methods and backend documents.
- `TEST-CHECKLIST.md`: every currently inventoried test method; each requires review and execution status.
- `01-environment-and-test-data.md`: detailed record created when this feature is reviewed.

## Current priority override — 2026-10-02

The user reprioritized chat, refunds, money and order-related admin flows ahead of the remaining sequential folders. Follow PRIORITY-FLOWS-2026-10-02.md. Reuse the existing happy lifecycle; consolidate distinct assertions and avoid duplicate order-creating methods. SSE is parked. Feature11 fresh success is part of the consolidated happy run; feature12 edge review remains pending. Older evidence belongs to the pre-cleanup baseline and is not current fixture state.

## Continuation

Read this checklist and the active feature record first. Finish the active feature before opening the next. Keep concrete failures and deferred scenarios in the owning plan PENDING.md, with this checklist linking their status. No broad suite rerun until shared setup and feature isolation are validated.

## Session workflow authorization — 2026-10-01

User authorized existing CustomerApplication/.agents/workflows publish/deploy paths only through2026-10-02 06:00 Asia/Kolkata. No workflow/script edits without stopping, explaining the exact proposal/reason and receiving approval. Scope publishing to reviewed fixes; no wipe/dummy reset/test cleanup. Existing local Deployment/deploy.sh and OracleDeployment/03_clean_deploy.sh changes predate this authorization and were not edited/committed by this audit. UI commit9be60fa was published via the existing UI-only CI build and deployed by deploy.sh. Required shared Dev config application was proven checksum-equal and ran as a no-op. Continue with the active feature before advancing. Session details: /Users/parthureddy/Documents/Codex/2026-10-01/do/outputs/session-deployment-authorization.md.


## Assigned source follow-up — telemetry batch

Before closing chat-calls-and-live-tracking/role-access security coverage, review DeliveryTelemetryController's batch publication against accepted-event and active-order binding. Source concern and required negative/positive checks: CommonMistakesDocumentation/Security/telemetry-batch-publication-followup-2026-10-02.md. WebSocket map success cannot satisfy it.


## Assigned source/runtime follow-up — terminal delivery assignment

A naturally abandoned delivery is FAILED in customer state but remains ASSIGNED/OUT_FOR_DELIVERY in delivery state with a matching active Redis key. Record and required immediate/fake-clock finalization checks: CommonMistakesDocumentation/DataAndState/terminal-delivery-assignment-followup-2026-10-02.md. Assigned to exception/dispatch review; no backend fix or waiting test pass is claimed.

## Latest checkpoint 15 — deployment gate

See [15-delivered-terminal-and-capture.md](../checkpoints/15-delivered-terminal-and-capture.md). Four prior services are deployed/healthy; PostgreSQL rejection persistence and 16 ledger dependency checks are verified. Retained quote rerun failed before submission because abandonment erroneously changed the delivered order to DELIVERY_FAILED. Delivered-state guards, explicit payment event routing and confirmed capture persistence are ready: 81 affected local checks passed. Deploy customer-service and payment-gateway; retained recovery, chat/quote and real support decisions remain unverified. No new lifecycle or cleanup.

## Checkpoint24 (2026-10-02T23:58:00+05:30)

Rows above are historical (17:47). Current per-feature state is in FEATURE-STATUS.md. Changes since: `06-admin-operations/support-and-refund-queues` is no longer "Not run": denial and reduced award pass live, SUPPORT-REFUND-03 fails on a product defect that is fixed locally. Refund item completion is proven live. Chat refund refusal replies were lost to a transactional rollback; fixed locally. See [checkpoint24](../checkpoints/24-support-refunds-live-and-refusal-rollback.md).
