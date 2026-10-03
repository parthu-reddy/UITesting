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
