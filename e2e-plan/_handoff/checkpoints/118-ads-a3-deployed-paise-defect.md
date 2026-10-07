# 118 — A3 deployed: bp-a3 red on sub-paisa ad prices, fix local (2026-10-06T21:55+05:30)

Release (fresh Dev, campaign_db wiped): campaign 591b651, restaurant f7a59ab, event-tracking d872a07, customer e4f622f,
UI 5d37904, gateway route (Deployment 9fb3cf6). Tunnel https://gulf-strike-dark-extras.trycloudflare.com.

- `CampaignModerationActivationFlowTest`: moderation (reject/resubmit/approve in the admin UI), activation, Sponsored
  first in the listing, impression, wallet charge — all PASS live. RED at the ledger: wallet −0.42, ledger 0.43 per
  impression, because the auction priced 0.4250 into NUMERIC(14,2) columns.
- Fix local (owner D-A3-PAISE): BiddingEngine prices in paise (HALF_UP, capped at max bid rounded down), drops 0.00
  prices; WalletService refuses sub-paisa debit/credit. Break-tests 4/4 RED; BiddingEngine 37/37, WalletService 107/107.
- Harness: ledger check waits for the outbox; post-activation steps pause in finally; new
  `AdCreativeQueueLatencyMeasurement` (p95 83 ms).
- bp-a2 2/2, bp-a1 1/1 (9999591671), RestaurantCampaignsLiveTest 3/3 PASS.

Next: owner redeploys **bidding-engine + wallet-service** (no wipe) → rerun
`CampaignModerationActivationFlowTest -Dbp.a3.preflight=true`. Then A4 on owner go-ahead.
