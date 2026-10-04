# Organisation and portal access scenarios

O1 implements APIs only; launcher/member-management UI belongs to O5. Test source is grounded in
IdentityService’s organisation controllers and services. Ordinary per-portal customer signup and
Dev Autofill Code remain unchanged until O4.

| Scenario | Assertion | Owning method |
|---|---|---|
| ORG-01 | A creates one organisation, is OWNER, and sees it listed | OrganisationLifecycleApiTest.organisationLifecycle |
| ORG-02 | Unrelated B sees 404; duplicate invite sees 409 | same |
| ORG-03 | B sees own invitation and accepts MANAGER; reads organisation | same |
| ORG-04 | MANAGER cannot invite; STAFF cannot rename; OWNER assignment via role PATCH rejected | same |
| ORG-05 | Removed B sees 404 and may accept a new ADMIN invitation | same |
| ORG-06 | Transfer makes B the sole OWNER and A ADMIN; A cannot remove OWNER | same |

The run requires two unused customer phones allocated read-only by run_organisation_o1_e2e.py.
It retains all created accounts, sessions, invitations and organisation records. No checkout, rider
duty, financial action or record cleanup is part of O1. After owner deployment, check the exact
organisation in identity_db read-only for one active owner and processed outbox rows.

## O2 scenarios under implementation — 2026-10-03T18:28:21+05:30

| Scenario | Required assertion | Owning method |
|---|---|---|
| ORG-07 | Seeded owner has one organisation and Brand 1 belongs to it | OrganisationRestaurantAccessTest.organisationRestaurantAccess |
| ORG-08 | Fresh 9999 restaurant signup accepts STAFF invite and sees Brand 1 outlets | same |
| ORG-09 | Staff UI stock off/on persists; price change and earnings APIs reject 403 and UI says Not permitted | same |
| ORG-10 | Promotion to MANAGER loads figures matching owner | same |
| ORG-11 | Removal is rejected after at most 5 s cache TTL; unrelated owner cannot modify outlet | same |
| ORG-12 | New internal restaurant endpoints reject public gateway access | same |

Pending O2 deployment, authorized clean wipe/fresh seed and complete phase regression. No O2 live fixture has been created yet.


## 2026-10-03T23:39:44+05:30 — Cache corrections pass focused guards and negative controls

Restaurant raw membership cache implemented only in its list service:5second expireAfterWrite,10000bound, immutable raw rows, coalesced caller lookup, permission/status filters on every call, no entity/grant/stale caching. Independently cold1/20JPA guard remains one call/one query each. Focused11/0/0/0; TTL60negativecontrol1/1/0/0 and source restored. Reviews events now register inside transaction after all writes; synchronous AFTER_COMMIT eviction. Actual Spring/JDBC commit/outer-rollback/outbox-failure guard3/0/0/0 plus command12/0/0/0. First guard batch3/2/0/0 was a test assertion over Spring bean init callbacks, corrected to no Redis delete; no runtime change for that correction. Original outside-transaction mutation first3/0/1/0 (listener-wrapped observer assertion) is retained; move observation assertion after command, second3/1/0/0 catches the exact bug without wrapper; source restored. Both full clean verify builds are running.

Additional existing OrderReviewsFlowTest method remainingDishReviewRefreshesTheCachedAggregate compiles in invocation2; first compile-only wildcard assertion error retained. It checks original two customer reviews unchanged/read-only, warms a previously unrated owned product aggregate twice at0, submits exactly one5-star dish review through normal UI, persists submitted manifest before further assertions, and requires the immediate post-POST aggregate1/5.00 plus read-only reopening. Once submitted, never repeat it; inspect partial manifest instead. No replacement order, manual OTP, cache clear, reset or financial writes. Needed exact-head producer/consumer publications and two ARM64 images before unchanged Oracle targeted rollout; live fresh-write proof, corrected public aggregate and final brand/outlet original budgets remain open. O2 incomplete; O3+ unstarted.

## 2026-10-04T00:40:47+05:30 — O1 and O2 complete; O3 next

The published Oracle Restaurant5b93a8d-29997b7 release passed final5-case regression batch and2-case same-order chat batch, zero failures/errors/skips. Original brand/outlet budgets pass: p95estimate31.317/53.687ms, enclosingbucket33.554/55.924ms (limits50.199/83.753ms and150ms). Oracle29intended/running, no image drift or automatic restarts; all configured healthchecks healthy; hardening15PASS and report-only reconcile29/0drift. Required full Dev wipe/fresh seed is checkpoint40 history, not rerun during corrections.

Latest checkpoint:41-business-platform-o2-regressions.md, final completion entry; durable41-final-o2-gate.json and individual reports. Six immutable reviews and canonical ordere82f8c51 remain; riderOFFLINE, restaurantnet19.11. Never repeat review writers or replace retained failures. O2 checklist is now fully marked with evidence. O3+ unimplemented; full platform incomplete. Next implement O3 Partner Applications from source/plan, recording scenarios first. Earlier dated entries are historical.

