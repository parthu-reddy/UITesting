# suite-isolation-and-regression

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


Status: in progress; first exact three-role isolation test implemented.

Scope: Test data cleanup, isolation, repeatability and smoke/regression execution.

`ResilienceRegressionUiTest` logs customer, restaurant and rider into simultaneous isolated contexts using randomized seeded accounts. It asserts each role's exact dashboard marker and proves role-specific controls do not leak into the other contexts. No order or shared data mutation is involved.

See `PENDING.md` for live validation and remaining repeatability work.

The two newest customer accessibility tests passed in two consecutive identical live runs, covering a small repeatability sample without consuming OTP capacity across the full suite.
