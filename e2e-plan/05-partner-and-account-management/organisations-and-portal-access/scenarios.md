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
