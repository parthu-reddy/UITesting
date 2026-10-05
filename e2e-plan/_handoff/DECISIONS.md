# User instructions and joint decisions register

## 2026-10-05T14:05:00+05:30 — Review decisions D-R1..D-R4 (owner, all recommended)

D-R1 access JWT 15 minutes (`Deployment/identity-service.yml jwt.expiration`) plus UI background renewal; chat/call/rider sockets key on person+session so renewal does not reconnect them. D-R2 invitation SMS (Identity outbox event + CommunicationIntegration template) plus in-app launcher/hub surface. D-R3 behaviour-neutral readability reformat of O1–O5 files across eight repos, last. D-R4 restaurant bank account masked to last four after save. The 15-minute live idle case is deferred (sessions-and-role-access/DEFERRED-WAIT-TESTS.md); real SMS delivery is a deferred production-provider check. Gate: `RandomDocuments/BusinessPlatformReview_2026-10-05/tools/validate_review.py` (baseline 0/20).

## 2026-10-05T12:02:56+05:30 — Final documentation publication and diagnostic retention

Choice: commit the bounded O4/O5 plan, checkpoint, retained-fixture and count/state evidence files (.md/.json/.txt), together with the two selected shared lesson documents. Keep raw XML/SQL/log files and Python caches local and unstaged. Historical Maven text diagnostics are published as bounded assertion/build/count excerpts, with full originals retained locally as unstaged logs. The old presigner negative-control portable excerpt redacts test-only signing credential/signature values while preserving signed headers and the original failed assertion/count; the full original log is retained locally. Both final phase checklists are complete within the documented Dev/UI-only scope; explicit deferrals remain unverified. Stop after these documentation commits. No later phase, new test run, reset or deployment is part of this final handoff.


## 2026-10-04T10:15:43.962704+05:30 — Initial O3 timing evidence

Admin queue navigation1/1 passed. Both server queue buckets are under300ms, but browser queue p95 is353.948ms (n4) and Restaurant status bucket447.392ms exceeds150ms (n6). Delivery status n1 is111.848ms. Small samples, no load claim. Evidence59-o3-initial-measurements.json; final measurements stay open. Collect after completed normal journeys; no API warm-up loop. Uploads await user Cloudflare sign-in, application CORS admin403.


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


## 2026-10-03T21:25:00+05:30 — Checkpoint40: published, verified and ready for fresh Dev rollout

All22 clean committed service/UI images exist in OCIR as linux/arm64, with immutable digests recorded. Current Deployment c9deb85 contains tags/config/seeds, preserving unrelated DEPLOY_LOG. All18 Oracle app profiles verified exclusively Dev. Decision: run the already authorised unchanged full --wipe workflow, then load-only seeds into healthy fresh schemas. No legacy migration/backfill/repair. No wipe/deploy/live O2 proof yet; all O2live/SQL/performance gates remain open. O3+ unstarted; full platform incomplete. See checkpoint40 for exact source/evidence/continuation; older dated entries are history.


## 2026-10-03T21:26:29+05:30 — Owner-authorised full Dev wipe started

Registry22/22 linux/arm64 verified; Deployment c9deb85 pushed; core56/56 remains green against current published tags. Started the unchanged03_clean_deploy.sh --wipe on Oracle after all18application profiles verified Dev. Explicit WIPE acknowledgement supplied to its TTY prompt under the owner's prior task-specific permission. No prompt was piped past and no workflow edited. Old Dev fixtures become historical as volumes are removed; fresh post-wipe fixtures must be recorded independently. Deployment/healthy schemas, load-only seed, all29container logs/restarts/digests/config, O2live/financial/regression/performance gates remain pending. Never claim wipe means rollout success.


## 2026-10-03T21:41:50+05:30 — Shared tracing configuration after fresh Dev rollout

The unchanged authorised full `03_clean_deploy.sh --wipe` completed with exit0. All29 declared containers run on their intended linux/arm64 images, with zero automatic restarts. Fresh seeds have not been loaded yet. Eleven application services log OTLP connection errors because shared application.yml reads OTLP_ENDPOINT with a localhost default while Compose supplies OTEL_EXPORTER_OTLP_ENDPOINT for deployed Jaeger. Decision: point the shared endpoint at `${OTEL_EXPORTER_OTLP_ENDPOINT:http://jaeger:4318}/v1/traces`, matching the seven corrected scoped configs. Remove the ineffective management.otlp.tracing.export.enabled property, absent from the pinned Spring Boot3.3.0 configuration metadata; retain tracing/sampling. The reviewed unchanged config workflow dry-run targets eighteen app consumers, sequentially with Gateway last, excluding Config/Eureka/UI. Publish the config before restarts. No new wipe, image build or workflow change. Preserve failed log evidence; check all fresh logs after restart, then load-only seed and public Oracle O2 gates. Eureka1 had one startup error and PostgreSQL three, with zero recent errors; inspect exact causes before declaring logs clean. Full platform remains incomplete.


## 2026-10-03T21:45:30+05:30 — Read-only measurements after the authorised wipe

Choice: reuse the existing readOnlyRequestsForServerLatencyMeasurements method with the fresh seeded restaurant OWNER's organisation by default. An explicitly supplied retained O1 customer remains supported. Old8999 accounts were intentionally wiped; creating replacement accounts only to generate read samples is unnecessary. The method still uses normal browser Dev Autofill login and thirty successful public Gateway reads each for organisations, brands and outlets, without product-data mutations. Compilation and actual Oracle run are separate gates.


## 2026-10-03T21:45:57+05:30 — Phase1 checklist reconciled with recorded live proof

Owner noticed two unchecked Phase1 rows. Choice: mark only the already-proven deployed lifecycle E2E row complete, linking its third-invocation1/0/0/0 and exact OWNER/outbox evidence. Keep measurement rows open: org-list30.758ms is recorded, but internal membership latency/generated breaker IDs must be measured at O2's first consumer. Wiped O1 fixture identities are historical, not current runtime claims. O2 fresh live gates have not run yet.


## 2026-10-03T21:52:36+05:30 — Shared trace endpoint verification

The first seven restarted app readers have zero startup errors; protected Jaeger telemetry lists their service names and confirms spans arrive. Fresh initial Identity/Restaurant/Chat/Notification schemas contain exactly two successful Flyway resources each (service baseline + published common baseline), required ownership/entity constraints and append-only audit triggers. Flyway's own installed_on timestamp belongs to the pinned framework; report it separately from application timestamps rather than edit framework history DDL. Exact OCIR digest/image/platform comparison passed22/22. PostgreSQL's three errors occurred during fresh initialisation/shutdown, and Eureka1's single heartbeat error during peer startup; both have zero recent errors. Keep these historical startup counts visible. No fresh seed or O2 E2E yet.


## 2026-10-03T21:55:31+05:30 — Shared config rollout complete; load-only seed started

The unchanged config workflow completed with exit 0, publishing application.yml before sequentially restarting all 18 readers. All 29 containers run their intended images, with zero automatic restarts; all application logs since their current StartedAt have zero errors. Read-only reconcile: 29 declared/running, zero missing/extra/drifting. One two-minute Reviews error came from its previous process before the restart, while current-boot errors were zero; retain that original audit and bound the helper's recent window to max(StartedAt, now minus two minutes). Do not hide failed-release logs. Decision: run unchanged dummy-data.sh --load-only now that the fresh schemas and services are ready; no second reset. Seed completion and post-seed relationships remain pending, then public Oracle staff/lifecycle/performance gates.


## 2026-10-03T21:56:44+05:30 — Fresh Dev seed complete; live O2 begins

Unchanged dummy-data.sh --load-only completed with exit 0. Static and deployed primary-key/admin-role checks passed for 14 organisations with exactly one active OWNER, 554 identities, 504 customers, 1,003 addresses, 34 riders, 13 brands, 104 outlets and 504 dishes. All 29 containers are running on intended images, automatic restarts 0; all current application boot logs and current recent windows have zero errors. Hardening and report-only reconcile passed. Choice: run the existing O2 phone/schema preflight and normal-browser staff registration on the public Oracle tunnel, retaining all new records and manifests; no automatic cleanup. After the staff gate, use one fresh canonical order and its same-order regression set. Live outcomes/latencies are not yet verified; O3+ remains unstarted.


## 2026-10-03T22:00:27+05:30 — O2 live denial request corrected; retain and resume the same member

First O2 invocation: 1 test, 1 failure, 0 errors/skips. Signup, STAFF invite/accept, stock off/on with live UI/readback passed before the price request returned 400 instead of 403. Source contract OutletMenuOverride requires isAvailable; the test omitted it. Choice: send a valid positive price and isAvailable=true, retaining the 403 assertion. No product authorisation relaxation. Preserve the original failed report and allocation/member manifests for phone 9999887458. Add an explicit --resume-phone mode guarded by the same Dev organisation, owned manifest and ACTIVE STAFF user UUID; normal browser login resumes this fixture without a new signup/invitation or server cleanup. Compilation/rerun pending. All later staff/manager/removal gates remain unverified.


## 2026-10-03T22:11:15+05:30 — Stock versus orderability; pending refund permissions

The second O2 live invocation (allocation6993e245b9d59a3c) failed1/0/1/0 on stock ON readback at22:01 IST. Read-only Oracle SQL proves the override is true/version3, while public effective availability is false; both applicable category windows close22:00. This is not evidence of a failed stock write or a cache/transaction race. Choice: assert the authenticated persisted override and actual PUT response for stock, preserve opening-hour checks separately, and initialise restaurant stock switches from overrides even outside category hours. Disable duplicate writes while each stock request is pending. Effective customer availability must be recomputed from current bulk queries and the injected UTC clock rather than caching a time-derived result for15 minutes; preserve category caching. An actual cached-read/clock-boundary guard must fail if effective-menu caching is restored. The observed pending-refund queue403 for ACTIVE STAFF is a product permission error: require named ORDERS_OPERATE membership for PROCESSING operational requests; financial summaries/statements/order earnings/completed refunds remain EARNINGS_VIEW. Keep the exact existing staff fixture, failed reports and snapshots; no cleanup, second wipe, replacement signup, SQL stock write or opening-hours bypass. Publish tested changes before targeted Oracle rollout, then rerun the retained live gate and regressions. O2 is incomplete; O3+ remains unstarted.


## 2026-10-03T22:19:36+05:30 — Canonical overnight Dev fixture and truthful sparse measurements

Choice: retain realistic category windows and add one idempotent all-day Food-category window at the existing canonical Brand1 Outlet3 in Dev dummy seeds, mirrored in the seed generator and enforced by the validator. Required canonical order regression otherwise cannot run after22:00, when every previously selected Food window is closed. This is configured dummy data, never an opening-hours exception in application code. Use the existing authorised load-only seed workflow after publishing the seed change; it inserts one new timing row and preserves the existing STAFF fixture/order data. No second wipe, SQL UPDATE/DELETE, replacement account, time-wait bypass or production claim. Closing-boundary behavior is separately proven by a fake-clock cache-advice guard. The first protected histogram snapshot has sparse/cold samples (membership p95estimate50.891ms, brands742.671ms/outlets2254.858ms), so it does not satisfy release budgets. Preserve it; collect the required public read sample and complete post-rollout measurements without dropping prior failures.


## 2026-10-03T22:21:49+05:30 — O2 live-discovered fixes verified locally

Source verification after the closing-hour/refund fixes: clean Restaurant94/0/0/0; clean Customer476/0/0/0; full UI126files/789tests,0failures/errors/skips; typecheck/lint/build passed. Real cache-advice controls reject restored effective-menu caching2/2 failures; the HTTP method-security guard rejects restored EARNINGS_VIEW queue gating1/4 failures, with source restored before clean builds. The first new Customer guard invocation23/0/1/0 was a Mockito restubbing error, corrected with doReturn and preserved; it is not a product failure or a passing invocation. Source Restaurant4a37fba/Customerc5001e8 is committed/pushed. O2static13/13, core56/56, timezone26/26 pass. UI publication/source commit and targeted image rollout remain pending. Required retained live gate, canonical regressions and release-budget measurements are still open; no new signup/wipe/cleanup. No production-readiness completion claim.


