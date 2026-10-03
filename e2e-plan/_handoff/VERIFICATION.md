# Verification and evidence boundaries

Proof labels: source review, local unit, local real JPA (H2 PostgreSQL mode), producer-stub/adapter contract, browser-routed UI fixture, real deployed cross-role flow, Oracle read-only state. Keep these separate. Test method counts differ from parameterized invocation totals. A skipped method, conditional absent-widget body or natural empty fixture does not prove its business scenario. An environment or fixture blocker remains unverified.

Historical fast passes are mapped in FEATURE-STATUS.md and checkpoints/; current active fixtures belong to the fresh seed after the user's resets. Never reuse a deleted historical ID, older fresh-run count or predeployment success as current settlement proof. CURRENT-STATE.md supersedes archived snapshots. Previously mapped source count was 71 classes/325 methods/28 features; rescan before interpreting counts after other agents change source.

Current key evidence is copied here, including 81 selected backend checks (16 ledger/47 customer/18 payment, no failures/errors/skips), 129 earlier focused checks, owned manifests, deployed image/record snapshots and raw cause/timeline summaries. Full raw reports remain in the original audit archive; portable summaries preserve the operational conclusions. None establishes whole-suite success.

Known checks and limitations:

- Original owned lifecycle really delivered and captured native cross-role chat successes, but failed at admin money HTTP 500 before aggregate chat/quote assertions. Do not say the whole canonical test passed. HTTP 500 fixed and admin money HTTP 200/arithmetic later verified separately.
- Expanded chat aggregate assertions were not all reached, so images/reconnect/typing/unread/history/grace cannot all be promoted to deployed passes merely because OrderChatChecks exists. Three routed retry/loading cases passed as fixture proof.
- Quote publisher became PROCESSED after Boot scan/publisher fix. Raw Kafka listeners then failed before business execution. Adapter-based local fixes and deployment followed, but corrupt delivered state currently blocks real quote roundtrip.
- Capture-only money returned balanced rows; strict payee assertion failed missing RESTAURANT_PAYABLE. Delivered payout quote/history is not posted earnings. Confirm exact transaction/owner/order/category/direction/net amounts with decimal arithmetic.
- Cancellation/rejection terminal UI actions succeeded but whole tests failed normal async refund completion; retained refunds are now FAILED. Approval/PROCESSING is not money returned, and updatedAt is not completedAt.
- 129 focused checks omitted LedgerDltRejectionTest. Its dependency/mock had become stale after recorder change; user found it.16direct ledger checks now pass including the existing missed test, wiring and actual producer contract. Do not call 129 the full suite.
- New capture→partial/full refund persistence test commits local records and checks actual counts/amounts/refund status/duplicates, plus existing HTTP webhook integration. It does not prove Oracle callback processing of retained historical records.
- Strict startup with bean overriding disabled revealed duplicate wallet Feign specification; deployed defaults allow overriding. Keep the stricter diagnostic separate; no defaults were changed to pass it.

Before deployment: run affected existing tests and meaningful new checks, compile/types/lint where relevant, verify diff, record count/report paths and concrete pending runtime work. After deployment: inspect exact build/state, rerun only dependent failed/unverified checks. If limits/time stop work, persist the checkpoint and exact next command here first.


Checkpoint 16 adds 32 selected local payment invocations (12 classes, zero failure/error/skip) for the exact-amount guard, including the expanded existing committed persistence fixture. Its final rerun is counted once. This is another local batch, not 32 new E2E cases, a live repair, or a full-suite result. The new guard is deployed and healthy at checkpoint 17; live financial recovery remains unverified.


Checkpoint18 adds 172 selected unique local invocations:120customer backend across20classes,22chat backend across7classes,30UI across5files. Final failures/errors/skips=0; reruns counted once. Includes producer-stub consumer contract, real local refund/outbox rollback, real local newest/older history query (61 messages plus isolation/tie checks), and fake-clock refund UI request isolation/retry/refresh. Typecheck/lint/diff checks passed. Initial failures and final class counts are preserved in evidence/18-local-results.json. User acknowledged deploying customer-service/chat-service/UI; confirmation is pending. No deployed proof or live financial action is included in this batch.


