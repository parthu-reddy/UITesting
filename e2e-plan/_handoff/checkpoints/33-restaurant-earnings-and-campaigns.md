# Checkpoint33: restaurant earnings never loaded; campaigns cannot work end to end

Updated 2026-10-03T09:55:00+05:30.

## Restaurant earnings (defect, fixed locally)

The real figures, from the databases for Brand 1 Outlet 3 this month: Net Earnings ₹76.44 (4 delivered orders; the cancelled and rejected ones are correctly excluded), Clawbacks ₹8.96, Pending ₹67.48 (ledger, no payouts). These are consistent: 76.44 − 8.96 = 67.48.

The live Earnings tab showed **"—" on every card**, and the gateway logged no summary or statement request at all. Cause: `App.tsx:96-97` mounts the restaurant dashboard with `restaurantId=""`, and the Earnings tab polls only when that id is truthy (`RestaurantPortal`, which would pass `outlets[0]?.id`, is imported nowhere). Earnings are per outlet, so the tab now follows the outlet selector (`outletId={selectedOutletId}`). A wiring test with the production prop was seen red when reverted. UI: typecheck, lint, vitest 759/121. New `RestaurantEarningsLiveTest` asserts the database figures, Pending = Net − Clawbacks, and that the statement adds up; it is red until the UI deploy.

## Campaigns (blocked on a decision)

`CampaignManagement` receives the same empty id as `advertiserId` and returns early, so the Campaigns tab never loads. Fixing the id alone is not enough: the campaign service authorizes by an `AdvertiserProfile` the user owns, and **no advertiser profile exists or can be created**. `POST /api/v1/advertisers` and `GET /api/v1/advertisers/me` exist but have no caller; campaign_db has 0 profiles. Decision asked of the user.

Evidence: [33-restaurant-earnings-and-campaigns.json](../evidence/33-restaurant-earnings-and-campaigns.json).