## 2026-10-03T22:28:08+05:30 — Corrected images published; retention timeout separate

All three corrected images are published and directly verified in OCIR as linux/arm64, exact digests in40-o2-fix-published-images.json: Restaurant4a37fba-fe7c9eb,Customerc5001e8-712cd8c,UI42f92fb-3664d99. Tag records and exact Dev hours validator are committed/pushed as Deployment e6ddca2 (seed862a399). Both exact-head producer stub workflows37138413376/37138416052 succeeded; Restaurant consumer37138634395 succeeded, Customer37138636849 remains running. Native publish.sh completed all image pushes and tag records, then exited1 during its unchanged registry-retention step with an Oracle registry tag-list socket timeout. This is a failed maintenance step, not missing images; preserve the failure, inspect/retry the unchanged retention policy separately without editing workflows. No corrected image has been deployed yet. Reviewed required UI Dev-config dry-run affects18 readers sequentially/Gateway last; execute after targeted image rollout, then load-only the one additive Dev timing seed and run final log/config/identity audits before the retained live gate. Sparse pre-fix histogram release budgets remain failed/pending.


## 2026-10-03T22:29:19+05:30 — Corrected O2 Oracle rollout

O2 corrections are committed/pushed, both producer and both consumer contract workflows succeeded at exact heads; all three images verified published linux/arm64 by registry digest, Deployment tags e6ddca2 pushed. Native publisher exited1 only in post-push registry tag-list cleanup timeout. Automatic approval review rejected the direct retention retry before execution because it can irreversibly delete multiple published image digests beyond the authorised publishing/deployment scope. Do not bypass it; registry cleanup is optional and left blocked, so proceed with the independently verified images. Targeted Oracle rollout has started for restaurant-service4a37fba-fe7c9eb,customer-servicec5001e8-712cd8c,UI42f92fb-3664d99. Required UI Dev config refresh follows the reviewed18-reader dry-run, then load-only the additive canonical Food-hours seed. No second wipe/server cleanup/replacement signup. All fresh logs/digests/config, retained STAFF9999887458 live gate, canonical order/chat/earnings/navigation/review regressions and release-budget measurements remain pending. O2 incomplete; O3+ unstarted.


## 2026-10-03T22:37:10+05:30 — O2 retained staff live gate passed

Corrected O2 retained STAFF invocation3 passed1/0/0/0 after exact three-image deployment. Stock OFF/ON verified by actual UI PUT responses and persisted overrides; STAFF pending refund queue200, price/earnings/statement/completed refunds403; promotion to MANAGER gives matching owner figures; removal refuses stock within 5298.113ms plus pending refunds, and outsider/internal routes403. Same original account9999887458/useraa46c426-b124-4819-bec8-f2330de82a71/org6bda9207-420f-53f6-8643-30dc731bff77 now has MANAGER role with REMOVED status; completed manifests retained. No replacement signup or cleanup. Do not resume it as ACTIVE STAFF again. Dev config apply exited0 with identical VM files, correctly skipping publication/restarts. Load-only canonical Food-hours addition exited0; existing stock/version3 and owned membership retained before live writes. Hardening all checks, reconcile29/0drift, exact22image/platform/digests pass. All29running/restarts0/recentErrors0; classify the historical initial Eureka1 peer error separately from business-app startup errors. Remaining O2 gates: one canonical lifecycle and within2h chat/history, exact nonzero SQL earnings, navigation/reviews, final measurements. O2 incomplete; O3+ unstarted.


## 2026-10-03T22:47:32+05:30 — Canonical order delivered; bounded runtime defect

Canonical HappyDeliveryFlowTest#completeOrderLifecycle invocation1 passed 1/0/0/0 (268.807s) on retained order e82f8c51-6041-4987-88b9-c3f02ac781a8, actual Brand 1 Outlet 10 / 4a187e63-659d-4ba6-925f-52cd278f8bf1. Read-only Oracle confirms HANDED_OVER/DELIVERED, payment SUCCESS, rider OFFLINE/APPROVED/active, restaurant payout and ledger balance both 19.11 INR, no payouts. Cross-role real chat, reconnect, image/history and refund quote checks passed. The initial chat503 is the existing deliberate browser retry fixture (OrderChatChecks lines44–48), not a server outage; chat logs have no errors. Retain the separate initial quote400 observation. Broken-pipe AsyncRequestNotUsableExceptions are client stream disconnections; SSE remains parked and these are classified separately rather than erased.

Actual defect: Restaurant DeliveryClient calls internal admin driver endpoints with the correctly signed SERVICE identity, causing repeated Delivery403 and Restaurant fallback errors; rider names are missing from fulfilled/history enrichment. Choice: use the existing least-data internal driver summary endpoints (id/fullName only), keep admin fleet guards unchanged, and prove producer/consumer contracts plus method security before publishing the targeted fix. Do not grant SERVICE access to all admin routes or expose phones/bank/profile fields. Preserve existing admin contracts for other consumers.

Existing same-order ChatAndRefundIsolationTest#outsidersAreRefused and ChatHistoryPagingTest#earlierMessagesCanBeLoaded are now running through the public Oracle tunnel, with intruder customer8000000485/restaurant9000000002/rider7000000002. No replacement order/account, reset, cleanup or manual OTP. Next: fix/publish/deploy rider-summary consumer, retain individual chat outcomes, then exact nonzero earnings19.11/0/19.11, navigation/reviews and final latency/breaker measurements. O2 incomplete; O3+ unstarted. Evidence:40-canonical-lifecycle-invocation1 and41-canonical-readonly-diagnostics.json. Staff9999887458 remains REMOVED, live revocation5298.113ms.


## 2026-10-03T22:51:50+05:30 — Same-order chat gates and summary verification

ChatAndRefundIsolationTest#outsidersAreRefused passed1/0/0/0 (21.875s): owner positive history/subscription and outsider customer/restaurant/rider refusals, no unauthorized message persisted. ChatHistoryPagingTest#earlierMessagesCanBeLoaded passed1/0/0/0 (11.711s), real retained history>50 with oldest messages loaded in UI. The original e82f8c51 order is retained; no new order. Exact authoritative SQL shows outlet4a187e63 Brand1Outlet10, restaurant payout19.11/ledger19.11, no payout, riderOFFLINE.

Summary source: Restaurant client now calls existing internal id/fullName endpoints, admin roles unchanged. Focused restaurant fulfillment8/0/0/0. First full Delivery verification164/1/0/1: two summary contracts passed; a new negative guard's fleet Pageable argument failed standalone binding with400 before method advice. Correct it to the existing no-argument fleet-cities route while retaining403 checks on admin individual and batch profiles. Preserve the failed report; no authorization rule was changed. The single pre-existing ignored getDeliveryStatus contract is explicitly future ONDC scope, not executed proof; retain it under the parked ONDC exception. Clean rebuild pending. Public thirty-read sampling started; actual histogram budgets still open.


## 2026-10-03T23:02:28+05:30 — Actual server measurements and Chat circuit breakers

Public thirty-read invocation1 passed1/0/0/0 (25.695s), no product mutations. The first collector failed only on Chat's deliberately authenticated Prometheus endpoint; keep that failure. Correct collector authentication on Oracle using the service's existing signed SERVICE identity, never moving/printing the secret or signature, and retain partial evidence on failures. Authenticated collection succeeded: org-list33 samples p95estimate29.919ms/bucket33.554ms; internal membership116 samples24.607ms/bucket27.962ms; internal user organisations343 samples28.641ms; outlet→org151 samples20.636ms. Actual generated IDs in Restaurant/Customer are recorded, including 4/3 definitive membership refusal calls rather than calling failures zero.

O2 brands47 samples87.521ms/outlets47 samples109.238ms satisfy150ms but fail pre-O2+20ms budgets (30.199+20 /63.753+20). They stay open; preserve cold/sparse and current measurements, do not relax the budgets or discard outliers. Final measurements follow the necessary corrected release and regression calls. The collector sees no Chat breakers because Chat never enabled OpenFeign circuit breaking and lacks the house resilience auto-configuration dependency. Choice: enable it only in Chat's source and deployment config, add the same managed resilience4j-spring-boot3 dependency used by other consumers, wire the real Restaurant fallback in the isolated consumer test and require actual successful generated breakers there. No blind instance configuration or global enablement. Shared500/800ms/no-retry settings remain. O2 incomplete.

Source proof: Delivery clean invocation2 164 reported/163 executed passed/0failures/errors, one explicit parked future ONDC contract; Restaurant clean96/0/0/0. Restored old admin client calls fail the actual Feign consumer suite4/2/0/0, then source restored before clean build. Delivery ab49331 producer publication37140397594 succeeded. Restaurant3ab2a2c committed/pushed; producer/remote consumer runs dispatched, not yet confirmed. No new image deployment yet. Empty order quotes are explicitly supported by calculateOrderQuote; do not change UI behavior on an assumption that empty requests are invalid. Initial canonical quote400 has no captured body and remains an observation, not an invented defect.


## 2026-10-03T23:04:35+05:30 — O1 measurement gate complete

Protected authenticated server histogram evidence41-before-summary-fix-server-measurements-authenticated.json follows public30-read invocation1/0/0/0. GET organisations33 successful samples p95estimate29.919ms, enclosingbucket33.554ms (budget100ms); internal membership116 successful samples p95estimate24.607ms, enclosingbucket27.962ms (budget50ms). Identity remains the same deployed release. Exact generated names: OrganisationServiceClientgetMembershipUUIDUUID, OrganisationServiceClientgetUserOrganisationsUUID (Restaurant); OrganisationServiceClientgetMembershipUUIDUUID and RestaurantServiceClientgetOutletOrganisationUUID (Customer). The shared contextId timeout500/800ms and Retryer.NEVER_RETRY are source-verified. Preserve expected failed membership lookups4/3 in breaker metrics and earlier sparse/cold failed snapshots. Mark O1 PERF-1 and measurements complete; the historical deployed lifecycle gate was already passed. O2 remains incomplete: final brand/outlet delta budgets, Chat resilience rollout and remaining exact-order regressions are open. No O3 implementation yet.


## 2026-10-03T23:06:58+05:30 — Non-deleting image publication choice

Chat focused actual consumer/breaker tests3/0/0/0; restoring disabled OpenFeign gives3/3/0/0, source restored before the full clean build. Restaurant producer37140681388 and remote consumer37140683165 are being checked at exact3ab2a2c; Delivery producer37140397594 succeeded atab49331.

Automatic approval review previously rejected registry retention deletion beyond the task scope. Both existing image publication entry points contain cleanup: native publish.sh ends with all-registry retention, while CI build-and-push invokes delete-all for its service before pushing. Choice: avoid those optional deletions entirely. After clean builds/source pushes/stub and remote contract gates, run only the existing native Docker build/push steps, using the same service-map context, linux/arm64 platform, clean source SHA plus SHA256 of unzipped artifact content, and reviewed Dev tag-file updates. No protected workflow/script is edited, no CI visibility change, no registry retention retry/deletion. The existing deploy.sh remains the deployment path. Publish only runtime-changed Restaurant and Chat images; Delivery's unchanged runtime already serves the verified internal summaries, and its new source is producer tests/contracts only. Record registry digest/platform before committing tag files and deployment. No second wipe.


## 2026-10-03T23:15:39+05:30 — O2 corrected release published; remaining gates

