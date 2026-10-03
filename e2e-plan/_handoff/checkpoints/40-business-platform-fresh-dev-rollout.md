# O2 fresh Dev rollout — 2026-10-03T21:25:00+05:30

All22 service/UI images are published at clean committed heads, registry digests recorded and linux/arm64 verified. Common37f6854 Packages publication,14producer runs plus Ledger privacy republish37133191391, and all12existing consumer workflows succeeded. UI0866c00; Deployment image/config/seed records c9deb85 (earlier config/seeds03eede1). Latest clean Ledger223/0/0/0, original full clean graph1877reported/1876executed passes/1parked ONDCskip, current full UI785/0/0/0 and final registration3/0/0/0; core56/56, O2static13/13. All18 Oracle application profiles verified exclusively Dev. Six workflow hashes unchanged.

Next action is the existing task-authorised full clean-deploy --wipe, with explicit WIPE acknowledgement, followed by dummy-data.sh --load-only. Consolidated initial schemas must not be applied to old volumes. Full fresh PostgreSQL/PostGIS/schema/seed proof is checkpoint38. No production data exists under the owner's standing policy; no legacy conversion, backfill, Flyway repair or validation suppression. Old retained Dev fixtures become historical only when this explicit wipe executes. No wipe/deployment has run at this checkpoint.

Then inspect every service's fresh logs/images/platform/restarts, run hardening/reconcile, validate seeded relationships and exact Flyway resources; product proof remains through the public Oracle HTTPS tunnel. Run O2staff/manager/removal/internal-route gate, then one canonical seeded delivery and required within-two-hour chat/history, SQL-verified nonzero earnings, navigation and reviews. Prove carried O1 membership latency/breaker IDs and O2latency limits before starting O3. No automatic cleanup.

Eleven pre-existing UI component-size findings remain for O3/O5's explicit redesign gates; no exceptions were added. O2source/typecheck/lint/runtime suite is green, but O2live is not yet verified. O3+ remains unstarted; the full Business Platform is incomplete.

Evidence: [published images](../evidence/40-published-images.json), [Dev profiles](../evidence/39-oracle-prewipe-profile.json), [consumer CI](../evidence/39-consumer-contract-runs.json), [Ledger privacy](../evidence/39-payout-privacy.json), [local release](39-business-platform-release-preparation.md).

Read-only log/image audit helper: RandomDocuments/BusinessPlatform_2026-10-03/tools/inspect_oracle_release.py, executed on the VM over SSH/stdin. It emits bounded metadata/error examples without environment values.


## 2026-10-03T21:26:29+05:30 — Owner-authorised full Dev wipe started

Registry22/22 linux/arm64 verified; Deployment c9deb85 pushed; core56/56 remains green against current published tags. Started the unchanged03_clean_deploy.sh --wipe on Oracle after all18application profiles verified Dev. Explicit WIPE acknowledgement supplied to its TTY prompt under the owner's prior task-specific permission. No prompt was piped past and no workflow edited. Old Dev fixtures become historical as volumes are removed; fresh post-wipe fixtures must be recorded independently. Deployment/healthy schemas, load-only seed, all29container logs/restarts/digests/config, O2live/financial/regression/performance gates remain pending. Never claim wipe means rollout success.


## 2026-10-03T21:41:50+05:30 — Shared tracing configuration after fresh Dev rollout

The unchanged authorised full `03_clean_deploy.sh --wipe` completed with exit0. All29 declared containers run on their intended linux/arm64 images, with zero automatic restarts. Fresh seeds have not been loaded yet. Eleven application services log OTLP connection errors because shared application.yml reads OTLP_ENDPOINT with a localhost default while Compose supplies OTEL_EXPORTER_OTLP_ENDPOINT for deployed Jaeger. Decision: point the shared endpoint at `${OTEL_EXPORTER_OTLP_ENDPOINT:http://jaeger:4318}/v1/traces`, matching the seven corrected scoped configs. Remove the ineffective management.otlp.tracing.export.enabled property, absent from the pinned Spring Boot3.3.0 configuration metadata; retain tracing/sampling. The reviewed unchanged config workflow dry-run targets eighteen app consumers, sequentially with Gateway last, excluding Config/Eureka/UI. Publish the config before restarts. No new wipe, image build or workflow change. Preserve failed log evidence; check all fresh logs after restart, then load-only seed and public Oracle O2 gates. Eureka1 had one startup error and PostgreSQL three, with zero recent errors; inspect exact causes before declaring logs clean. Full platform remains incomplete.


## 2026-10-03T21:45:30+05:30 — Read-only measurements after the authorised wipe

