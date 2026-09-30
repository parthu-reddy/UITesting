# 05 — Campaigns and Promotions — All Scenarios

Uses: `RestaurantDashboardPage` (Campaigns tab). No campaigns are permanently created during tests — drafts are discarded or tests clean up after themselves.

## Batch 1 — Campaign list

| ID | Description | Action | Expected result |
|---|---|---|---|
| CAMPAIGN-01 | Campaigns tab accessible | Login as restaurant → tap "Campaigns" tab. | Implemented as a strict active test requiring Ad Spending History and New Campaign. The current deployment redirects back to Live Kitchen; see `PENDING.md`. |
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

## Admin campaign scenarios

The current product has no admin Campaigns screen. Campaign creation, budgets, wallet balance,
and performance belong to the restaurant dashboard, so the obsolete `CAMPAIGN-ADM-*` and `PERF-*`
scenarios are not part of admin E2E coverage. Restaurant campaign work stays in the batches above.
