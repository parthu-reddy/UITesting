# Checkpoint 114 — Business wallet: W1/A1 pass, W2 red (fix local), W3 written — 2026-10-06T13:35+05:30

Deployed release: W1+A1 (live-green 2026-10-06 ~10:00) and W2 (deployed ~12:13, E2E red). Nothing from this
checkpoint's work is deployed; all of it is local and uncommitted.

## State
- **BW-W1-001 PASS**, **AM-A1-001 PASS**, **AM-A1-002 PASS 3/3** — recorded in the plan files on 2026-10-06; copied
  into the new feature folders [business-wallet](../../05-partner-and-account-management/business-wallet/AUDIT-STATUS.md)
  and [ads-manager](../../05-partner-and-account-management/ads-manager/AUDIT-STATUS.md) today (they had no e2e-plan record).
- **BW-W2-001 RED**: 400 "PaymentService is down" on the first top-up. Root cause found and fixed locally: WalletService
  never registered `PaymentServiceClient` as a Feign client. A whole-workspace scan found the same gap only in ONDC (parked).
- **BW-W3-001 written, not run**: `BusinessWalletUiTest` + `BusinessWalletPage` compile; locator audit PASS.

## Waiting on the owner
1. Redeploy WalletService (no wipe). It carries the W2 fix and W3's `WalletTransactionDto.category`.
2. Then rerun BW-W2-001 (new 9999 phone).
3. Deploy the W3 UI (after WalletService) and run BW-W3-001. Also re-run `RestaurantCampaignsLiveTest` (AM-A1-002): its locators
   changed with W3's Campaigns screen (full locator audit: 76 FAIL, all pre-existing at HEAD; none introduced).
Commands: [business-wallet/PENDING.md](../../05-partner-and-account-management/business-wallet/PENDING.md). Full deploy list:
RandomDocuments/BusinessPlatform_2026-10-03/W2-HANDOFF.md.

## Fixtures (retained, none cleaned)
W1 members of Brand 1's organisation: 9999763656, 9999621330, 9999733049, 9999848324. W2 phones 9999324833,
9999006041 (no membership). A1: 9999937555; DRAFT campaign 967aa365-3ddf-4e64-a8eb-b434c5e7cc97; two Brand 2 DRAFTs.
