# 120 — A4 Ads Manager built locally, deploy open (2026-10-07)

Local, uncommitted: CommonLibrary common-storage, CampaignService (V1 edited — **wipe campaign_db**), BiddingEngine,
GovernmentIDValidationService (new storage signature only), Deployment (compose + campaign-service.yml), UI, UITesting.
Deploy order and reruns: RandomDocuments/BusinessPlatform_2026-10-03/A4-HANDOFF.md.

- New gate `AdsManagerPortalUiTest` (`bp-a4`, `-Dbp.a4.preflight=true`, owner default 9000000002, `-Dcustomer.phone`
  near a Brand 2 outlet): wizard with an **uploaded PNG**, private until approved, published on approval, served on the
  customer card, activated and paused in the UI; restaurant portal has no Campaigns tab.
- Deleted: `RestaurantCampaignsLiveTest`, CAMPAIGN-01 (`PartnerOperationsUiTest`), the Campaigns steps in
  `RestaurantNavigationUiTest`, `RestaurantDashboardPage.openCampaignsTab/campaignsScreen`. A3 test reads `previewUrl`.
- `Portal.ADS` added. Teardown sign-out (`SessionSignOut`) is in `TestBase` since checkpoint 119's follow-up.

Next: owner deploys per A4-HANDOFF.md → run bp-a4, then rerun bp-a3, bp-a2, bp-a1, RestaurantNavigationUiTest,
PartnerOperationsUiTest.
