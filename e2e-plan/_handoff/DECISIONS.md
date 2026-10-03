# User instructions and joint decisions register

Updated 2026-10-02T17:28:25+05:30. USER-INSTRUCTIONS.md contains the full standing rules; this register preserves decision rationale and reversals. Every continuing agent must append/update decisions here and record results in this plan, as required by ../AGENTS.md. Dates below identify the conversation period; older exact turn timestamps are not inferred.

| Decision / period | Reason and scope | Current consequence |
|---|---|---|
| Dev OTP: admin-only provisioning restriction, 2026-10-01 | The user rejected restricting non-admin random generated registration numbers. A number alone must not grant privileged roles. | Any valid 10-digit non-admin Dev number can use UI autofill; approved active ADMIN provisioning remains restricted. Portal role checks remain authoritative. |
| Dev government autoapproval and mock payments, 2026-10-01 | The user explicitly clarified real government/payment providers are not part of this Dev validation. | Keep normal Dev registration/payment behavior, avoid real providers; do not claim production provider proof. |
| Source-first audit, one feature at a time, 2026-10-01 | Understand folder/source/test structure before reading broadly; repair missing/wrong implementation and plan. | Inspect one feature's source/page objects/tests; maintain exact coverage and open-gap mapping. |
| Retain all test-created data, 2026-10-01 | The user rejected disposable-account/server teardown cleanup. | No delete/deactivate/revoke/reset/restore cleanup. Keep owned manifests. Explicit business actions under test and browser disposal are separate. |
| Idle rider OFFLINE exception, 2026-10-01 | The user approved making a rider offline after work. | Use authoritative idle-rider OFFLINE confirmation; preserve active deliveries, do not cycle an already ONLINE rider. |
| Intentional waits and Dev limits deferred, 2026-10-01 | OTP expiry and other long waits consume the audit budget; Dev limits are relaxed. | Per-feature DEFERRED-WAIT-TESTS/DEFERRED-RATE-LIMIT-TESTS; skip/opt in, never count them passed. Immediate local/fake-clock checks remain allowed. |
| Protected publish/deploy workflows, 2026-10-01 | The user said six workflows already work and any change requires an explanation and approval first. | Do not edit workflows/scripts immediately. Temporary execution authorization ended 2026-10-02 06:00 IST; user now deploys unless renewed. |
| SSE parked, 2026-10-02 | The user requested ignoring the tunnel/SSE obstacle to finish other work. | Do not make public SSE a blocker or keep testing it. Preserve the limitation as deferred. |
| Rider item contents withdrawn, 2026-10-02 | The user clarified a rider's job is assigned package delivery. Earlier item-dialog work exceeded that role. | Remove item visibility/checklist acceptance scope; do not reinstate it or expose extra backend item data. Prior dialog deployment is history. |
| Prioritize chat, refunds, money, order admin, 2026-10-02 | Important flows first, then lesser flows; not every combination across every screen. | Cover distinct meaningful money/business outcomes before low-value exhaustive cases. |
| Reuse canonical happy lifecycle; no duplicate acceptance, 2026-10-02 | The user rejected repeating work and asked how shared fixtures span tests. | Reuse HappyDeliveryFlowTest and owned manifests; combined follow-up assertions; separate orders only for incompatible outcomes. Do not hide failures with replacements. |
| Deployed financial proof is separate from UI approval/local tests, throughout audit | Acceptance/PROCESSING, quoted earnings and routed fixtures do not show real completed money or posting. | Assert exact refund/payment/ledger states, amount/destination/identity/completedAt; explicitly label proof boundaries. |
| Document issues and common practices, 2026-10-01/02 | The user required issues in CommonMistakesDocumentation and reusable lessons in CodingPracticesAcrossAllServices. | Maintain these and link/copy concise notes into the plan handoff. |
| No reset/reseed needed for latest fixes, 2026-10-02 | Deployment fixes future processing but not failed historical records; reset erases retained validation evidence. User asked whether another reset was needed. | Keep current database. Exact historical recovery remains open; no reset/cleanup/financial mutation performed by this checkpoint. |
| Portable context and mandatory agent updates, 2026-10-02 | Usage limits require switching agents without losing results/decisions. The user explicitly requires each later agent to update results in the plan itself. | AGENTS.md governs handoffs; feature statuses, evidence, CURRENT-STATE/NEXT-STEPS/inventory/decisions stay current inside e2e-plan. |

