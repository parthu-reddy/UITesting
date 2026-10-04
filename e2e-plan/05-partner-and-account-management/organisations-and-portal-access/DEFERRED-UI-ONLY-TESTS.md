# Deferred UI-only O3 tests

## 2026-10-04T07:03:09+05:30 — current owner boundary

The owner requires every O3 E2E scenario to use the deployed product UI only. A test may observe
the request or response caused by a real visible control, but may not use `GatewayApi`, browser
`fetch`, a direct URL/API call, direct database or Redis access, SSH, a fixture service, or a
manually injected browser state such as `localStorage`. Embedded H2 is for local service
persistence tests only; it is not an E2E target. Docker and every external database fixture are
out of scope.

Each item below is **DEFERRED, not executed, and not counted as a pass**. It records coverage that
an earlier API-driven plan described but that a compliant browser scenario cannot currently prove.
The old wording remains historical; it is not authority to reintroduce a bypass.

| ID | Deferred assertion | Why it is deferred | Permitted replacement or unblock |
|---|---|---|---|
| O3-UI-001 | IDOR/direct-endpoint probes: another organisation member writing an application, `GET /verification/status/me`, and the retired `/status/{other}` route returning 404. | These assertions require choosing a URL, an identity, or a request payload outside a visible user action. They are authorization/controller checks, not browser journeys. | Keep them in focused service/controller tests with embedded H2 or mocks. A future UI-only E2E may cover a rendered cross-account control only if the product exposes one. |
| O3-UI-002 | A stale cart, hidden outlet by-id/catalog, and quote/add-to-cart refusal after an outlet becomes hidden. | A customer cannot discover a hidden outlet through the UI, and creating a retained stale cart by `localStorage`, API payload, direct catalog lookup, or seeded database state is prohibited. | Build a real UI journey that adds an outlet while it is visible, changes its state through the admin UI, and then revisits the cart through the UI. Until then, keep the server refusal in service/controller coverage. |
| O3-UI-003 | The former thirty-request direct endpoint measurement loop for application/queue reads. | Repeated direct requests are not user interactions and would bypass the UI-only E2E boundary. | Measure a real browser journey with browser timing after deployment, or obtain an operator-owned platform metric outside the E2E suite. Do not treat either as a browser E2E pass until its method is recorded. |
| O3-UI-004 | Reading restaurant/delivery outbox rows, audit rows, or Redis allocation data to prove publication or reserve a phone number. | Direct infrastructure reads are prohibited from E2E, even when read-only. | Prove transition and visibility through the UI; retain outbox/audit transaction coverage in local embedded-H2 service tests. A separately authorised operational audit must remain outside E2E. |
| O3-UI-005 | Wider old RegistrationUiTest/RiderOnboardingTest/RiderSettingsPage assertions and pre-O3 comments after replacing the wizard. | Shared current page objects were updated, but the owner limits this batch to current O3 work and prohibits touching old code. Those broader old assertions are not part of the four-class O3 release gate. | A future owner-authorized legacy test pass must use current visible onboarding/approval/duty UI only. Until then, do not count these tests or comments as current O3 live proof. |

The four O3 class names are retained for the validator, including legacy `*ApiTest` names, but their
future browser implementation must be UI-only. They must not be marked complete merely because a
local service test, a static check, or a previous direct-request run passes.