O1 source/deployed/measurement gates are complete. O2 retained STAFF gate passed; account9999887458/useraa46c426-b124-4819-bec8-f2330de82a71 remains MANAGER/REMOVED (revocation5298.113ms), no new signup. Canonical order e82f8c51-6041-4987-88b9-c3f02ac781a8 is DELIVERED at Brand1Outlet10/4a187e63-659d-4ba6-925f-52cd278f8bf1, rider7000000001 OFFLINE. Lifecycle, same-order outsider isolation and history paging passed individually1/0/0/0. SQL restaurant payout/ledger19.11, clawbacks0, no payout.

Restaurant3ab2a2c clean96/0/0/0, producer37140681388 and remote consumer37140683165 SUCCESS; Delivery test/contract headab49331 published37140397594, existing runtime unchanged. Chat52463c3 clean73/0/0/0; disabled-breaker mutation3/3 failures, restored. Remote consumer37141292582 still running. Native necessary build/push steps without any registry deletion published/verified linux/arm64 Restaurant3ab2a2c-d32f146 and Chat52463c3-b74099e; exact digests in41-published-runtime-fixes.json. Dev tag/config commit9ecc6c0 push/dry-run pending confirmation. Protected six workflow hashes unchanged. VM corrected release not deployed yet.

Next: confirm last remote gate and single-reader Chat config dry-run; unchanged targeted image deploy, config apply, log/image/hardening checks. Then retain same-order exact nonzero earnings, reviews/read-only reopen, outlet/navigation, post-rollout chat positive/refusal/history checks and final30-read server measurements. Current brand/outlet estimates87.521/109.238ms meet150ms but fail baseline+20ms; do not mark O2 complete or weaken budgets. O3+ unstarted. Existing original failed invocations and expected stream-disconnection/error counts stay recorded.


## 2026-10-03T23:17:47+05:30 — Verified targeted Oracle rollout started

All four required exact-head producer/remote consumer gates SUCCESS (41-required-publication-contract-gates.json). Restaurant3ab2a2c-d32f146 and Chat52463c3-b74099e were published/OCIR-digest/platform verified before any deploy, without registry deletion. Deployment9ecc6c0 tags and Chat config pushed; unrelated DEPLOY_LOG remains local/unstaged. Reviewed unchanged Chat config dry-run affects only one reader. Unchanged deploy.sh started for Restaurant and Chat; no data reset or new fixture. Apply reviewed Chat config after image completion; require clean startup and exact digest checks, then same e82f8c51 regressions/final latency budgets.


## 2026-10-03T23:23:36+05:30 — Corrected Oracle release verified

Restaurant3ab2a2c-d32f146 and Chat52463c3-b74099e were published and ARM64/digest verified before the unchanged targeted deployment, which completed0. Reviewed chat-service.yml config publication/restart completed0, only Chat reader. First sandbox-only config attempt could not resolve GitHub/SSH; no remote mutation occurred; preserve its log. Authorized network retry completed. Oracle read-only audit shows29running/intended images, no drift, automatic restarts0 and recentErrors0; both changed services startupErrors0. Hardening15checks and report-only reconcile29/0drift pass. Exact digest evidence41-post-summary-oracle-release.json matches41-published-runtime-fixes.json. No wipe/reseed/replacement/cleanup. Read-only monthly payout is one delivered order/19.11, ledger19.11/clawbacks0/no payouts; riderRider1 OFFLINE; reviewcount0 before first authorized review submission.

Now running post-rollout same-order chat isolation/history invocation2, then retained-order nonzero earnings19.11/0/19.11, navigation and reviews/read-only reopening, final30public reads and authenticated server measurements. O1 complete; O2 final regression and delta-budget gates incomplete; O3+ unstarted. Latest checkpoint41; older entries are dated history.



## 2026-10-03T23:27:24+05:30 — Retained-order earnings and reviews verified

Corrected Restaurant/Chat release is deployed with matching ARM64 digests,29running/no drift/restarts0/recent errors0, hardening and reconcile passed. Post-rollout ChatAndRefundIsolationTest and ChatHistoryPagingTest invocation2 each1/0/0/0 (21.955s/7.875s) on the same e82f8c51 order. RestaurantEarningsLiveTest exact monthly net19.11/clawbacks0/pending19.11 and the signed nonempty statement passed1/0/0/0 (9.269s). OrderReviewsFlowTest passed1/0/0/0 (21.5s): four real immutable reviews and read-only reopening; retained manifest41-canonical-e82f8c51-6041-4987-88b9-c3f02ac781a8-reviews.json. Never repeat this once-per-order writer.

Preserve compilation batch invocation1 (0 executed/live requests) and navigation batch invocation2 (1/1/0/0): added summary assertion mistakenly used Java riderName rather than the explicit Jackson API name deliveryExecutiveName. This was a test-field error, not proof the deployed name was missing; the session commentary has been corrected. Source/API assertion now uses the public field with unchanged Rider1 expectation from read-only SQL, no runtime change. Navigation, positive public review aggregate and30-read measurement batch invocation3 is running. O1 complete; O2 remains open until these and original baseline+20ms performance budgets pass; O3+ unstarted.



## 2026-10-03T23:31:31+05:30 — Review cache and brand latency corrections

Final navigation invocation3: restaurantSectionsRender passed12.837s with real response deliveryExecutiveName=Rider1; public aggregate method failed13.696s: correct customer review4 displayed while aggregate showed0/No reviews yet. Four original immutable reviews remain; do not rerun participantsReviewEachOther. Actual source defect: ReviewCommandService publishes AggregateUpdatedLocalEvent after transactionTemplate.execute returns, but RedisCacheUpdater is AFTER_COMMIT without fallbackExecution; no transaction exists to register its callback. Choice: publish from inside the write transaction after outbox rows; evict synchronously AFTER_COMMIT before POST returns, proving real transaction commit/rollback behavior and breaking the old placement. Retain prior failed public result. After necessary Reviews rollout, prove fresh cache eviction with one previously unrated dish on this same owned order (warm its aggregate, submit its review through normal UI, immediately read aggregate); preserve existing four reviews and create no replacement order.

Public30-read measurement invocation3 passed1/0/0/0 (13.253s). Protected41-final-release-server-measurements-invocation2: brands55samples p95estimate60.118ms/bucket61.516ms fails baseline30.199+20=50.199; outlets55samples79.692ms/bucket89.478ms, estimate within63.753+20=83.753 and150ms. Keep exact histogram/bucket limitations and all prior failed snapshots. Actual Chat OrganisationServiceClientgetMembersUUIDOrganisationPermission success3 and RestaurantServiceClientgetOutletOrganisationUUID success10; five definitive negative membership calls remain recorded.

Choice: narrow Restaurant membership-list cache, raw immutable rows keyed by user, bounded10000 entries, expireAfterWrite5seconds using monotonic ticker; never cache permission grants or restaurant/outlet results, never extend on a hit/outage. Membership/status/permission filters remain per call, cache lookup failures fail closed; no new stale-access exception. Cold1-vs20 test must use independently cold users, still one bulk call/one SQL each, with warm expiry/revocation/permission/coalescing guards. Shared membership policy5seconds/60second original-fetch operational outage policy remains unchanged. Clean builds, negative controls, exact-head publication/contract gates and necessary Reviews+Restaurant images must precede targeted Oracle deployment. No wipe, schema change, registry deletion or protected-workflow edits. O2 incomplete; O3+ unstarted.



## 2026-10-03T23:39:44+05:30 — Cache corrections pass focused guards and negative controls

Restaurant raw membership cache implemented only in its list service:5second expireAfterWrite,10000bound, immutable raw rows, coalesced caller lookup, permission/status filters on every call, no entity/grant/stale caching. Independently cold1/20JPA guard remains one call/one query each. Focused11/0/0/0; TTL60negativecontrol1/1/0/0 and source restored. Reviews events now register inside transaction after all writes; synchronous AFTER_COMMIT eviction. Actual Spring/JDBC commit/outer-rollback/outbox-failure guard3/0/0/0 plus command12/0/0/0. First guard batch3/2/0/0 was a test assertion over Spring bean init callbacks, corrected to no Redis delete; no runtime change for that correction. Original outside-transaction mutation first3/0/1/0 (listener-wrapped observer assertion) is retained; move observation assertion after command, second3/1/0/0 catches the exact bug without wrapper; source restored. Both full clean verify builds are running.

Additional existing OrderReviewsFlowTest method remainingDishReviewRefreshesTheCachedAggregate compiles in invocation2; first compile-only wildcard assertion error retained. It checks original two customer reviews unchanged/read-only, warms a previously unrated owned product aggregate twice at0, submits exactly one5-star dish review through normal UI, persists submitted manifest before further assertions, and requires the immediate post-POST aggregate1/5.00 plus read-only reopening. Once submitted, never repeat it; inspect partial manifest instead. No replacement order, manual OTP, cache clear, reset or financial writes. Needed exact-head producer/consumer publications and two ARM64 images before unchanged Oracle targeted rollout; live fresh-write proof, corrected public aggregate and final brand/outlet original budgets remain open. O2 incomplete; O3+ unstarted.



## 2026-10-03T23:49:34+05:30 — Cache release committed and producer gates passed

Restaurant74b006a2c25db5ea166a170139fb83f1206dc6d0 and Reviewsd035d2e7a90dfae75fec5ba1a7d66079c385cd62 are committed/pushed from restored full clean builds103/99, zero failures/errors/skips. Source diff checks pass; O2static13/13/core56/56 remain green. Producer publications37143374142/37143378521 SUCCESS at exact heads; both remote consumer contract runs now dispatched, not yet confirmed. Necessary native ARM64 image publication will use the already-reviewed non-deleting build/push steps with unchanged Dockerfiles/service-map; preserve the old41-published-runtime-fixes.json and write41-published-cache-fixes.json separately. No registry cleanup/visibility/workflow change. VM still runs Restaurant3ab2a2c and previous Reviews8fae4f2; source completion is not deployed completion.

Existing isolation test now reads/subscribes as all three legitimate retained participants before outsider probes, parses owner phones from its original manifest and requires an actual CONNECTED frame; an empty owner socket timeout cannot pass. Compilation pending for this addition; no new live invocation yet. Four original reviews retained. After required producer/consumer gates, image publication/digest verification/tag commit and unchanged targeted Restaurant+Reviews deploy/log audit, run once remainingDishReviewRefreshesTheCachedAggregate (unrated owned dish; submitted manifest protects against duplicates), public review aggregate, exact nonzero earnings/navigation, strengthened chat controls within19:12UTC window and final30read histograms. Brand budget50.199ms remains failed at60.118; original baseline/delta never relaxed. O2 incomplete; O3+ unstarted.



## 2026-10-03T23:56:42+05:30 — Published cache release; Oracle rollout running

All four exact-head producer/consumer gates SUCCESS;41-cache-fix-required-contract-gates.json. Necessary native no-deletion publication completed0: Restaurant74b006a-cfdb337 digest23d9a663b00ca8844b2f1c7985e302f2c6ac586321f5ddeadc2e1fb64cb60614 and Reviewsd035d2e-3643e1f digest60dab5a96cedef2ad887c06206379079d73d71865c09408249b237361c2baaeb, bothlinux/arm64;41-published-cache-fixes.json. Deployment023d535 tag-only commit pushed, userDEPLOY_LOG preserved. Unchanged targeted deploy.sh is running for those two services; no configuration/schema/wipe/seed/cleanup changes. E2E6a6b771 compiled/pushed, including strengthened owner restaurant/rider positive controls plus nonempty CONNECTED frame guard. Four original reviews and e82f8c51 canonical order retained.

After actual deployment/log/digest/hardening/reconcile confirmation, once-only dish cache write on original unrated product, public review aggregate, same-order earnings/navigation/chat positive/refusal/history checks and required30public reads then protected histograms. Never repeat original participantsReviewEachOther writer or any dish manifest with submitted=true. Brand/outlet budgets remain50.199/83.753ms plus150ms, with enclosing buckets and all historical failed snapshots retained. O2 remains incomplete; O3+ unstarted.