If a user changes one of these decisions, mark its previous scope historical, record the replacement and date, and update affected plan/test instructions. Do not let conflicting old README/checkpoint text silently become active again.

Checkpoint 17 (2026-10-02T17:44:47+05:30): user requested bounded continuation due to roughly 6% remaining and may switch agents. Latest payment deployment verified healthy; no gate pending. Record/verify the handoff; avoid a large unfinished state-changing run. Existing retained-data, priority and deferred decisions remain in force.

Checkpoint18 (2026-10-02T18:13:07+05:30): user resumed after usage reset. Temporary bounded pass is over; continue priority audit. Source choices: exact-ID refund queueing retains refund/business identity and attempt history; no removal of gateway keys/provider reinitiation. Refund UI refresh uses bounded sequential polling while mounted, with fake-clock local checks. Chat page0 newest stable window; current UI sorts chronological. Old-window UI pagination remains open, not claimed complete.

## 2026-10-02T18:27:17+05:30: stored-confirmation recovery design

Independent priority recovery uses the original persisted payment confirmation as evidence, never caller-supplied/fabricated payment data or a duplicate provider action. Exact source/order/gateway/amount/time/balance checks, ADMIN actor, intent locking, existing-capture verification, source-linked reference and posttransaction structured logging govern recovery. Refund initiation/completion and ledger correction remain separate. Existing admin DLQ routing is reused so no protected workflow/config changes are needed. This records the implementation decision, not new permission for cleanup/deploy or proof of live completion.

## 2026-10-02T19:10:00+05:30: checkpoint20 decisions (user)

| Decision | Reason | Consequence |
|---|---|---|
| Delete the stored-confirmation capture-recovery endpoint (reverses the checkpoint19 design) | Nothing is in production. It was repair code for 3 Dev rows, its outbox source data is deleted after 7 days, and it couldn't complete a refund anyway | Files deleted before any commit. Recovery happens by data reset, not code |
| Fix the admin refund retry in both services | The retry was dropped by the gateway's held key and then failed again by the sweeper budget | Gateway releases the key on definitive failure; customer service gets a `sweep_attempts` budget, routes by destination, uses one enqueue helper |
| Reset and reseed Dev data (reverses "No reset/reseed needed" and "resume exact owned IDs" for the 3 fixtures) | The user offered to delete all data once told about inconsistencies | Verify fixes on fresh owned orders after deployment and reset. Retained-data, no-cleanup and no-duplicate-lifecycle rules still apply to new runs |


## 2026-10-02T23:05:00+05:30: checkpoint23 decisions (user)

| Decision | Reason | Consequence |
|---|---|---|
| Delete the unreachable full-order customer support path (UI modal and history button, shared RefundModal, `/api/v1/customer/orders/{id}/refund-request`, gateway route/RBAC) | Rendered nowhere; the no-dead-code rule | Customers raise support refunds only via chat item quotes; full-order refunds remain admin/system actions; the remaining-cap at approval is unit-tested only |
| Add the reverse schema guard to the shared SchemaConsistency | refund_items.amount broke every item refund and no test saw it | Every service schema test now flags required columns no entity writes once CommonLibrary is published |


## 2026-10-02T23:40:00+05:30: checkpoint24 decisions

| Decision | Reason | Consequence |
|---|---|---|
| Never commit or push (user, permanent) | Explicit user instruction for this and all future sessions | Changes stay local; the user commits/pushes/deploys |
| SupportRefundResolutionFlowTest runs on **one** delivered order (`-Dsupport.order.id`), denial → reduced award → refusal; replaces `-Dsupport.partial.order.id`/`-Dsupport.deny.order.id` | The reset erased both fixtures. Source shows only an OPEN ticket blocks a new one (`ChatRefundProcessorService:196`) and a denial consumes no quantity (`sumCompletedQuantity` counts completed refunds), so the outcomes are compatible on one order. "Separate orders only for incompatible outcomes" | One fresh happy lifecycle per run instead of two; the award also asserts the earlier REJECTED ticket is unchanged |

