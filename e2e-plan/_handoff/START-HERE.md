# Resume the Food Delivery E2E audit

## 2026-10-03T21:04:40+05:30 — O2 publication gate

Local O2 static13/13 and core56/56 are green. Latest UI full785/785,0failures/skips; typecheck/lint clean; final validation-schema relocation receives a rebuilt dist, typecheck/lint and existing registration3-case rerun before image publication. All14 producer workflows plus latest Ledger privacy republish succeeded; all12 actual consumer workflows succeeded at exact current heads. Latest Ledger clean223/0/0/0. All21 Java Compose artifacts are fresh. Six protected workflow hashes unchanged. All18 Oracle application profiles verified exclusively Dev. Deployment config/seeds/library provenance03eede1 pushed, preserving unrelated DEPLOY_LOG.

Next: commit/push tested UI; existing unchanged native publish.sh --all; verify each current registry tag/digest; commit/push env tags; existing fullclean --wipe with authorized WIPE acknowledgement; fresh dummy-data.sh --load-only; inspect every log, hardening/reconcile, then public Oracle O2 and required lifecycle/chat/nonzero-earnings/latency proof. No O2 image/deployment/wipe has run yet; O3+ not started. Eleven older oversized UI components remain a source gap for O3/O5's full redesign gates; no line-count exceptions added. No production-readiness completion claim.

Earlier dated entries below are history.

## 2026-10-03T20:55:30+05:30 — O2 final local privacy release

Payout bank disclosure is fixed by PAYOUTS_MANAGE filtering on the mapped response, preserving earnings amounts and entity snapshots. Focused restored3 and full clean Ledger223 tests pass with0failures/errors/skips. Source0aca2ee pushed; exact latest stub publication is dispatched before consumer CI. UI final component build passed; full phase3 is running, phase4/5 pending. O2 static13/13, core56/56 pass. No new O2 image/deployment/wipe/live fixture yet; full platform remains incomplete.

Earlier dated entries below are history.

### 2026-10-03T20:27:21+05:30 — Full graph verification

Pass1 clean install/package completed for all modules;15stub jars each contain contract resources. Privacy mutation actual HTTP guard1failure, source restored. First pass2 failed IdentitySigning8/1/0/0 because the parent test-only secret overrides the environment fixture; dependent modules skipped, not verified. Fixture isolation restored8/0/0/0 without runtime changes; full pass2 is rerunning. E2E test-compile passed; public Oracle tunnel HTTP200. No images/deploy/wipe executed.

Current 2026-10-03T20:21:43+05:30: CommonLibrary O2 commit37f68545a8293d42c9837c290d7bd62cfd673105 is published by GitHub Packages run37130225264 (success). Safe BrandSummaryDto prevents member brand lists/events from exposing tax/bank application data; HTTP privacy and actual OpenAPI guards2/0/0/0 pass. Final clean Customer472, Reviews96 and Gateway27 have zero failures/errors/skips before the full dependency rebuild now running. UI full125files/785tests passed; subsequent corrected brand3tests, typecheck and lint pass. Earlier concurrent API regeneration caused23 import failures and is preserved as a failed invocation. Core56/56, readiness61/61 static checks (one optional execution check skipped; UI independently executed), core16 and readiness10 positive/broken guard controls pass. Scoped hash-bound Dev schema recreation manifest protects unreviewed SQL and expires on production declaration. Full fleet clean artifacts/contracts are underway; O2 service/UI publication, authorised full --wipe/fresh seed and Oracle live gates remain pending. Oracle currently healthy; no wipe/new O2 live fixture. O3+ unstarted; full platform incomplete.

Latest checkpoint: [checkpoint39](checkpoints/39-business-platform-release-preparation.md). Earlier dated sections are historical.