## 2026-10-04T00:06:23+05:30 — Retain a write after an ambiguous UI response wait

A timeout waiting for a POST response does not prove the write failed. Preserve the failed report and prepared manifest, read Oracle first, and mark the current fixture submitted=true only when its exact review row is confirmed. Do not submit the immutable dish again or replace the order. The normal UI actorRole query parameter must be matched by URI path and explicit role rather than a URL suffix. Immediate post-write assertions that did not run remain unverified; collect the real eviction log and complete read-only proof before phase completion. Both published cache images are deployed, but O2 remains open for final regressions and unchanged latency budgets.

## 2026-10-04T00:12:11+05:30 — Chat protocol-close proof and retained review continuation

Retain chat invocations 3 and 4 as failures (1/1/0/0 each); history passed separately. Oracle logs correlate the authenticated outsider with a denied logical subscription and Spring's inbound MessageDeliveryException. The corrected decoder still observed CONNECTED then close code1002, not an ERROR frame. Inspected the installed Spring6.1.8 StompSubProtocolHandler bytecode: sendErrorMessage always closes PROTOCOL_ERROR, including when sending ERROR fails. The test accepts explicit Access Denied or that exact authenticated1002 refusal, never a timeout/normal close, and requires all three identical legitimate subscriptions to remain open for their observation window. Invocation5 was compilation-only (0executed) due AssertJ wildcard generic capture; corrected using get/anyMatch. Invocation6 completed successfully; reports recorded separately. No Chat product or authorization relaxation.

The original dish writer remains submitted=true and must never repeat. Its live commit log shows exact PRODUCT cache eviction before the successful command return; the immediate client aggregate assertion did not execute. Finish it via an explicit read-only method, preserving its failed writer report. For a fully executed immediate write/read cache proof without replacing or resetting fixtures, use the same order's still-unrated RESTAURANT-to-DRIVER target once: normal restaurant UI write; assigned rider reads its own warmed private aggregate before/after; keep all five prior immutable reviews unchanged. Verify eligibility and aggregate first, record attempted/submitted manifests before/after click, and never repeat an ambiguous write. Correct response matching uses URI path plus actorRole. No new order, money operation, duty change, cache clear, migration, wipe or seed. O2 remains incomplete until these proof and latency gates pass.

## 2026-10-04T00:19:32+05:30 — Final live regressions passed; outlet delta budget remains red

Final public invocation4: earnings1, navigation/public aggregate2, organisation30-read sample1 and retained review/read-only+immediate driver-cache proof2 all passed (6/0/0/0). Driver cache changed1/5.00 to2/4.50 immediately after normal restaurant UI201; new remaining review retained, no repeat. Original dish immutable5-star review verified read-only; its earlier timeout remains preserved. Total six immutable reviews on the same delivered order; money unchanged and rider OFFLINE. Chat invocation6 isolation/history each1/0/0/0 with actual three legitimate subscriptions and supported outsider refusal.

Final protected cumulative histograms after required30reads: brands55samples p95estimate32.156ms / bucket33.554 (baseline30.199+20=50.199 PASS); outlets55samples92.275 / bucket111.848 (baseline63.753+20=83.753 FAIL; absolute150ms PASS). Internal outlet→org50samples30.758/bucket33.554; user-outlets3samples38.308/bucket39.147. Identity org93samples14.470 and membership162samples21.754 meet original O1 budgets. Actual Chat generated member/bulk/outlet breakers have successful12/3/43 calls; definitive failed membership11 retained. Do not add warm runs, reset metrics or relax budgets to turn this green.

Source confirms one fetch-graph query with timings and existing indexes; current one-vs20 cold query test already guards no N+1. Jaeger sample shows cold downstream membership and read processing costs, not proof of missing indexes. Narrow candidate: mark only the bulk list repository query read-only with Hibernate read-only hints, avoiding dirty-check snapshots while retaining fresh SQL, permissions,5-second membership expiry, timings and API shape. The database read-only transaction starts after membership resolution; no network call inside it. Real Hibernate guard will verify both outlets/timings are read-only even inside an enclosing writable transaction and accidental edits are not persisted, plus the unchanged one/20 query guard. Trial must pass negative controls, clean build, required publication/contract gates and publish before a necessary targeted rollout, then original-budget remeasurement. No entity/grant/list cache, TTL extension, schema/wipe/seed or protected workflow change.

Review locator audit has12 false-source failures from conditional radiogroup role and template labels rendered by StarRating/RateOrderModal; actual live selectors passed. Correct the source parser with positive/negative controls, preserving this failed invocation; do not whitelist/delete those checks. O2 remains incomplete; O3+ unstarted.

## 2026-10-04T00:27:40+05:30 — Read-only outlet release tested; exact-head gates pending

Restaurant5b93a8d524dca534da6b2bf4720abd8b327c99cf is clean and pushed. Full clean verify104/0/0/0 produced a fresh boot jar; focused9/0/0/0 includes the unchanged independently cold one/20 one-statement guard and real Hibernate read-only outlet/timing behavior. Removing only the query hint inside an outer writable transaction caused1/1/0/0; exact source restored before clean verify. First focused invocation9errors was sandbox-blocked Mockito agent attachment before assertions; unchanged-source retry outside sandbox passed. Producer37146029352 is dispatched at this exact head; remote consumer follows only after its successful stub publication. VM still runs Restaurant74b006a-cfdb337; no image publication or rollout for5b93 yet.

UITesting9ca39ad9f3c776550cba156c62387884f15d07f2 is pushed after the actual Oracle6-case regression batch and two-case chat/history batch passed. Original dish timeout, chat invocations3/4, and compilation-only invocation5 remain retained. Six immutable reviews remain on e82f8c51; never repeat either remaining-dish/driver writer.

Source locator parser now understands conditional explicit roles and rendered accessible/visible text templates, including bounded numeric array values, collection lengths and conditional plural suffixes. It excludes unanchored bare props and class/event templates. Reviews audit65PASS/7DYNAMIC/0FAIL/0EXCEPTED; all29 positive/negative probes verified. First proof24/29 and second27/29 retained; they exposed overbroad template matching and were corrected. The old parameter-propagation probe text UPI / Netbanking is present in the current shared PaymentModal, so it cannot be a globally absent text guard; change only that injected missing label to E2E nonexistent payment method, retain the two-level propagation proof and the separate wrong-screen radio guard. No UI, locator expectation, whitelist or deployed authorization changed to satisfy this parser.

O2 remains incomplete: publish the clean image after both exact-head gates, commit/push only its Dev tag, use unchanged one-service deployment, verify Oracle/logs/digest/hardening/reconcile, then same-order read-only earnings/navigation/reviews and original30read/latency budgets. Existing histograms55samples32.156brand(PASS)/92.275outlet(FAIL against83.753) remain retained. O1 complete; O3+ unstarted.

## 2026-10-04T00:40:47+05:30 — Close O2 against its original deployed gates

Choice: mark O2 PERF-3, measurement and final publication/deployment/E2E rows complete only after the final necessary read-only-query image is published/deployed and the unchanged original server budgets pass, including their enclosing buckets. Final brands31.317ms/outlets53.687ms (buckets33.554/55.924) meet50.199/83.753 and150ms. Actual final5+2 cases passed; no failure is erased or promoted, no extra warming/reset/budget waiver. Read-only query hints eliminate dirty-check snapshots for the one bulk list read; actual Hibernate guard and removed-hint negative control prove that behavior, with one/20 query limits unchanged. O1/O2 complete, choose O3 Partner Applications next. O3+ and whole-platform production readiness remain incomplete. See checkpoint41 and41-final-o2-gate.json.

## 2026-10-04T00:43:48+05:30 — O3 source audit started; O1/O2 complete

O2 final deployed gate is green with recorded original latency budgets; no remaining O2 rollout is pending. Begin O3 Partner Applications in checkpoint42. D4 automated checks then admin approval was already confirmed by the owner. Baseline O3 validator0PASS/19FAIL/0STALE,140 verification-status call sites and81 old endpoint/component references are saved before code edits. Source currently auto-approves riders; restaurant public lists lack an application-status gate and the old document paths expose unsafe ownership checks. No O3 source change, new fixture, publication, deployment or reset yet.

Choice: use complete fresh initial schemas and deterministic new seed data under the standing all-Dev/disposable policy. Consolidate existing required Delivery/GovernmentID DDL only after inspecting and proving the resulting schema, retaining required constraints/indexes/functions; never add legacy upgrades or blindly delete SQL. Extend the existing hash-bound reviewed schema recreation mechanism only after exact SQL tests. Preserve checkpoint41 failures and all six canonical immutable reviews until a necessary schema rollout; no wipe to hide evidence. Planned O3 E2E records are in the owning feature scenarios, with new applicant allocation only after actual rollout. Next: implement/prove shared36-pair lifecycle and typed events, then restaurant/delivery/document security from actual source. Full platform remains incomplete.

## 2026-10-04T00:54:18+05:30 — Verify actual R2 and keep ordering approval fresh

Choice: use Oracle's already configured R2 for Dev document uploads; remove the unusable localhost mock URL when the owned document API is replaced. Before accepting any reference, always HEAD the server-registered object and verify its exact declared length/type/ETag; do not infer R2 enforcement from SDK unit tests. Run a tiny synthetic storage compatibility probe using secrets/signatures only on Oracle, retain the exact key and redacted HTTP codes, and never create/modify an applicant or seed during this probe. Check whether the configured public image URL exposes this document; if so, require private KYC storage rather than putting private documents in a public image bucket. No registry deletion, database wipe or fixture replacement.

Source also shows Customer RestaurantClient caches by-id/menu results: public Restaurant approval filters alone cannot revoke a previously cached outlet for a new quote/order. Use an uncached approval read for ordering and typed status-event invalidation for cached browsing; prove suspended refusal without weakening accepted-order processing. Keep the removed public executive-id summary replaced by principal-derived /status/me, plus a distinct SERVICE/ADMIN-only internal summary for legitimate background verification. Existing provider callbacks retain their shape. Common lifecycle/event full clean install292/0/0/0 and the auto-approval bypass negative control37/1/0/0 are local proof, not an O3 deployment.

## 2026-10-04T01:11:14+05:30 — O3 document ownership and signing proof

Local shared lifecycle/event clean build292/0/0/0 remains proof of its earlier source snapshot. Actual S3Presigner/HEAD document guard4/0/0/0 passed; removing signed content length caused1/1/0/0, then source was restored. These are unit/negative-control results, not an O3 deployment. Oracle-to-R2 synthetic compatibility probe42-r2-signed-storage-probe-invocation1.json: valid PUT200, changed size403, changed MIME403, HEAD200/application-pdf/15bytes, anonymous configured public origin403. Retained only the synthetic key documents/storage-compatibility/40de4e1c-a427-426a-89e9-8d0552a3b4dc/synthetic.pdf; no applicant/seed mutation, secrets or signed URLs in evidence.

Choice: register each server-generated document key with its authenticated owner, purpose, type, declared size and application before signing. Complete only after exact R2 HEAD; all verification references and own downloads must match the registered owner/type and exact documents/<caller>/ prefix. Restaurant uploads additionally require fresh BUSINESS_APPLY and DRAFT/REJECTED. Admin document downloads use5minute URLs and a transactional KYC_DOCUMENT_VIEWED audit, with IDs only in details. Replace public /status/{executiveId} with /status/me and retain a distinct SERVICE/ADMIN-only internal summary for real background jobs. Remove caller-selected identities from GovernmentID MCP tools and remove its provider-webhook mutation tool. Current working edits are not yet compiled or deployed.

