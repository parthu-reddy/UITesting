# Organisation and portal access audit status

Updated 2026-10-03T15:50:36+05:30. ORG-01..06 map to OrganisationLifecycleApiTest#organisationLifecycle (scenarios.md).
GatewayApi uses two same-origin browser sessions; registration uses existing LoginPage/Dev Autofill Code.
The scenario retains owned records, without cleanup.

O1 local code/unit/security/transaction/query/rate/generated contracts and real PostgreSQL checks pass.
CommonLibrary and all consumer/gateway clean suites pass with existing skips described in
[checkpoint35](../../_handoff/checkpoints/35-business-platform-o1-local.md). O1 validator20/20;
existing baselines unchanged; E2E test-compile passes. Feign ordering and redundant UTC Clock defects
were caught/fixed. Exact per-class counts and mutations are linked by checkpoint35.

**Live scenario not run; O1 release incomplete.** Checkpoint34 deployment, D15 confirmation, O1 owner
rollout, lifecycle/SQL/outbox and latency/breaker measurements remain open. No live fixtures created.
O2+ not started. UI consistency belongs to O5/W3/A4. Local passes do not prove production readiness.

## Current Oracle result — 2026-10-03T17:36:31+05:30

ORG-01..06 actual OrganisationLifecycleApiTest#organisationLifecycle passed1/0/0/0 in third invocation
4bbc471d321c081b. First invocation1error stale signup locator/no rows; second1failure Gateway PATCH CORS;
both retained. Third registered two fresh8999 customer accounts, exercised role/rank/nonmember guards,
invites/duplicates, removal/reinvite and ownership transfer. Exact SQL has one ACTIVE OWNER and all
outbox rows PROCESSED; Identity scheduling defect fixed and deployed. See checkpoint36/evidence and
fixtures. Latency and generated breaker IDs remain unverified; O2+ not started.

## Current O2 source progress — 2026-10-03T18:28:21+05:30

O1 deployed lifecycle/owner/outbox and org-list latency are verified; seven repaired services are healthy with zero restarts/no fresh errors. Dev push mock is owner-confirmed and production Firebase is required. O2 now has shared policies, restaurant schema/permission/stock paths, updated order/review/money/chat consumers, seed relationships and UI changes locally. Targeted security/query/real-Feign/PostgreSQL guards are green; complete clean suites, UI checks, break-tests and new O2 E2E are underway. O2 remains unpublished/undeployed. Internal membership latency/breaker measurement remains a carried gate before declaring O2 complete. Full platform incomplete.

## Local batch 2026-10-03T18:37:44+05:30

Current 2026-10-03T18:37:44+05:30: O2 is implemented in local working source across restaurant ownership/permissions, shared money access, order/review access, outlet-entity chat, deterministic seeds and existing UI flows. Static gate13/13; shared clean251, Customer clean467 and Reviews clean95 (zero failures/errors/skips). Real role/query/stock/Feign/chat guards passed; actual PostgreSQL ownership migration guard1/0/0/0 passed in an isolated disposable schema, not an Oracle application. UI role guards21/0/0/0, typecheck/lint passed. New O2 E2E class compiles only, no new live fixture. Remaining gates: final clean consumers/contracts, required break-tests, full UI suite/build, O2 preflight runner, publishing, authorised fresh Dev wipe/reseed, Oracle staff/lifecycle/chat/financial regressions and carried membership latency/breaker measurements. O2 remains unpublished/undeployed; no wipe has run. O3+ remains unstarted. Full platform production readiness is incomplete.

See E2E checkpoint37/evidence37-o2-local-progress.json for per-class report counts/timestamps. Restaurant and chat ownership migrations require the authorised fresh Dev wipe; neither has run on Oracle.

### 2026-10-03T18:40:34+05:30 — Owner clarification: Dev-only fresh recreation