Choice: reuse the existing readOnlyRequestsForServerLatencyMeasurements method with the fresh seeded restaurant OWNER's organisation by default. An explicitly supplied retained O1 customer remains supported. Old8999 accounts were intentionally wiped; creating replacement accounts only to generate read samples is unnecessary. The method still uses normal browser Dev Autofill login and thirty successful public Gateway reads each for organisations, brands and outlets, without product-data mutations. Compilation and actual Oracle run are separate gates.


## 2026-10-03T21:45:57+05:30 — Phase1 checklist reconciled with recorded live proof

Owner noticed two unchecked Phase1 rows. Choice: mark only the already-proven deployed lifecycle E2E row complete, linking its third-invocation1/0/0/0 and exact OWNER/outbox evidence. Keep measurement rows open: org-list30.758ms is recorded, but internal membership latency/generated breaker IDs must be measured at O2's first consumer. Wiped O1 fixture identities are historical, not current runtime claims. O2 fresh live gates have not run yet.


## 2026-10-03T21:52:36+05:30 — Shared trace endpoint verification

The first seven restarted app readers have zero startup errors; protected Jaeger telemetry lists their service names and confirms spans arrive. Fresh initial Identity/Restaurant/Chat/Notification schemas contain exactly two successful Flyway resources each (service baseline + published common baseline), required ownership/entity constraints and append-only audit triggers. Flyway's own installed_on timestamp belongs to the pinned framework; report it separately from application timestamps rather than edit framework history DDL. Exact OCIR digest/image/platform comparison passed22/22. PostgreSQL's three errors occurred during fresh initialisation/shutdown, and Eureka1's single heartbeat error during peer startup; both have zero recent errors. Keep these historical startup counts visible. No fresh seed or O2 E2E yet.


## 2026-10-03T21:55:31+05:30 — Shared config rollout complete; load-only seed started

The unchanged config workflow completed with exit 0, publishing application.yml before sequentially restarting all 18 readers. All 29 containers run their intended images, with zero automatic restarts; all application logs since their current StartedAt have zero errors. Read-only reconcile: 29 declared/running, zero missing/extra/drifting. One two-minute Reviews error came from its previous process before the restart, while current-boot errors were zero; retain that original audit and bound the helper's recent window to max(StartedAt, now minus two minutes). Do not hide failed-release logs. Decision: run unchanged dummy-data.sh --load-only now that the fresh schemas and services are ready; no second reset. Seed completion and post-seed relationships remain pending, then public Oracle staff/lifecycle/performance gates.


## 2026-10-03T21:56:44+05:30 — Fresh Dev seed complete; live O2 begins

Unchanged dummy-data.sh --load-only completed with exit 0. Static and deployed primary-key/admin-role checks passed for 14 organisations with exactly one active OWNER, 554 identities, 504 customers, 1,003 addresses, 34 riders, 13 brands, 104 outlets and 504 dishes. All 29 containers are running on intended images, automatic restarts 0; all current application boot logs and current recent windows have zero errors. Hardening and report-only reconcile passed. Choice: run the existing O2 phone/schema preflight and normal-browser staff registration on the public Oracle tunnel, retaining all new records and manifests; no automatic cleanup. After the staff gate, use one fresh canonical order and its same-order regression set. Live outcomes/latencies are not yet verified; O3+ remains unstarted.


## 2026-10-03T22:00:27+05:30 — O2 live denial request corrected; retain and resume the same member

First O2 invocation: 1 test, 1 failure, 0 errors/skips. Signup, STAFF invite/accept, stock off/on with live UI/readback passed before the price request returned 400 instead of 403. Source contract OutletMenuOverride requires isAvailable; the test omitted it. Choice: send a valid positive price and isAvailable=true, retaining the 403 assertion. No product authorisation relaxation. Preserve the original failed report and allocation/member manifests for phone 9999887458. Add an explicit --resume-phone mode guarded by the same Dev organisation, owned manifest and ACTIVE STAFF user UUID; normal browser login resumes this fixture without a new signup/invitation or server cleanup. Compilation/rerun pending. All later staff/manager/removal gates remain unverified.


## 2026-10-03T22:11:15+05:30 — Stock versus orderability; pending refund permissions

The second O2 live invocation (allocation6993e245b9d59a3c) failed1/0/1/0 on stock ON readback at22:01 IST. Read-only Oracle SQL proves the override is true/version3, while public effective availability is false; both applicable category windows close22:00. This is not evidence of a failed stock write or a cache/transaction race. Choice: assert the authenticated persisted override and actual PUT response for stock, preserve opening-hour checks separately, and initialise restaurant stock switches from overrides even outside category hours. Disable duplicate writes while each stock request is pending. Effective customer availability must be recomputed from current bulk queries and the injected UTC clock rather than caching a time-derived result for15 minutes; preserve category caching. An actual cached-read/clock-boundary guard must fail if effective-menu caching is restored. The observed pending-refund queue403 for ACTIVE STAFF is a product permission error: require named ORDERS_OPERATE membership for PROCESSING operational requests; financial summaries/statements/order earnings/completed refunds remain EARNINGS_VIEW. Keep the exact existing staff fixture, failed reports and snapshots; no cleanup, second wipe, replacement signup, SQL stock write or opening-hours bypass. Publish tested changes before targeted Oracle rollout, then rerun the retained live gate and regressions. O2 is incomplete; O3+ remains unstarted.