Checkpoint19:58selected unique payment-gateway local invocations across15classes passed, failures/errors/skips0.22new invocations (15service,1committed concurrent JPA,6method security) plus affected existing persistence/HTTP/consumer contract/startup checks. Initial broader run passed57tests but logged4background Redis mock errors; final setup-corrected run logs0unexpected scheduled task errors and is counted once. Recovery endpoint uses existing admin route confirmed from gateway source. No deployed financial recovery/provider receipt/full-suite claim. Exact class counts: evidence/19-local-results.json.

Checkpoint19 handoff relative-link verification checked48documents (handoff Markdown plus root AGENTS/README);0missing links. Refund capture recovery is REFUND-22; REFUND-21 remains initiation-versus-completion. No duplicate scenario IDs introduced.


Checkpoint24: real deployed flows: fresh HappyDeliveryFlowTest PASS 1/1 (order 0554f250); SupportRefundResolutionFlowTest 3 run, 2 pass (denial, reduced award; DB money confirmed), 1 fail (product defect). Local: customer full `clean test` 465 tests / 92 classes, 0 fail/error/skip, including 9 new tests in 3 classes. The new real-proxy test reproduced the production `UnexpectedRollbackException` before the fix, and three mutations were seen red. The consumer refusal fix has local proof only; no live INITIATED-intent cancellation was run. Validators: money audit 0/23 (clean), readiness 59/61 without --with-tests (4.1/5.4 pre-existing), lifecycle 68/68, validate_core_services 50/56 (6 pre-existing). [Local results](evidence/24-local-results.json).

Checkpoint25: real deployed flows on customer-service cc04ed7. Resumed HappyDeliveryFlowTest PASS 1/1 (d3acfc93; the fresh attempt's error is recorded, not counted as a pass). SupportRefundResolutionFlowTest PASS 3/3, plus the 03 precondition both ways (stale fails fast, fresh passes). DB/outbox/log proof in evidence/25-deployed-support-refunds.json. The earlier 03-only attempt on 0554f250 errored on the chat window, not the fix. Local UI: typecheck, lint, vitest 743/121; guard seen red. The UI change is not deployed.

Checkpoint26: real deployed UI 41578ee. ChatSupportWindowClosedTest (new, CHAT-REFUND-05) PASS 2/2 invocations on aged orders; delivered follow-up PASS 1/1 on bb43e2a4 (quote through the button). The fresh lifecycle that delivered bb43e2a4 FAILED at :732 (timing) and is not counted as a pass. Evidence: evidence/26-ui-button-fix-verified.json.

Checkpoint27: real deployed: ChatAndRefundIsolationTest 1 run, all probes pass except the order read (500, defect); ChatHistoryPagingTest red on UI 41578ee as expected. Local: customer 467/93, chat 63/20, UI 750/121 + typecheck/lint; guards seen red (OrderReadOwnershipTest 500 before fix; chat bound mutation 3 red; UI paging mutations 2 red). No deployed proof yet for the three fixes.

Checkpoint28: local only. payment-gateway clean test 93/28 (seam test seen red); UI vitest 754/121 + typecheck/lint (tab tests seen red). AdminRefundRetryFlowTest written and compiled, not run. SupportRefundResolutionFlowTest helper move compile-checked, not re-run live.

Checkpoint29: real deployed, all 1/1 with 0 skips: ChatAndRefundIsolationTest, fresh HappyDeliveryFlowTest (cf608115), ChatHistoryPagingTest, AdminRefundRetryFlowTest; DB and log proof in evidence/29-deployed-priority-runs.json. SupportRefundResolutionFlowTest not re-run after its helper move.

Checkpoint30: real deployed: OrderCancellationFlowTest PASS (19359711), RestaurantRejectFlowTest PASS (bf109947); AdminOrderMoneyOutcomesTest 5/5 red on UI 766b214 (expected). Local UI 758/121 + typecheck/lint, mutation seen red.

Checkpoint31: real deployed: AdminOrderMoneyOutcomesTest 5/5, 0 skips (red 5/5 on the previous UI).

Checkpoint32: real deployed: OrderReviewsFlowTest PASS (after two test-side attempts that wrote nothing), REVIEW-AGG-01 PASS, My Reviews PASS (tightened; seen red), RiderReviewHistoryApiTest PASS. Reviews validator 85/85 after stale-check updates, each seen red except the warn-level tag check.