Additional source finding: a presigned PUT can be replayed until expiry, so HEAD alone does not make a reviewed file immutable. Finalize each accepted upload by a conditional, matching-ETag copy to a server-only key that is never issued a PUT capability. Prove actual R2 conditional copy before using it, then return/use that finalized reference. Also keep document edits frozen during review; delivery daily selfie remains a separate own document operation. Cloudflare's official S3 compatibility documentation lists CopyObject with x-amz-copy-source-if-match; that listing is not yet runtime proof.

O1/O2 complete; O3 still incomplete, local only. No O3 publication, deployment, database wipe or new applicant yet. Next: finish document API/client/schema wiring and meaningful security/audit tests, then application services and UI before required clean release/live E2E.

## 2026-10-04T01:23:07+05:30 — Document security and fresh schema verified locally

Common clean invocation2:297/0/0/0. GovernmentID security invocation1:26/0/1/0 (Mockito restubbing setup error retained); corrected test setup invocation2:26/0/0/0, with all original assertions. Three negative controls each1/1/0/0 caught foreign-prefix ownership, restored caller-selected summary path and missing admin-view audit; exact source restored before the full GovernmentID clean build, currently running. Actual audit tests use JPA commits and rollbacks; these results are local proof, not an O3 rollout. Evidence42-gov-security-invocation{1,2}.json, individual stripped XML and42-gov-document-negative-controls.json.

Isolated PostgreSQL17 initial-schema proof preserved all73 original columns/types and all original indexes, retained append-only audit, and proved valid document acceptance plus six invalid-row constraints. Only after that comparison, removed V20260822100000__add_missing_columns.sql and V20260925110000__timestamps_tz.sql; their final TIMESTAMPTZ/verification-data columns remain in V1, original files preserved as evidence. The hash-bound Dev recreation manifest is not yet extended: do not deploy this schema incrementally onto the existing Oracle database. Actual R2 conditional copy200, wrong-ETag412 and finalized HEAD200/15bytes/application-pdf; synthetic source and finalized object retained, no applicant/seed changes. Registered browser uploads now finalize under a server-only accepted key; no PUT credential is ever issued for that accepted object.

Next from source: replace the old BRAND_CREATED consumer that carries financial PII with the typed SUBMITTED application event and a SERVICE-only read of its current verification request. Correlate automated callbacks with the submitted application revision while retaining the existing callback URL/body fields, and persist failed callback delivery for retry. This prevents stale callbacks and the old24-hour penny-drop lock from approving a resubmission incorrectly or leaving it stuck. Implement/prove this together with the single Restaurant/Delivery transition owners before claiming O3 completion. O1/O2 completed; O3 incomplete/local, no O3 publish/deploy/wipe/new applicant.

## 2026-10-04T01:45:31+05:30 — Verification delivery and non-Dev safety

Choice: retain existing brand-check HTTP request/response contracts as SERVICE-only compatibility entry points, but derive every checked field from the current submitted Restaurant application; caller-supplied brand/GSTIN/bank values never authorize or overwrite another application. Remove the corresponding public GovernmentID MCP tools. Typed SUBMITTED events start the same idempotent checks. Store callback results and retry state in the GovernmentID database, correlated to the submitted revision; a delivery outage cannot silently drop a result. Keep intentional Dev/test provider shortcuts. Production must refuse verification when no actual configured provider exists; never simulate successful financial/biometric approval in production. UI/application lifecycle work and production provider configuration are still pending. All Dev recreation remains authorized only through the unchanged reviewed clean workflow, after publication and schema proof.

## 2026-10-04T02:09:40+05:30 — O3 provider safety and explicit resource binding

Choice: remove successful non-Dev banking/selfie simulations; wire explicit production adapter interfaces with unavailable defaults that return503 and never create verification history. Preserve the existing deliberate Dev/test shortcuts and all real lockout behavior; lockout tests inject controlled provider results rather than choosing success/failure by a URL substring. Real provider choice/configuration is unresolved; owner question asks which provider, with continuing Dev mocks as the stated default. This does not block independent Dev feature implementation or authorize a production-readiness claim.

Choice: bind application outlet edits to both IDs in the URL through the current outlet→organisation projection, then fresh BUSINESS_APPLY. Keep service-level brand binding too; add no scanner exemption. A controlled removal of each ownership/revision guard fails an actual assertion. Complete the original fresh-schema manifest review only after final schemas AND their seeds pass; current Gov hash alone is insufficient for rollout.

## 2026-10-04T02:22:43+05:30 — Fresh approval reads and provider expiry

Choice: keep the existing customer browsing caches, evict them on typed application transitions, and use a separate uncached call to the authoritative Restaurant public by-id endpoint for every quote and checkout. Preserve definitive4xx refusals and return503 for outages; never infer application approval from passed provider flags or a cached browsing row. Cache eviction errors roll back the persisted retry claim. Exact public SQL is proved across all six states before release.

Choice: production DL/RC checks refuse missing/mock provider configuration and do not log document numbers, dates of birth, names, provider payloads or exception bodies. Preserve deliberate Dev/test provider result values. Parse their ISO expiry and the configured provider's dd-MM-yyyy format strictly; refuse malformed returned dates before persistence instead of storing an approved result with no expiry. Real-provider choice/configuration remains pending; no production integration/readiness claim. These choices do not authorize schema manifest exemptions without final seed proof.

## 2026-10-04T02:46:52+05:30 — Rider identity, frozen review documents, suspension and fresh schema

Use only the signed identity phone claim for first rider draft; missing claim requires a fresh sign-in. Application write DTOs contain no user-selected phone or identity. Daily selfies are a distinct owned DELIVERY_DUTY document purpose (SELFIE only, approved/suspended riders), while application documents and bank details freeze outside DRAFT/REJECTED. Every document operation uses fresh SERVICE-only application context; foreign/missing/unavailable context refuses the action. Suspension sets inactive and removes future availability after commit, preserving ON_DELIVERY assignment and liveness so the current delivery can finish; rollback emits no Redis/duty side effect.

Under the owner's Dev-only policy, consolidate eight Delivery incremental DDL definitions into complete V1 only after exact isolated old-versus-new schema proof; retain originals in evidence. Preserve all93 original column definitions except explicitly retired executive verification_status, all other constraints/indexes, city type, telemetry geometry, durable assignments, prepaid-only check and mandatory version. GovernmentID complete initial schema also preserves all73 original columns/indexes and adds constrained separate duty-selfie purpose. Proofs42-delivery-fresh-initial-schema-proof-invocation1.json and42-government-fresh-initial-schema-proof-invocation4.json; current seeds are still untested, so the hash-bound recreation manifest is not yet extended. Publish clean current artifacts before the authorised unchanged full Dev wipe/deploy/fresh seed. These choices are locally implemented with focused proof, not yet an Oracle release or production-provider integration.

## 2026-10-04T03:02:46+05:30 — Fresh verification, current contract stubs and deterministic Dev fixtures

Provider approval must match the latest owned immutable application document, with unexpired results; replacement, foreign ownership and operational selfies cannot inherit application approval. Preserve only the deliberately configured legacy Dev/test seed shortcut; unconfigured or production profiles do not fabricate checks. Durable stored callback intent prevents repeat verification when general idempotency keys expire. Application contracts use an actual ISO timestamp example so generated consumer stubs exercise valid Instant binding, with no financial fields in transition events. Current producer stubs must be installed locally before consumer proof; local installation is not external publication.

Add vehicle-registration uniqueness to complete Delivery initial SQL, matching the existing entity and existing duplicate-vehicle assertion; prove it on PostgreSQL and refresh the final schema hash before publication. Baseline Dev riders seed OFFLINE rather than imply live WebSocket/duty readiness. Preserve all baseline/scenario UUIDs; scenario statuses follow the O3 mapping with explicit reasons, passed provider fixtures for IN_REVIEW and genuine approval-filter discrimination from active nearby pending/rejected outlets. Approved brands have synthetic FSSAI. Private reviewed upload proof comes from the run's actual new applicant uploads, never public dummy images. Static validation passed; actual final schemas/seeds, UI and Oracle rollout remain pending. No Oracle seed/wipe ran in this batch.

## 2026-10-04T03:14:06+05:30 — O3 reviewed fresh schemas and SDK generation

Choice: retain required complete initial schemas and common audit/idempotency DDL. Consolidate only the10 inspected GovernmentID/Delivery incremental files whose exact previous definitions are proven in42 schema comparisons. Bind all seven initial files by SHA256 in DEV-SCHEMA-RECREATION.json only after all three changed schemas accept actual fresh seeds and idempotent replay. Keep prior Identity/chat/notification proof for unchanged bytes. Six isolated seed mutations must fail. No legacy conversion or Flyway repair; publish required clean consumers before unchanged clean-deploy --wipe/fresh seed. This permission expires on an owner production declaration.

Choice: generate scoped OpenAPI explicitly and serially before SDK generation; ordinary Maven clean suites exclude it, and concurrent clean removes target specs. Assert new own application/document endpoints exist and old paths are absent. Test-only controller configurations use TestConfiguration and are excluded from production-controller scans; existing authorization assertions remain. UI consumes generated authenticated clients and registers completed immutable private uploads.

## 2026-10-04T03:33:50+05:30 — O3 applicant/admin UI and SDK wire contract

Choice: reuse Surface/Input/Button/Select/Tabs/StatusPill and the existing organisation selector, place search, coordinates and shifts. Applicant details remain editable only for DRAFT/REJECTED with server permission; submitted data is read-only and polled every10seconds until its status changes. Preserve a failed first application's form and already-created organisation in component memory; never create duplicate organisations on retry. Approved delivery details also remain locked in settings; suspension preserves an active delivery screen and server duty restrictions.

Choice: use generated authenticated SDK aliases for draft and outlet PUTs. Zodios injectAliasEndpoints overwrites the generic put method when the OpenAPI operation alias is put. A generic three-argument call would silently send the URL as its body. Use the actual two-argument generated draft alias and updateOutlet alias; verify the real factory/transport/auth plugin, not only a vi.fn mock. Normal public image uploads remain distinct from registered immutable private KYC files. Accept a document reference only after real PUT and server completion; no mock URL bypass, manual localStorage auth or signed URL logging.

Choice: admin approvals use paged queues, current versions, completed private uploads and fresh checks. Rejection/suspension reasons require10–500characters. Confirm the concrete decision, refresh detail/queue on409 and never optimistically grant approval. Admin document links are audited and expire in five minutes. Every source choice is local, not Oracle verification.

## 2026-10-04T03:39:41+05:30 — O3 approval, frozen edits and daily duty UX

Require completed private review documents on server approval/reinstatement as well as in the admin UI; frozen applications cannot be changed through legacy outlet operations. Recheck slow provider results against current application state/revision before persisting them and refuse obsolete document results. Bind admin history to its selected application. Offer an approved rider the existing private DELIVERY_DUTY selfie operation when the server reports a daily selfie is due; a selfie result does not itself mark the rider ONLINE. Existing active deliveries survive suspension. Read-only seeded legacy document shortcuts are not proof of private browser upload or review. These choices are being implemented and require final local and Oracle validation.

## 2026-10-04T03:54:40+05:30 — Current private-file checks and fresh-schema validator

Require admin approvals in both services to read completed private review files. Current application selfie liveness binds to its exact immutable file; verified operational duty selfies affect readiness, while unverified replacement application files remain unapproved. Initial-only Dev schemas must be validated for the final delivery_executives definition (application_status present, verification_status absent) rather than requiring a legacy ALTER/DROP migration. Test those guards with isolated negative controls before release. Keep hostile-timezone architecture checks and normal approval permissions unchanged.

## 2026-10-04T05:54:23+05:30 — Historical pre-UI-only O3 E2E draft