Owner confirms nothing is in production; all current application data is Dev and may be completely deleted and recreated using dummy seeds. No legacy data transfer, preservation or backfill is required. Use the already-authorised unchanged clean-deploy --wipe followed by updated dummy seeds for this organisation transition. Update schema definitions needed by the new code and dummy organisation/member/brand relationships; Flyway SQL runs during fresh database recreation (ddl-auto=validate), not a migration of existing business data. Do not add production-data conversion or infer old owners. The two existing O2 SQL files only establish required schema fields/constraints on fresh Dev databases; they contain no data backfill. Earlier retained fixture evidence remains historical after the authorised wipe. No wipe has run yet.

## O2 local gate progress — 2026-10-03T18:59:24+05:30

Clean shared251, Restaurant83 (then expanded role table/endpoint cases), Identity99, Chat73, Wallet57, Ledger220, Customer467 and Reviews95 all had zero failures/errors/skips before deliberate mutations. Full UI124files/782tests passed with four workers; one earlier unbounded dialog-animation failure and isolated10test rerun retained. Rendered brand registration3/0/0/0 now verifies no-org creation, existing-org reuse and failed brand retry without duplicate organisation; labelled inputs now connect to accessible fields. Exact new consumer contracts: Reviews2/0/0/0; Customer contract7/0/0/0 plus OrderSecurityHelper4/0/0/0. All nine required unique break labels caught (including extra query/timeout guards), each source restored; persistence unique-index break1failure then restored PostgreSQL guard1/0/0/0. Disposable unit container removed; no Oracle application target or reset. Final restored clean library/consumers/specs/types/build still pending.

Core validator50/56 retains six BASE findings; O2 obsolete strict fields were corrected to orderId-only creation and entityId/type participation, with unrelated Identity/Ledger schema ratchets still open. Full money readiness retains pre-existing test-method-count and admin-refund-queue findings; phase7 alone8/8. Money as-is audit is historical0/23, not a passing current implementation gate. Reviews85/85, static seeds and role names pass. Required guard evidence37-o2-mutations.json; exact reports/current state37-o2-local-progress.json. O2 remains unpublished/undeployed; no Dev wipe or new O2 live account has occurred. User confirms all data Dev/disposable until explicit forget/production report; permanent memory and authoritative DECISIONS updated.


## Reviewed schema cleanup — 2026-10-03T19:52:33+05:30

Current 2026-10-03T19:52:33+05:30: reviewed Dev schema cleanup is local only. Twenty incremental SQL files in Common/Identity/Restaurant/Chat/Notification are consolidated into initial schemas; all four full schemas and Identity/Restaurant seeds execute in disposable PostgreSQL/PostGIS,13 additional constraints pass. Clean Common251, Restaurant91, Chat73, Notification43 have zero failures/errors/skips; Identity clean99/0/0/3, plus all3 conditional rate guards3/0/0/0 separately. Schema integration guards1/0/0/0 each; O1 static20/20,O2 static13/13. Current O2 safe brand summary/final consumers/specs/UI build and existing baseline findings remain open. Publish Common/stubs then clean dependent images/UI before full authorised Dev --wipe, seed and public-Oracle live gates. No cleanup release/wipe occurred. O3+ wallet/Ads phases unstarted; full platform production readiness incomplete.

Latest checkpoint: [checkpoint38](../../_handoff/checkpoints/38-business-platform-fresh-dev-schemas.md). Older dated paragraphs below are history.

## Release preparation — 2026-10-03T20:21:43+05:30

Current 2026-10-03T20:21:43+05:30: CommonLibrary O2 commit37f68545a8293d42c9837c290d7bd62cfd673105 is published by GitHub Packages run37130225264 (success). Safe BrandSummaryDto prevents member brand lists/events from exposing tax/bank application data; HTTP privacy and actual OpenAPI guards2/0/0/0 pass. Final clean Customer472, Reviews96 and Gateway27 have zero failures/errors/skips before the full dependency rebuild now running. UI full125files/785tests passed; subsequent corrected brand3tests, typecheck and lint pass. Earlier concurrent API regeneration caused23 import failures and is preserved as a failed invocation. Core56/56, readiness61/61 static checks (one optional execution check skipped; UI independently executed), core16 and readiness10 positive/broken guard controls pass. Scoped hash-bound Dev schema recreation manifest protects unreviewed SQL and expires on production declaration. Full fleet clean artifacts/contracts are underway; O2 service/UI publication, authorised full --wipe/fresh seed and Oracle live gates remain pending. Oracle currently healthy; no wipe/new O2 live fixture. O3+ unstarted; full platform incomplete.

