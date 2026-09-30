# Validation and pending work

## Added read-only E2E coverage

`AdminSupportUserReviewTest` and `AdminUserOpsTest` now require successful user lookup responses, exactly one seeded phone result, exact phone/UUID/role/status details, and a loaded active-order empty state or rows. Role filtering and pagination assert the selected server response, current page, and rendered users. The suspension test opens and cancels its confirmation and asserts no status update was sent.

Category checks assert a successful list response and a real card or explicit empty state. A one-character name must show validation without sending a create request. The edit test opens a saved category form and stops before saving. Review checks validate entity and author lookup, explicit empty results, visible star ratings for returned records, and the read-only state.

Deployed Dev validation: `AdminSupportUserReviewTest` ran 13 tests with 0 failures, 0 errors, and 1 fixture-dependent skip; `AdminUserOpsTest` passed its 1 test. The only error in that earlier overall admin run was the separate fleet-map viewport issue.

`AdminUserCatalogModerationRoutedUiTest` now covers fulfilled and rejected role grant/removal and status transitions, category create/update/error paths, and review lookup isolation through browser-local fixtures. The create-error case retains the new category fields after a rejected POST and asserts that no false success or list update occurs. It asserts exact mutation paths and request bodies while terminating all writes outside the selected fixture.

## Source-verified behavior and findings

- The user lookup source now distinguishes UUIDs from phone numbers and calls `/api/v1/internal/admin/users/by-phone`. The old report's “phone passed to the UUID path” issue is addressed in the current checked-in source; live deployment behavior remains unverified here.
- User detail shows ID, status, phone, roles, and active orders. It does not show name, registration date, or a full order-history count. Supported roles include CUSTOMER, DELIVERY, RESTAURANT, and ADMIN.
- User suspension asks for confirmation; activation, role grant/removal, and category create/update are covered through browser-local fixtures. Target checks still need an isolated user and catalog fixture because real role changes revoke sessions and catalog changes affect shared users.
- Categories support create/update only; there is no delete control. Browser-routed coverage opens, cancels, creates, updates, and handles rejected create and update paths through the visible form.
- Reviews support entity and author search and are read-only by design. There is no hide/restore action or rating filter. Campaigns belong to the restaurant surface; the admin portal has no campaigns route.
- User, support, refund, live-order, and intervention screens use polling. Browser-routed coverage explicitly waits for the matching response when changing selection, filter, or page, but a deployed run should still verify visible state changes immediately after a real mutation.

No backend defect is confirmed by the source findings above.