## O3 planned scenarios — 2026-10-04T00:43:48+05:30 (historical pre-UI-only draft)

These are planned, not executed. This table preserves the original pre-UI-only draft, including its
API-oriented assertions; it is superseded for browser work by the 2026-10-04T07:03:09+05:30 scope
below. Do not restore direct calls, infrastructure reads or browser-state injection from it.

| Scenario | Assertion | Planned method/class |
|---|---|---|
| BP-O3-REST | Own draft→submit→provider checks→IN_REVIEW; customer cannot list/order; admin reject reason; edit/resubmit→approve; public visibility; unrelated organisation refused | RestaurantApplicationApiTest |
| BP-O3-DELIVERY | Own draft/document uploads→IN_REVIEW; cannot go on duty; principal-derived summary and old IDOR path404; admin approval permits online, then confirmed idle OFFLINE | DeliveryApplicationApiTest |
| BP-O3-ADMIN | Actual documents/check results; required reject reason; owned restaurant reject/rider approve leave queues; seeded pending brand is observed only | AdminPartnerApprovalsUiTest |
| BP-O3-HIDDEN | Seeded IN_REVIEW/REJECTED outlets absent public nearby/by-id/catalog; quote/add-to-cart clear4xx and UI message | UnapprovedOutletHiddenTest |
| BP-O3-SUSPEND | Real transactional guard: active ON_DELIVERY assignment survives suspension; future duty blocked | DeliveryApplicationServiceTest |
| BP-O3-CONCURRENT | Two real transactions/admins compete; one status+outbox+audit commits, loser409 current status | ConcurrentApplicationDecisionTest in both services |
| BP-O3-UPLOAD | MIME/declared-size allow-list; signed length and object HEAD; server key/purpose ownership; exact self prefix; audited admin document download | VerificationSelfAccessTest and R2 guards |

After rollout record each invocation separately, outbox publication versions and p95 of admin queues≤300ms/onboarding reads≤150ms. Preserve all old failure evidence; never claim planned/local proof as deployed completion.

### 2026-10-04T02:46:52+05:30 — Local O3 proof boundary

BP-O3-SUSPEND has32-case lifecycle batch proof, but actual persisted assignment/rollback proof remains pending. BP-O3-UPLOAD adds frozen review writes and separate daily-selfie assertions (45-case focused batch). BP-O3-HIDDEN includes actual annotated PostGIS queries across all six states plus restored negative control; its Oracle UI/API scenario remains unexecuted. All BP-O3 E2E rows remain pending publication/deployment.

## 2026-10-04T03:54:40+05:30 — Additional O3 scenarios

| Scenario | Assertion | Proof owner |
|---|---|---|
| BP-O3-LATE-CHECK | A blocked bank/DL/RC/selfie provider cannot write after submit, changed revision or file replacement | VerificationInFlightGuardTest, local7cases |
| BP-O3-PRIVATE-APPROVAL | Provider booleans alone cannot approve without completed required private review files | Restaurant/DeliveryApplicationServiceTest |
| BP-O3-DUTY-SELFIE | An explicit DAILY_SELFIE_REQUIRED refusal offers own operational private upload; success syncs readiness and remains OFFLINE until a normal duty request | DailySelfiePrompt/useRiderDuty tests, Oracle pending |
| BP-O3-HISTORY-SELECTION | Late history from a different selected application never appears in current detail | PartnerApprovalsPage.test |

## 2026-10-04T07:03:09+05:30 — O3 UI-only E2E scope

The owner supersedes the earlier API-driven O3 E2E wording. Every O3 browser journey must use real
visible UI controls on the deployed public application. It may observe the request or response
caused by a click, but it must not use `GatewayApi`, browser `fetch`, direct URLs/API clients,
database/Redis/SSH access, local-storage injection, or a Docker/external-database fixture. The
legacy `*ApiTest` filenames stay only because the validator names them; they are not permission to
perform API-driven E2E.

| Scenario | UI-only journey to implement | Planned browser class | Current status |
|---|---|---|---|
| BP-O3-REST-UI | Sign up as Restaurant Partner with normal Dev Autofill, complete the restaurant wizard including visible document controls, observe the submitted/review status, let an admin reject with a visible reason, edit/resubmit through the wizard, approve through Partner approvals, then search from the customer UI for the resulting approved outlet. | `RestaurantApplicationApiTest` (legacy filename) | Deployed UI invocation7; see current result table below. |
| BP-O3-DELIVERY-UI | Sign up as Delivery Executive, complete the rider wizard and visible document controls, observe the review status, attempt the rendered duty control, approve from Partner approvals, then confirm the rendered rider state/duty control. | `DeliveryApplicationApiTest` (legacy filename) | Deployed UI invocation7; see current result table below. |
| BP-O3-ADMIN-UI | Create fresh restaurant and rider applicants through their visible wizards; in the admin UI inspect each rendered application, require a rejection reason, reject/approve through real buttons, and observe each applicant's visible result. | `AdminPartnerApprovalsUiTest` | Deployed UI invocation7; see current result table below. |
| BP-O3-HIDDEN-UI | Log into the read-only pending/rejected seeded applicants to observe their rendered status, then use customer discovery/search UI to confirm their brands are absent. | `UnapprovedOutletHiddenTest` | Deployed UI invocation7; see current result table below. |

