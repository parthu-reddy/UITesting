# Standing instructions for every agent using this E2E plan

The user explicitly requires all agents continuing this audit to update their results and decisions **inside this plan folder**. The next agent must be able to continue from this folder without the previous chat, private task directory or memory. This applies when using the plan to edit/run tests or product code elsewhere in the assembled workspace, as well as when editing these documents.

## Running tests against Dev (mandatory since 2026-10-07)

- **More than one test class → `python3 _handoff/tools/run_e2e_batch.py`**, never a raw multi-class
  `mvn -Dtest=A,B,C`. It refuses to start during a deploy, runs a canary per role first, runs one class
  per invocation with progress + ETA, paces admin step-up (Identity: 5 verifies / 5 min, 10 sends /
  10 min per admin phone; the history persists across invocations in `_handoff/ADMIN-STEP-UPS.json`), and stops
  on the second identical setup failure. Single-method probes go through the runner too: a direct `mvn` step-up is
  invisible to its pacing.
- **Quick runs:** `--exclude-tags slow,auto-cancel` skips the live order lifecycles (`slow`: median ≥ 60 s) and tests
  that leave or need orders for the server's 10/60-min auto-cancel (`auto-cancel`). Phase6 gate keeps the tags true. Its guards are unit-tested:
  `python3 _handoff/tools/test_run_e2e_batch.py`.
- **Run what the change touches, not a sweep (owner, 2026-10-08).** Every test carries `@Tag("feature-<name>")`. The
  vocabulary and source map are in `feature-tags/features.json`.
  - `run_e2e_batch.py --changed [--since REF]` runs the tests of the features your uncommitted (or since-REF) changes
    touch.
  - `--features chat,reviews` runs named features.
  - Preview without running: `python3 feature-tags/select_tests.py --git`.
  - `slow`/`auto-cancel` need `--include-slow`. `parked`/`measurement`/`auth-rate-limit` never run.
  - Runs are fast by default (no slow-mo, headless, no video). Add `--debug` to watch one or keep its video.
  - A new test needs a feature tag, and a new source file must resolve to a feature.
  - Gate: `python3 feature-tags/validate_feature_tags.py` (6/6). Break tests: `python3 feature-tags/test_feature_tags.py`.
  - No full sweep unless the owner asks for one.
- **Owner says a deploy is in progress → `touch _handoff/DEPLOY-IN-PROGRESS`** (the runner refuses while
  it exists); delete it when the owner says "deployed", after checking DEPLOY_LOG == pin == HEAD.
- **Never leave a run unwatched.** Watch the runner's output (`Monitor`/tail) and report the expected
  duration to the owner before starting. Admin-heavy batches take ≥1 min per admin test by design.
- Background: P0-2 batch 1 lost ~80 of 89 minutes to two repeated setup failures nobody watched
  (`RandomDocuments/PendingWork_2026-10-07/P02_E2EInventory/Phase3_SafeLiveRuns/mistakes_and_improvements.md`).

- **Routed fixtures:** register `page.routeWebSocket` before the page loads (or `navigate` to the page under test
  right after registering; do not `reload`, which lands wherever the app redirected). A stub registered on a
  loaded page is dead: the socket reaches real Dev. Fixture JSON must satisfy the generated Zod schema in
  `FoodDeliveryAppUI/src/api/generated/schemas/` (checkpoint132).

## Before work

1. Read [_handoff/START-HERE.md](_handoff/START-HERE.md), [USER-INSTRUCTIONS.md](_handoff/USER-INSTRUCTIONS.md), [CURRENT-STATE.md](_handoff/CURRENT-STATE.md) and [NEXT-STEPS.md](_handoff/NEXT-STEPS.md).
2. Read the owning feature's AUDIT-STATUS.md, scenarios.md and PENDING.md where present. Inspect current source/runtime state before relying on historical reports. Do not load every file or duplicate previously verified happy/acceptance coverage.
3. Observe the current user constraints and expired deployment authorization. Do not reset/seed/delete fixtures, run parked SSE/intentional-duration/Dev-rate-limit scenarios, or change protected workflows without the authorization recorded in USER-INSTRUCTIONS. New direct user instructions take precedence; record them here promptly.

## Required updates during work and before handing over

After every meaningful batch, failure, decision, deployment confirmation/gate, pause or usage-limit stop:

- Update the owning feature's AUDIT-STATUS.md with scenarios/methods covered, changes, exact proof, unresolved gaps and next action. Update scenarios/PENDING/deferred files when scope changes. Preserve contradictory older claims only as clearly labeled history; do not silently promote them to current results.
- Update _handoff/CURRENT-STATE.md and NEXT-STEPS.md with current timestamps, deployed versus working versions, pending deployments, owned fixture identities/state, blockers and exact continuation commands/preconditions. Retain existing original manifests; never put credentials, OTPs, tokens or browser storage here.
- Save bounded redacted counts/state evidence in _handoff/evidence and durable owned manifests in _handoff/fixtures. Record selected test methods/classes, invocation counts, failures/errors/skips, proof type and artifact references. A skipped/no-op/blocked test is not a pass; local/routed proof is not deployed completion. Count reruns separately from unique cases.
- Update FEATURE-STATUS.md and relevant inventory/checklist entries. Keep historical inventories dated; rescan source before treating old method counts as current. Save a numbered checkpoint under _handoff/checkpoints for substantial work, linking the original audit archive when relevant.
- Record new/changed user instructions and joint decisions in USER-INSTRUCTIONS.md and DECISIONS.md, including reversals, date/scope, affected feature and remaining work. Never carry a decision only in conversation or a private memory.
- Record encountered issues in the workspace CommonMistakesDocumentation and reusable architecture/performance/security/testing/coding lessons in CodingPracticesAcrossAllServices. Link or copy the relevant concise note into this handoff so continuation keeps the lesson.
- Before ending a turn/handoff, ensure START-HERE points to the latest checkpoint/deployment gate, links resolve, and results describe the actual current state. Explicitly state what is completed, unverified, deferred and waiting on the user. Do not stop with all context only in target/ or another agent's outputs folder.

These updates are part of completing the work, not optional housekeeping. Each future agent has the same responsibility, even if they only inspect or rerun an existing test. No automatic server cleanup or deployment is authorized by this document.
