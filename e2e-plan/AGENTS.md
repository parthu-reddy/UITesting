# Standing instructions for every agent using this E2E plan

The user explicitly requires all agents continuing this audit to update their results and decisions **inside this plan folder**. The next agent must be able to continue from this folder without the previous chat, private task directory or memory. This applies when using the plan to edit/run tests or product code elsewhere in the assembled workspace, as well as when editing these documents.

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
