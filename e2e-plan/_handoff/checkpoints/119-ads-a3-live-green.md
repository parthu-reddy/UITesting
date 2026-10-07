# 119 — A3 live-green after the paise fix (2026-10-07T06:04+05:30)

Release: bidding-engine b8fbaef + wallet-service 37c860d on the checkpoint-118 A3 release (0 drift, 29/29 running).
Tunnel https://gulf-strike-dark-extras.trycloudflare.com.

- `CampaignModerationActivationFlowTest -Dbp.a3.preflight=true -Dcustomer.phone=8000000019 -Dadmin.phone=1000000002`:
  PASS. Charged 0.43, ledger debit 0.43 (campaign 5c66cb23, left PAUSED).
- `PromotedOutletApiTest` 1/1, `SponsoredListingRegressionTest` 1/1 PASS.
- Gotcha: admin 1000000001 hit the 3-session limit (409 on /auth/admin-session) from earlier runs; use 1000000002 or
  let sessions expire. Leftover DRAFT campaign 4c2afa06 (harmless).

Next: A4 (Ads Manager portal) on owner go-ahead — BP validator 135 PASS / 5 FAIL, all five are A4.
