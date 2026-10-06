# Deferred rate-limit tests — organisation and portal access

## 2026-10-04T07:03:09+05:30 — O3 Dev limit boundary

Do not exhaust application or document-upload rate limits as part of the normal O3 E2E suite.
Dev limits may be relaxed, and repeated direct HTTP calls would violate the UI-only rule. The cases
below are **DEFERRED, not executed, and not counted as a pass**.

| Scenario | Why deferred | Final-run requirements |
|---|---|---|
| `RL_APP_WRITE`: application edit/submit quota is exhausted and the UI remains usable after the visible refusal. | The plan's limit must be verified against the deployed configuration rather than assumed, and direct request loops are prohibited. | Use a separately authorised rate-enabled target and a retained fresh applicant. Drive every attempt through the same visible wizard action; assert the rendered refusal and no misleading success state. |
| `RL_DOC_UPLOAD`: document-upload URL quota is exhausted. | Upload retries create persistent private documents and the normal Dev target is unsuitable for quota exhaustion. | Use an isolated, explicitly authorised target with non-sensitive dummy files. Drive each upload from the visible file control only; do not fabricate URLs, make direct upload calls, or inspect storage/Redis. |

Focused service/controller tests may verify limiter wiring with H2/mocks. They do not turn these
deferred deployed UI scenarios into passes.

## 2026-10-05T22:05:00+05:30 — Do not exhaust admin OTP limits by running suites back-to-back

Running O4, O5 and all O3 cases consecutively with one seeded admin (1000000001) hit the ordinary admin OTP limit (10 codes / 10 minutes) and showed "Too many attempts; try again later" — correct behaviour, but it is the deferred rate-limit category and must not happen in normal runs. Both runners accept `--admin-phone 1000000001|1000000002`; alternate them between suites or wait out the window.
