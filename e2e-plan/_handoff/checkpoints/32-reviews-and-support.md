# Checkpoint32: reviews and support

Updated 2026-10-03T09:47:00+05:30. First of the remaining features after the priority list.

- **Reviews validator 81/85 → 85/85.** Its five findings were stale (the 2026-09-28 multi-actor review design and moved files), not regressions. The checks now encode the current design, and each was seen red on a reintroduced defect; one was tightened after its break-test stayed green. Details in [the feature status](../../04-order-exceptions-and-support/reviews-and-support/AUDIT-STATUS.md).
- **Live:** new `OrderReviewsFlowTest` has customer, restaurant and rider review each other on bb43e2a4 through the real dialogs, checked against server eligibility, the role matrix and a read-only reopen. The restaurant's Reviews tab shows the public review; My Reviews lists it (that check was tightened to require it); rider history reads while off duty. DB: four reviews with the intended visibility; aggregates and the outlet rating propagated.
- **Retired:** SUPPORT-01..11 (the post-delivery support modal was deleted at checkpoint23).
- No product defects found in this feature; no deployment needed.

Evidence: [32-reviews-live.json](../evidence/32-reviews-live.json).