Current-session local source evidence is
[checkpoint46](../../_handoff/checkpoints/46-o3-current-local-gates.md) and its
[evidence record](../../_handoff/evidence/46-o3-current-local-gates.json): fresh 179-source test
compilation, a zero-match prohibited-pattern source audit, and current selected/local service and
UI validation. The dated 178-source no-recompile snapshot remains historical only. These are not
browser evidence. The O1/O2 direct paths are
[deferred, not executed, and not passed](DEFERRED-O1-O2-UI-ONLY-TESTS.md). None of this replaces a
deployed UI execution.

The direct endpoint/IDOR, stale-cart hidden-outlet, direct measurement loop, and outbox/audit
infrastructure assertions are explicitly deferred in [DEFERRED-UI-ONLY-TESTS.md](DEFERRED-UI-ONLY-TESTS.md).
The elapsed-time and Dev-rate-limit cases are separately deferred in
[DEFERRED-WAIT-TESTS.md](DEFERRED-WAIT-TESTS.md) and
[DEFERRED-RATE-LIMIT-TESTS.md](DEFERRED-RATE-LIMIT-TESTS.md). Deferred is not a pass.

## 2026-10-04T09:37:14.676791+05:30 — First O3 public UI invocation

The four current O3 methods executed through the public Oracle UI and all failed: restaurant wizard setup, rider required-label selector, admin restaurant setup, and second seeded-applicant login. Totals: 4 tests / 2 failures / 2 errors / 0 skips. Retain each failure and its data; no scenario is passed. See checkpoint52. Local 19-test UI repair and 180-source compilation are distinct from live proof.

## 2026-10-04T10:14:06.424177+05:30 — Read-only queue/navigation supplement

BP-O3-QUEUE-UI: AdminPartnerApprovalsUiTest#reviewQueuesNavigation logs in through normal Dev Autofill, opens/refreshed both queues, confirms retained pending seed rows, and passively measures resulting browser GETs. First version passed1/1 on d306599 (four queue requests). Extended current source also changes the status filter to Changes requested then Awaiting admin review, checks seeded rejected/pending rows and collapsed picker. Extended version compiled; not yet run. Runner --only queues selects it; default remains the original four lifecycle methods. No application decision or fixture mutation.

## 2026-10-04T13:56:10.198172+05:30 — Current O3 outcomes

| Method | Actual invocation7 result | Remaining |
|---|---|---|
| restaurantApplicationLifecycle | Error after admin approval; stale customer feed | Browser reload/single-outlet repair compiled, pending rerun |
| deliveryApplicationLifecycle | Error after approval; duty disabled by stale profile flag | Current O3 handoff repair local25/25, pending publication/deployment/rerun |
| privateReviewAndDecisions | Passed | Private upload/completion and admin image view actually observed |
| pendingAndRejectedOutletsAreNotDiscoverableThroughTheCustomerUI | Passed | Retained seeded fixtures unchanged |

## 2026-10-04T14:16:06.678712+05:30 — Current image invocation8

Delivery lifecycle, private admin review/decisions and hidden-listing methods passed. Restaurant lifecycle failed only after actual approval/summary refresh, on a brand-name query that the older outlet-name filter cannot match. Brand-alias search is [O3-UI-006 deferred](DEFERRED-LEGACY-UI-TESTS.md), not passed. The corrected O3 scenario will search its saved outlet, assert the exact renamed card and open its owned outlet through visible UI. The current image is unchanged; only this method needs rerun.

## 2026-10-04T14:26:25.838614+05:30 — Current required O3 methods accepted

| Method | Passing invocation on b5ab30e | Scope |
|---|---|---|
| deliveryApplicationLifecycle | 8 | Private onboarding, admin approval, actual UI Online/Offline |
| privateReviewAndDecisions | 8 | Private uploads/checks/admin image viewing and real decisions |
| pendingAndRejectedOutletsAreNotDiscoverableThroughTheCustomerUI | 8 | Seeded pending/rejected statuses and absent discovery cards |
| restaurantApplicationLifecycle | 9 | Rejection/reason/edit/resubmit/approval, consistent summary, exact renamed-brand card via saved-outlet search and owned storefront |

Invocation8 remains3passed/1error/0skips; targeted9 is1passed/0errors/0skips. Legacy brand-name search is O3-UI-006 failed/deferred, not passed. No order/menu placement claim.
