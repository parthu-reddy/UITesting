# 06 — Users, Categories, and Moderation — All Scenarios

Uses: `AdminUserManagementPage`, `AdminReviewsPage`, `AdminPortalPage`.

## Batch 1 — User management

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-USER-01 | User management tab accessible | Login as admin → navigate to Users. | `AdminUserManagementPage` renders; search interface visible. |
| ADMIN-USER-02 | Search by customer phone | Enter a seeded customer phone → Search. | One matching record appears with UUID, phone, and role chips. |
| ADMIN-USER-03 | Search by restaurant phone | Enter 9000000001 → search. | Restaurant partner record appears. |
| ADMIN-USER-04 | Search by rider phone | Enter 7000000001 → search. | Rider record appears with document verification status. |
| ADMIN-USER-05 | Search unknown phone | Enter a phone not in the system → search. | "No Users Found" appears; no crash. |
| ADMIN-USER-06 | Search with empty query | Leave the field blank. | The role list is shown; Search itself does not submit an empty lookup. |
| ADMIN-USER-07 | View user profile | Click a search result. | Detail shows UUID, status, phone, roles, and active orders; it does not show registration date or full history. |
| ADMIN-USER-08 | Suspend user | On an active seeded user, tap "Suspend User". | Confirmation dialog appears with the suspend action. Cancel it for shared Dev data. |
| ADMIN-USER-09 | Confirm suspension | Confirm only in a disposable user fixture. | User status changes to "Suspended" and the detail action changes to Activate User. Durable access revocation still needs a target check. |
| ADMIN-USER-10 | Reinstate user | Reactivate only a disposable suspended fixture. | User status returns to "Active" and the detail action changes to Suspend User. Login revocation/renewal still needs a target check. |

## Batch 2 — Category management

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-CAT-01 | Categories tab accessible | Navigate to Categories. | Editor renders with saved cards or the explicit "No categories found." state. |
| ADMIN-CAT-02 | Category list state | On categories page. | Count matches visible cards; an empty list is a valid explicit state. |
| ADMIN-CAT-03 | Category name field editable | Click a saved category's edit icon. | The form switches to Edit Category and is prefilled. |
| ADMIN-CAT-04 | Cancel category edit | Edit a saved category → click the X cancel control. | Form returns to Global Categories without a PUT. |
| ADMIN-CAT-05 | Add category form | Open Categories. | Name/description form is present; there is no separate Add Category dialog. |
| ADMIN-CAT-06 | Invalid category name blocked | Enter a one-character name → submit. | "Category name is required" appears and no POST is sent. A blank name currently returns without a message. |
| ADMIN-CAT-07 | Add valid category | Create a unique category in an isolated catalog fixture. | New category appears after the fixture-backed save and refresh. Do not add shared Dev data. |
| ADMIN-CAT-08 | Delete category | — | Not supported by the current admin category UI. |
| ADMIN-CAT-11 | Rejected category create recovery | Submit a valid new category through a browser-local fixture that returns a rejection. | The name and description stay editable; the existing list is unchanged and no creation success notice appears. |

## Batch 3 — Review moderation

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-REVIEW-01 | Reviews tab accessible | Navigate to Review Moderation. | `AdminReviewsPage` and its lookup form render; results are empty until a search is submitted. |
| ADMIN-REVIEW-02 | Moderation notice visible | On reviews page. | Read-only moderation notice rendered (if applicable); not a blank section. |
| ADMIN-REVIEW-03 | Review list shows reviews | Search a seeded entity or author with submitted reviews. | Cards show rating, date, comment when present, author, order, target, and review IDs. |
| ADMIN-REVIEW-04 | Hide a review | — | Not supported; review moderation is read-only by design. |
| ADMIN-REVIEW-05 | Restore hidden review | — | Not supported; review moderation is read-only by design. |
| ADMIN-REVIEW-06 | Filter by rating | — | Not supported by the current review lookup form. |

## Batch 4 — Advanced user management (`AdminUserManagementPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-USER-11 | Filter by role | `filterByRole("CUSTOMER")`. | Only customer users listed; `getUserCount()` matches visible cards. |
| ADMIN-USER-12 | Filter by role — DELIVERY | `filterByRole("DELIVERY")`. | Only delivery users listed. |
| ADMIN-USER-13 | Filter by role — RESTAURANT | `filterByRole("RESTAURANT")`. | Only restaurant users listed. |
| ADMIN-USER-14 | Assign role to user | Select a user and grant a role in an isolated fixture. | User role chips update after the confirmed fixture response. Actual role assignment and session revocation remain target-fixture-gated. |
| ADMIN-USER-15 | Detail panel shows phone | After `selectUser(0)`, `isDetailPanelOpen()` returns true. | `getUserPhone()` exactly matches the searched phone. |
| ADMIN-USER-16 | Detail panel shows name | After selecting user. | Not displayed by the current detail panel. |
| ADMIN-USER-17 | User active orders | After selecting user. | Active order cards render or the explicit "No active orders for this user." state appears. |
| ADMIN-USER-18 | User pagination — next | With many users, `nextPage()`. | Page advances; different users shown. |
| ADMIN-USER-19 | User pagination — prev | After navigating to page 2, `prevPage()`. | Returns to page 1. |

## Batch 5 — Categories visibility (`AdminCategoriesPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-CAT-09 | Categories visibility check | On categories page. | `AdminCategoriesPage.isCategoriesVisible()` returns true. |
| ADMIN-CAT-10 | Category count | On categories page. | `getCategoryCount()` matches visible cards, or the explicit empty state is shown. |

## Batch 6 — Reviews entity/user mode (`AdminReviewsPage`)

| ID | Description | Action | Expected result |
|---|---|---|---|
| ADMIN-REVIEW-07 | Switch to entity mode | Click `switchToEntityMode()`. | Entity search form visible (entity type dropdown, entity ID input). |
| ADMIN-REVIEW-08 | Switch to user mode | Click `switchToUserMode()`. | User ID search form visible. |
| ADMIN-REVIEW-09 | Search by entity | Select entity type and a known/unknown entity ID → `searchByEntity()`. | Successful lookup shows matching cards or the explicit empty state. |
| ADMIN-REVIEW-10 | Search by author | Enter a seeded user UUID → `searchByUser()`. | Successful lookup shows that author's review cards or the explicit empty state. |
| ADMIN-REVIEW-11 | Star ratings visible | On non-empty review results. | Every card has one visible accessible star-rating image. |
| ADMIN-REVIEW-12 | Empty state for unknown entity | Search with a non-existent entity ID. | `isEmptyState()` returns true; "No reviews" message shown. |