See checkpoint39 and evidence39 for exact proof and continuation.

### 2026-10-03T20:27:21+05:30 — Full graph verification

Pass1 clean install/package completed for all modules;15stub jars each contain contract resources. Privacy mutation actual HTTP guard1failure, source restored. First pass2 failed IdentitySigning8/1/0/0 because the parent test-only secret overrides the environment fixture; dependent modules skipped, not verified. Fixture isolation restored8/0/0/0 without runtime changes; full pass2 is rerunning. E2E test-compile passed; public Oracle tunnel HTTP200. No images/deploy/wipe executed.

## 2026-10-03T20:38:03+05:30 — Final graph and consistency choices

Full restored clean backend graph1877reported cases,1876executed passes,0failures/errors;1explicitly disabled future ONDC delivery-status contract remains parked. All26generated producer classes have reports. All21Java Compose artifacts are fresh; removed SQL resources absent. Restaurant final actual OpenAPI1guard, fleet map2existing rendered tests pass. Timezone26/26 with26positive and77negative controls; the multiline @Table parser now reads the actual table-name line, avoiding a false missing payout_operations.created_at finding. Use shared Select for fleet city and its visible selected value/listbox interactions. Set a4-worker Vitest budget consistent with the completed785-test runs to avoid unbounded resource contention; no scenarios removed. Record Common published commit in Deployment/published-libraries.json so shared SQL has publication protection even though a library has no Compose tag; core20positive/broken controls cover missing/unknown publication and unreviewed shared SQL after production declaration. Final UI build and full required UI gates underway; publication/deploy/wipe/live remain pending.


## 2026-10-03T20:55:30+05:30 — O2 final local privacy release

Payout bank disclosure is fixed by PAYOUTS_MANAGE filtering on the mapped response, preserving earnings amounts and entity snapshots. Focused restored3 and full clean Ledger223 tests pass with0failures/errors/skips. Source0aca2ee pushed; exact latest stub publication is dispatched before consumer CI. UI final component build passed; full phase3 is running, phase4/5 pending. O2 static13/13, core56/56 pass. No new O2 image/deployment/wipe/live fixture yet; full platform remains incomplete.


## 2026-10-03T21:04:40+05:30 — O2 publication gate

Local O2 static13/13 and core56/56 are green. Latest UI full785/785,0failures/skips; typecheck/lint clean; final validation-schema relocation receives a rebuilt dist, typecheck/lint and existing registration3-case rerun before image publication. All14 producer workflows plus latest Ledger privacy republish succeeded; all12 actual consumer workflows succeeded at exact current heads. Latest Ledger clean223/0/0/0. All21 Java Compose artifacts are fresh. Six protected workflow hashes unchanged. All18 Oracle application profiles verified exclusively Dev. Deployment config/seeds/library provenance03eede1 pushed, preserving unrelated DEPLOY_LOG.

Next: commit/push tested UI; existing unchanged native publish.sh --all; verify each current registry tag/digest; commit/push env tags; existing fullclean --wipe with authorized WIPE acknowledgement; fresh dummy-data.sh --load-only; inspect every log, hardening/reconcile, then public Oracle O2 and required lifecycle/chat/nonzero-earnings/latency proof. No O2 image/deployment/wipe has run yet; O3+ not started. Eleven older oversized UI components remain a source gap for O3/O5's full redesign gates; no line-count exceptions added. No production-readiness completion claim.
