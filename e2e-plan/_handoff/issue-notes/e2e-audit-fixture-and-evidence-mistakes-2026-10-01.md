# E2E audit: fixtures, selectors and proof boundaries — 2026-10-01

These are test/plan/workflow mistakes, distinct from the confirmed [UI defects](../../../../CommonMistakesDocumentation/Frontend/e2e-audit-state-and-recovery-2026-10-01.md). Existing September incidents remain in [the earlier test record](../../../../CommonMistakesDocumentation/UI/e2e-test-mistakes-sept2026.md); current rendered copy must always be reread.

| Mistake encountered | Required correction and evidence boundary |
|---|---|
| Expecting a role card's accessible name to equal its title when its description is included | Read the actual component; choose a source-grounded accessible-name prefix or scoped exact child. The initial environment test failed and its corrected rerun passed. |
| Stale case-sensitive `Today’s Earnings` text and selecting the wrong adjacent span | Scope the current earnings label and the currency-bearing span; validate the amount, not another counter. Initial lifecycle failure occurred before order creation. |
| Incorrectly rooting a nested `has` locator | Build the relative child locator from `page`, then scope to the actual named dialog/button. Do not extend timeouts for a selector that cannot match. |
| Reusing nonexistent or no-longer-negative fixtures | A saved Home whose coordinates were corrected had no naturally far Brand1 outlet. Far/keyboard-disabled checks now route distances while preserving real IDs/names and are labeled UI-contract proof. Missing fixture means unverified coverage, not product failure or a conditional pass. |
| Treating seeded Work as a positive second-address checkout fixture | Work is outside the selected outlet's service area. Its correct quote rejection proves the negative boundary and correct address binding; a successful alternate-address checkout remains separately required. |
| Assuming discovery radius equals delivery radius | Discovery requests10km; outlet eligibility/delivery cutoff is5km. Compare the displayed catalog/distance against its owning business rule. |
| Assuming acceptance means PREPARING, dispatch must wait for READY, or customer status is always SSE | Source has separate ACCEPTED/PREPARING transitions, dispatch scheduled15minutes before estimated completion, and adaptive customer polling. Correct the plan before writing assertions. There is no restaurant Completed tab: assert terminal queue disappearance and exact outlet history. |
| Accepting another retained order offered to the same rider | Bind the newly created UUID from the successful POST; inspect dispatch identity before clicking; block an unrelated accept request before server mutation. Preserve active assignments for recovery. |
| Treating reload success as proof of the initial stream | The first post-deployment run captured two200responses after reload but cancelled the initial stream before its response. The test now requires initial200 before navigation. This exposed the separate [pool leak](../../../../CommonMistakesDocumentation/PerformanceAndResilience/sse-jpa-pool-exhaustion-2026-10-01.md). |
| Running during deployment, or retrying by creating replacement orders | Record interrupted failures and rerun only after deployment confirmation. If an assigned order already exists, use the manifest/resume path instead of creating another order. |
| Using destructive teardown with shared Dev fixtures | Current user policy retains accounts/profiles/addresses/orders/server sessions. Close browser resources normally; only an idle test rider is madeOFFLINE with server confirmation. Preserve ON_DELIVERY. Explicit logout/device removal/cancellation stays only when that behavior is being tested. Earlier cleanup reports are historical and do not authorize new cleanup. |
| Treating local/mocked/conditional coverage as full deployed proof | Keep local unit, local integration, routed browser, real Dev command, production provider and database concurrency evidence separate. Never mark a skipped or unexecuted conditional body passed. A mocked callback does not prove transaction atomicity. |
| Running expiry/quota tests in the fast Dev pass | Record intentional waits in each feature's DEFERRED-WAIT-TESTS.md and rate/quota cases in DEFERRED-RATE-LIMIT-TESTS.md; leave skipped/opt-in. Fake clocks/already-expired fixtures and ordinary async readiness remain allowed. No government provider calls or real payment charges are required in Dev. |

The original audit and its failed attempts remain under [E2ECoverageAudit_2026-10-01](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/CHECKLIST.md). Read one feature's scenarios/source/evidence before advancing; preserve unresolved scenarios and exact validation status.

[Reusable E2E rules](../../../../CodingPracticesAcrossAllServices/09_Testing/e2e-ui-testing-practices.md).

## Current assertion correction

The stronger customer-progress run verified ACCEPTED, assigned rider, PREPARING, READY_FOR_PICKUP, arrival and outbound delivery without customer reload, then failed a newly added exact-case assertion expecting `Order Delivered`. The current delivered receipt heading is `Order delivered`; the exact-case history label is a different component. Corrected the assertion against receipt source and retained the failed report rather than reporting the invocation green. Source copy must be checked independently for each owning component, even when status semantics match.

