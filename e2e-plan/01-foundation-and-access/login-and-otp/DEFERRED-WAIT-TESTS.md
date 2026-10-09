# Deferred tests requiring intentional waits

None remain for login (2026-10-08, FastE2E_2026-10-08 Phase 2).

The natural-expiry E2E (`LoginValidationTest#expiredOtpIsRejectedWithoutCreatingASession`, tag `slow-auth`, waited
305 s) is deleted. Its claims now live where they run in milliseconds:

| Claim | Test |
|---|---|
| A login code lives 5 minutes | IdentityService `AuthServiceTest#loginCodeIsStoredForFiveMinutes` |
| An expired code (Redis key gone) gets 401 "Invalid or expired code" and creates no account or session | IdentityService `AuthServiceTest#expiredCodeIsRefusedAndCreatesNoSession` |
| The login screen shows that message | `LoginValidationTest#resendRejectsThePreviousCode`: a replaced code takes the same `verifyOtp` branch, and the test asserts the alert contains "expired" |

The `slow-auth` tag and the `auth.slow.enabled` property no longer exist.
