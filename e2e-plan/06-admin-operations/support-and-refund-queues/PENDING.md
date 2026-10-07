# Validation and pending work

## 2026-10-07T11:33:21+05:30 — Approved fixture completed; never rerun its write

Owned47ffbccd on6fbe0289 is REJECTED. Same-ticket resume verifies audit and0refunds with0writes,
1/1green. Original writer error after rejection200 is retained; no populated-audit fixture blocker
remains. [Checkpoint124](../../_handoff/checkpoints/124-deployed-receipt-owned-refund-and-active-rider-gate.md). Exact ticket manifest is durable in _handoff/fixtures.

## Checkpoint123 — exact dummy visual fixture approved, waiting on UI deployment

All Tickets real UI read passes1/1,GET200,0rows/0resolution writes. Populated detail/confirm/audit
is unverified. Owner approved one normal support request and rejection for delivered6fbe0289,
with no refund approval or payout. `OwnedRefundVisualUiTest` compiles and uses normal login/History/
support/queue controls and observed UI responses only. Run after owner UI6eb4ef6 is live and the
retained receipt continuation passes:

```sh
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn test -Dtest=OwnedRefundVisualUiTest \
  -Dvisual.refund.order.id=6fbe0289-645e-4f1c-ae0d-caab21689070 \
  -Dcustomer.phone=8000000001 -Drestaurant.phone=9000000001 -Drider.phone=7000000001 \
  -Dadmin.phone=1000000002 -Dheadless=false -Dslow.mo=0 -Drecord.video=false \
  -Ddefault.timeout=30000 -Dvisual.audit.dir=e2e-plan/_handoff/evidence/123-owned-refund-visual
```

Refuse any other order/reason; reuse the exact matching ticket on retry and never repeat a final
rejection. Approve confirmation is cancelled; only this dummy rejection commits. Verify no order
write, exact rejected response/audit and actual UI refund reads empty before/after. Preserve the
two-hour CTA prerequisite and all captures/identifiers. [Fixture scope](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ADMIN-FIXTURE.md).
This permission does not reopen actual refund payments or the older full financial flow below.

## Local browser-routed coverage

`AdminSupportRefundQueueTest` now checks the actual OPEN/IN_REVIEW/RESOLVED/REJECTED support requests, response status, page counts and empty states. It opens a support rejection confirmation and cancels it. Refund checks verify the queue response/state and open/cancel both approval and rejection confirmations; a request listener asserts that no resolve POST was sent.

`AdminSupportUserReviewTest` adds strict support counts and page navigation. These checks do not resolve, reject, message, or refund shared Dev records.

Deployed Dev validation: `AdminSupportRefundQueueTest` ran 3 tests with 0 failures, 0 errors, and 2 fixture-dependent skips. `AdminSupportUserReviewTest` ran 13 tests with 0 failures, 0 errors, and 1 fixture-dependent skip.

`AdminSupportTicketResolutionRoutedUiTest` and `AdminRefundPolicyRoutedUiTest` now cover approved/rejected resolution contracts and failed refund-resolution recovery in browser-local fixtures. `AdminSupportChatIsolationRoutedUiTest` covers ticket-isolated session reads, moderator send/receipt rendering, and gallery-image upload success/failure recovery through local HTTP/STOMP fixtures. The image cases assert the exact `data.url` response field and an authoritative IMAGE broadcast, while blocking every unexpected write. They are safe against shared Dev data but cannot prove target R2 persistence or cross-role delivery.

## Source findings and prerequisites

- The support status buttons change list state, and the current code fetches support tickets on a 15-second polling loop. Changing status does not trigger an immediate fetch through `usePolling`; a changed tab can show the prior status's rows until the next poll. The E2E waits for the status request to make this visible rather than treating the heading alone as success.
- Approving a support request validates the refund amount and opens an amount-specific confirmation. Rejecting requires notes and opens a confirmation. Both close a ticket and require a disposable fixture for committed tests.
- Support chat binds the selected ticket's order ID to `ChatWidget`; the browser fixture verifies that switching tickets uses distinct session reads and that a moderator send/receipt stays inside the selected session. Target validation still needs a safe real order and isolated role contexts.
- `useWebRTC.initializePeerConnection` now makes a new peer connection current before the asynchronous microphone-permission request. The local lifecycle repair and focused frontend test are complete. Browser E2E still needs a controlled, isolated cross-role WebRTC/media fixture to prove an admin ticket call reaches the remote participant, renders CallOverlay state, and cleans up safely without touching shared Dev data.
- Refund queue actions also require an amount-specific confirmation and write to financial state. Refund amount/status assertions beyond the visible table need an isolated refund fixture.
- The current queues have no ticket search or date-range filter. Status values are OPEN, IN_REVIEW, RESOLVED, REJECTED, plus ALL on the refund queue.

The remaining target work is a controlled live support/refund fixture that verifies durable ticket/refund state and actual cross-role chat fanout. No new backend defect is confirmed by the local fixture coverage.
