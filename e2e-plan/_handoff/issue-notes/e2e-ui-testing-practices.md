# E2E UI Testing Practices

When writing, maintaining, and debugging End-to-End (E2E) UI tests across Food Delivery applications, the following practices must be strictly adhered to by all developers and AI agents.

## 1. Do Not Parse the DOM Dynamically for Debugging
When a UI test fails, **DO NOT** use AI browser subagents or manual DOM parsing tools to click through the UI to see what went wrong. 
- AI DOM parsing is slow, token-costly, and brittle. 
- It simulates an entirely different interaction model than the automated test script and does not reliably reproduce script timing issues.

## 2. Rely on Native E2E Framework Debugging Tools
When using Playwright (or similar E2E frameworks), leverage the framework's native, built-in recording mechanisms to investigate failures. This is the only correct way to view what the script saw at the moment of failure.

### Recommended Approaches
1. **Video Recordings:** Always configure the test runner to output video recordings of test executions (e.g., in a `videos/` or `target/videos/` directory). Review the video to see the visual state right before the timeout.
2. **Screenshot on Failure:** Ensure the framework captures a screenshot at the exact moment an exception is thrown.
3. **Tracing:** For complex failures, enable Playwright Tracing to capture a time-travel snapshot of the DOM, network requests, and console logs.
4. **Headed Execution (Slow Motion):** For local debugging, run the test in headed mode (turning headless mode off) and add a slow-motion delay (e.g., `500ms` between actions) so you can watch the test run in real-time.

By standardizing on these native debugging tools, we avoid wasting time and resources on generic AI DOM-parsing agents that guess at what the test runner was attempting to do.

## Source-grounded, repeatable feature validation

Read the current rendered component/API contract and one feature's scenario map before choosing selectors or expected states. Use exact resource IDs returned by the operation; source-grounded accessible locators must reflect viewport and nested scope. Do not translate Java Pattern.quote delimiters into browser regular expressions. Existing [text/locator lessons](../../../../CommonMistakesDocumentation/UI/e2e-test-verify-ui-text.md) are historical examples, not a guarantee of today's copy.

Follow UITesting/e2e-plan/TEST-DATA.md for seeded accounts, Home, live eligible outlet and isolated role contexts. Rider readiness requires authoritative status, connected socket/current location telemetry and proximity before each checkout. Do not cycle an already-ONLINE rider or take ON_DELIVERY offline. Preserve current user-authorized retained data/sessions; browser-resource disposal is separate from destructive remote teardown. If an assigned order survives a failure, resume its exact manifest rather than create a replacement. Explicit logout/device removal/cancellation remains only when that behavior is the test.

Require a deliberately exercised negative/boundary fixture. Natural data may no longer contain it after a product fix. Routed fixtures can exercise UI behavior while preserving real identities, but must be labeled routed proof. A rejected out-of-service address does not prove successful alternate-address checkout. Empty conditional returns and skips are unverified gaps, not passes.

Keep local unit/integration, routed browser, real Dev command, actual provider and database-concurrency evidence distinct. Dev approval/payment are mocked; actual application order records still persist. Track deployment-interrupted failures and rerun only after user confirmation. Require the initial stream response before navigation; later reload success alone cannot cover startup authorization. A mock transaction callback cannot prove atomic database/outbox behavior.

For the current Dev audit, intentionally slow/expiry cases belong in each feature's DEFERRED-WAIT-TESTS.md and rate/quota cases in DEFERRED-RATE-LIMIT-TESTS.md, skipped/opt-in by default. Fake clocks/already-expired fixtures and normal async readiness are allowed. Never add an arbitrary sleep to work around a missing fixture, wrong selector or backend failure. [Current audit mistakes/evidence](e2e-audit-fixture-and-evidence-mistakes-2026-10-01.md).

Animated transitions may briefly retain both old and new screens with the same resource ID. Scope assertions to the expected semantic state (for example, the container holding the delivered heading) as well as the ID. Do not replace a strict state assertion with arbitrary first() selection. OTP reads must be scoped to the exact tracked order; if reload restores another active order, select the owned one through the UI before reading its code.

Populated-history, cancelled-history and pagination tests require their named fixtures and must fail clearly if absent. A separate routed empty-state test covers the empty contract. Never swallow the loading timeout or return early from the named populated test and report it as passed. Scope exact full IDs rather than unstable layout classes.


Browser response observers must accept non-HTTP/opaque asset URLs such as data: and blob:. Filter endpoint paths defensively; URI.getPath may be null. An observer exception can interrupt an unrelated actor operation, so retain the failed report and resource manifest, correct the observer, and continue that resource if its authoritative state still permits it. A pending-order continuation must reject terminal state and cannot recreate or reset the order.


## Prove rejected commands and recovery

A negative OTP test must derive a code different from the owned actor's visible code, require the exact order's rejected server response and explicit UI error, prove no completed phase after reload, and recover with the correct code on the same order. Avoid catch-all expected-error blocks: they can swallow selector/network failures. Short-format validation must visibly reject without sending a status command. Navigation tests must assert the actual opened target, distinguishing external-content routing from provider integration.

Complete successful order fixtures as an explicit tested lifecycle; preserve records and failed active jobs. No cancellation/deletion/reset teardown. Confirm idle riderOFFLINE authoritatively. A local success test must not catch arbitrary completion failures and call a partial mock interaction success; assert final invariants with complete fixtures, while reserving transaction atomicity for actual database tests. [Pickup audit incident](e2e-audit-fixture-and-evidence-mistakes-2026-10-01.md#pickupdelivery-false-positive-and-retained-state-repairs-2026-10-02).


## Confirm actor responsibility before extending product scope

Review scenario requirements against the actor’s job before adding controls or exposing data to make a test pass. Riders verify and deliver the assigned package; item names, quantities and prices are not required. Do not reintroduce rider item-list coverage without an explicit product decision. See [the role-scope rule](role-scope-before-e2e-product-changes.md).


## Actor-specific state projections

Inspect the owning service enum and projection mapper before asserting lifecycle status. A customer pending order may be CREATED in the restaurant service. Keep service-specific phases explicit and assert command success plus final owned state. Do not share enum assumptions across actors merely because they describe the same business moment.

Navigation/queue setup must not create orders or acquire rider duty. Parameterize retained fixtures with exact manifests, verify all three role owners and current nonterminal phase, and resume through ordinary UI reads instead of creating replacement orders.