During overlapping-order validation, the live tracker and delivered receipt briefly shared the same data-order-id while AnimatePresence transitioned. A broad exact-ID parent locator was therefore ambiguous despite the correct receipt heading being visible. Scope terminal assertions to the exact-ID container containing the delivered heading; do not use an arbitrary first() to mask duplicate matches. The retained pair was resumed first; the second order subsequently reached its normal acceptance timeout while assertions were corrected. Both original records remain retained. A fresh pair is permitted only after verifying that terminal state, without resetting or deleting the old records.

Restaurant and customer pending-state attributes are separate contracts: the incoming restaurant card uses `CREATED`, while the customer tracker uses `PENDING_ACCEPTANCE`. A new concurrency assertion incorrectly copied the customer value into the restaurant expectation. The source-backed correction changes only that assertion. Preserve its failed report and independently rerun the whole pair; successful delivery of the first order alone does not prove the concurrency scenario.

## History readiness and populated-row coverage

The existing history page object matched layout classes that no longer identify rendered order buttons, accepted transient content and swallowed loading timeouts. Its summary/detail tests returned successfully for an empty screen instead of exercising their named scenarios. The baseline with15known retained orders produced1pass/2fail; the pass did not exercise populated summary checks. Scope rows by the actual stable test ID/full order ID, require settled success explicitly, and require an eligible fixture for populated/cancelled/pagination tests. Keep deterministic empty/error/loading routed contracts separately labelled; never infer persistence or real cancellation from them.

The first strengthened deployed summary run also exposed that `span.font-mono` matches both the short ID and the status badge. Match the exact visible short ID derived from the full row identity, and independently scope the status badge. The seven-pass/one-failure report is retained; the corrected whole eight-method class passed.


## Map response observer — 2026-10-02

The new live-map observer called URI.getPath().endsWith for every browser response. A legitimate opaque data: map asset has no path, so the listener threw and stopped both methods: the first after creating an unaccepted retained order, the second before creating orders. This is a test defect, not a product/map failure. Guard a null path before matching a backend endpoint; persist only sanitized path/status/content-type evidence. The first order is resumed through a guarded pending-order path requiring the same seeded actors and actual PENDING_ACCEPTANCE state; no additional order POST or record deletion. Retain the failed report and classify the corrected live invocation separately.


The existing ScreenshotOnFailure extension expands the viewport height to2500 before capture. A screenshot can therefore show a map that was zero-height at the failing1280×720viewport. Keep normal-viewport dimensions in sanitized evidence before the visibility assertion; do not dismiss the failure on the basis of a differently sized screenshot. The production correction is nonshrinking rail map layout, not changing the deployment workflows or weakening the assertion.


## Pickup/delivery false-positive and retained-state repairs —2026-10-02

Seven historical feature tests timed out; source used outdated checkout, arbitrary sleeps, broad dispatch selection, stopped mid-job and swallowed wrong-code/navigation exceptions. Remaining in a phase cannot distinguish a rejected command from a command that never ran;000000 is not guaranteed incorrect. Repair uses a guaranteed-distinct visible actor OTP, exact owned POST400/explicit message, reload/recovery and complete delivery on the same retained order. Navigation requires actual accessible directions action/new target instead of absent-selector catch-all. Seven repaired methods passed; all orders/assignments reached delivered/released, riderOFFLINE. New itemized-details product assertions still await user deployment. [Feature10](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/10-pickup-and-delivery.md).

A local backend OTP-success test swallowed arbitrary strategy completion errors and accepted an outbox invocation even when rider completion failed. Supply the complete local fixture and require no exception plus final assignment/rider invariants. Mock transaction execution remains distinct from database atomicity proof.


## Restaurant service status and fixture scope —2026-10-02

The restaurant projection uses CREATED while the customer order uses PENDING_ACCEPTANCE. A test rewrite mistakenly asserted the customer enum on restaurant cards; three runs stopped before commands. Preserve the reports and manifests, correct the actor-specific assertion, and resume owned pending orders. Do not turn this into a product defect or create replacements.

The older restaurant suite created an order in every setup, including queue-only coverage, used obsolete rider readiness and conditional acceptance/start-cooking assertions. Queue/navigation checks must not create orders. Stateful flows require canonical quote/payment/rider preflight, full-ID command confirmation and explicit successful completion or tested rejection. UI reload persistence is separate from server duplicate-command concurrency.


## Checkpoint18: long-chat history and local assertion mistakes

The UI requested page0,size50; chat repository ordered ascending, so sessions over50messages reopened with the oldest messages. Repository now uses createdAt DESC,id DESC; UI already sorts chronology. One local real JPA fixture checks61messages/newest+older windows/no overlap/other-session exclusion/empty page and equal-time stability. New tie assertion initially used signed UUID Java comparison; corrected to lexical ordering compatible with database UUID sort. New local H2 fixture explicitly disables datasource replacement and specifies driver/dialect; inherited PostgreSQL defaults caused one startup failure before correction. Preserve those failures separately from final22chat passes. Old-message UI pagination remains open, and local query proof is not deployed cross-role history proof.
