# Validation and pending work

## Added read-only E2E coverage

`AdminSupportRefundQueueTest` now checks the actual OPEN/IN_REVIEW/RESOLVED/REJECTED support requests, response status, page counts and empty states. It opens a support rejection confirmation and cancels it. Refund checks verify the queue response/state and open/cancel both approval and rejection confirmations; a request listener asserts that no resolve POST was sent.

`AdminSupportUserReviewTest` adds strict support counts and page navigation. These checks do not resolve, reject, message, or refund shared Dev records.

Validation performed: `mvn -q -DskipTests test-compile` passed. Deployed browser E2E has not run because the configured Oracle tunnel hostname does not resolve from this workstation.

## Source findings and prerequisites

- The support status buttons change list state, and the current code fetches support tickets on a 15-second polling loop. Changing status does not trigger an immediate fetch through `usePolling`; a changed tab can show the prior status's rows until the next poll. The E2E waits for the status request to make this visible rather than treating the heading alone as success.
- Approving a support request validates the refund amount and opens an amount-specific confirmation. Rejecting requires notes and opens a confirmation. Both close a ticket and require a disposable fixture for committed tests.
- The support details component passes the support-ticket UUID as `ChatWidget`'s `orderId`. Confirm the chat API contract and use a disposable conversation before sending any message through this UI.
- Refund queue actions also require an amount-specific confirmation and write to financial state. Refund amount/status assertions beyond the visible table need an isolated refund fixture.
- The current queues have no ticket search or date-range filter. Status values are OPEN, IN_REVIEW, RESOLVED, REJECTED, plus ALL on the refund queue.

No backend defect is confirmed by these source findings.
