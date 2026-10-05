# Checkpoint 44 — O3 UI-only local source proof

## 2026-10-04T07:17:12+05:30

Checkpoint43 set the browser-only boundary. This checkpoint records the first current local proof
after the O3 source rewrite. It does not run a deployed browser journey and does not change any
publication, deployment, seed, wipe, or live-E2E status.

## Historical local evidence at 2026-10-04T07:17:12+05:30

| Check | Result | Boundary |
|---|---|---|
| `mvn -B -o -DskipTests test-compile` in `UITesting` | success; 179 sources | Compiles the E2E source only. |
| `mvn -B -o test -Dtest=SeededRiderDutyTest` | 4/0/0/0 | Local visible-duty helper behavior only. |
| `validate_business_platform.py --phase O3` | 19 PASS / 0 FAIL / 0 STALE | Static plan/source validation only. |
| `python3 -m py_compile scripts/run_partner_applications_o3_e2e.py` | success | Runner syntax only. |

The four tagged O3 classes now use browser page objects and normal UI login: the historical
`RestaurantApplicationApiTest` and `DeliveryApplicationApiTest` filenames remain solely for
validator compatibility. A focused static source scan of the four classes and runner found no
`GatewayApi`, browser `fetch`/`evaluate`, browser-state injection, SSH/DB/Redis/JDBC/Oracle fixture,
`localhost`, or `curl` product-call pattern. This is a source review, not an execution result.

The complete bounded record is
[44-o3-ui-only-local-source-evidence.json](../evidence/44-o3-ui-only-local-source-evidence.json).

## 2026-10-04T07:24:40+05:30 — current-tree and embedded-H2 supplement

The 179-source result above remains the dated 07:17 snapshot. The dated 07:24 no-recompile
`UITesting` snapshot reported **178 sources**; it is historical, and the later fresh 179-source
compilation is the current source result. `GatewayApi.java` has been deleted. The former O1/O2 direct paths
are deliberately deferred under the browser-only rule in
[DEFERRED-O1-O2-UI-ONLY-TESTS.md](../../05-partner-and-account-management/organisations-and-portal-access/DEFERRED-O1-O2-UI-ONLY-TESTS.md);
that record is not E2E evidence.

| Local service command | Result | Fixture boundary |
|---|---:|---|
| `mvn -B -o test -Dtest=ApprovedOnlyListingPersistenceTest,ConcurrentApplicationDecisionTest` in `RestaurantApplication` | 10 / 0 / 0 / 0 | Embedded H2 only; no Docker or external database. |
| `mvn -B -o test -Dtest=ConcurrentApplicationDecisionTest` in `DeliveryExecutiveApplication` | 5 / 0 / 0 / 0 | Embedded H2 only; no Docker or external database. |
| `mvn -B -o test -Dtest=FreshRestaurantApprovalTest,RestaurantApplicationCacheInvalidationTest,RestaurantApplicationCacheTransactionTest` in `CustomerApplication` | 8 / 0 / 0 / 0 | Local focused service test only; no browser or external database proof. |

Two test-harness failures are resolved without changing their product assertions. The initial
WireMock dynamic HTTPS-port failure is resolved by pinning
`wiremock.server.https-port=-1` in `ApprovedOnlyListingPersistenceTest`, the Restaurant
`ConcurrentApplicationDecisionTest`, and the Delivery `ConcurrentApplicationDecisionTest`. The
JDK 26 inline-MockMaker attachment failure is resolved with test-only
`mockito-extensions/org.mockito.plugins.MockMaker` resources in the Restaurant, Delivery, and
Customer application repositories, each set to `mock-maker-subclass`. The initial Customer focused
run produced zero product assertion results because Mockito's inline agent could not attach on JDK
26; that harness failure is resolved by Customer's test-only subclass-maker resource.

These H2 outcomes prove only local persistence/concurrency behavior. They do not run a browser,
use Oracle, publish an artifact, deploy anything, or close a live-E2E, health, measurement, or
release gate.

## 2026-10-04T07:31:02+05:30 — Delivery baseline audit and runner boundary