## 2026-10-03T04:40:00+05:30: checkpoint25

| Decision | Reason | Consequence |
|---|---|---|
| Resume the errored fresh lifecycle on its own order (d3acfc93), not a replacement | Standing rule: a failed retained lifecycle is resumed on its exact owned id | Resume passed; the support class ran on it |
| Hide "Something wrong with this order?" whenever the order chat is not offered (agent, local) | It opened nothing after the two-hour window: a silent no-op. Same rule for both, from one function | UI deploy pending; the window itself is unchanged |
| Server-side two-hour support window: **not implemented, asked** | The backend accepts refund chat commands at any age; enforcing it is a product decision | Pending with the user |

## 2026-10-03: support window (user)

| Decision | Reason | Consequence |
|---|---|---|
| Keep the two-hour post-delivery support window UI-only | User decision after checkpoint26 asked | No backend change; do not re-raise. E2E support tests still need an order updated within two hours |

| Decision (2026-10-03, user) | Reason | Consequence |
|---|---|---|
| Add a Dev-mock refund failure seam | Dev mocks always succeed refunds, so admin retry had no live path | Seam lives only in the profile-gated mocks; no production contract or endpoint gains a test flag |
| Restaurant advertiser onboarding: "Start advertising" step (2026-10-03, user) | Campaigns need an advertiser profile and nothing created one | UI looks up `/advertisers/me`; a 404 offers the form (brand name, outlet zone) calling `POST /advertisers`; nothing auto-created. Implemented locally at checkpoint34 |

## Business Platform — 2026-10-03T15:50:36+05:30

Implementation follows supplied dependency/release order. O1 local only pending checkpoint34 rollout and O1 live gate; no permission to skip prerequisites inferred. D15 60 s bounded operational stale access implemented for review, not confirmed. D3/D4/D6/D13 remain unconfirmed. UI consistency belongs to O5/W3/A4.

## Publishing and deployment authorized through Business Platform completion — 2026-10-03T16:57:40+05:30

Direct user request: investigate restarting bidding-engine/event-tracking-service; make services
production ready. User authorizes publishing and deployment as required until the current work is
completely done. This supersedes the owner-only publish/deploy restriction for this task. Use existing
protected workflows/scripts unchanged. The permanent never-commit/push instruction has not been
explicitly revoked. No DB reset/seed/cleanup authorization inferred. Product defaults D3/D4/D6/D13/D15
remain unconfirmed; a bundled clarification is pending while the startup incident is being fixed.

## Product defaults confirmed — 2026-10-03T16:59:55+05:30

Owner confirmed D3/D4/D6/D13/D15. The authoritative dated choices are in
[BusinessPlatform DECISIONS.md](../../../RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md),
beside the plan as explicitly requested. This supersedes checkpoint35/incident statements that those
choices were pending. Do not keep product decisions only in this E2E register or the chat.

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

### 2026-10-03T18:37:44+05:30 — O2 schema and denied-access choices

Both restaurant ownership and outlet-entity chat require fresh Dev schema/data. Use the already authorised clean --wipe + current organisation seeds only after local gates pass; no legacy owner inference/backfill. Do not treat this as an Oracle hosting migration. Keep the prior earnings denial until a successful server response, including while retrying, so cached financial rows cannot reappear. Initialise registration loading in the opening action, load every page, fail incomplete roles/pagination explicitly, and preserve server authority for permission checks. O2 E2E covers the required staff lifecycle and public internal-route rejection; compile-only remains unverified live. No wipe/migration deployed yet.

### 2026-10-03T18:40:34+05:30 — Owner clarification: Dev-only fresh recreation

