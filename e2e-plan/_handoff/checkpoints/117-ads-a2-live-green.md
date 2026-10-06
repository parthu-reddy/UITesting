# 117 — A2 promoted outlet + serving: LIVE-GREEN (2026-10-06T18:22+05:30)

Release (deployed 12:28–12:36Z, fresh Dev): restaurant dfec814, campaign e93c39b, bidding a5de54a, customer a91c421,
UI 5899092. Tunnel https://gulf-strike-dark-extras.trycloudflare.com (from collect_oracle_release_state.py).

- `PromotedOutletApiTest` PASS, `SponsoredListingRegressionTest` PASS (`-Dbp.a2.preflight=true`).
- `AdAccountPerOrganisationApiTest` PASS (phone 9999197089), `RestaurantCampaignsLiveTest` PASS 3/3
  (`-Drestaurant.phone=9000000001 '-Dcampaign.outlet=Brand 1 Outlet 3' -Dcampaign.create=true`).
- Nearby listing p95 132 → 154 ms (+22, budget +150).

Retained: A2 campaign 31fa0c7c-008f-49fe-b2e7-102449c2351f (Brand 1), A1 member 9999197089 + its DRAFT campaign,
one "E2E draft …" campaign from RestaurantCampaignsLiveTest. Records: ads-manager/AUDIT-STATUS.md, A2 validation.md.
Next: A3 (creatives/moderation/activation) on owner go-ahead.
