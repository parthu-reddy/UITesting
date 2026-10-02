# Resume the Food Delivery E2E audit

Latest: [checkpoint23](checkpoints/23-support-refunds-item-refund-defect.md), updated 2026-10-02T23:05:00+05:30. Item-refund defect fixed by migration; dead full-order support UI and endpoint deleted; reverse schema guard added to CommonLibrary (all local). **Deploy customer-service, gateway config and UI, and publish CommonLibrary**, then rerun SupportRefundResolutionFlowTest.

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