Owner confirms nothing is in production; all current application data is Dev and may be completely deleted and recreated using dummy seeds. No legacy data transfer, preservation or backfill is required. Use the already-authorised unchanged clean-deploy --wipe followed by updated dummy seeds for this organisation transition. Update schema definitions needed by the new code and dummy organisation/member/brand relationships; Flyway SQL runs during fresh database recreation (ddl-auto=validate), not a migration of existing business data. Do not add production-data conversion or infer old owners. The two existing O2 SQL files only establish required schema fields/constraints on fresh Dev databases; they contain no data backfill. Earlier retained fixture evidence remains historical after the authorised wipe. No wipe has run yet.

### 2026-10-03T18:41:50+05:30 — Persistent Dev policy requested

Owner explicitly requests this Dev-only disposable-data/no-legacy-migration guidance be remembered permanently until they ask to forget it or report deployment to production. Persistent note saved under Codex memories/extensions/ad_hoc/notes/2026-10-03T18-41-39-food-delivery-dev-data-recreation.md. That future production report ends the disposable-data assumption and requires reassessing preservation/migration/deployment before destructive actions. This applies across the Food Delivery workspace, including the active Business Platform plan.

### 2026-10-03T18:59:24+05:30 — O2 contract and UI proof choices

CreateSessionRequest accepts only orderId because authoritative roster lookup supplies participants; ParticipantDto requires entityId/type, not an employee userId for restaurants. Correct only these obsolete strict-schema expectations; preserve unrelated ratchets and report their failures. Optional media sender hints are checked before upload and forwarded for authoritative sender attribution; offline audio retains its original entity hint. New isolated consumer contexts register the required Feign fallback but assert it was not called, so stubs prove actual DTO/list transport. Use explicit positive-path assertions in role break-tests and preserve the initial error invocation. UI full suite uses four workers after the unchanged dialog-animation assertion timed out under unrestricted concurrency; isolated rerun passed, full782 passed, original failure retained. Registration inputs now have proper labels and its actual API write tests prove no duplicate organisation on retry.

### 2026-10-03T19:00:55+05:30 — Safe organisation brand listings

ORG_VIEW now admits outlet staff to brand lists; the legacy raw Brand response includes bank account, PAN/GSTIN and document-related fields. UI uses only display/ownership/status metadata. Return a dedicated safe brand summary for the membership list so expanding access does not expose bank/document identifiers. Keep the separate business-application write/verification permissions. This is an O2 access-boundary correction; O3 still owns the full application/document model.


### 2026-10-03T19:49:03+05:30 — Owner requests appropriate migration removal

Owner asks to remove migration code and explicitly cautions against blindly deleting files or causing SQL failures. Decision: use the final initial schema for disposable Dev data, consolidate incremental DDL and remove historical conversion/backfill/duplicate-chat merge code only after inspecting dependencies. Twenty incremental SQL files in CommonLibrary, IdentityService, RestaurantApplication, CommunicationService and CommunicationIntegration are folded into their initial schema resources. Keep required database creation, table definitions, column nullability, indexes/checks and append-only audit triggers. Flyway remains the schema bootstrap runner; its resource-folder name does not imply transferring old business data. Other repositories' existing schema history is retained until reviewed with their owning phase; no claim that every workspace SQL history file was deleted.

All four full fresh service schemas execute successfully in an isolated disposable PostgreSQL/PostGIS unit database. Current baseline and scenario Identity/Restaurant seeds load successfully. Thirteen additional SQL constraint guards pass. Shared clean install251/0/0/0; real PostgreSQL fresh-schema Identity1/0/0/0 and Restaurant1/0/0/0. A first non-clean Identity test errored on stale copied SQL in target/classes; clean rebuild removed that resource and passed. Do not suppress Flyway validation or repair history to hide this.

Because the shared applied baseline/checksums changed, rebuild and publish every affected consumer image from clean output and use the authorised **full clean-deploy --wipe** workflow before seeding. Existing Oracle volumes must not receive these consolidated schemas through a normal one-service deploy. Protected workflow files remain unchanged. Oracle is unchanged; no wipe/reseed/deploy has run in this batch. Fresh-schema guards replace legacy conversion tests. This choice supersedes the plan's old forward-only/no-baseline-edits rule while the owner's Dev-only policy applies.


