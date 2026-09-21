# 06 — Users, Categories, and Moderation — All Scenarios

Uses: `AdminUserManagementPage`, `AdminReviewsPage`, `AdminPortalPage`.

## Batch 1 — User management

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-USER-01 | User management tab accessible | Login as admin → navigate to Users. | `AdminUserManagementPage` renders; search interface visible. |
| ADMIN-USER-02 | Search by customer phone | Enter seeded customer phone (8000000001) → tap Search. | Customer record appears with name, phone, role, and registration date. |
| ADMIN-USER-03 | Search by restaurant phone | Enter 9000000001 → search. | Restaurant partner record appears. |
| ADMIN-USER-04 | Search by rider phone | Enter 7000000001 → search. | Rider record appears with document verification status. |
| ADMIN-USER-05 | Search unknown phone | Enter a phone not in the system → search. | "Not found" / empty result message; no crash. |
| ADMIN-USER-06 | Search with empty query | Tap Search with blank field. | Validation; no blank search submitted or all users listed (document actual behavior). |
| ADMIN-USER-07 | View user profile | Click on a search result. | User profile detail view opens with name, phone, role, order history count, and status. |
| ADMIN-USER-08 | Suspend user (if available) | Admin taps "Suspend" on a user. | Confirmation dialog appears. |
| ADMIN-USER-09 | Confirm suspension | Confirm the suspension dialog. | User status changes to "Suspended"; suspended user cannot login. |
| ADMIN-USER-10 | Reinstate user | Admin reinstates a suspended user. | User status returns to "Active"; login works again. |

## Batch 2 — Category management

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-CAT-01 | Categories tab accessible | Navigate to Categories. | Category list/editor renders with existing categories (e.g. "Biryani", "Pizza"). |
| ADMIN-CAT-02 | Category list non-empty | On categories page. | At least one category visible; not a blank page. |
| ADMIN-CAT-03 | Category name field editable | Click on a category → name field becomes editable. | Name field accepts input. |
| ADMIN-CAT-04 | Cancel category edit | Edit name → cancel. | Original name restored. |
| ADMIN-CAT-05 | Add new category form | Tap "Add Category". | New category name input appears. |
| ADMIN-CAT-06 | Submit blank category name blocked | Enter blank name → submit. | Validation error. |
| ADMIN-CAT-07 | Add valid category | Enter valid name → submit. | New category appears in list. |
| ADMIN-CAT-08 | Delete category | Tap "Delete" on an unused category (not assigned to any restaurant). | Category removed from list; confirmation dialog appears before deletion. |

## Batch 3 — Review moderation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-REVIEW-01 | Reviews tab accessible | Navigate to Review Moderation. | `AdminReviewsPage` renders; review list visible. |
| ADMIN-REVIEW-02 | Moderation notice visible | On reviews page. | Read-only moderation notice rendered (if applicable); not a blank section. |
| ADMIN-REVIEW-03 | Review list shows reviews | With submitted reviews. | Review entries visible with customer name, rating, and text. |
| ADMIN-REVIEW-04 | Hide a review | Admin taps "Hide" on an inappropriate review. | Review hidden from customer and restaurant views (flagged/hidden status). |
| ADMIN-REVIEW-05 | Restore hidden review | Admin restores a hidden review. | Review visible again. |
| ADMIN-REVIEW-06 | Filter by rating | Filter reviews by 1-star rating. | Only 1-star reviews shown. |

## Batch 4 — Advanced user management (`AdminUserManagementPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-USER-11 | Filter by role | `filterByRole("CUSTOMER")`. | Only customer users listed; `getUserCount()` reflects customer-only count. |
| ADMIN-USER-12 | Filter by role — RIDER | `filterByRole("DELIVERY_EXECUTIVE")`. | Only rider users listed. |
| ADMIN-USER-13 | Filter by role — RESTAURANT | `filterByRole("RESTAURANT_PARTNER")`. | Only restaurant partner users listed. |
| ADMIN-USER-14 | Assign role to user | Select a user → `assignRole("ADMIN")` or `assignRole("ADMIN", userId)`. | User's role updated; `getUserRole()` reflects new role. |
| ADMIN-USER-15 | Detail panel shows phone | After `selectUser(0)`, `isDetailPanelOpen()` returns true. | `getUserPhone()` returns a valid phone number. |
| ADMIN-USER-16 | Detail panel shows name | After selecting user. | `getUserName()` returns a non-empty name. |
| ADMIN-USER-17 | User active order count | After selecting user. | `getUserActiveOrderCount()` returns a number ≥ 0. |
| ADMIN-USER-18 | User pagination — next | With many users, `nextPage()`. | Page advances; different users shown. |
| ADMIN-USER-19 | User pagination — prev | After navigating to page 2, `prevPage()`. | Returns to page 1. |

## Batch 5 — Categories visibility (`AdminCategoriesPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-CAT-09 | Categories visibility check | On categories page. | `AdminCategoriesPage.isCategoriesVisible()` returns true. |
| ADMIN-CAT-10 | Category count | On categories page. | `getCategoryCount()` ≥ 1; matches visible category cards. |

## Batch 6 — Reviews entity/user mode (`AdminReviewsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-REVIEW-07 | Switch to entity mode | Click `switchToEntityMode()`. | Entity search form visible (entity type dropdown, entity ID input). |
| ADMIN-REVIEW-08 | Switch to user mode | Click `switchToUserMode()`. | User ID search form visible. |
| ADMIN-REVIEW-09 | Search by entity | Select entity type "RESTAURANT" → fill entity ID → `searchByEntity()`. | Reviews for that restaurant shown; `getReviewCount()` ≥ 0. |
| ADMIN-REVIEW-10 | Search by user | Enter a user ID → `searchByUser()`. | Reviews by that user shown. |
| ADMIN-REVIEW-11 | Star ratings visible | On review results. | `hasStarRatings()` returns true; ★ indicators present. |
| ADMIN-REVIEW-12 | Empty state for unknown entity | Search with a non-existent entity ID. | `isEmptyState()` returns true; "No reviews" message shown. |

