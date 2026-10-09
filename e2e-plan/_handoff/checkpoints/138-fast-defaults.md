# Checkpoint 138: fast runs by default; OTP expiry is a unit test (2026-10-08 18:35 IST)

Owner asked (18:10) whether slow tests can be made fast, then said "proceed". Plan and gates:
`RandomDocuments/PendingWork_2026-10-07/FastE2E_2026-10-08/` (README, 3 phases, tools/validate_fast_e2e.py, tools/prove_fast_e2e.py).

## Done
- `TestConfig` defaults are now `slow.mo=0`, `headless=true`, `record.video=false`. They were 400 ms, a visible
  browser and video, and the runner never overrode them. `run_e2e_batch.py --debug` restores the old settings.
- The 305 s `LoginValidationTest#expiredOtpIsRejectedWithoutCreatingASession` is deleted, along with the `slow-auth`
  tag and the `auth.slow.enabled` property. Its claims now live in IdentityService
  `AuthServiceTest#loginCodeIsStoredForFiveMinutes` and `#expiredCodeIsRefusedAndCreatesNoSession` (21/21), plus a
  `containsText("expired")` check in `resendRejectsThePreviousCode`.
- Gates: phase 1 5/5, phase 2 6/6, phase 3 3/3. Break tests 9/9 red-then-green. Runner tests 21/21, feature tags 6/6 +
  17/17, Phase 6, p02 6/6 (inventory rebuilt at the same cutoff), Phase 4 10/10 (dossier rebuilt), locator audit PASS.

## Live on Dev (read-only)
- `RestaurantUiTest#verifyRestaurantDashboardUI`: `--debug` 7.7 s vs default 3.1 s. Per runner invocation:
  20.7 s vs 8.6 s. One sample each.
- `LoginValidationTest#resendRejectsThePreviousCode` 4/4 with the fast defaults (new "expired" assertion included).

## Open
- Owner approval: re-time one order test with the fast defaults (`CustomerOrderPlacementTest#tippedOrderMatchesMockPaidTotal`,
  1 order, old 30.0 s). No order flow has run without the 400 ms delay yet. A new failure there means a hidden race:
  fix the wait, never restore the delay.
- Phase 6 `slow` tags were measured under the old settings. Re-measure them as tests run under the new defaults.
- Commit (owner): UITesting, IdentityService (`AuthServiceTest` only; no runtime change, nothing to deploy).