## 2026-10-03T22:19:36+05:30 — Canonical overnight Dev fixture and truthful sparse measurements

Choice: retain realistic category windows and add one idempotent all-day Food-category window at the existing canonical Brand1 Outlet3 in Dev dummy seeds, mirrored in the seed generator and enforced by the validator. Required canonical order regression otherwise cannot run after22:00, when every previously selected Food window is closed. This is configured dummy data, never an opening-hours exception in application code. Use the existing authorised load-only seed workflow after publishing the seed change; it inserts one new timing row and preserves the existing STAFF fixture/order data. No second wipe, SQL UPDATE/DELETE, replacement account, time-wait bypass or production claim. Closing-boundary behavior is separately proven by a fake-clock cache-advice guard. The first protected histogram snapshot has sparse/cold samples (membership p95estimate50.891ms, brands742.671ms/outlets2254.858ms), so it does not satisfy release budgets. Preserve it; collect the required public read sample and complete post-rollout measurements without dropping prior failures.


## 2026-10-03T22:21:49+05:30 — O2 live-discovered fixes verified locally

Source verification after the closing-hour/refund fixes: clean Restaurant94/0/0/0; clean Customer476/0/0/0; full UI126files/789tests,0failures/errors/skips; typecheck/lint/build passed. Real cache-advice controls reject restored effective-menu caching2/2 failures; the HTTP method-security guard rejects restored EARNINGS_VIEW queue gating1/4 failures, with source restored before clean builds. The first new Customer guard invocation23/0/1/0 was a Mockito restubbing error, corrected with doReturn and preserved; it is not a product failure or a passing invocation. Source Restaurant4a37fba/Customerc5001e8 is committed/pushed. O2static13/13, core56/56, timezone26/26 pass. UI publication/source commit and targeted image rollout remain pending. Required retained live gate, canonical regressions and release-budget measurements are still open; no new signup/wipe/cleanup. No production-readiness completion claim.


## 2026-10-03T22:28:08+05:30 — Corrected images published; retention timeout separate

All three corrected images are published and directly verified in OCIR as linux/arm64, exact digests in40-o2-fix-published-images.json: Restaurant4a37fba-fe7c9eb,Customerc5001e8-712cd8c,UI42f92fb-3664d99. Tag records and exact Dev hours validator are committed/pushed as Deployment e6ddca2 (seed862a399). Both exact-head producer stub workflows37138413376/37138416052 succeeded; Restaurant consumer37138634395 succeeded, Customer37138636849 remains running. Native publish.sh completed all image pushes and tag records, then exited1 during its unchanged registry-retention step with an Oracle registry tag-list socket timeout. This is a failed maintenance step, not missing images; preserve the failure, inspect/retry the unchanged retention policy separately without editing workflows. No corrected image has been deployed yet. Reviewed required UI Dev-config dry-run affects18 readers sequentially/Gateway last; execute after targeted image rollout, then load-only the one additive Dev timing seed and run final log/config/identity audits before the retained live gate. Sparse pre-fix histogram release budgets remain failed/pending.


## 2026-10-03T22:29:19+05:30 — Corrected O2 Oracle rollout

O2 corrections are committed/pushed, both producer and both consumer contract workflows succeeded at exact heads; all three images verified published linux/arm64 by registry digest, Deployment tags e6ddca2 pushed. Native publisher exited1 only in post-push registry tag-list cleanup timeout. Automatic approval review rejected the direct retention retry before execution because it can irreversibly delete multiple published image digests beyond the authorised publishing/deployment scope. Do not bypass it; registry cleanup is optional and left blocked, so proceed with the independently verified images. Targeted Oracle rollout has started for restaurant-service4a37fba-fe7c9eb,customer-servicec5001e8-712cd8c,UI42f92fb-3664d99. Required UI Dev config refresh follows the reviewed18-reader dry-run, then load-only the additive canonical Food-hours seed. No second wipe/server cleanup/replacement signup. All fresh logs/digests/config, retained STAFF9999887458 live gate, canonical order/chat/earnings/navigation/review regressions and release-budget measurements remain pending. O2 incomplete; O3+ unstarted.


## 2026-10-03T22:31:32+05:30 — Corrected image rollout complete

