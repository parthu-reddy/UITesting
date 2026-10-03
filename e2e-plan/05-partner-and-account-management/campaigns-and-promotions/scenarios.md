# 05 — Campaigns and Promotions — All Scenarios

Rewritten 2026-10-03 (checkpoint34) from source. The product is **prepaid ad campaigns** (budget, bid, dates) run by an advertiser profile that belongs to the signed-in restaurant owner, not discount promotions. Test: `tests/features/restaurant/RestaurantCampaignsLiveTest` (`-Dcampaign.outlet`; writes only with `-Dcampaign.onboard=true` / `-Dcampaign.create=true`). The superseded table is kept below as history.

## Batch 1 — Reaching the tab and onboarding

| ID | Description | Expected result | Test |
|---|---|---|---|
| CAMPAIGN-01 | Campaigns tab shows the right screen | `/advertisers/me` 404 → "Start advertising" step; 200 → campaign screen (New Campaign, Ad Spending History). Opening the tab writes nothing | `tabMatchesTheOwnersAdvertiser`; smoke `PartnerOperationsUiTest`, `RestaurantNavigationUiTest` accept either screen |
| CAMPAIGN-ONBOARD-01 | Start step is prefilled from the selected outlet | Business name = the outlet's brand name; text names the outlet's IANA zone | `tabMatchesTheOwnersAdvertiser` |
| CAMPAIGN-ONBOARD-02 | Start advertising registers exactly once | One `POST /api/v1/advertisers`; `/me` then returns companyName = brand, timeZone = outlet zone, walletBalanceId set; campaign screen shown | `tabMatchesTheOwnersAdvertiser` with `-Dcampaign.onboard=true` |
| CAMPAIGN-ONBOARD-03 | Lookup failure is not "not advertising" | Non-404 → "Campaigns could not load" + Try again; no start step | vitest `RestaurantCampaigns.test.tsx` |
| CAMPAIGN-WALLET-01 | Owner reads own ad wallet | `GET /api/v1/money/advertiser/ADVERTISER/{id}` 200 for the owner (was 403 for every owner); card shows the balance on open | `tabMatchesTheOwnersAdvertiser`; wallet `PayeeWalletControllerAuthorizationTest` |

## Batch 2 — Campaign list and draft

| ID | Description | Expected result | Test |
|---|---|---|---|
| CAMPAIGN-02 | Empty list message | "No campaigns found. Create your first ad campaign!" when the server list is empty | `tabMatchesTheOwnersAdvertiser` |
| CAMPAIGN-03 | Card shows the server's campaign | Name, status, daily budget and **lifetime** budget in rupees | `tabMatchesTheOwnersAdvertiser`; vitest `CampaignManagement.test.tsx` |
| CAMPAIGN-05..07 | Draft opens and is discarded on Cancel | Dialog "New Ad Campaign"; Cancel closes it; no write; no card | `draftIsDiscardedOnCancel` |
| CAMPAIGN-14 | Launch stores rupees as typed | POST body dailyBudget 50, lifetimeBudget 500, maxBid 1.5 (was ×100); stored and listed as DRAFT | `launchedCampaignIsStoredInRupees` with `-Dcampaign.create=true` |
| CAMPAIGN-TOPUP-01 | Top-up asks for the typed amount | Payment dialog shows ₹100.00 for 100 (was ₹10,000.00) | vitest; live top-up not yet run |

## Not reachable in the product (user decision pending)

Activation (DRAFT → SCHEDULED/ACTIVE) needs an ad group, an APPROVED creative and wallet balance. There is no UI for ad groups or creatives and creative moderation has no caller, so CAMPAIGN-04 (status labels over a date range), CAMPAIGN-16 (pause an active campaign) and performance data cannot be produced. Server-side validation (end before start, budget/bid rules) is enforced by `CampaignBudgetValidator` and `createCampaign`; the form has no client-side rule beyond required fields.

## History: superseded scenario table (before 2026-10-03)


Uses: `RestaurantDashboardPage` (Campaigns tab). No campaigns are permanently created during tests — drafts are discarded or tests clean up after themselves.

### Batch 1 — Campaign list

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-01 | Campaigns tab accessible | Login as restaurant → tap "Campaigns" tab. | Implemented as a strict active test requiring Ad Spending History and New Campaign. The current deployment redirects back to Live Kitchen; see `PENDING.md`. |
| CAMPAIGN-02 | Empty campaign list message | If no campaigns created. | Empty state message shown ("No campaigns yet"); not a blank page. |
| CAMPAIGN-03 | Existing campaign card | If a seeded campaign exists. | Campaign card shows name, discount/offer type, start date, end date, and status. |
| CAMPAIGN-04 | Campaign status labels | Active, Scheduled, Expired campaigns shown with correct labels. | Status label matches actual date range (e.g. "Active" if today is within the campaign dates). |

### Batch 2 — Create new campaign (draft + cancel)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-05 | New campaign form opens | Tap "New Campaign" / "Create Campaign". | Campaign form renders with name, discount type, discount value, start date, end date fields. |
| CAMPAIGN-06 | Type unsaved draft name | Enter "Unsaved E2E Draft" in name field. | Text appears in the name field. |
| CAMPAIGN-07 | Cancel new campaign | Tap "Cancel". | Form closes; campaign list shown; "Unsaved E2E Draft" NOT in the list. |
| CAMPAIGN-08 | Close without saving | Open form → close browser tab or navigate away → return. | Draft is discarded; list unchanged. |

### Batch 3 — Campaign form validation

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-09 | Submit without name blocked | Leave name blank → tap Submit. | Validation error; form not submitted. |
| CAMPAIGN-10 | Submit without discount blocked | Leave discount value blank → tap Submit. | Validation error. |
| CAMPAIGN-11 | Submit with end date before start date | Set end date earlier than start date → tap Submit. | Validation error: "End date must be after start date". |
| CAMPAIGN-12 | Discount percentage > 100 blocked | Enter 150 in % discount field → tap Submit. | Validation error; max 100% or platform-specific cap. |
| CAMPAIGN-13 | Discount = 0 blocked | Enter 0 in discount field → tap Submit. | Validation error; 0% discount is meaningless. |

### Batch 4 — Campaign CRUD (if backend is stable)

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-14 | Create valid campaign | Fill all fields with valid data → tap Submit. | Campaign created; appears in list with "Scheduled" or "Active" status. |
| CAMPAIGN-15 | Edit campaign name | Open an existing campaign → edit name → save. | Updated name appears in list. |
| CAMPAIGN-16 | Deactivate campaign | Tap "Deactivate" on an active campaign. | Campaign status changes to "Paused" or "Inactive". |

### Admin campaign scenarios

The current product has no admin Campaigns screen. Campaign creation, budgets, wallet balance,
and performance belong to the restaurant dashboard, so the obsolete `CAMPAIGN-ADM-*` and `PERF-*`
scenarios are not part of admin E2E coverage. Restaurant campaign work stays in the batches above.
