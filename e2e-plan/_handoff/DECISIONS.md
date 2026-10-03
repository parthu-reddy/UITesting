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
