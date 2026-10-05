# Retained Ready/arrival and response observation

## 2026-10-05T06:56:59+05:30 — checkpoint105: Ready and arrival committed; same order resumes pickup

Happy invocation8 on UI509d084 executes1/0pass/0fail/1error/0skip (78.587s), at the arrival-response matcher. Real UI reaches Ready, then the arrival button disappears and pickup form remains for exact60631296. Separate native operator timeline confirms ORDER_READY and completed AT_RESTAURANT plus outbox publication. This verifies the kitchen correction's real effect but is not a full lifecycle pass. Evidence54 retains the original timeout and bounded observations. No checkout or duty reset occurs; owned order remains READY_FOR_PICKUP/AT_RESTAURANT.

UITestingafe12f1 is pushed, compile31 passes. Response matching now uses POST plus the owned URI path and subsequent visible phase; it does not depend on optional request-body metadata or a query-free raw URL. The exact reason the former predicate missed the response is not claimed beyond that overly restrictive predicate. The two-line extra teardown was removed after source verification that TestBase already invokes the safe shared idle-rider helper. Reuse existing code instead of a duplicate helper. Assigned Happy invocation9 is running for the same order's pickup/delivery/chat/history/payout.

Current deployment remains UI509d084/Customer0849ee6; GitHub941/941, hardening15/15 and reconciliation29/0drift are unchanged. Final phase gates, same-order money/quote/review/read-only regressions and measurements/checklists remain open. Stop after O4/O5.