### 2026-10-03T19:50:18+05:30 — Publishing precedes deployment

Owner reiterates that publishing must happen before deployment. Required order: validate source and clean outputs; publish CommonLibrary and the current producer stubs; clean-build/publish the dependent service and UI images; verify registry tags and record them in Deployment; execute the appropriate existing deployment workflow (full authorised wipe for consolidated baseline changes); seed fresh Dev data; inspect every service log and run live regressions. Configuration goes through the existing Config Server bundle workflow before application consumers start. Do not deploy stale/unpublished images or treat successful local tests as publication. No cleanup changes have been published or deployed yet.

## 2026-10-03T20:21:43+05:30 — O2 release choices

Use the safe BrandSummaryDto for all member brand lists and legacy brand event updates; keep application secrets out of those views and show KYC/bank verification status in settings. Preserve intentional Dev Autofill header capability reads. Correct stale source guards to current contracts and typed UI calls, with positive and broken controls; restore Lombok-generated model accessors where actual boilerplate violated the code rule. Allow only exact reviewed initial SQL hashes and absent retired paths under DEV-SCHEMA-RECREATION.json, with PostgreSQL proof and mandatory clean --wipe; production declaration removes this exception. Publish Common37f6854 via existing Packages workflow (run37130225264success). Build all shared consumers clean in two artifact/test passes, keep ONDC compile-only, then use unchanged local publication before full Dev wipe/seed and public Oracle gates. Do not run generated UI writes concurrently with tests/typecheck.

## 2026-10-03T20:25:58+05:30 — Signing test environment isolation

The full clean graph exposes an existing environment-binding test collision with the parent Maven test-only HMAC property. Remove systemProperties only from that fixture Spring Environment so it exercises IDENTITY_HMAC_SECRET fallback. Keep runtime signing configuration and keys unchanged. First fleet pass2 had signing8/1/0/0 and skipped dependents, so it is a failed invocation; rerun the full clean graph after the focused guards pass. No deployment yet.

## 2026-10-03T20:38:03+05:30 — Final graph and consistency choices

Full restored clean backend graph1877reported cases,1876executed passes,0failures/errors;1explicitly disabled future ONDC delivery-status contract remains parked. All26generated producer classes have reports. All21Java Compose artifacts are fresh; removed SQL resources absent. Restaurant final actual OpenAPI1guard, fleet map2existing rendered tests pass. Timezone26/26 with26positive and77negative controls; the multiline @Table parser now reads the actual table-name line, avoiding a false missing payout_operations.created_at finding. Use shared Select for fleet city and its visible selected value/listbox interactions. Set a4-worker Vitest budget consistent with the completed785-test runs to avoid unbounded resource contention; no scenarios removed. Record Common published commit in Deployment/published-libraries.json so shared SQL has publication protection even though a library has no Compose tag; core20positive/broken controls cover missing/unknown publication and unreviewed shared SQL after production declaration. Final UI build and full required UI gates underway; publication/deploy/wipe/live remain pending.


## 2026-10-03T20:53:03+05:30 — O2 payout privacy and UI consistency

All14 current producer-stub publication runs succeeded (exact runs in39-contract-publication-runs.json). Earnings readers could receive a full bank beneficiary snapshot from payout list/detail responses. Red guard3tests/2failures/0errors/0skips proves both disclosures. Decision: retain amounts/status/lines for earnings readers but require PAYOUTS_MANAGE to return bank beneficiary details; redact the mapped DTO, never the persisted payout. Resolve this permission once per request. Owners/admins with payout management retain the details. Restored focused3/0/0/0 passes; clean Ledger release verification underway.

UI phase2 full15/15 passed. Phase3 caught two structural regressions: customer menu screen size and role branching in the shared chat widget. Extract the cohesive restaurant review button with unchanged rendered behavior and participant selection into the communication domain model; typecheck and existing component5tests pass. Final build and full phase3/4/5 gates still pending. No O2 image publication, clean wipe or live fixture has run yet.


