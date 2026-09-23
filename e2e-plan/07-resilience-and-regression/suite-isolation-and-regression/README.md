# suite-isolation-and-regression

Status: in progress; first exact three-role isolation test implemented.

Scope: Test data cleanup, isolation, repeatability and smoke/regression execution.

`ResilienceRegressionUiTest` logs customer, restaurant and rider into simultaneous isolated contexts using randomized seeded accounts. It asserts each role's exact dashboard marker and proves role-specific controls do not leak into the other contexts. No order or shared data mutation is involved.

See `PENDING.md` for live validation and remaining repeatability work.

The two newest customer accessibility tests passed in two consecutive identical live runs, covering a small repeatability sample without consuming OTP capacity across the full suite.
