# Ads Manager scenarios

Owner request: Business Platform plan, RandomDocuments/BusinessPlatform_2026-10-03/03_AdsManager (E2E gates explicitly
requested). The ad account IS the organisation (D10); campaigns spend from the organisation's business wallet
([../business-wallet](../business-wallet/scenarios.md)). At A4 this folder supersedes
[campaigns-and-promotions](../campaigns-and-promotions/README.md) (mark that folder then; do not delete it).

| ID | Phase | Executable method | Required outcome |
|---|---|---|---|
| AM-A1-001 | A1 | `AdAccountPerOrganisationApiTest#adAccountPerOrganisation` (`bp-a1`) | Ad account 404 → start 201 with `id` = organisation id → repeat 200 unchanged; STAFF 403; MANAGER 200 and creates a DRAFT campaign; another organisation's owner 404; a person with no business 403 at the gateway; business wallet 200. |
| AM-A1-002 | A1 | `RestaurantCampaignsLiveTest` (3 tests: `tabMatchesTheOwnersAdvertiser`, `draftIsDiscardedOnCancel`, `launchedCampaignIsStoredInRupees`; needs `-Dcampaign.outlet`, `-Dcampaign.onboard=true -Dcampaign.create=true`) | Restaurant Campaigns tab uses the owner's organisation as advertiser; "Start advertising" when none; cancel discards a draft; launched campaign stored in rupees (50/500/1.5). |

A2–A4 scenarios are added when those phases start (see each phase's validation.md § E2E gate).
