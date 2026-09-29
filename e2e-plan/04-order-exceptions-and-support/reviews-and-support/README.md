# reviews-and-support

Status: in progress. Focused deployed E2E validation passed for customer submission and immutability, My Reviews, and the restaurant review list and aggregate. The expanded all-participant flow exposed that restaurant Order History is unreachable in the deployed UI; a route fix is in source and awaits deployment. Restaurant/delivery-partner submissions and negative eligibility cases remain unvalidated end to end.

Scope: Ratings, reviews and post-delivery support.

Implementation: `ReviewFlowTest.submitReview` completes a real seeded order and verifies customer submission and immutability; it is being extended to cover restaurant and delivery-partner submissions. `CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState` checks the customer’s review history. `RestaurantNavigationUiTest.restaurantReviewsShowPublicFeedbackAndAggregate` checks public feedback and the outlet aggregate. See [validation failures and pending work](PENDING.md).
