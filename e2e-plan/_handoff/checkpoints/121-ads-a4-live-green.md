# 121 — A4 Ads Manager live-green (2026-10-07T08:43+05:30)

Release (= local HEADs, clean trees): campaign-service bcdcc6b, bidding-engine 457d125, UI 7839221, government-id
f28dbb5, CommonLibrary 6f718c8, Deployment 725c7be; 29/29 healthy, 0 restarts. Tunnel https://gulf-strike-dark-extras.trycloudflare.com.

- `AdsManagerPortalUiTest` (bp-a4, `-Dbp.a4.preflight=true -Dcustomer.phone=8000000019 -Dadmin.phone=1000000001`): **PASS**
  on invocation 2. Campaign 51f340aa (Brand 2 Outlet 8) left PAUSED; Brand 2 wallet topped up ₹100.
  Invocation 1 RED = harness defect: `CampaignWizardPage.outlet` used `Pattern.quote` (Java `\Q…\E`) in a Playwright
  name regex, which runs as a JS RegExp. Fixed locally (UITesting, uncommitted). CommonMistakesDocumentation/UI/playwright-java-pattern-quote-2026-10-07.md.
- Verified live: R2 key copies private → public; public `pub-…r2.dev` URL served on the customer card; private-bucket CORS OK.
- Reruns PASS: bp-a3 (charged 0.43 = ledger, campaign eeb1d320 PAUSED), PromotedOutletApiTest, SponsoredListingRegressionTest,
  AdAccountPerOrganisationApiTest (9999788045), PartnerOperationsUiTest, RestaurantNavigationUiTest 2/2 with
  `-Drestaurant.phone=9000000001`.
- Follow-up same morning (08:52): `RestaurantNavigationUiTest` hard-coded Brand 1 outlets (REVIEW-AGG-01 and REST-NAV)
  while the owner is random — FIXED: outlets now come from the signed-in owner's `/api/v1/outlets`; class 2/2 in 5
  unpinned runs. Details: 04-order-exceptions-and-support/reviews-and-support/AUDIT-STATUS.md.

BP validator 146/0. Next: nothing open in the business-platform plan; owner decides what follows.
- 09:02: A3 admin **Reject now behind a danger confirm** (redesign phase 4 check 8 → 13/13). UI local, undeployed;
  `AdminAdCreativesPage.reject` already expects the dialog → **after the UI deploy, rerun `CampaignModerationActivationFlowTest`**
  (`-Dbp.a3.preflight=true -Dcustomer.phone=8000000019 -Dadmin.phone=1000000001`); before it, that test will fail at reject.
- 09:41: UI 3d25d56 deployed; `CampaignModerationActivationFlowTest` **PASS** through the danger confirm (REJECTED with the
  exact reason; campaign c8d67414 charged 0.43 = ledger, left PAUSED). Nothing pending.
