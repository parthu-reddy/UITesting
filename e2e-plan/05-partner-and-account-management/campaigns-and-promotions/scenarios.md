# 05 — Campaigns and Promotions — All Scenarios

Uses: `RestaurantDashboardPage` (Campaigns tab). No campaigns are permanently created during tests — drafts are discarded or tests clean up after themselves.

## Batch 1 — Campaign list

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-01 | Campaigns tab accessible | Login as restaurant → tap "Campaigns" tab. | Campaign list page renders; active/scheduled/past campaign sections visible. |
| CAMPAIGN-02 | Empty campaign list message | If no campaigns created. | Empty state message shown ("No campaigns yet"); not a blank page. |
| CAMPAIGN-03 | Existing campaign card | If a seeded campaign exists. | Campaign card shows name, discount/offer type, start date, end date, and status. |
| CAMPAIGN-04 | Campaign status labels | Active, Scheduled, Expired campaigns shown with correct labels. | Status label matches actual date range (e.g. "Active" if today is within the campaign dates). |

## Batch 2 — Create new campaign (draft + cancel)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-05 | New campaign form opens | Tap "New Campaign" / "Create Campaign". | Campaign form renders with name, discount type, discount value, start date, end date fields. |
| CAMPAIGN-06 | Type unsaved draft name | Enter "Unsaved E2E Draft" in name field. | Text appears in the name field. |
| CAMPAIGN-07 | Cancel new campaign | Tap "Cancel". | Form closes; campaign list shown; "Unsaved E2E Draft" NOT in the list. |
| CAMPAIGN-08 | Close without saving | Open form → close browser tab or navigate away → return. | Draft is discarded; list unchanged. |

## Batch 3 — Campaign form validation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-09 | Submit without name blocked | Leave name blank → tap Submit. | Validation error; form not submitted. |
| CAMPAIGN-10 | Submit without discount blocked | Leave discount value blank → tap Submit. | Validation error. |
| CAMPAIGN-11 | Submit with end date before start date | Set end date earlier than start date → tap Submit. | Validation error: "End date must be after start date". |
| CAMPAIGN-12 | Discount percentage > 100 blocked | Enter 150 in % discount field → tap Submit. | Validation error; max 100% or platform-specific cap. |
| CAMPAIGN-13 | Discount = 0 blocked | Enter 0 in discount field → tap Submit. | Validation error; 0% discount is meaningless. |

## Batch 4 — Campaign CRUD (if backend is stable)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-14 | Create valid campaign | Fill all fields with valid data → tap Submit. | Campaign created; appears in list with "Scheduled" or "Active" status. |
| CAMPAIGN-15 | Edit campaign name | Open an existing campaign → edit name → save. | Updated name appears in list. |
| CAMPAIGN-16 | Deactivate campaign | Tap "Deactivate" on an active campaign. | Campaign status changes to "Paused" or "Inactive". |

## Batch 5 — Admin Campaign Management (`AdminCampaignsPage`)

Advanced campaign creation with budgets and bid amounts. Admin view of campaign management.

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-ADM-01 | Admin campaigns tab accessible | Admin navigates to Campaigns tab via `AdminPortalPage.openCampaignsTab()`. | `AdminCampaignsPage.isCampaignsVisible()` returns true; campaign list renders. |
| CAMPAIGN-ADM-02 | Campaign count | On admin campaigns page. | `getCampaignCount()` matches visible campaign cards. |
| CAMPAIGN-ADM-03 | Open create campaign modal | `openCreateCampaignModal()`. | Create form/modal opens with name, daily budget, total budget, and bid amount fields. |
| CAMPAIGN-ADM-04 | Fill campaign name | `fillCampaignName("E2E Test Campaign")`. | Name field accepts input. |
| CAMPAIGN-ADM-05 | Fill daily budget | `fillDailyBudget("500")`. | Daily budget field shows ₹500. |
| CAMPAIGN-ADM-06 | Fill total budget | `fillTotalBudget("5000")`. | Total/lifetime budget field shows ₹5000. |
| CAMPAIGN-ADM-07 | Fill bid amount | `fillBidAmount("10")`. | Bid amount field shows ₹10. |
| CAMPAIGN-ADM-08 | Submit campaign via admin | `submitCampaign()`. | Campaign created; appears in campaign list. |
| CAMPAIGN-ADM-09 | Submit without daily budget blocked | Fill name + bid but leave daily budget blank → submit. | Validation error; campaign not created. |
| CAMPAIGN-ADM-10 | Toggle campaign status | `toggleCampaignStatus(0)` to Pause/Resume/Activate. | Campaign status toggles; button label changes accordingly. |
| CAMPAIGN-ADM-11 | Campaign wallet balance visible | On campaigns page. | `getWalletBalance()` returns a non-null amount (e.g., "₹500"). |
| CAMPAIGN-ADM-12 | Open wallet top-up | `openWalletTopup()`. | Wallet top-up modal/form opens with amount input field. |
| CAMPAIGN-ADM-13 | Fill top-up amount | `fillTopupAmount("1000")`. | Amount field shows ₹1000. |

## Batch 6 — Ad Performance Dashboard (`AdminCampaignsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| PERF-01 | Open ad performance | `openAdPerformance()`. | Performance dashboard renders. |
| PERF-02 | Performance dashboard visible | After opening. | `isPerformanceDashboardVisible()` returns true; headings like "Ad Performance", "Impressions", or "Clicks" visible. |
| PERF-03 | Metrics non-null | On performance dashboard. | Impression and click counts are ≥ 0; not `null` or "undefined". |

