# Validation and pending work

## Added read-only E2E coverage

`AdminSupportUserReviewTest` and `AdminUserOpsTest` now require successful user lookup responses, exactly one seeded phone result, exact phone/UUID/role/status details, and a loaded active-order empty state or rows. Role filtering and pagination assert the selected server response, current page, and rendered users. The suspension test opens and cancels its confirmation and asserts no status update was sent.

Category checks assert a successful list response and a real card or explicit empty state. A one-character name must show validation without sending a create request. The edit test opens a saved category form and stops before saving. Review checks validate entity and author lookup, explicit empty results, visible star ratings for returned records, and the read-only state.

Validation performed: `mvn -q -DskipTests test-compile` passed. The live browser tests have not run against Dev in this turn because the configured Oracle tunnel hostnames do not resolve from this workstation.

## Source-verified behavior and findings

- The user lookup source now distinguishes UUIDs from phone numbers and calls `/api/v1/internal/admin/users/by-phone`. The old report's “phone passed to the UUID path” issue is addressed in the current checked-in source; live deployment behavior remains unverified here.
- User detail shows ID, status, phone, roles, and active orders. It does not show name, registration date, or a full order-history count. Supported roles include CUSTOMER, DELIVERY, RESTAURANT, and ADMIN.
- User suspension asks for confirmation. Activation, role changes, and category create/update actions mutate data; no final action is exercised by these E2E tests.
- Categories support create/update only; there is no delete control. The edit icon button has no accessible name in current source. Add an `aria-label` such as `Edit category <name>` and cover it with an accessibility assertion.
- Reviews support entity and author search and are read-only by design. There is no hide/restore action or rating filter. Campaigns belong to the restaurant surface; the admin portal has no campaigns route.
- The user, support, refund, live-order, and intervention screens use a polling hook whose callback changes with filter/page state, but the hook does not refetch immediately when that callback changes. Lists can remain stale until their 15–30 second poll. The new tests wait for the matching server request; a UI follow-up should trigger a fetch on status/filter/page changes and verify it with the same E2E checks.

No backend defect is confirmed by the source findings above.
