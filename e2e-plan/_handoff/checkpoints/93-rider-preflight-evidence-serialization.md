# Rider preflight evidence serialization

## 2026-10-05T05:44:13+05:30 — checkpoint93: lifecycle stopped before checkout; harness evidence fix

Both phase UI gates remain8/8 on deployed UI2aed6af. Canonical lifecycle invocation1 executes one case with0passes/0failures/1error/0skips before any order is created. Actual server ONLINE/socket/location and visible readiness checks passed; saving the sanitized artifact failed because Playwright's Java serializer rejects Long locationAgeMs. Change only that evidence field to Double, preserving the fifteen-second readiness rule. Existing nine mocked readiness checks are running; then push the harness and rerun the lifecycle. No application image/config change, direct API/DB/Redis test access, additional wipe or fixture cleanup.

The same-order regressions and final histograms/checklists remain mandatory. Stop after phases4and5; internal/historical-baseline/CSP-negative deferrals remain explicit.