Use real Oracle public HTTPS and normal Dev Autofill for applicant/admin E2E. Retain owned accounts, applications, organisations, outlets and immutable upload IDs on every failure; no silent reset, seed, approval or SQL write. Versioned review follows explicit provider checks and completed private files. This former allowance to reconstruct a retained cart through browser storage is superseded by the 07:03 UI-only decision: do not inject browser state. Signed private URLs must not appear in evidence or browser error logs.

Preserve current delivery access during suspension. Await the authoritative active-order request before deciding a non-approved rider is idle, show actionable loading/failure/retry states, refresh application status on suspension events and render an owned active job before the offline screen. Verify the real hook and rendered dashboard locally, then verify against Oracle after publication/deployment. These choices are not an O3 deployed completion claim.

## 2026-10-04T05:58:10+05:30 — Owner requires GitHub-only image builds

Direct user instruction: "never use docker, only use github workflows to build images for deployment". Effective immediately, never start or use local Docker and never build/publish deployment images through local or ad-hoc Docker commands. Build deployment images only through the affected repositories GitHub Actions build-and-push workflows, using the existing CI publication path. Required commits/pushes remain authorised. Publication must finish successfully and exact image tags must be recorded before the existing authorised Oracle deployment path runs. Do not choose the local Docker fast path even where an older workflow recommends it. No Docker was started in response to this instruction; the current process check found only the pre-existing network helper.

O3 is incomplete. Keep release/live-E2E/measurements boxes unchecked until the exact proof passes; mark implementation-only items only after current source and local evidence are reviewed. Do not equate prepared/compiled E2E with executed Oracle proof.

## 2026-10-04T06:22:54+05:30 — Owner requires embedded H2 and UI-only E2E

Direct owner instructions: "No never do that 'disposable databases on Oracle' tests should never depend on DBs directly, why no use H2?" and "All E2E tests should only go through UI, no direct backend db or redis connections should be made for now."

Choice: no Docker test fixtures, no external database test fixtures, and no direct DB/Redis access from E2E, including read-only allocation/audit queries. Use embedded H2 for service persistence/rollback/concurrency assertions and documented mocked infrastructure for minimal unit/contract/startup/OpenAPI contexts. E2E business setup/actions/assertions go through real UI controls; observe only the requests/responses those controls produce. Do not use browser fetch/API clients to bypass the UI. Keep PostgreSQL/PostGIS runtime behavior separately unverified until the deployed UI exercises it. This temporary UI-only policy remains in force until the owner changes it.

The earlier Oracle disposable-database choice and unpublished native-PostGIS CI proposal are superseded. The owned SSH database tunnel was closed. Both external database helpers were removed; the same five Delivery concurrency/rollback/suspension cases now pass on embedded H2. Restaurant embedded H2 visibility/concurrency plus compiled-query structural guards pass15/0/0/0. The failed first Restaurant H2 compilation is retained (zero executed tests; wrong DTO package corrected). Prior Oracle/PostGIS results remain historical evidence, not current test setup authority. O3 browser fixture/runner rewrites are in progress and have not run; do not mark deployed E2E complete. Image builds remain GitHub-only and publish must succeed before deployment.

## 2026-10-04T07:03:09+05:30 — O3 UI-only E2E scope clarification

The owner clarified that an E2E test is a real browser user journey: all setup, business actions and
assertions use visible UI controls. Observing a click-generated request/response is allowed. Direct
API clients/URLs, browser `fetch`, DB/Redis/SSH reads, local-storage injection and fixture-side state
changes are not E2E. Docker and external database fixtures are prohibited; embedded H2 is limited to
local service persistence tests. The feature's direct-IDOR, stale-cart, direct-measurement and
outbox/audit cases are deferred in `organisations-and-portal-access/DEFERRED-UI-ONLY-TESTS.md` and
are not passes. This supersedes the earlier limited allowance to reconstruct a retained cart through
browser storage.

## 2026-10-04T07:29:00+05:30 — Fresh Dev V1 baseline cross-link

The authoritative decision is maintained beside the Business Platform plan in
[BusinessPlatform DECISIONS.md](../../../RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md).
It keeps the reviewed Delivery V1-only fresh-Dev baseline and treats the authorised clean Dev wipe,
Flyway bootstrap and dummy seed as a future rollout sequence after publication. It does not restore
old migration scripts, authorize a direct-database E2E fixture, or record any completed publication,
deployment, wipe, seed, or live-E2E gate in this handoff.

## 2026-10-04T08:32:47+05:30 — Current O3 scope leaves legacy Phase 4 files untouched

The owner-limited O3 scope does not authorize edits to the eleven pre-existing >300-line legacy UI
files reported by the Phase 4 static check. Keep that aggregate result at 11/12; it is neither a
pass nor an exception. The authoritative plan decision is
[BusinessPlatform DECISIONS.md](../../../RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md).

## 2026-10-04T08:36:01+05:30 — O3 existing GitHub release path

The eight affected image workflows intentionally delete registry history; that behavior is
task-authorized and does not require a workflow change. Use the existing direct GitHub path, with
UI dispatch `-f deploy_dev=true` only after publication. IdentityService has no O3 diff and is not
an O3 commit/image/stub/contract target; it remains a UI checkout dependency only. No release action
has been taken.

## 2026-10-04T09:37:14.676791+05:30 — Correct current O3 UI integration and preserve failed proof

Use `organisation.list`, because the generated `get` alias selects the by-ID route. Preserve pagination and regression-check the actual generated transport. Match required form labels precisely and isolate seeded UI logins with empty browser contexts, retaining server sessions/data. Rebuild/publish the fixed UI in GitHub, deploy it to Oracle, then rerun real UI journeys. Owner takes legacy 300-line cleanup later; it is deferred rather than passed.

### 2026-10-04T10:23:56.486302+05:30 — Current release access dependency

Choice: keep all four O3 release/private-upload/measurement checks open. The latest UI is published/deployed as1caf57c,845CI tests and exact Oracle digest verified; read-only queue/filter UI method1/1 passes separately. GetBucketCors403 prevents bucket administration with the deployed app key. The user must sign in to the retained Cloudflare dashboard session; no credentials/tokens are created, exposed or broadened. Preserve existing rules and inspect actual public access before applying the prepared exact Dev-origin CORS rule or claiming private KYC. R2 documentation confirms virtual-hosted presigned URLs are supported, so do not change old SDK routing to guess away this CORS failure. Preserve the restaurant-status target failure in evidence59; final performance is unresolved.

### 2026-10-04T13:56:10.198172+05:30 — Preserve private proof and fix current handoffs

Choice: retain real failing invocation7 and all owned applicants. Correct only the current O3 saved-profile callback and wizard refresh integration; leave old profile hooks, customer feed code and multi-outlet helper unchanged. A successful complete server profile clears initial required flags; missing fields/failure stay blocked, and no automatic duty request occurs. Successful manual application refresh also refreshes surrounding brand summary. Customer discovery after cross-session admin approval reloads through the normal browser before a strict card/storefront assertion; the fresh one-outlet brand does not render the multi-outlet chooser. These are local repairs pending GitHub publishing, Oracle deployment and real UI rerun. Private storage boundary is proven by actual visible UI upload/complete/admin view in the passing review journey. No migrations, wipe/reseed or E2E direct-state setup.

### 2026-10-04T14:04:33.403014+05:30 — Published handoff fix and required Dev configuration

Choice: publish only the reviewed current O3 UI integration and regressions with the existing GitHub workflow before selected Oracle UI deployment. Existing repository visibility is already public; no visibility change or local Docker. Follow the explicitly required deploy-ui-only runbook Dev overlay application after the UI image, including controlled sequential restarts of its18 readers. Preserve exact existing image tags, all data, private-bucket configuration and the deployment log. Finish runtime verification before E2E. No wipe, reseed or legacy code change.

### 2026-10-04T14:05:48.665461+05:30 — Verified overlay no-op and isolated UI rollout

Choice: accept the existing deploy workflow exact-checksum no-op for the required Dev overlays. They already match Oracle, so the helper correctly skips publishing and all18 planned reader restarts. Metadata independently proves only UI restarted; preserve unchanged private Government ID9652625 and other images/data. Complete public UI invocation8 on the exact published/deployed b5ab30e image and collect final operator metrics separately.

### 2026-10-04T14:16:06.678712+05:30 — Preserve outlet identity and defer legacy search

Choice: preserve the actual3/4 failure and unchanged owned approved applicant. Source and visible UI prove a legacy mismatch: discovery filters outlet name, while card label is brand name. Renaming a brand must not silently rename its outlet to hide the issue. Leave old search/card/menu code unchanged per the owner, record O3-UI-006 as a known failed/deferred UX assertion, and keep it out of passing counts. Current O3 approval/discovery proof uses normal saved-outlet search, exact renamed-brand card and owned-outlet heading. Fresh dummy applicant opening hours are set explicitly00:00–23:59 with visible controls; no approval/SQL/hours bypass. Rerun only the corrected restaurant method on the unchanged verified b5ab30e image; retain the three current-image passes from invocation8.

### 2026-10-04T14:26:25.838614+05:30 — Current O3 release acceptance and limits

Choice: accept the four required current-image O3 methods using the three unchanged passing methods from invocation8 plus the successful corrected restaurant method in targeted9. Retain both original failed full runs and all owned data; do not call this a new full-green invocation or repeat unrelated writers. Use the plan-permitted protected operator server histograms for endpoint latency acceptance; preserve supplemental browser timings and cold/small failed baselines. Runtime audit must compare since-start errors with the prior baseline, preserving the same four historical infrastructure records instead of demanding or claiming zero lifetime errors. All current configuration/tag/health/log gates pass. Complete only O3 owner scope with explicit old-search/UI-only/wait/rate/300-line deferrals. Later phases and production-provider/load validation remain unimplemented or unverified as recorded.

### 2026-10-04T15:52:35.149522+05:30 — Resume remaining Business Platform phases

Choice: resume the remaining Business Platform phases in dependency order, starting O4 after the recorded O3 current-image four-method acceptance. Retain the earlier legacy-search/300-line exclusions while the scope question is pending. Required auth/harness/service changes named by the current plan are in scope. Use fresh initial schema definitions and matching dummy seeds rather than migration/backfill statements. Existing task authorization covers required commits/pushes, GitHub-only publication before Oracle Dev deployment; no local Docker. E2E remains visible UI only, persistence fixtures embedded H2. Earlier wait/rate/SSE deferrals remain in force. No new deployment, data reset or E2E has occurred at this baseline.

### 2026-10-04T16:02:22.369399+05:30 — Include legacy search and component-size cleanup

Choice: the owner explicitly reopened legacy brand-name search and the 300-line component cleanup. Fix discovery to match the displayed brand as well as the saved outlet and cuisine without renaming fixtures. Refactor every component flagged by the existing UI redesign validator through cohesive extraction; keep its unchanged 300-line threshold and exclusions. Complete these UI items with meaningful regression checks, GitHub publication, Oracle deployment and visible UI proof before continuing O4. Preserve all earlier failed and passing O3 evidence; a local fix alone does not close the deployed search gap. This supersedes the previous owner exclusions and the pending scope question.

### 2026-10-04T16:17:41.581786+05:30 — Publish reopened O3 UI fixes

Choice: publish UI d79c33ae644b2924112332a6bba48e0c143272da using existing GitHub workflow37196297954 after local856/856, lint/typecheck/build and full redesign13/13 gates pass. The repository is already public, so no visibility change is needed. Retain existing workflow image-history deletion behavior. Only this independent UI batch is committed; unfinished O4 Common/Identity foundation stays local/unpublished. Compiled read-only E2E source29e9dff reuses the retained approved invocation8 applicant; no lifecycle writer or fixture reset is needed for brand-search proof. Oracle baseline remains29running/26healthy/3withoutchecks, zero drift/restarts/recent errors and four unchanged historical records. Await successful image publication before deployment.

