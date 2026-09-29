# reviews-and-support

Status: review journeys are verified on the deployed development environment, but the application is not production-ready yet. Customer, restaurant, and delivery-partner review flows passed across focused runs; My Reviews and restaurant feedback/aggregate also passed. The test run found a restaurant order-earnings HTTP 500 that makes the details UI show ₹0.00 fallback values. A backend fetch fix and a strict E2E assertion are now in source; the fix still needs deployment and live retesting. One separate full-delivery attempt also failed with HTTP 500 while setting the rider to AT_RESTAURANT.

Scope: Ratings, reviews and post-delivery support.

Implementation: `ReviewFlowTest.submitReview` covers customer ratings for the order's restaurant, assigned delivery partner, and purchased dish, plus restaurant and delivery-partner ratings for their permitted order participants. It reopens submitted reviews and checks they are read-only. `CustomerSettingsUiTest.myReviewsTabShowsReviewsOrDefinedEmptyState` checks customer review history. `RestaurantNavigationUiTest.restaurantReviewsShowPublicFeedbackAndAggregate` checks public feedback and the outlet aggregate. Backend tests cover ineligible actors and targets. See [validation failures and pending work](PENDING.md) for the current deployment blockers and the live-test boundary.
