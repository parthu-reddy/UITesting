# Scenarios and validation

## Unsaved campaign cancellation

Current deployed validation on 2026-09-23 fails before the draft opens. Clicking the Ad Campaigns tab returns to Live Kitchen, so the New Campaign button never appears and the test times out. This matches the restaurant dashboard route override recorded under restaurant acceptance/navigation. No campaign mutation occurred. A previous run had passed the draft-cancellation assertions, but that result does not describe the current deployment.

`PartnerOperationsUiTest.verifyCampaignManagement` is now a strict CAMPAIGN-01 check rather than a no-op boolean lookup. Its live run fails because `Ad Spending History` never becomes visible after clicking `Ad Campaigns`; the browser also reports HTTP 403 for `/api/v1/brands/stream`. The test remains active so the navigation defect cannot be reported as a pass.
