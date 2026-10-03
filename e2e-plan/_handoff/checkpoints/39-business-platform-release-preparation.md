# O2 release preparation — 2026-10-03T20:21:43+05:30

Current 2026-10-03T20:21:43+05:30: CommonLibrary O2 commit37f68545a8293d42c9837c290d7bd62cfd673105 is published by GitHub Packages run37130225264 (success). Safe BrandSummaryDto prevents member brand lists/events from exposing tax/bank application data; HTTP privacy and actual OpenAPI guards2/0/0/0 pass. Final clean Customer472, Reviews96 and Gateway27 have zero failures/errors/skips before the full dependency rebuild now running. UI full125files/785tests passed; subsequent corrected brand3tests, typecheck and lint pass. Earlier concurrent API regeneration caused23 import failures and is preserved as a failed invocation. Core56/56, readiness61/61 static checks (one optional execution check skipped; UI independently executed), core16 and readiness10 positive/broken guard controls pass. Scoped hash-bound Dev schema recreation manifest protects unreviewed SQL and expires on production declaration. Full fleet clean artifacts/contracts are underway; O2 service/UI publication, authorised full --wipe/fresh seed and Oracle live gates remain pending. Oracle currently healthy; no wipe/new O2 live fixture. O3+ unstarted; full platform incomplete.

Common package: https://github.com/parthu-reddy/FoodDeliveryCommonLibrary/actions/runs/37130225264

Guard changes are source-grounded: ignore commented/hidden/opt-in controller routes; distinguish mapped GET operations from DTO accessors; retain intentional Dev-capability header reads; require current immutable ledger-entry contract fields; resolve numeric Spring rate defaults and reject invalid rates; compare SQL against real deployment env tags with the owner's narrowly verified fresh-Dev exception. Three actual delomboked models restore Lombok accessors/builders while retaining persistence/Jackson metadata. Money readiness recognises meaningful committed persistence fixtures and typed admin UI calls plus producer operation paths. No tests or requirements were dropped.

Failed invocations: UI API generation raced reading tests (23 files fail import,663 tests executed), then full regenerated suite785pass. Typecheck then caught unsupported Testing Library exact option and private gstin access; both corrected, final typecheck/lint and brand3 pass. Earlier core controls caught a same-line route boundary bug; corrected fixture controls16 pass.

Continuation: finish full two-pass clean local jar/test-jar/stub artifact graph, prove privacy guard red/restored, inspect packaging and every generated contract report; final regenerate/build UI sequentially. Commit/push reviewed service changes and publish all shared consumers through unchanged local workflows; verify registry publication, commit deployment tags excluding unrelated DEPLOY_LOG. Only then full authorised clean-deploy --wipe, fresh seed, all logs/hardening/reconcile. Run public Oracle O2 staff/earnings-removal gate, canonical delivery/chat/history/nonzero earnings and SQL/latency/breaker gates before O3. ONDC compile-only, parked scenarios remain excluded.

Evidence: [39-release-preparation](../evidence/39-release-preparation.json), [38 schema proof](../evidence/38-fresh-schema-verification.json), [37 guard mutations](../evidence/37-o2-mutations.json).

## 2026-10-03T20:25:58+05:30 — Signing test environment isolation

The full clean graph exposes an existing environment-binding test collision with the parent Maven test-only HMAC property. Remove systemProperties only from that fixture Spring Environment so it exercises IDENTITY_HMAC_SECRET fallback. Keep runtime signing configuration and keys unchanged. First fleet pass2 had signing8/1/0/0 and skipped dependents, so it is a failed invocation; rerun the full clean graph after the focused guards pass. No deployment yet.

## 2026-10-03T20:38:03+05:30 — Final graph and consistency choices

Full restored clean backend graph1877reported cases,1876executed passes,0failures/errors;1explicitly disabled future ONDC delivery-status contract remains parked. All26generated producer classes have reports. All21Java Compose artifacts are fresh; removed SQL resources absent. Restaurant final actual OpenAPI1guard, fleet map2existing rendered tests pass. Timezone26/26 with26positive and77negative controls; the multiline @Table parser now reads the actual table-name line, avoiding a false missing payout_operations.created_at finding. Use shared Select for fleet city and its visible selected value/listbox interactions. Set a4-worker Vitest budget consistent with the completed785-test runs to avoid unbounded resource contention; no scenarios removed. Record Common published commit in Deployment/published-libraries.json so shared SQL has publication protection even though a library has no Compose tag; core20positive/broken controls cover missing/unknown publication and unreviewed shared SQL after production declaration. Final UI build and full required UI gates underway; publication/deploy/wipe/live remain pending.


## 2026-10-03T20:53:03+05:30 — O2 payout privacy and UI consistency