### 2026-10-04T16:42:23.916935+05:30 — Close reopened O3 UI items and start coherent O4 auth work

Choice: accept O3-UI-006 only from strengthened current-image invocation11 and accept the four required O3 methods from the single green invocation13. Preserve traversal-only10, sandbox startup failure12 and all previous failures. The same GitHub-published Oracle image also passes unchanged300-line/redesign gates; no owner exclusion remains for those two items. Keep unchanged UI-only/wait/rate/SSE deferrals and real production provider/load gaps. O4 DB versions are changed atomically, excluded from ordinary dirty-user updates, and mirrored only after commit through monotonic cache writes with durable publication retry records. Transfer batches both users in one publication; organisation DTOs batch businesses rather than query per row. These O4 choices have local H2 proof only and are unfinished/unpublished. Continue whole plan; no automatic fixture cleanup.

### 2026-10-04T17:12:45.532346+05:30 — One login security and staff administration

Choice: only staff ADMIN assignments are writable; CUSTOMER/RESTAURANT/DELIVERY/BUSINESS come from active profiles, approved projections and active organisation membership. Admin user lists compute these roles in batches and role-filtered pages use database predicates, preserving pagination. Keep a final active administrator and refuse self-suspension/removal. Remove unused user_devices.portal and old AuthPortal/service-name role aliases from the complete fresh V1 schema, without legacy migration. Refuse changing the sign-in phone through the unverified profile edit endpoint; existing name/email completion stays available. OTP consumption is atomic and deferred until session capacity/replacement checks pass, so409 retains the code. Admin sessions count toward the same three-person-session cap and may replace another owned session but cannot replace the everyday session calling step-up. Version mirrors live for twice the longest configured everyday/admin token lifetime. Require an ev claim on protected fresh-release JWTs; deploy through the authorised clean Dev reseed, not old-token compatibility. Local auth33/33 and gateway30/30 pass; none of O4 is published or deployed yet.

### 2026-10-04T17:21:35.186761+05:30 — O4 browser proof follows UI-only policy

Choice: replace the obsolete O4 E2E draft API/DB/token assertions with visible login, portal switching, profile completion, invitation acceptance, owned-applicant approval/revocation and separate admin step-up journeys. Exact claims/keys/storage/expiry stay in service/H2/gateway/UI unit checks. No browser storage/token decoding/injection, product API requests or direct DB/Redis/SSH in E2E. Keep deferred internals/waits/rate/SSE outside passing totals; publish before task-authorised fresh clean Dev deployment.

## 2026-10-04T17:43:58.319159+05:30 — O4 dispatch identity and strict negative membership checks

Every normal/retry dispatch reads the authoritative customerId from the existing signed Customer dispatch-details endpoint and merges it into exclusions before publishing. This adds one Customer RPC per dispatch attempt; no extra database connection is held across the call. Manual assignment checks the same immutable customer identity before mutations and records SELF_ASSIGNMENT as a terminal audited refusal. Preserve the existing retry/discovery/reservation flow. Exact review authorization remains Customer-owned: one fresh strict membership check per customer batch containing outlet/product targets, no check per dish. A confirmed membership404 means absence; malformed responses/outages503 refuse the review, never grant by a false-negative operational cache result. Ordinary operational permissions retain the approved60second stale grace. A CUSTOMER member can still rate another rider; RESTAURANT cannot rate themselves as CUSTOMER. These choices have local unit/H2/service proof only and require the coherent O4 release.

## 2026-10-04T18:17:32+05:30 — O4/O5 coherent UI-only release and verification discipline

Choice: implement O5's dependent UI after local O4 contract proof and publish/deploy O4+O5 as one release. O4's member invitation/removal/revocation E2E has no permissible setup before the O5 UI exists under the owner's UI-only rule. Both gates and regressions remain mandatory; do not mark either phase complete from local tests. This corrects the original standalone O4→O5 deployment order, without opening wallet/ads work early.

Choice: run the existing `specs` Maven profile for changed endpoint/DTO modules before UI generation, then wait for generation to finish before typecheck/lint/tests. Keep the parent's normal spec exclusion unchanged. Mixed-source or overlapping checks are diagnostics only; preserve failed invocations separately.

Choice: the gateway reads the person's owned active session record in the same Redis multiGet as blacklist/version. Validate purpose and token expiry within absolute lifetime; absent or malformed state denies access even after Dev data/session wipes. No additional Redis round trip or negative session cache. Local tests and deployed performance measurements are required before release.

Known boundary to verify/document: missing version is0 only under the planned normal TTL/no-eviction/persisted Redis premise. Durable after-commit version publication retries every5seconds; an isolated cache write failure with healthy stale reads does not prove instantaneous revocation. Do not represent that outage case or arbitrary early version-key deletion as passed live evidence. Internal fault setup remains deferred under the UI-only policy.

## 2026-10-04T18:58:39+05:30 — O5 contract validation, fresh fixtures and header packaging

Choice: retain the existing global SDK validate:false setting while adding explicit generated-schema validation to the new portal/organisation/session read models and login responses. Existing legacy void-response specs prevent safely enabling it for every old client in this release. Do not claim global generated response validation. Malformed new contracts must fail with a visible error rather than empty data or a fabricated token.

Choice: present organisation names in person invitations through a batched Identity response; omit optional null reason/name fields using NON_NULL for the new wire models. Keep this in the combined O4/O5 contract release.

Choice: package security headers in one nginx include and include it at server, SPA and cached-asset scopes because nginx add_header inheritance stops when a location declares its own headers. Origin entries come from source and must be checked against a recorded normal UI run of every portal. No live CSP acceptance until that run passes.

Choice: remove remote allocation/audit from the registration E2E runner. Allocate and retain local phone candidates; the normal UI must show fresh-profile completion, otherwise reject the collided candidate clearly. Do not infer uniqueness from a random number or query application databases. Retain all created accounts/sessions and manifests on success and failure.

Choice: preserve failed UI invocations separately. Closing the launcher invalidates pending navigation; reopening remounts its data request. No length, style, auth or response-validation gate is exempted to pass these changes.

## 2026-10-04T19:36:11+05:30 — Resume and O4/O5 scope/fixture choices

Owner requested a brief pause, then resume; task-specific commits/pushes, unchanged GitHub publication and Oracle Dev clean wipe/fresh seeding remain authorised. Publish before deploy; never local Docker; embedded H2 service tests and rendered UI-only E2E remain binding. Use existing restaurant-brand Suspend/Reinstate controls for the original O4 suspension gate; remove the accidental extra admin organisation screen. Preserve the calling everyday SID in admin step-up replacement. Restaurant launcher state belongs to the selected organisation, with a volatile per-person context fallback if persistence is disabled. The E2E URL is normalised without a trailing slash; fresh phone candidates are retained locally and confirmed through normal profile UI, never allocated/audited through a database.

## 2026-10-04T19:53:32+05:30 — Current guard and schema choices

Use actual aliased H2 UPDATE inspection for the50-person counter batch and dispatch fixtures that do not already exclude their customer. Keep the guard source restored before publication. Register the two new delegated consumers at their measured one envelope read, without raising old ceilings. Replace retired SessionInfo.serviceName strictness with required purpose/createdAt/absoluteExpiresAt. Review fresh Identity SQL through embedded H2 and explicit dialect bridges; native PostgreSQL bootstrap is a separate deployment check, never an external DB test fixture. Common publication37208293403 succeeds before dependent images.

## 2026-10-04T20:07:27+05:30 — Full registry validation and committing H2 suite isolation

Choice: inspect every cached session row before accepting an owned match; reject invalid purposes/expiry and duplicate IDs using the existing single Redis multiGet. The added registry test retains valid multi-device acceptance.

Choice: isolate the committing entitlement consumer suite in its own embedded H2 database. CI's pagination failure is fixture contamination; do not change query semantics, increase expected counts or use an external database. Preserve failed publication37209498593/149tests/one failure and verify the CI profile with consumer-before-pagination ordering before retrying.

Choice: preserve CSP evidence in a finally block and accumulate header failures before asserting. Static locator defects are repaired against current rendered source; dynamic data and intentional absent-control assertions need explicit source dispositions and live proof, with no exemptions. No live acceptance or global locator green is claimed.

## 2026-10-04T20:25:44+05:30 — Owner stop boundary: complete O4 and O5 only

Direct owner instruction: "sure complete those and stop after phase 4 and phase 5 are completed."

Choice: finish the combined O4/O5 source, unchanged GitHub publication/contract workflows, authorised Oracle Dev fresh wipe/seeding/deployment, UI-only E2E/regressions, measurements and accurate checklists. Stop once both phases meet the current acceptance scope. Do not start W1–W3 or A1–A4. Standing task commits/pushes and publish-before-deploy authorisation remain applicable to this completion; no local Docker or direct-state/API E2E. Explicit existing wait/rate/SSE/internal proof deferrals stay visible and are never counted as passes.

## 2026-10-04T20:37:04+05:30 — O4/O5 telemetry and proof boundaries

Choice: protect non-health Gateway Actuator GETs with verified fresh SERVICE signatures, enable metrics/prometheus/histograms, and remove env exposure. Actuator mutations remain denied. Gateway clean58/58 and image37211376848 pass. Record operator telemetry separately from rendered UI E2E; the unavailable old Gateway histogram baseline cannot be reconstructed or called an unchanged-latency pass.

Choice: defer the deliberately removed CSP-origin browser break test as O5-CSP-001 under the current UI-only rule. Preserve the negative unit/static probes and run positive headers/origin/console checks on all deployed portals. Do not deploy intentionally broken CSP to shared Dev or inject/intercept browser state. Stop after O4 and O5 are complete; Wallet and Ads phases remain outside this resumed scope.

## 2026-10-04T20:47:42+05:30 — Runtime packaging and UI-only regression choice

Choice: include Reviews common-persistence at runtime; it is required by actual messaging imports. Tests previously had it on the test classpath and could not detect the missing packaged library. Preserve GitHub workflows and repair only the service dependency. Fresh volumes are already wiped; resume without another wipe. Local97/97 plus nested runtime-class proof is separate from pending replacement deployment.

Choice: use passive UI-generated eligibility/money responses in the required regression journey. Keep synthetic chat retry/socket-close fixtures and three older direct aggregate/eligibility cases deferred under O4-INT-002; do not count them as passes.

## 2026-10-04T20:52:59+05:30 — HEAD health probe compatibility

Choice: permit GET and HEAD only on Gateway health paths, preserving the existing remote wget --spider health probe. All non-health Actuator HEAD requests and mutations remain denied; signed SERVICE GET telemetry remains protected. Initial af80a72 has GET health UP but the remote HEAD probe is401, so clean deployment invocation1 timed out before the four strict-chain services. Corrected0b729f0 passes58/58 clean local tests including root/readiness HEAD probes and negative non-health HEAD/health POST. Publish the replacement first, then resume Gateway/Reviews and the four unstarted services without another wipe. No browser E2E is run on that failed rollout.

## 2026-10-04T21:08:57+05:30 — Portal model correction and completed rollout

Choice: preserve the source model that omits unavailable ADMIN for non-staff; assert tile absence, not an Unavailable tile. No UI behavior is changed. Gateway/Reviews runtime repairs are published before selected redeployment; fresh seeds have now completed successfully. Run the positive browser gates only on this healthy seeded rollout, retaining all fixtures.

## 2026-10-04T21:30:26+05:30 — SDK get alias and portal setup choice

Use the generated organisation get(options) alias with typed UUID params; it shadows the SDK generic get(path, options). Preserve generated source and endpoint contracts. Add actual generated-client/shared-fetch request coverage, then publish before UI redeployment. Portal E2E dismisses the existing Customer location dialog through its visible Close dialog control; never force a click through it or inject browser/address state. Failed invocation1 remains retained, not passing coverage.

