# Ads Manager audit status

## 2026-10-06T18:22+05:30 — A2 LIVE-GREEN (bp-a2 PASS); A1 + AM-A1-002 re-run PASS

| Scenario | State | Proof |
|---|---|---|
| AM-A2-001 `PromotedOutletApiTest` | **PASS** | own outlet 201 + region = outlet city; client geo 400; other org (active/inactive) 403; campaign 31fa0c7c… retained |
| AM-A2-002 `SponsoredListingRegressionTest` | **PASS** | 10 outlets, 0 sponsored, all with cityId; no badge |
| AM-A1-001 re-run (now sends `promotedOutletId`) | **PASS** | phone 9999197089 |
| AM-A1-002 re-run (asserts promoted outlet) | **PASS 3/3** | 9000000001, Brand 1 Outlet 3 (`-Drestaurant.phone` is required: the default owner is random) |

Release and details: RandomDocuments/BusinessPlatform_2026-10-03/03_AdsManager/Phase2_PromotedOutletAndServing/validation.md. Next: A3.

## 2026-10-06T14:42+05:30 — AM-A1-002 re-run on W3 UI: PASS 3/3

**2026-10-06T14:40:37+0530 — re-run on the deployed W3 UI (489e2c8): PASS 3/3.** Invocation 1: `tabMatchesTheOwnersAdvertiser`
and `launchedCampaignIsStoredInRupees` PASS; `draftIsDiscardedOnCancel` SKIPPED (Brand 2's ad account was gone after the W2
wipe; that test ran first). The first test re-created the account through the UI "Start advertising" step. Invocation 2
(14:41:33): `draftIsDiscardedOnCancel` PASS. Owner 9000000002, Brand 2 Outlet 1; one new DRAFT campaign retained.

## 2026-10-06T13:35+05:30 — A1 PASS (deployed with W1); A2–A4 not started

Folder created this date; A1's results were recorded only in
RandomDocuments/BusinessPlatform_2026-10-03/03_AdsManager/Phase1_AdAccountPerOrganisation/validation.md (detailed source).

| Scenario | State | Proof |
|---|---|---|
| AM-A1-001 | **PASS** 2026-10-06T09:56:42+0530 (invocation 1, phone 9999937555, org Brand 1; DRAFT campaign `967aa365-3ddf-4e64-a8eb-b434c5e7cc97` retained) | A1 validation.md § E2E result. p95 83 ms. |
| AM-A1-002 | **PASS 3/3** 2026-10-06T09:59:59+0530 (invocation 3; 9000000002, Brand 2 Outlet 1). Invocation 1: `draftIsDiscardedOnCancel` skipped (ran before the account existed) and the launch body assertion failed (no request body through `authFetch`; assertion removed, stored values prove rupees). | A1 validation.md. Two Brand 2 DRAFT campaigns from invocations 1–2 retained. |

The class has exactly 3 `@Test` methods (`ownerOnCampaigns` is its `@BeforeEach`), so 3/3 is the whole class.

**2026-10-06T13:45 — AM-A1-002 must be re-run after the W3 UI deploy:** W3 removed the Campaigns tab's own wallet UI, so
`RestaurantCampaignsLiveTest` (and `RestaurantDashboardPage.campaignsScreen`) now key on the "Ad Campaigns" heading and
the `campaigns-wallet-balance` test id + "Manage wallet" button (locator audit: no new FAIL vs HEAD). Its 3/3 above is for
the pre-W3 UI.

Next: A2 (promoted outlet and serving) after W3 is live-green and the owner says to continue.