## 2026-10-03T20:56:23+05:30 — Shared chat policy consistency

Phase3 full12/13: menu size corrected and typecheck/lint/full test pass, but the shared widget still derives a CUSTOMER refund capability inline. Centralise that capability alongside participant selection and reuse it in the actual send guard so there is one domain rule. Preserve server authorisation and user-visible behavior. The failed phase3 invocation is39-ui-phase3-first-restored.txt; final rebuilt/restored gates remain pending.


## 2026-10-03T21:01:55+05:30 — Exact contract scope and UI phase boundary

All12 existing consumer workflows succeeded against current published stubs; Identity and Tracking have producer publication only and no contract-tests.yml. An attempted Identity consumer dispatch returned404 and started nothing; corrected dispatch follows the existing orchestrator's source-file inventory, with exact12run IDs/heads in39-consumer-contract-runs.json. Latest Ledger producer37133191391 succeeded at0aca2ee. No workflow or visibility change.

Full UI phase3 restored13/13. Phase4 structural probe fails the component-size check with12files, including11pre-existing oversized screens and the new organisation-registration expansion. Decision: extract registration ownership loading/selection/create/retry into a cohesive hook now, preserving rendered controls and the disabled guard; final full typecheck/lint/test and rebuilt dist are required before O2 publication. The first hook typecheck caught the submit guard still referencing moved state; its exact equivalent now comes from the hook. Carry the11pre-existing phase4 size findings to O3/O5's explicit all-redesign-gates work, without exceptions or claimed green status. O2's own validation requires typecheck/lint/vitest; those must be current and green, along with all backend/contract/security/live gates. Phase5 source14/14 passes, full phase5 remains unverified. No image publication/deploy/wipe yet.


## 2026-10-03T21:04:40+05:30 — O2 publication gate

Local O2 static13/13 and core56/56 are green. Latest UI full785/785,0failures/skips; typecheck/lint clean; final validation-schema relocation receives a rebuilt dist, typecheck/lint and existing registration3-case rerun before image publication. All14 producer workflows plus latest Ledger privacy republish succeeded; all12 actual consumer workflows succeeded at exact current heads. Latest Ledger clean223/0/0/0. All21 Java Compose artifacts are fresh. Six protected workflow hashes unchanged. All18 Oracle application profiles verified exclusively Dev. Deployment config/seeds/library provenance03eede1 pushed, preserving unrelated DEPLOY_LOG.

Next: commit/push tested UI; existing unchanged native publish.sh --all; verify each current registry tag/digest; commit/push env tags; existing fullclean --wipe with authorized WIPE acknowledgement; fresh dummy-data.sh --load-only; inspect every log, hardening/reconcile, then public Oracle O2 and required lifecycle/chat/nonzero-earnings/latency proof. No O2 image/deployment/wipe has run yet; O3+ not started. Eleven older oversized UI components remain a source gap for O3/O5's full redesign gates; no line-count exceptions added. No production-readiness completion claim.


## 2026-10-03T21:09:39+05:30 — Publication prerequisite and locator proof

First native image publication aborted before building/pushing any image: local Docker daemon socket absent. Start the existing Docker Desktop builder and retry the unchanged publisher; Oracle has not changed. UI0866c00 and Deployment03eede1 are pushed. Latest UI785/0/0/0 full test run, final pure validation extraction rebuilt/typechecked/linted and rendered registration3/0/0/0 passed.

Focused locator audits for O2 access, O1 lifecycle and earnings all pass:6/3/4static PASS respectively,0FAIL/DEAD/EXCEPTED,3/0/4dynamic runtime-only sites. Restrict stale exception checking to selected suite paths when --only is used; full audits still check all exceptions. A deliberately stale O2-selected exception makes the real audit fail and original JSON is restored. Earnings now selects tbody rows of the accessible Account statement table and excludes its empty placeholder, retaining signed-amount parsing and full balance assertions. E2E final test-compile passed. A profile preflight typo used an absent governmentid-service name, causing a read-only failure; corrected inventory from service-map.tsv verifies all18 actual application profiles exclusively Dev. Do not guess deployed service names.
