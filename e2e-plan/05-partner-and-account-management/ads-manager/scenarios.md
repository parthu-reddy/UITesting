# Ads Manager scenarios

Owner request: Business Platform plan, RandomDocuments/BusinessPlatform_2026-10-03/03_AdsManager (E2E gates explicitly
requested). The ad account IS the organisation (D10); campaigns spend from the organisation's business wallet
([../business-wallet](../business-wallet/scenarios.md)). At A4 this folder supersedes
[campaigns-and-promotions](../campaigns-and-promotions/README.md) (mark that folder then; do not delete it).

| ID | Phase | Executable method | Required outcome |
|---|---|---|---|
| AM-A1-001 | A1 | `AdAccountPerOrganisationApiTest#adAccountPerOrganisation` (`bp-a1`) | Ad account 404 → start 201 with `id` = organisation id → repeat 200 unchanged; STAFF 403; MANAGER 200 and creates a DRAFT campaign; another organisation's owner 404; a person with no business 403 at the gateway; business wallet 200. |
| AM-A1-002 | A1 (deleted at A4) | ~~`RestaurantCampaignsLiveTest`~~ (3 tests: `tabMatchesTheOwnersAdvertiser`, `draftIsDiscardedOnCancel`, `launchedCampaignIsStoredInRupees`; needs `-Dcampaign.outlet`, `-Dcampaign.onboard=true -Dcampaign.create=true`) | Restaurant Campaigns tab uses the owner's organisation as advertiser; "Start advertising" when none; cancel discards a draft; launched campaign stored in rupees (50/500/1.5). |

| AM-A4-001 | A4 | `AdsManagerPortalUiTest#ownerRunsACampaignInAdsManager` (`bp-a4`, `-Dbp.a4.preflight=true`, owner `-Dbp.a4.owner` default 9000000002, `-Dcustomer.phone` near a Brand 2 outlet) | Launcher → Ads Manager on Brand 2's organisation; start advertising if offered; wizard Restaurant → outlet → ₹20/₹100/₹0.50 → **uploaded PNG** → "In review"; the creative is UPLOAD + PENDING with a signed private preview; activation refused; admin approves in the A3 page; approval publishes it (public URL, no signature, bytes equal the upload); Activate → Live; listing serves the outlet first, Sponsored, `adData.adm` = the published URL and the card image shows it; performance counts the impression; Pause → Paused, out of the listing; the restaurant portal has no Campaigns tab. Campaign left PAUSED. |

A2/A3 gates are recorded in their phase validation.md files (`bp-a2`, `bp-a3`).
