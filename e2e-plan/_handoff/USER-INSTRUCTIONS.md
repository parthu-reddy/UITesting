# Primary user instructions and accepted decisions

## Scope, ordering and efficiency

- Audit every currently added E2E test and referenced backend failure against UITesting/e2e-plan; fix missing/wrong test, product or plan implementation when source evidence supports it. The user explicitly authorized missing coverage during this audit. Earlier generic advice against new E2E tests does not override this request.
- Understand the folder structure first. Work one feature at a time, write/map its scenarios, inspect only relevant source/page objects/tests/report evidence, implement and validate before advancing. Do not blindly read all files or run the full suite.
- Current priority overrides simple phase order: chat, refunds, money, and order-related admin pages/decisions first; lower-value cases later. Cover distinct meaningful business outcomes and important money boundaries, rather than every combination across every screen.
- Avoid duplicate work. Use HappyDeliveryFlowTest as the canonical example and reuse successful acceptance/preparation/dispatch/delivery/chat/payout assertions. Consolidate related cases into one existing test when sensible. Create separate owned orders only for incompatible rejection/cancellation/refund outcomes. Do not rerun known successful lifecycle coverage without a change or unresolved concern.
- If backend behavior is wrong, inspect the source, fix the actual cause, add meaningful local checks and run every affected existing test/contract. Prepare a concrete deployment handoff, wait for user deployment confirmation, then rerun the exact retained flow. Do not weaken assertions or silently pass failures.

## Dev behavior and login

- Target is the Dev profile. Government verification and payments are mocked; do not trigger actual government APIs/payment providers. Dev onboarding autoapproval is intended, not proof of production provider integration.
- Non-admin Dev OTP autofill must support any valid 10-digit phone, including random fresh registration numbers. Restrict only admin autofill/login provisioning to approved active ADMIN accounts (1000000001/1000000002). A phone does not grant a portal role. 9000000001 is RESTAURANT, not Delivery Executive.
- Use the normal UI Dev Autofill Code control. Do not replace it with SSH/manual OTP/JWT extraction or the parked secret runner harness. Do not expose tokens/OTP/webhook secrets in logs or docs.
- Ordinary E2E actors use the seeded ranges and Home/Brand1 outlet rules in TEST-DATA.md. Fresh signup tests use their existing unused-number runner only when reviewing registration; do not repeat actual signup merely for financial fixtures.

## Retained data and rider duty

- The user explicitly said: no cleanup; tests can execute without cleanup. Retain users, profiles, addresses, partner/outlet records, orders, stock changes and server sessions. Do not delete/deactivate/revoke/reset/restore them as teardown. Keep owned manifests. Browser context disposal is normal; it is not server cleanup.
- Logout, explicit device removal, cancellation or stock change is permitted when that action is the behavior under test and scope is owned. Do not touch unrelated manual/shared records.
- Sole automatic remote duty exception: make an idle test rider OFFLINE, with server-authoritative confirmation. Preserve ON_DELIVERY riders and their active assignment. Never cycle already ONLINE riders OFFLINE/ONLINE or commandeer another manual session.
- For checkout, authoritative ONLINE state, active WebSocket/geolocation, no location-loss warning, and nearby eligible outlet must hold at the time of checkout. Use isolated customer/restaurant/rider/admin contexts and serial conflicting flows.
- A failed retained lifecycle must be resumed on its exact owned ID. Do not create a replacement to bypass financial failure. An audited correction/recovery is not cleanup, but must preserve records and identities and be reviewable before action.

## Deferred and withdrawn scope

