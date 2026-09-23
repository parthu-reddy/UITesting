# rider-availability-and-dispatch

Status: first UI-only availability batch validated on 2026-09-23. `RiderAvailabilityUiTest` passed with 1 test, 0 failures and covers DISPATCH-01 through DISPATCH-04, DISPATCH-16 and DISPATCH-18. The passing `HappyDeliveryFlowTest` additionally covers dispatch appearance and acceptance for an exact order (DISPATCH-05, DISPATCH-09 and the active-job transition in DISPATCH-11).

Scope: Rider availability, location, dispatch eligibility and job acceptance.

The availability test logs into a random approved seeded rider from `7000000001`–`7000000030`, records that the fresh login exposes an explicit Online or Offline state, verifies Online survives reload, verifies Offline survives reload, and restores the selected rider to Online before opening completed-delivery history. Every state change uses the visible duty control. Use `-Drider.phone=...` to reproduce one account's behavior.

Validation command: `mvn -q -Dtest=RiderAvailabilityUiTest -Dheadless=true -Dslow.mo=0 -Drecord.video=false test`.

Remaining dispatch details, decline behavior, timeout, reload during an assigned job, blocked-account behavior and wallet assertions remain pending. Dispatch timeout is intentionally deferred because it requires waiting for the real timeout.
