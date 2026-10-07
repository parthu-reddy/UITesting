# Primary user instructions and accepted decisions

## 2026-10-07T12:05+05:30 — Resume after usage limit; fix mistakes

The owner asked this session to check the previous agent's progress after its usage-limit stop and
resume the work, adding: "If there are mistakes fix them". Applied as follows. The unrecorded
invocations 3 and 4 were recorded and corrected. The owned order ce254f3a was resumed and verified
rather than replaced. Rider UI defects found in its live captures were fixed locally with red-first
tests. Deployment remains the owner's, so the UI-only deploy is pending; nothing was committed or pushed.

## 2026-10-07T11:33:21+05:30 — Owner confirms UI-only deployment

Direct owner: “I've deployed UI alone for now”. Served UI6eb4ef6 was independently verified from the
actual CI asset during normal rider login. Proceed with authorized item3 E2E; no backend deployment
or agent commit/push is inferred. Exact approved ticket47ffbccd is now persisted REJECTED on6fbe0289;
the successful resume made0resolution writes. Do not repeat its writer to obtain fresh screenshots.

## 2026-10-07T11:16+05:30 — Additional UI deployment and owned dummy refund fixture approved

Owner answered the active-state UI handoff “I'll deploy the UI now” and the exact fixture plan
“Yes, create and reject the owned dummy ticket”. Authorization is one normal UI support request and
final rejection on retained delivered Dev order6fbe0289-645e-4f1c-ae0d-caab21689070, customer8000000001,
owner9000000001/rider7000000001, following
[ITEM-3-ADMIN-FIXTURE](../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ADMIN-FIXTURE.md).
Approval confirmation may be opened and cancelled; never approve or pay a refund. Verify quote,
matching ticket, rejection response/audit and absence of customer refund records; preserve all data.
Only owner commits/builds/deploys UI; no renewed agent deployment/cleanup authority. New UI head
6eb4ef6 is publishing through owner GitHub37577846909; intent/publication is not live confirmation.
M1/A5b remains paused until item3 closes. Do not ask again for this exact fixture authorization.

## 2026-10-07T10:35+05:30 — Owner UI deployment in progress

Owner answered the concrete item3 deployment handoff: “I'll deploy the UI now.” Scope is
FoodDeliveryAppUI rider canvas/sidebar labels only. This is intent, not live confirmation; rerun
the final strengthened RoleVisualAuditUiTest after actual confirmation. Maps/ETA stays paused.
No change to the standing owner-managed deployment or local-only work rule.

## 2026-10-07T10:14+05:30 — Sequential work, production-ready UX and E2E

Direct owner instructions: "remember that I want production ready code with best UI for best UX.
Try creating a plan for one work at a time and fix one by one." Follow-up: "Do not forget to
include E2E tests as well". The sequential plan is [PendingWork_2026-10-07](../../../RandomDocuments/PendingWork_2026-10-07/README.md).
Only one active item; finish its appropriate checks and evidence before starting the next. Existing
parallel tests may finish safely; maps/ETA implementation is paused until its turn. Functional
completion requires meaningful deployed UI E2E, with local-only/deployment-waiting gaps kept explicit.

Further owner clarification: mark already-fixed items fixed; never assume, always inspect code,
and learn application flows from existing E2E tests. Status closure must name source/evidence,
and new test design should reuse the real established UI journey and prerequisite checks.

## 2026-10-07T10:10+05:30 — Resume screenshot pending work

Direct owner request: "I want you to start working on these pending works and complete them",
with the screenshot listing business-platform paperwork, existing uncommitted changes,
restaurant harness follow-ups and the older UI redesign backlog. This resumes the ordinary pending
work beyond the prior stop boundary. Verify source and current evidence before closing stale items.
The latest owner-managed build/deploy and local-only changes rule remains: no commit, push,
publication or deployment is inferred from this request. Keep explicitly parked design/measurement/
CSP/SSE/duration/rate cases separate unless the owner changes their scope. No local servers,
Docker, direct-state E2E or browser-state injection; live checks use Dev Autofill and visible UI controls.
Root coordinates current handoff records; parallel work owns paperwork, restaurant harness and maps/ETA.