- Do not execute intentional waits such as natural OTP expiry, long abandonment/chat grace/quota reset windows. Record them in DEFERRED-WAIT-TESTS.md in each owning feature; implemented tests must be skipped/opt-in in normal execution. Immediate unit/fake-clock boundary checks are allowed; ordinary UI/network asynchronous readiness waits are different.
- Dev rate limiting is relaxed/removed. Ignore rate exhaustion now; record in DEFERRED-RATE-LIMIT-TESTS.md and skip execution. Do not exhaust shared seeded accounts or call routed 429 proof live enforcement.
- SSE is parked at the user's request. Quick Tunnel live-stream validation does not block other flows; do not keep researching/retesting it now. Other ordinary polling/WebSocket behavior can be validated separately.
- Riders deliver assigned packages; item-level food details/checklists are not a requirement. Remove that scope from the plan/tests; do not expand backend item exposure or implement more item UI to satisfy it. The earlier deployed item dialog is historical and not a rider acceptance requirement.

## Reporting, documentation and deployment

- Distinguish local unit/JPA/contract, browser-routed fixture, real deployed UI/API, and authoritative database evidence. Skips, no-op conditionals, missing fixtures and environment blockers are not passes. Approval is not refund completion; balanced capture is not payee settlement.
- Record all encountered issues in CommonMistakesDocumentation. Add reusable architectural/performance/security/coding lessons in CodingPracticesAcrossAllServices. Link the owning plan and handoff. Do not hide an earlier failed run when a repair passes.
- The user requested all operational context in e2e-plan so another agent can continue after usage limits. Maintain this handoff after meaningful changes; do not require the previous chat or task directory to resume.
- Publish/deploy authorization was temporary, through 2026-10-02 06:00 Asia/Kolkata, and has expired. User currently commits/pushes/deploys and confirms. Do not self-publish/deploy now without renewed authorization. Deployment underway can cause failures: mark them and rerun only after completion is confirmed.
- Six CustomerApplication/.agents/workflows files are protected: deploy-one-service.md, deploy-ui-only.md, clean-deploy.md, publish-one-service.md, publish-all-changes.md, publish-ui-only.md. Understand and reuse unchanged when authorized. If a workflow/script change is needed, STOP, explain exactly what and why, and wait for explicit approval. Do not modify them immediately. Clean-deploy is not authorization to wipe the retained Dev database.
- Do not message other agents/people or create new threads without the user's authorization. No subagents were requested for this audit. Do not write separate Codex memory files: this request names the project plan as the handoff destination.


## Mandatory continuation updates — explicit user follow-up, 2026-10-02

Any agent working from this folder must update its results in this folder so other agents remain current. Update the owning feature, CURRENT-STATE, NEXT-STEPS, evidence, inventory/checklists and decisions after meaningful work and before ending/pausing. Root AGENTS.md makes this a standing handoff requirement. Do not keep the only result/decision in a chat, private memory or another task's outputs. DECISIONS.md records the instructions/decisions taken together and must also be updated when they change.


## Bounded continuation and agent switch — 2026-10-02

The user confirmed latest deployment and requested only what fits the roughly 6% remaining usage, with another agent able to continue later. Save a safe checkpoint instead of opening a large state-changing run that cannot be finished. This is a scope/budget instruction, not renewed automatic deployment/workflow modification/cleanup authorization. Checkpoint 17 records the finished read-only deployment verification and exact open recovery work.


## Resumed after usage reset — 2026-10-02

The user explicitly resumed continuing work with a reset usage window, superseding checkpoint17's temporary 6% bounded-pass scope. Continue priority money/refund/chat/admin work; original retained-data, no-duplicate, deferred scope and deployment/workflow constraints remain active. Each completed batch still requires results/decisions in this plan.

## Data reset available. User, 2026-10-02

"Currently nothing is deployed to production. If there's data inconsistency let me know; I'll delete all data and reinitialize with dummy data so that new tests can be executed again." Report inconsistencies to the user instead of writing recovery code for them. This supersedes "Do not reset", "a failed retained lifecycle must be resumed on its exact owned ID" and "an audited correction/recovery" wherever the inconsistency comes from a since-fixed bug. Agents still never reset the data themselves: the user does it.

