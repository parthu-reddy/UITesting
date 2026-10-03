# Checkpoint34: campaigns get an advertiser that can actually be created, and the ad screens stop charging 100×

Updated 2026-10-03T10:27:00+05:30. Everything below is **local, not deployed**. Never commit or push.

## User decision applied

"Start-advertising step" (USER-INSTRUCTIONS, 2026-10-03): the Campaigns tab looks up `GET /api/v1/advertisers/me`; on a 404 it offers a short form (business name prefilled from the selected outlet's brand, time zone from that outlet) that calls the existing `POST /api/v1/advertisers`. Nothing is created until the owner presses the button.

## Defects found and fixed (all read in source, each guard seen red)

| # | Where | Defect | Fix | Guard (seen red when reverted) |
|---|---|---|---|---|
| 1 | CampaignService `AdvertiserService.getAdvertiserByUserId` | "no advertiser" was `IllegalArgumentException` → 400, indistinguishable from a bad request | `ResourceNotFoundException` → 404 (shared `GlobalExceptionHandler`, scanned via `com.fooddelivery.common`) | `MyAdvertiserLookupTest` 2/2 (red: "expected 404 but was 400") |
| 2 | WalletService `PayeeWalletController` (3 endpoints) | `hasAnyRole('ADVERTISER','ADMIN')`; `RoleName` has no ADVERTISER, the gateway admits only `restaurant` to `/api/v1/money/advertiser` → **no owner could read their own ad wallet** (403) | `hasAnyRole('RESTAURANT','ADMIN')`; ownership stays `MoneyAccessPolicy` (advertiser owner via CampaignService internal `/owner`) | `PayeeWalletControllerAuthorizationTest` 3/3 (red: owner got 403); workspace `validate_role_names.py` |
| 3 | UI `CreateCampaignModal` | budgets and bid sent `× 100`; the wallet is rupees and an impression debits the cleared bid as-is (UserTrackingService → BillingEvent → WalletService `debit`) → a ₹1.50 bid could charge ₹150/impression, ₹50/day allowed ₹5,000 | rupees via `roundRupees` | vitest "sends budgets and the bid in rupees" |
| 4 | UI `CampaignManagement` → `PaymentModal` | top-up amount passed `× 100`; `PaymentModal` formats rupees → "Add ₹10,000.00" for ₹100 (the charge itself was already rupees) | `roundRupees(topupAmount)` | vitest "asks to pay the top-up in rupees" |
| 5 | UI `CampaignCard` | read `totalBudget`; server sends `lifetimeBudget` → card always ₹0.00 | model + card use `lifetimeBudget` | vitest "lifetime budget, the field the server sends" |
| 6 | UI `CampaignManagement` | wallet balance never loaded on open (only after a top-up) → ₹0.00 | `loadWalletData()` on open | vitest "balance on open" |
| 7 | UI `CreateCampaignModal` | "Targeting Radius (km)" collected, never sent (targeting is on ad groups: `AdGroupRequest.geoTargeting`) | field removed | vitest |
| 8 | UI dashboard | `restaurantId=""` threaded App → Dashboard → TabPanels → SettingsShell; `RestaurantPortal.tsx` dead | prop chain deleted; `RestaurantPortal.tsx` deleted (registration lives in `RestaurantSettingsShell`) | typecheck |
| 9 | GovernmentIDValidationService `VerificationController:51` | names nonexistent role `RESTAURANT_MANAGER` (dead; RESTAURANT also listed) | removed | `validate_role_names.py` |
| 10 | CampaignService `AdvertiserService.registerAdvertiser` | asked WalletService for an ad wallet in currency `"AD_CREDIT"`; `getOrCreate` inserts it as-is into `wallets.currency VARCHAR(3)` (V1; Dev confirmed read-only: `character varying(3)`) → insert fails → "Failed to provision advertiser wallet" → rollback: **"Start advertising" could never succeed** | the injected platform currency (INR; the wallet is topped up and debited in rupees). `AdvertiserWalletBackfillRunner` (startup "one-time backfill" for pre-sync advertisers, hardcoded "INR") deleted as compat code | `AdvertiserRegistrationTest` (red: got "AD_CREDIT") |

New: `RestaurantCampaigns.tsx` (lookup → start step → `CampaignManagement` with the real id and zone). `CreateCampaignModal` takes the zone from the loaded profile instead of re-fetching `/advertisers/{id}`.

## Uncontracted call (noted)

`WalletServiceClient.getOrCreateWallet` (`POST /api/v1/internal/wallets`) has no contract; WalletService publishes only get-wallet. That is why `"AD_CREDIT"` was never caught. Its two producers (CustomerMoneyController, AdvertiserService) now both send the platform currency; enumerated by grep over every `getOrCreateWallet`/`.currency(` call site.

## Product gap, not fixed (needs the user)

**No campaign can ever reach ACTIVE.** `activateCampaign` requires an APPROVED creative and wallet balance. The UI has no ad-group, creative-upload or activate step, and creative moderation (`POST /api/v1/internal/creatives/{id}/audit`, SERVICE/ADMIN) has **no caller anywhere** (no admin screen, no service). So campaigns stay DRAFT, never serve and never spend. Building that journey (ad group + creative upload via the presigned URL + admin moderation + activate) is a scope decision.

## Validation (local)

- UI: typecheck 0, `npm run lint` 0, vitest **771/123** (was 759/121). Six campaign mutations + top-up mutation each red, restored green.
- CampaignService `mvn -o clean test` **32/0/0** after defect 10 (HttpTest 2, MessagingTest 9, CampaignContractConsumerTest 1 ran); WalletService 56/0/0 (ContractVerifierTest + 3 consumer contracts ran); GovernmentIDValidationService 33/0/0 (ContractVerifierTest 3 ran).
- `tools/validate_role_names.py`: PASS 255 role names, 0 unknown; seen FAIL on the reintroduced ADVERTISER gate.
- Ad validator PASS 40; money audit 0/23 (clean); readiness 4.1/5.4 only (pre-existing).
- E2E: new `RestaurantCampaignsLiveTest` compiles; locator audit 26 pass / 0 fail. `PartnerOperationsUiTest` CAMPAIGN-01 and `RestaurantNavigationUiTest` accept either Campaigns screen; `PartnerReadOnlyUiTest.campaignDraftCanBeCancelled` moved into the new test (needs an advertiser).
- Locator audit failures **pre-existing at UI HEAD**, not touched: `RestaurantNavigationUiTest:43,66` (region names "Incoming, N orders" / "In the kitchen, N orders"), `PartnerReadOnlyUiTest:64,75` ("Today’s Earnings", rider feature).

## Deploy (user)

food-delivery-app-ui (earnings from checkpoint33 + campaigns), campaign-service (404; INR ad wallet; backfill runner removed), wallet-service (role gate). governmentid-service is behaviour-neutral and can ride along. Order does not matter for safety: a UI ahead of campaign-service shows "Campaigns could not load" (a 400 is not treated as "not advertising").

Evidence: [34-campaigns-onboarding.json](../evidence/34-campaigns-onboarding.json).