Targeted three-image rollout completed exit0 at the exact published tags, VM.env pins22 tags. The reviewed workflow-required four-file Dev config publication has now started;18 readers restart sequentially with Gateway last. No data reset. Corrected live E2E is intentionally waiting for final restarts/log/config checks; all regression/measurement rows remain open. Protected workflow/script source is unchanged. Registry retention retry remains auto-review-blocked and was not executed.


## 2026-10-03T22:37:10+05:30 — O2 retained staff live gate passed

Corrected O2 retained STAFF invocation3 passed1/0/0/0 after exact three-image deployment. Stock OFF/ON verified by actual UI PUT responses and persisted overrides; STAFF pending refund queue200, price/earnings/statement/completed refunds403; promotion to MANAGER gives matching owner figures; removal refuses stock within 5298.113ms plus pending refunds, and outsider/internal routes403. Same original account9999887458/useraa46c426-b124-4819-bec8-f2330de82a71/org6bda9207-420f-53f6-8643-30dc731bff77 now has MANAGER role with REMOVED status; completed manifests retained. No replacement signup or cleanup. Do not resume it as ACTIVE STAFF again. Dev config apply exited0 with identical VM files, correctly skipping publication/restarts. Load-only canonical Food-hours addition exited0; existing stock/version3 and owned membership retained before live writes. Hardening all checks, reconcile29/0drift, exact22image/platform/digests pass. All29running/restarts0/recentErrors0; classify the historical initial Eureka1 peer error separately from business-app startup errors. Remaining O2 gates: one canonical lifecycle and within2h chat/history, exact nonzero SQL earnings, navigation/reviews, final measurements. O2 incomplete; O3+ unstarted.


## 2026-10-03T22:39:49+05:30 — Canonical lifecycle regression started

Canonical HappyDeliveryFlowTest#completeOrderLifecycle invocation1 started through the public Oracle HTTPS tunnel with customer8000000001/restaurant9000000001/rider7000000001/admin1000000001. Fresh read-only prerequisite snapshot: customer0orders, exact Home, riderOFFLINE/APPROVED/active/0assignments, Brand1Outlet3 activeBLR and1294.8m from Home/2819m from browser location. This SQL snapshot is not WS/GPS readiness proof; the existing SeededRiderDuty helper must prove ONLINE/currentfix/no warning and retain the live context before checkout. Actual order identity/state must be preserved immediately if this invocation fails; no replacement order. Required within2h chat/history, nonzero SQL earnings, navigation/reviews, public read samples and final server histograms remain pending. O2 still incomplete.


## 2026-10-03T22:47:32+05:30 — Canonical order delivered; bounded runtime defect

Canonical HappyDeliveryFlowTest#completeOrderLifecycle invocation1 passed 1/0/0/0 (268.807s) on retained order e82f8c51-6041-4987-88b9-c3f02ac781a8, actual Brand 1 Outlet 10 / 4a187e63-659d-4ba6-925f-52cd278f8bf1. Read-only Oracle confirms HANDED_OVER/DELIVERED, payment SUCCESS, rider OFFLINE/APPROVED/active, restaurant payout and ledger balance both 19.11 INR, no payouts. Cross-role real chat, reconnect, image/history and refund quote checks passed. The initial chat503 is the existing deliberate browser retry fixture (OrderChatChecks lines44–48), not a server outage; chat logs have no errors. Retain the separate initial quote400 observation. Broken-pipe AsyncRequestNotUsableExceptions are client stream disconnections; SSE remains parked and these are classified separately rather than erased.

Actual defect: Restaurant DeliveryClient calls internal admin driver endpoints with the correctly signed SERVICE identity, causing repeated Delivery403 and Restaurant fallback errors; rider names are missing from fulfilled/history enrichment. Choice: use the existing least-data internal driver summary endpoints (id/fullName only), keep admin fleet guards unchanged, and prove producer/consumer contracts plus method security before publishing the targeted fix. Do not grant SERVICE access to all admin routes or expose phones/bank/profile fields. Preserve existing admin contracts for other consumers.

Existing same-order ChatAndRefundIsolationTest#outsidersAreRefused and ChatHistoryPagingTest#earlierMessagesCanBeLoaded are now running through the public Oracle tunnel, with intruder customer8000000485/restaurant9000000002/rider7000000002. No replacement order/account, reset, cleanup or manual OTP. Next: fix/publish/deploy rider-summary consumer, retain individual chat outcomes, then exact nonzero earnings19.11/0/19.11, navigation/reviews and final latency/breaker measurements. O2 incomplete; O3+ unstarted. Evidence:40-canonical-lifecycle-invocation1 and41-canonical-readonly-diagnostics.json. Staff9999887458 remains REMOVED, live revocation5298.113ms.
