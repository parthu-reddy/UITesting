# Deferred duration tests — organisation and portal access

## 2026-10-04T07:03:09+05:30 — O3 review-queue duration

Do not execute intentional duration waits during O3. Ordinary Playwright waits for a visible
control or the response caused by that control are allowed; they are readiness waits, not a
business-duration scenario. The cases below are **DEFERRED, not executed, and not counted as a
pass**.

| Scenario | Why deferred | Final-run requirements |
|---|---|---|
| Review queue remains in `IN_REVIEW` for more than 24 hours and triggers the queue-age alert. | Waiting a full day in Dev would make the normal E2E suite slow and would retain an intentionally stale applicant. | Run only in a separately authorised final duration pass with an owned fresh applicant, the deployed UI, and an operator-confirmed alert observation. Do not accelerate time by writing the database or calling a backend endpoint. |
| Provider-check or submission polling beyond the normal UI readiness bound. | The O3 UI should surface status promptly; a prolonged real wait is not a normal browser acceptance case. | Keep the bounded UI-visible poll in the normal journey. Investigate an overdue status through UI/error evidence; use no direct DB, Redis, API, or browser-state bypass. |

There is no O3 long-wait test currently enabled. This file does not authorize an opt-in run.
