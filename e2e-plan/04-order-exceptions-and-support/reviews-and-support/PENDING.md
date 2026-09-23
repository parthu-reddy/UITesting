# Validation failures and pending work

## My Reviews returns HTTP 403

`CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState` is implemented as a strict read-only test. It logs in with a randomized seeded customer, opens Account Settings → My Reviews, and requires either at least one review article or the defined `You haven't reviewed anything yet` empty state.

Live validation on 2026-09-23 fails. `GET /api/v1/reviews/me?page=0&size=20` returns HTTP 403, and the tab renders neither the review list nor its defined empty state. The test remains active and failing. No review was submitted, edited, or deleted.

Evidence: `target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.CustomerSettingsUiTest.xml` and `target/screenshots/myReviewsTabShowsReviewsOrDefinedEmptyState___customer.png` / `.html`.

Review submission and post-delivery support still require a suitable completed order and remain pending.