The Restaurant V1 SHA-256 remains aligned with `DEV-SCHEMA-RECREATION.json` at
`93ac35ea9831837be4bbd5524d9e88db490800c32899a603f70366a62fc67af6`. The current Delivery
`V1__init_schema.sql` SHA-256 is
`400326430433bdf520302274651f6c76870eb1e4faf3095faca613f1b2689b14`, which differs from the
manifest and prior evidence (`2e18ec0771eb6966075fd3542ce30c8d294fd19c2dc7d7e2556d550c9df833b3`)
and from the Delivery Git HEAD version's content SHA-256
`9ead10291d30fba19c414b14a1719826224a28de39365b2c69868348b1590e3c`.

All prior fresh Delivery schema and seed evidence is therefore **stale for the current baseline**.
The Restaurant/Delivery H2 and Customer focused results above remain local source/service evidence;
they do not prove the current Delivery fresh schema, seed, Oracle state, publication, deployment,
or browser E2E. Rebuild and reprove the current Delivery V1 baseline and seeds before any
publication or authorised Dev wipe.

The O1 and O2 runner scripts now return exit code 2 with an explicit deferred notice and no remote,
process, browser, or server action. `run_registration_e2e.py` rejects loopback, creates only local
random retained candidates, invokes only `RegistrationUiTest`, and returns exit code 2 for the
deferred session-case options. Its focused Python runner test is 5/5. This is runner-policy proof
only: no browser, Oracle, publication, deployment, or live E2E run occurred.

## 2026-10-04T07:40:23+05:30 — current Delivery baseline static/H2/seed-source proof

The active Delivery V1 SHA-256 is
`400326430433bdf520302274651f6c76870eb1e4faf3095faca613f1b2689b14`, and the active
`DEV-SCHEMA-RECREATION.json` now binds that exact value. The current static
`DeliveryFreshSchemaBaselineTest` report is 2/0/0/0; it checks the one-V1 inventory and current
lifecycle, geometry, vehicle-uniqueness and assignment source contract. The current embedded-H2
`ConcurrentApplicationDecisionTest` report is 5/0/0/0. The committed seed source validator passes
all of its count, relationship, lifecycle/reason, scenario and approved-FSSAI checks.

The current scoped O3 service results are Restaurant 10/0/0/0, Delivery 5/0/0/0 and Customer
8/0/0/0. Repository-wide Mockito MockMaker resources were removed to avoid changing legacy/default
test behavior; targeted Maven invocations use a matching Byte Buddy javaagent instead. `UITesting`
now test-compiles 179 sources, and the O3 runner uses only the visible UI flow with normal Dev
Autofill. This is source/runner policy proof only, not a deployed browser execution.

These are bounded local proofs. The static test does not execute SQL; H2 does not prove PostGIS;
the seed validator does not load/replay a database. Current Delivery PostGIS/Flyway bootstrap and
seed replay are therefore unproven until artifacts are published and the authorised clean Dev
`--wipe`, deployment and fresh dummy-data reseed run. The old `42-*` Delivery records stay
historical because they bind SHA `2e18ec0771eb6966075fd3542ce30c8d294fd19c2dc7d7e2556d550c9df833b3`.
The complete bounded record is
[45-o3-delivery-current-baseline-static-h2-seed-proof.json](../evidence/45-o3-delivery-current-baseline-static-h2-seed-proof.json).

## Still open

- No O3 browser case has run against the public deployed UI.
- O3 is uncommitted, unpublished and undeployed.
- The current Delivery V1 baseline has no current PostGIS/Flyway fresh-bootstrap or seed-replay
  proof. Verify it only after publication through the authorised clean Dev `--wipe`, deployment
  and fresh dummy-data reseed.
- Publication must precede the authorised clean Dev deployment/seed.
- The UI-only deferred inventory remains unchanged; direct IDOR/API, injected stale-cart state,
  direct measurement loops, infrastructure reads, duration and rate-limit cases are not passes.
- Locator audit, live browser execution, service health, measurements and all release gates remain
  open.

## Next sequence

1. Perform the pending locator/source audit without reintroducing a UI bypass.
2. Finish the remaining current local gates and publish through GitHub workflows.
3. Deploy the published artifacts through the authorised clean Dev path and seed fresh data.
4. Run the non-deferred public UI O3 journeys and record exact results separately.

Previous: [checkpoint43](43-o3-ui-only-e2e-boundary.md).