All14 current producer-stub publication runs succeeded (exact runs in39-contract-publication-runs.json). Earnings readers could receive a full bank beneficiary snapshot from payout list/detail responses. Red guard3tests/2failures/0errors/0skips proves both disclosures. Decision: retain amounts/status/lines for earnings readers but require PAYOUTS_MANAGE to return bank beneficiary details; redact the mapped DTO, never the persisted payout. Resolve this permission once per request. Owners/admins with payout management retain the details. Restored focused3/0/0/0 passes; clean Ledger release verification underway.

UI phase2 full15/15 passed. Phase3 caught two structural regressions: customer menu screen size and role branching in the shared chat widget. Extract the cohesive restaurant review button with unchanged rendered behavior and participant selection into the communication domain model; typecheck and existing component5tests pass. Final build and full phase3/4/5 gates still pending. No O2 image publication, clean wipe or live fixture has run yet.


## 2026-10-03T20:56:23+05:30 — Shared chat policy consistency

Phase3 full12/13: menu size corrected and typecheck/lint/full test pass, but the shared widget still derives a CUSTOMER refund capability inline. Centralise that capability alongside participant selection and reuse it in the actual send guard so there is one domain rule. Preserve server authorisation and user-visible behavior. The failed phase3 invocation is39-ui-phase3-first-restored.txt; final rebuilt/restored gates remain pending.


## 2026-10-03T21:01:55+05:30 — Exact contract scope and UI phase boundary

All12 existing consumer workflows succeeded against current published stubs; Identity and Tracking have producer publication only and no contract-tests.yml. An attempted Identity consumer dispatch returned404 and started nothing; corrected dispatch follows the existing orchestrator's source-file inventory, with exact12run IDs/heads in39-consumer-contract-runs.json. Latest Ledger producer37133191391 succeeded at0aca2ee. No workflow or visibility change.

Full UI phase3 restored13/13. Phase4 structural probe fails the component-size check with12files, including11pre-existing oversized screens and the new organisation-registration expansion. Decision: extract registration ownership loading/selection/create/retry into a cohesive hook now, preserving rendered controls and the disabled guard; final full typecheck/lint/test and rebuilt dist are required before O2 publication. The first hook typecheck caught the submit guard still referencing moved state; its exact equivalent now comes from the hook. Carry the11pre-existing phase4 size findings to O3/O5's explicit all-redesign-gates work, without exceptions or claimed green status. O2's own validation requires typecheck/lint/vitest; those must be current and green, along with all backend/contract/security/live gates. Phase5 source14/14 passes, full phase5 remains unverified. No image publication/deploy/wipe yet.


## 2026-10-03T21:04:40+05:30 — O2 publication gate

Local O2 static13/13 and core56/56 are green. Latest UI full785/785,0failures/skips; typecheck/lint clean; final validation-schema relocation receives a rebuilt dist, typecheck/lint and existing registration3-case rerun before image publication. All14 producer workflows plus latest Ledger privacy republish succeeded; all12 actual consumer workflows succeeded at exact current heads. Latest Ledger clean223/0/0/0. All21 Java Compose artifacts are fresh. Six protected workflow hashes unchanged. All18 Oracle application profiles verified exclusively Dev. Deployment config/seeds/library provenance03eede1 pushed, preserving unrelated DEPLOY_LOG.

Next: commit/push tested UI; existing unchanged native publish.sh --all; verify each current registry tag/digest; commit/push env tags; existing fullclean --wipe with authorized WIPE acknowledgement; fresh dummy-data.sh --load-only; inspect every log, hardening/reconcile, then public Oracle O2 and required lifecycle/chat/nonzero-earnings/latency proof. No O2 image/deployment/wipe has run yet; O3+ not started. Eleven older oversized UI components remain a source gap for O3/O5's full redesign gates; no line-count exceptions added. No production-readiness completion claim.


## 2026-10-03T21:09:39+05:30 — Publication prerequisite and locator proof

First native image publication aborted before building/pushing any image: local Docker daemon socket absent. Start the existing Docker Desktop builder and retry the unchanged publisher; Oracle has not changed. UI0866c00 and Deployment03eede1 are pushed. Latest UI785/0/0/0 full test run, final pure validation extraction rebuilt/typechecked/linted and rendered registration3/0/0/0 passed.

Focused locator audits for O2 access, O1 lifecycle and earnings all pass:6/3/4static PASS respectively,0FAIL/DEAD/EXCEPTED,3/0/4dynamic runtime-only sites. Restrict stale exception checking to selected suite paths when --only is used; full audits still check all exceptions. A deliberately stale O2-selected exception makes the real audit fail and original JSON is restored. Earnings now selects tbody rows of the accessible Account statement table and excludes its empty placeholder, retaining signed-amount parsing and full balance assertions. E2E final test-compile passed. A profile preflight typo used an absent governmentid-service name, causing a read-only failure; corrected inventory from service-map.tsv verifies all18 actual application profiles exclusively Dev. Do not guess deployed service names.