## 2026-10-04T21:41:52+05:30 — Passive rider preflight, single financial fixture and measured baseline limit

Choice: observe the real offline dashboard profile before an Online click; require the actual tracking socket DUTY_STATUS and recent outgoing location before checkout. Preserve ON_DELIVERY. Reuse one canonical UI-created delivered order for completed-trip, unrated-review, money and earnings regressions; remove synthetic responses/request interception. All created fixture records remain retained. Record Gateway latency now and defer the unavailable historical comparison as O4-PERF-001; numeric httpStatusCode labels determine successful samples. No direct product/DB/Redis tests or fabricated baseline.


### 2026-10-05T05:03:22+05:30 — O4/O5 exact R2 host and addressless portal setup

Use the actual signed private bucket virtual hostname in CSP, retaining explicit origins and same-origin scripts. Local config tests read the repository file directly because Vitest transforms import.meta.url to HTTP. In UI portal setup, select normal GPS when the real location dialog is open; Close can race the initial empty address response. No browser-state injection, forced clicks, credentials change, second wipe, or cleanup. O4 invocation2 remains5pass/1failure/2errors/0skips until repaired live proof. Resume authorized work; stop after phases4and5.


### 2026-10-05T05:08:45+05:30 — Respect Close in the product, then prove it through UI

GPS selection works on the existing deployed UI: one targeted approved-rider portal case passes, zero failures/errors/skips, with no new profile/order. However the underlying slow empty-address response still reopens a dialog the user closed. Correct the product hook so only disappearance of a previously selected saved address prompts again; initial addressless entry already opens the dashboard dialog. A new delayed-response unit preserves Close, existing vanished-ID/GPS/reconciliation cases remain. Local14/14 pass; typecheck/lint running. Harness reverts GPS setup to the real Close control to prove the repaired UX live. Current GitHub37244246232 builds CSP-only73058ad; publish the tested address follow-up afterward and deploy the final image, preserving publication before deployment. No new data reset or Cloudflare credential changes.


### 2026-10-05T05:20:57+05:30 — Real confirmation and explicit availability refresh

Wait for the actual200 private-document completion and visible uploaded label rather than treating the normal stale-entitlement401 intermediate attempt as final. Give users an explicit portal availability refresh; do not poll a closed launcher. Observe asynchronous application decisions through that visible control, bounded10seconds, with attempt/time evidence. Common outbox interval is2seconds. Keep the suspended failed fixture and unused member candidate documented; no cleanup or financial duplicate. This corrects the preliminary chat description of a membership-removal failure: it was unreached upload setup.


### 2026-10-05 — Observe recovery and projection through the real UI

Upload observers now count only actual401responses carrying X-Auth-Reason=ENTITLEMENTS_CHANGED; retain those counts alongside200+Uploaded proof. Initial invocation3classification was inferred from the central transport source, not directly observed. Restaurant/rider approval follows the same two-second asynchronous read-model bridge as suspension; use the explicit visible refresh with a10second bound, recording actual attempt count/time. An independent targeted revocation subset runs on the existing deployed image while the new portal control image builds; no financial order or cleanup.


### 2026-10-05 — Targeted revocation subset reveals stale owner view

Subset1 on ba3650b/e55e063:1test,0passes/0failures/1error/0skips. Both uploads confirm200+visibleUploaded with0observed completion401challenges. Brand approval, invitation acceptance and STAFF operational access pass before the owner tries removal. Owner page still shows only its pre-acceptance membership and PENDING invitation; its real hook reloads on focus. Reopen All organisations→Open organisation→Members through real controls, assert the accepted member row, then remove. No backend role defect is inferred; the owner snapshot was never refreshed in the harness. Owner9999059061/member8999949211 remain a retained approved/active STAFF fixture because removal was not executed. No financial order. Compile21/push running.

UI2aed6af published successfully by unchanged GitHub37245286714 and deployed via existing UI-only script (invocation5). Reviewed Dev configs, hardening/reconciliation and metadata still pending this rollout. Local43/43/typecheck/lint and locator13/13 pass. Full O4 invocation3 remains6pass/2failures/0errors/skips; final positive O4/O5 gates/regressions remain open.


### 2026-10-05T05:36:28+05:30 — Provider-check completion uses final response

Extend final200synchronization to driving-licence,RC,bank,selfie checks and rider submit, which share central single entitlement refresh/retry. Step controls remain gated by authoritative visible verification state; a refusal still fails. All-origin CSP/network and actual decoded-image smoke passes; retain failed rider draft and all allocated/created records. O5 invocation1 remains7/8until full corrected rerun.

## 2026-10-05T05:42:32+05:30 — O4/O5 live gate closure and canonical regression fixture

Both eight-case UI gates passed on the published Oracle release. Reuse one fresh retained seeded-account delivery order for required downstream regressions; respect ordinary OTP limits and keep all prior failed fixtures. Current UI publication remains2aed6af; harness-only response synchronization does not require a new application image. Stop after phases4and5; the named internal, historical-baseline and deliberate-CSP-negative deferrals remain explicit.

## 2026-10-05T05:44:13+05:30 — Rider preflight evidence type

Emit the observed location age as a Playwright-supported Double, while keeping the server/socket/recent-fix readiness checks unchanged. Lifecycle invocation1 stopped before checkout and created no order. Rerun once after harness validation; later failures after an order exists must resume that exact retained order.

## 2026-10-05T05:56:52+05:30 — Retained-order polling and provisioning regressions

Repair the actual selected-outlet dependency and verified-customer concurrent insert within required O4/O5 regression scope. Retain1a375719 and resume it only while active; never repeat checkout as a workaround. Save an order manifest immediately after receiving its ID, before optional post-response assertions. The browser renders the selected card method and the authoritative returned paymentMethod; a streaming Request may not expose Playwright postData, so do not silently claim request-body inspection. Publish all application repairs before deployment.

## 2026-10-05T06:04:00+05:30 — Replacement after proven terminal order

Rendered UI proves1a375719 CANCELLED_BY_RESTAURANT. Keep that manifest and failure proof; after publication/deployment of verified kitchen and Customer repairs, create one replacement canonical delivery for required phase regressions. This exception does not allow duplicate active checkouts. The state probe records only rendered status/visibility/timestamp, with no API response-body parsing or raw tracker text. The initial rejected diagnostic was never executed. No migration, reset, seed or financial-provider mutation is needed.

## 2026-10-05T06:16:59+05:30 — Preserve native absent-body transport behavior

Actual generated accept/reject/cancel requests reproduce the incorrect form Content-Type for no body. Correct the shared adapter's effective-body handling, preserving JSON/multipart and ordinary auth refresh, then publish/deploy before resuming. Retain0dbe6a15 and original1a375719. Never bypass the UI or create duplicate active orders; a further fixture is justified only after rendered UI proves a retained attempt terminal and its cause has been repaired. No timeout bypass, reset or seed.

## 2026-10-05T06:33:00+05:30 — Canonical fixture after both repaired terminal attempts

Both1a375719 and0dbe6a15 are now proven CANCELLED_BY_RESTAURANT through rendered UI. Keep both manifests/failure/state evidence. The selected-outlet polling and absent-body transport causes are repaired/published/deployed. Create one fresh canonical lifecycle on UIe055ea1 for required O4/O5 regressions, saving the ID immediately; never duplicate active checkout or bypass timeout/DB/Redis/UI. Resume that exact new fixture if recovery is necessary. No reset/seed or refund submission.

## 2026-10-05T06:40:58+05:30 — Serialize visible kitchen progression with server completion

Keep60631296 assigned/PREPARING and resume it; no duplicate order. Ready400 is a real optimistic UI race, not a pickup-code locator change. Await successful kitchen mutation before rendering its next status/action, and release busy state on settled success/refusal. Verify actual-component and deferred-request guards, publish through GitHub, deploy, then resume. The grouped outlet label is required O5 UX; preserve it and update existing reload assertions to compare the complete visible choice.

## 2026-10-05T06:46:38+05:30 — Confirm kitchen changes before advancing; reuse existing journeys

Choice: keep the current kitchen state/action busy until its real save completes, publish the confirmed state after success, and release busy on completion. An optimistic PREPARING render had enabled Ready before prepare committed and produced a real400. Use existing Happy/financial/review/read-only tests on the retained owned order; new local tests target only this demonstrated race. Retained checks read normal rendered History/receipt and combined GST, observe order writes passively, and never intercept product traffic or invent a tax split. Publish through GitHub before Oracle deployment; stop after O4/O5.

## 2026-10-05T06:56:59+05:30 — Existing owned rider-action observation and shared teardown

Choice: match the real UI action response by POST and exact owned URI path, then assert the expected visible phase, avoiding request-body metadata dependency. Arrival was saved even while the old observer timed out. Reuse TestBase's existing idle-rider teardown; remove the extra call added in57eca7b. Retain the failure and same assigned fixture; no new checkout or backend mutation.

## 2026-10-05T11:20:29+05:30 — Use the issued sessionId claim and observable theme completion

Choice: ActiveSessions and AdminStepUp use Identity AuthService's issued sessionId claim; existing fixtures use the same wire contract. Do not add a sid fallback for nonexistent deployed history. Keep the two original settings failures and rerun only those cases on the published replacement. Theme E2E waits for rendered CSS endpoint colors instead of sampling mid-transition. No live session deletion or duplicate happy order is needed. Scope: O4/O5 final release only; stop after these phases.

## 2026-10-05T11:26:21+05:30 — Correct canonical rider-name input, preserve original failure

Choice: the existing RestaurantNavigationUiTest uses expected rider name `Rider 1`, exactly as deterministic dummy_riders.sql/dummy_riders_customers_identity.sql and the real UI-generated history response. The prior invocation's command supplied `Rider1` and failed the strict enrichment assertion; retain its1failure/3passes. No product or test assertion is changed. Rerun only restaurantSectionsRender on the replacement UI; nonzero earnings/statement, public saved-review/aggregate and admin delivered-money already passed and are not repeated.

## 2026-10-05T11:42:24+05:30 — Resolve an outlet beneficiary through its owning brand

Choice: correct the demonstrated RESTAURANT_PAYABLE outlet-id/brand-id mismatch in the internal Restaurant beneficiary lookup. Read Outlet first, then its brand; retain the existing route, masked output and SERVICE/ADMIN restriction. Missing outlet/brand remains404, never a guessed beneficiary. No schema, migration, data reset, payout action or new Wallet/Ads phase. Prove locally, publish by GitHub before Oracle service deployment, then verify the retained payee through real read-only admin UI.

## 2026-10-05T11:46:20+05:30 — Report the current accepted bank callback states consistently

Choice: beneficiary verification treats both APPROVED and VERIFIED as passed, matching the existing O3 RestaurantApplicationService callback contract and Government ID producer. The deterministic seed and real callback use APPROVED. A new actual HTTP assertion catches the previous false verified flag (4passes/1failure); fix that read projection without changing bank checks, approval rules, schemas/seeds or money. The old outlet-to-brand bug was independently caught by4 failing assertions, then4passed after the lookup correction. Full clean Restaurant/H2/contracts proof is running.

## 2026-10-05T11:57:12+05:30 — Accept O4/O5 and stop

Choice: complete O4/O5 within current Dev/UI-only boundaries after successful GitHub publication, Oracle rollout, final8+8 UI gates, required existing regressions, the demonstrated beneficiary correction and current portal p95 proof. Retain every original failure and owned fixture. Record missing historical Gateway latency as O4-PERF-001 and deliberate CSP mutation as O5-CSP-001; internal/duration/rate/SSE/provider/load/parked ONDC limits remain unverified. Do not count deferrals as passes. No further wipe/seed/automatic cleanup or financial action. Stop after Phase5; W1–W3/A1–A4 are unstarted and require a later instruction. The entire platform is not declared production-validated.
