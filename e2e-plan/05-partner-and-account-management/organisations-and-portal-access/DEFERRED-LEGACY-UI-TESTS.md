# Reopened legacy UI assertions — 2026-10-04

## 2026-10-04T16:42:23.916935+05:30 — O3-UI-006 resolved on the published Oracle UI

This reopened assertion is now resolved by strict UI invocation11 and the brand-search lifecycle in four-method invocation13 on d79c33a. Both brand and outlet searches wait for the one-result feed before card navigation. Earlier exclusion, failure and reopening entries below are history. This closes search only; an empty new catalogue remains outside menu/order coverage.

The owner explicitly reopened legacy brand-name search and the 300-line component cleanup on
2026-10-04. O3-UI-006 is now in progress; the earlier exclusion below is historical. This is a known UX gap, not a passing test or completed global production-readiness
claim. Retain the actual failed invocation and its owned applicant.

| ID | Deferred assertion | Observed failure and reason | Permitted current proof / unblock |
|---|---|---|---|
| O3-UI-006 | Customer search by a renamed brand whose outlet has a different name. | UI invocation8 timed out after actual approval. `CustomerRestaurantBrowser` filters `restaurant.name` (outlet), while `CustomerRestaurantCard` displays `brandName`. The retained approved brand is visible in the fresh unfiltered feed and when searching its saved outlet name. The owner has now authorized repairing this component. Local regression reproduction is in progress; deployment and visible UI proof remain required. | The current O3 lifecycle searches the exact saved outlet name, asserts the exact renamed approved brand card, opens it through the UI, and checks the owned outlet heading. Brand-name search is reopened; close only after published Oracle UI proof. |

Evidence: [invocation8](../../_handoff/evidence/68-o3-ui-invocation8-completed.json).
The canonical screenshot is Business Platform phase evidence/68-o3-retained-approved-restaurant.png.
The visible retained outlet is 2.6 km away in discovery; its empty catalog correctly renders
“Menu unavailable — This kitchen hasn't published any menu items yet.” No order/menu coverage
is claimed. This inspection used normal seeded customer login and Dev Autofill, visible search
and card navigation only; no API, database, Redis, SSH, browser storage or token injection.

Fresh O3 applicant outlets use visible Opens/Closes controls for an explicit00:00–23:59 test
window. The normal server opening-hours and approval filters remain authoritative. Natural
closed-hour boundary assertions are outside this positive onboarding scenario; no wait is added.
