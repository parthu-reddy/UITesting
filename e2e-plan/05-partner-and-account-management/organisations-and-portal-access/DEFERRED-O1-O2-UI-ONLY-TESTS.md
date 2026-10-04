# Deferred O1/O2 UI-only browser coverage

## 2026-10-04T07:20:19+05:30 — direct-request E2E withdrawn

The owner requires E2E to operate only through visible product controls. A browser test may
observe a request or response produced by a click, but it may not call `GatewayApi`, browser
`fetch`, a direct endpoint/URL, a database, Redis, SSH, a fixture service, or injected browser
state. Docker and external database fixtures are also prohibited. Embedded H2 remains for local
service persistence tests, not E2E.

Source inspection found that `FoodDeliveryAppUI` exposes organisation creation/selection only as
part of the Restaurant Application wizard (`useBrandOrganisationSelection`). It has no rendered
organisation list, invitation inbox, member list, role editor, member removal, ownership transfer,
or admin organisation-management flow. The restaurant portal exposes rendered menu and earnings
screens, but no rendered way to create an organisation member or alter a member's role. The
former O1/O2 classes therefore cannot retain their direct-request implementations.

`OrganisationLifecycleApiTest#organisationLifecycle` and
`#readOnlyRequestsForServerLatencyMeasurements` are deliberately **disabled**: their direct code
was removed and a run reports `SKIPPED`, never pass. `OrganisationRestaurantAccessTest` is now a
read-only browser projection: an existing seeded restaurant partner signs in with the normal Dev
Autofill control and sees the rendered Menu tab and a stock switch. It neither writes stock nor
proves the organisation API/membership invariants below.

Each deferred row is **DEFERRED, not executed, and not counted as a pass**.

| ID | Prior requirement | Missing visible UI control | Correct current proof boundary / unblock |
|---|---|---|---|
| O1-UI-001 | ORG-01..06: create/list an organisation, invite/accept/reinvite members, change roles, remove a member, and transfer ownership. | There is no organisation-management view, invitation inbox, member table, role editor, removal action, or ownership-transfer action. The onboarding wizard's implicit create/select control is not a lifecycle-management UI. | Keep authorization/lifecycle assertions in focused Identity service/controller tests with embedded H2 or mocks. Add those product controls before a UI-only browser journey is written. |
| O1-UI-002 | The former thirty direct reads for histogram/latency measurement. | A request loop is not a visible user interaction or rendered journey. | Define a browser-timing journey or use separately authorised operator metrics outside E2E; record its method before treating it as evidence. |
| O2-UI-001 | ORG-07/08: prove the seeded owner-to-brand relationship and create a fresh staff account, invite it, and accept the invitation. | The portal does not render organisation membership, Brand-to-organisation ownership, invite, or accept controls. | Existing UI smoke only proves a seeded restaurant portal renders; ownership/membership services stay covered by focused service/controller tests until a member-management UI exists. |
| O2-UI-002 | ORG-09..11: change a staff member's stock/price/earnings permissions, promote to manager, remove membership, and bound revocation by the five-second cache TTL. | No UI can establish the staff fixture, edit roles, remove membership, or show the cache's authoritative revocation boundary. | Keep authorization/cache timing behavior in focused service tests. A future browser E2E must start with visible invite/role/removal controls and assert their visible consequences. |
| O2-UI-003 | ORG-12: public callers receive refusals from internal restaurant endpoints. | Internal service endpoints are intentionally not product UI. | Keep this as controller/gateway authorization coverage; do not turn an internal endpoint into a browser E2E probe. |

`run_organisation_o1_e2e.py` and `run_organisation_o2_e2e.py` are now explicit deferred notices:
they retain compatibility arguments but return exit code 2 before any remote, process, browser or
server action. Their output directs the operator to this record. That is an enforcement safeguard,
not an E2E pass; a browser-only runner can be added only after the missing controls exist.

`run_registration_e2e.py` separately enforces the UI-only boundary for the still-supported
registration journey: it rejects loopback URLs, locally allocates retained random candidates without
server inspection, invokes only `RegistrationUiTest`, and exits 2 for the deferred session-case
options. Its focused runner-policy test is 5/5. No browser, Oracle, publication, deployment or
live E2E execution occurred while validating those runner rules. Historical O1/O2 direct-run
evidence remains historical only and is not evidence for this UI-only contract.