Current 2026-10-03T19:52:33+05:30: reviewed Dev schema cleanup is local only. Twenty incremental SQL files in Common/Identity/Restaurant/Chat/Notification are consolidated into initial schemas; all four full schemas and Identity/Restaurant seeds execute in disposable PostgreSQL/PostGIS,13 additional constraints pass. Clean Common251, Restaurant91, Chat73, Notification43 have zero failures/errors/skips; Identity clean99/0/0/3, plus all3 conditional rate guards3/0/0/0 separately. Schema integration guards1/0/0/0 each; O1 static20/20,O2 static13/13. Current O2 safe brand summary/final consumers/specs/UI build and existing baseline findings remain open. Publish Common/stubs then clean dependent images/UI before full authorised Dev --wipe, seed and public-Oracle live gates. No cleanup release/wipe occurred. O3+ wallet/Ads phases unstarted; full platform production readiness incomplete.

Latest checkpoint: [checkpoint38](checkpoints/38-business-platform-fresh-dev-schemas.md). Older dated paragraphs below are history.

Current 2026-10-03T18:37:44+05:30: O2 is implemented in local working source across restaurant ownership/permissions, shared money access, order/review access, outlet-entity chat, deterministic seeds and existing UI flows. Static gate13/13; shared clean251, Customer clean467 and Reviews clean95 (zero failures/errors/skips). Real role/query/stock/Feign/chat guards passed; actual PostgreSQL ownership migration guard1/0/0/0 passed in an isolated disposable schema, not an Oracle application. UI role guards21/0/0/0, typecheck/lint passed. New O2 E2E class compiles only, no new live fixture. Remaining gates: final clean consumers/contracts, required break-tests, full UI suite/build, O2 preflight runner, publishing, authorised fresh Dev wipe/reseed, Oracle staff/lifecycle/chat/financial regressions and carried membership latency/breaker measurements. O2 remains unpublished/undeployed; no wipe has run. O3+ remains unstarted. Full platform production readiness is incomplete.

Latest local phase checkpoint: [checkpoint37](checkpoints/37-business-platform-o2-local.md). Earlier dated entries are history.

Current 2026-10-03T18:07:18+05:30: O1's deployed lifecycle, exact single OWNER and processed outbox are verified. Seven repaired Oracle services are healthy with zero restarts and no fresh errors; final notification/Identity images are deployed. Notification 43 and Identity 98 local tests passed; normal Dev browser login passed live (1/0/0/0), and the nullable pre-account OTP audit/migration is verified. Dev mock push is owner-confirmed; production requires Firebase credentials. O2 implementation has begun locally: shared explicit-user membership and earnings/payout checks passed 23 focused tests. Restaurant schema/access source changes are in progress and incomplete; O2 is unpublished, undeployed, and no wipe has run. O1's unused internal membership latency/breaker measurement is carried to O2's first real consumer. Full platform production readiness remains incomplete.

Earlier dated sections below are history.

Latest: [checkpoint36](checkpoints/36-business-platform-startup-repair.md), updated 2026-10-03T17:49:32+05:30.
O1 lifecycle/owner/outbox, campaign3checks, earnings empty-state and read-only server latency verified.
New Notification43/Identity98 tests green; OTP/Dev provider repairs publishing, service not yet clean
(six other repaired services clean/stable). O2 baseline/source enumeration saved; no O2 product edits.
**Full platform incomplete.** Five defaults confirmed; task commits/pushes/publish/deploy and required
clean --wipe+seed authorized. No wipe executed. See CURRENT-STATE for exact fixtures and repair boundary.

The checkpoint34 entry below is prerequisite history.

Latest: [checkpoint34](checkpoints/34-campaigns-onboarding-and-ad-money.md), updated 2026-10-03T10:27:00+05:30. Campaigns "Start advertising" step built per the user's decision, plus ten defects (registration could never succeed: wallet currency "AD_CREDIT" into VARCHAR(3); 100× ad budgets/bids; an advertiser wallet no owner could read; …), all local. **Deploy pending: UI, campaign-service, wallet-service** (governmentid-service optional). Then run RestaurantEarningsLiveTest and RestaurantCampaignsLiveTest. Open question for the user: no campaign can ever be activated (no creative/moderation path). **Never commit or push** (permanent user rule).