## 2026-10-05T14:05:00+05:30 — O1–O5 production-readiness review; nothing runs locally

Direct owner instructions (2026-10-05): review everything completed so far (O1–O5) and make it production ready with the best UI/UX. "The trycloudflare address is for dev purpose, don't worry about using it. When you make changes and want to deploy any service let me know, I'll build and deploy them. Don't do anything locally as nothing is running locally." Learn flows from these E2E tests rather than re-reading all UI source; respect this folder's deferred-test categories.

Choice: no local servers/mocks (a scratch mocked UI on localhost was started once, then stopped and removed at the owner's request). Unit/contract suites still run locally. Product and harness edits stay local and uncommitted; the owner builds and deploys, then the O4/O5 runner verifies on the Dev tunnel. Signing in on the tunnel needs an OTP, which the agent will not type into a non-local site; the existing harness Dev-autofill flow or an owner sign-in in the browser pane is used instead. Plan: `RandomDocuments/BusinessPlatformReview_2026-10-05/README.md`.

## 2026-10-05T12:05:12+05:30 — Current scope fulfilled; stop after O4/O5

The owner's final scope is to complete Phase4 and Phase5, then stop. Both are now published, deployed and accepted within the documented Dev/UI-only scope; the remaining documentation publication completes this handoff. Existing task authorization covered the required commits, pushes, GitHub publication and Oracle Dev rollout. Preserve the UI-only/H2/GitHub-only rules, retained fixtures and explicit unverified deferrals. Wallet/Ads and the broader E2E audit require a later user instruction; no new tests, deployment, wipe or seed is needed for this final documentation commit. Earlier status statements below describe historical checkpoints.


## 2026-10-04T06:22:54+05:30 — Owner requires embedded H2 and UI-only E2E

Direct owner instructions: "No never do that 'disposable databases on Oracle' tests should never depend on DBs directly, why no use H2?" and "All E2E tests should only go through UI, no direct backend db or redis connections should be made for now."

Choice: no Docker test fixtures, no external database test fixtures, and no direct DB/Redis access from E2E, including read-only allocation/audit queries. Use embedded H2 for service persistence/rollback/concurrency assertions and documented mocked infrastructure for minimal unit/contract/startup/OpenAPI contexts. E2E business setup/actions/assertions go through real UI controls; observe only the requests/responses those controls produce. Do not use browser fetch/API clients to bypass the UI. Keep PostgreSQL/PostGIS runtime behavior separately unverified until the deployed UI exercises it. This temporary UI-only policy remains in force until the owner changes it.

The earlier Oracle disposable-database choice and unpublished native-PostGIS CI proposal are superseded. The owned SSH database tunnel was closed. Both external database helpers were removed; the same five Delivery concurrency/rollback/suspension cases now pass on embedded H2. Restaurant embedded H2 visibility/concurrency plus compiled-query structural guards pass15/0/0/0. The failed first Restaurant H2 compilation is retained (zero executed tests; wrong DTO package corrected). Prior Oracle/PostGIS results remain historical evidence, not current test setup authority. O3 browser fixture/runner rewrites are in progress and have not run; do not mark deployed E2E complete. Image builds remain GitHub-only and publish must succeed before deployment.


## 2026-10-04T05:58:10+05:30 — Owner requires GitHub-only image builds

Direct user instruction: "never use docker, only use github workflows to build images for deployment". Effective immediately, never start or use local Docker and never build/publish deployment images through local or ad-hoc Docker commands. Build deployment images only through the affected repositories GitHub Actions build-and-push workflows, using the existing CI publication path. Required commits/pushes remain authorised. Publication must finish successfully and exact image tags must be recorded before the existing authorised Oracle deployment path runs. Do not choose the local Docker fast path even where an older workflow recommends it. No Docker was started in response to this instruction; the current process check found only the pre-existing network helper.

O3 is incomplete. Keep release/live-E2E/measurements boxes unchecked until the exact proof passes; mark implementation-only items only after current source and local evidence are reviewed. Do not equate prepared/compiled E2E with executed Oracle proof.


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


## Never commit or push. User, 2026-10-02T23:40+05:30 (permanent)

"Never commit and push changes, always keep changes locally. Remember this permanently for any future sessions as well." Agents leave every change in the local working tree. The user commits, pushes, publishes and deploys, then confirms. This holds for every future session and agent, alongside the deployment restrictions above.

## Continuation after the checkpoint23 deployment. User, 2026-10-02

The user handed the work to a new agent with the checkpoint23 screenshot ("Deploy customer-service, the gateway config and the UI. Publish CommonLibrary. After that I'll rerun the support-refund test") and asked it to continue from this handoff. Reaffirmed: never assume, always check the code; the goal is a production-ready application; use CommonMistakesDocumentation and CodingPracticesAcrossAllServices to take decisions.

## Two-hour support window stays UI-only. User, 2026-10-03

"Keep the 2-hour window UI-only, continue the priority list." Do not add a server-side support window. The backend keeps accepting chat refund quotes and requests on a delivered order of any age; the customer UI offers the chat and its entry point for two hours after the order's last update (`isOrderChatOffered`).

## Dev-mock refund failure seam. User, 2026-10-03

Asked how to prove the admin refund retry live, the user chose "Add a Dev-mock failure seam": the Dev-only mock gateways fail the first attempt of a deliberately marked refund and succeed on retry, so one live E2E can run fail → admin retry → money returned across customer-service, Kafka and payment-gateway. Mocks stay excluded from the prod profile.

## Restaurant advertiser onboarding. User, 2026-10-03

Asked how a restaurant becomes an advertiser, the user chose the "Start advertising" step: the Campaigns tab looks up the owner's advertiser (`GET /api/v1/advertisers/me`); if there is none it offers a short form (company name from the brand, time zone from the outlet) that calls the existing `POST /api/v1/advertisers`, then loads campaigns. Nothing is created without the owner's action; no backend auto-provisioning.

## Business platform plan. User, 2026-10-03

The owner asked for an implementation plan (no implementation yet) for organisations (groups of users
owning businesses), one login with a Google-style portal switcher where any user reaches a portal once
the business/service behind it is verified and approved, a business wallet per organisation (separate
from the personal customer wallet, by the owner's choice), and a separate Ads Manager portal for any
business type. The plan, with E2E gates per phase (explicitly requested), is
`RandomDocuments/BusinessPlatform_2026-10-03/`. It supersedes the restaurant-portal Campaigns tab from
checkpoint 34 at its phase A4; checkpoint 34 should still be deployed first. Five decisions there are
marked CONFIRM for the owner (D3, D4, D6, D13, D15). The plan also carries cross-cutting security,
performance and observability requirements (`SECURITY-AND-PERFORMANCE.md`), added at the owner's request.

## Business Platform — 2026-10-03T15:50:36+05:30

Direct user request: understand every line of RandomDocuments/BusinessPlatform_2026-10-03 and finish its application production-ready with UI/UX consistent with the current application. Follow the supplied phase release gates and existing owner-only commit/publish/deploy rules. This request does not confirm the five marked owner design choices.

## Publishing and deployment authorized through Business Platform completion — 2026-10-03T16:57:40+05:30

Direct user request: investigate restarting bidding-engine/event-tracking-service; make services
production ready. User authorizes publishing and deployment as required until the current work is
completely done. This supersedes the owner-only publish/deploy restriction for this task. Use existing
protected workflows/scripts unchanged. The permanent never-commit/push instruction has not been
explicitly revoked. No DB reset/seed/cleanup authorization inferred. Product defaults D3/D4/D6/D13/D15
remain unconfirmed; a bundled clarification is pending while the startup incident is being fixed.

## Required publishing commits/pushes authorized — 2026-10-03T17:04:49+05:30

Owner explicitly authorized the commits and pushes required by the publishing workflows for this
Business Platform task. This is a task-specific override of the standing no-commit/no-push rule, lasting
until this work is complete. Commit/push only reviewed changes needed for this work; preserve unrelated
user changes. Existing protected workflows/scripts remain unchanged. Publishing/deployment is also
authorized for this duration; Dev data reset/seed/cleanup is not authorized by this choice.

## Dev seed-only authorization — 2026-10-03T17:13:03+05:30

Owner explicitly authorized the existing Dev seed workflow **without reset** after deployed Identity and restaurant tables were found empty. Use Deployment/dummy-data.sh --load-only, preserve records, no Redis flush or cleanup. Verify current relationships and browser login afterward. This supersedes the no-seed rule only for this restoration. Authoritative dated choices remain in RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md.

## Clean wipe/reseed authorized — 2026-10-03

Owner permits clean-deploy --wipe followed by fresh seeding for this Business Platform task. This supersedes the previous no-reset rule within this task; use when the reviewed phase rollout requires schema recreation, not for a configuration fix. Existing protected workflow scripts remain unchanged. Decisions and Dev phone policy review are authoritative beside the plan in DECISIONS.md. Non-admin Dev autofill supports any ten-digit phone; only provisioned test admins are allowlisted.

## Firebase preference confirmed — 2026-10-03

Owner confirmed mock push in Dev, Firebase credentials required in production. Record is authoritative
in RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md. Existing Dev image follows this; no real
Firebase/provider delivery claim from a mocked audit. No pending owner answer for this preference.

### 2026-10-03T18:40:34+05:30 — Owner clarification: Dev-only fresh recreation

Owner confirms nothing is in production; all current application data is Dev and may be completely deleted and recreated using dummy seeds. No legacy data transfer, preservation or backfill is required. Use the already-authorised unchanged clean-deploy --wipe followed by updated dummy seeds for this organisation transition. Update schema definitions needed by the new code and dummy organisation/member/brand relationships; Flyway SQL runs during fresh database recreation (ddl-auto=validate), not a migration of existing business data. Do not add production-data conversion or infer old owners. The two existing O2 SQL files only establish required schema fields/constraints on fresh Dev databases; they contain no data backfill. Earlier retained fixture evidence remains historical after the authorised wipe. No wipe has run yet.

### 2026-10-03T18:41:50+05:30 — Persistent Dev policy requested

Owner explicitly requests this Dev-only disposable-data/no-legacy-migration guidance be remembered permanently until they ask to forget it or report deployment to production. Persistent note saved under Codex memories/extensions/ad_hoc/notes/2026-10-03T18-41-39-food-delivery-dev-data-recreation.md. That future production report ends the disposable-data assumption and requires reassessing preservation/migration/deployment before destructive actions. This applies across the Food Delivery workspace, including the active Business Platform plan.


## Migration cleanup with SQL correctness — 2026-10-03T19:49:03+05:30

Direct owner request: remove migration code, taking appropriate decisions rather than blindly removing files. Preserve required schema creation and constraints; consolidate only inspected definitions and verify fresh schema plus seeds before retirement. Dev-only policy remains in force. Full wipe, clean builds, republishing affected shared-library consumers and fresh seeding are prerequisites to rollout of changed baselines; no Flyway-repair workaround. Current scoped removal and proof are in checkpoint38 and the Business Platform DECISIONS.md.


### 2026-10-03T19:50:18+05:30 — Publishing precedes deployment

Owner reiterates that publishing must happen before deployment. Required order: validate source and clean outputs; publish CommonLibrary and the current producer stubs; clean-build/publish the dependent service and UI images; verify registry tags and record them in Deployment; execute the appropriate existing deployment workflow (full authorised wipe for consolidated baseline changes); seed fresh Dev data; inspect every service log and run live regressions. Configuration goes through the existing Config Server bundle workflow before application consumers start. Do not deploy stale/unpublished images or treat successful local tests as publication. No cleanup changes have been published or deployed yet.

## 2026-10-04T07:03:09+05:30 — UI-only means no browser-state injection

For the active O3 work, do not use `localStorage`, cookies, indexed storage, a handcrafted request,
or a direct page URL to create or assert product state. Login through the normal rendered Dev
Autofill control, then perform every business step through visible controls. A click-generated
network response may be observed only to synchronize/assert that same rendered user action. Keep
all direct endpoint/IDOR, stale-state, direct measurement and infrastructure reads in the feature's
deferred inventory. This adds detail to the 06:22 UI-only instruction and remains effective until
the owner changes it.

## 2026-10-04T09:37:14.676791+05:30 — Current task authorisation and 300-line owner deferral

The owner's current Business Platform task permits required commits/pushes, GitHub image publication and Oracle Dev deployment, with authorised clean wipe/fresh dummy seed. This supersedes earlier expired audit-only deployment limits for this task. Images use GitHub Actions only, and E2E uses visible deployed UI controls only. Latest instruction: finish current work; owner will complete the 300-line rule later. Leave unchanged legacy refactors deferred.

## 2026-10-04T10:23:56.486302+05:30 — Current continuation and required sign-in

Owner says continue and finish the current work. Standing task-specific commits/pushes, GitHub publication and Oracle Dev deployment authorisation remains active; do not re-request those permissions. No local Docker, direct API/DB/Redis or state injection in E2E; old-code/300-line cleanup stays deferred. The live bucket-administration attempt returned403; the browser is at Cloudflare sign-in and a user sign-in question is pending. No bucket policy changed and no replacement credentials were created. Continue after actual access is available; do not treat elapsed time as an answer or invent an E2E pass.

## 2026-10-04T16:11:29.048510+05:30 — Legacy scope explicitly reopened

Direct owner answer: "Include legacy search and 300-line cleanup." This supersedes the earlier no-old-code and component cleanup exclusions for these two items. Existing task commits/pushes, GitHub publication before Oracle Dev deployment remain authorized. All E2E stays visible UI only; no local Docker or external database test fixtures. Other wait/rate/UI-only deferrals retain their recorded scope. Record dated decisions beside the Business Platform plan.

### 2026-10-04T18:58:39+05:30 — task scope retained

The owner included legacy search and the 300-line cleanup; checkpoint71 verifies both on Oracle. Required task commits/pushes, publish-before-deploy and full Dev fresh wipe/seed remain authorised. GitHub builds images; no local Docker. H2 backend fixtures and rendered-UI-only E2E remain binding. O4/O5 are combined only to provide permissible UI membership setup.

## 2026-10-04T19:36:11+05:30 — Resume and O4/O5 scope/fixture choices

Owner requested a brief pause, then resume; task-specific commits/pushes, unchanged GitHub publication and Oracle Dev clean wipe/fresh seeding remain authorised. Publish before deploy; never local Docker; embedded H2 service tests and rendered UI-only E2E remain binding. Use existing restaurant-brand Suspend/Reinstate controls for the original O4 suspension gate; remove the accidental extra admin organisation screen. Preserve the calling everyday SID in admin step-up replacement. Restaurant launcher state belongs to the selected organisation, with a volatile per-person context fallback if persistence is disabled. The E2E URL is normalised without a trailing slash; fresh phone candidates are retained locally and confirmed through normal profile UI, never allocated/audited through a database.

## 2026-10-04T20:25:44+05:30 — Owner stop boundary: complete O4 and O5 only

Direct owner instruction: "sure complete those and stop after phase 4 and phase 5 are completed."

Choice: finish the combined O4/O5 source, unchanged GitHub publication/contract workflows, authorised Oracle Dev fresh wipe/seeding/deployment, UI-only E2E/regressions, measurements and accurate checklists. Stop once both phases meet the current acceptance scope. Do not start W1–W3 or A1–A4. Standing task commits/pushes and publish-before-deploy authorisation remain applicable to this completion; no local Docker or direct-state/API E2E. Explicit existing wait/rate/SSE/internal proof deferrals stay visible and are never counted as passes.