Timestamps: read `date` before stamping. Checkpoints 32 and 33 were stamped ahead of real time and corrected at checkpoint34.

Updated 2026-10-02T17:13:01+05:30 (Asia/Kolkata). This folder contains the durable handoff for switching agents after a usage limit. It records project/task context and decisions, not hidden reasoning or credentials. Read this file, USER-INSTRUCTIONS.md, CURRENT-STATE.md and NEXT-STEPS.md before running anything. Read the owning feature's AUDIT-STATUS.md, scenarios.md and PENDING.md (where present) next; do not load every source/report at once.

## Objective and current focus

Reconcile all existing executable E2E tests, historical BackendIssues_2026-09-29 and E2EFullSuite_2026-09-27 reports, and this plan. Repair missing/wrong implementations in tests, UI/backend and plan. Work feature by feature, but prioritize chat, refunds, money and order-related admin scenarios. Reuse canonical successful flows and combine assertions; do not create another happy lifecycle to avoid a blocked retained order.

The complete audit is not finished. Prior fast feature passes are historical and span database resets. Current owned money fixtures are blocked, although the previous fixes are deployed and healthy. The earlier amount guard is deployed; checkpoint18 and19 deployment gates remain pending. Read CURRENT-STATE and DEPLOYMENT-GATE. No full-suite/all-scenarios green claim exists.

## Read in this order

The root [AGENTS.md](../AGENTS.md) requires every agent to update this plan during work and before handoff. The [joint decisions register](DECISIONS.md) preserves agreed decisions and reversals.

1. [User instructions](USER-INSTRUCTIONS.md): retained data, Dev behavior, priority, exclusions and deployment restrictions.
2. [Current state](CURRENT-STATE.md): latest deployment and exact owned records; supersedes old checkpoints.
3. [Next steps](NEXT-STEPS.md): ordered work and stop conditions.
4. [Feature status matrix](FEATURE-STATUS.md) and the owning feature's AUDIT-STATUS.md.
5. [Commands and workspace](WORKSPACE-AND-COMMANDS.md), [verification boundaries](VERIFICATION.md), [deferred work](DEFERRED.md), [issue register](ISSUE-REGISTER.md).
6. Full [source/test inventory](inventory/inventory.json), [method checklist](inventory/TEST-CHECKLIST.md), or one linked checkpoint only when needed. Class/method counts are inventory, not passing totals.

## Update contract for every agent

After every completed batch, failure, deployment gate or pause, update CURRENT-STATE.md, NEXT-STEPS.md and relevant feature AUDIT-STATUS.md, then save redacted evidence/manifests here. Preserve historical checkpoints. Record exact selected test methods/counts, failures/errors/skips and proof type. Copy authoritative completed records into inventory, with a current timestamp. Keep artifacts out of target-only or another agent's task directory. Run affected existing tests and contracts as well as new tests; dependency searches must include constructors, handlers and @Import contexts.

This handoff is the current entry point; archived checkpoints keep their historical language. User instructions override older cleanup/allowlist/item-details/publish instructions. The existing TEST-DATA.md remains mandatory. No deployment workflow was changed.

## Prompt to give a replacement agent

> Continue the existing Food Delivery E2E audit. Read `_handoff/START-HERE.md`, `USER-INSTRUCTIONS.md`, `CURRENT-STATE.md`, and `NEXT-STEPS.md` in this plan folder first. Follow the recorded user constraints, inspect current source/runtime state, and resume the priority work on the owned fixtures. Preserve the handoff and update it before stopping. Historical passes and deployment confirmation are not proof that retained failed financial records recovered. Do not reset the database, clean up fixtures, duplicate a happy lifecycle, execute deferred waits/rate-limit/SSE tests, or publish/deploy without renewed authorization.
