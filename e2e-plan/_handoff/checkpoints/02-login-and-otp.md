# 02 — Login and OTP

## Current retained-data policy — 2026-10-01

The user explicitly requested no automatic test cleanup, with rider duty as the exception. Preserve created users, profiles, addresses, brands, outlets, orders and server sessions. Do not deactivate, delete, revoke or restore them as teardown. Retain run manifests to identify test-created data. Explicit logout/device removal/cancellation actions stay when they are the behavior being tested; they are not automatic teardown. Browser/tab/resource disposal remains normal. An authenticated idle rider is made OFFLINE at teardown, with authoritative server confirmation; an active delivery is preserved until completion. Slow and rate-limit scenarios remain opt-in and excluded.

Earlier cleanup/retirement evidence below describes past runs before this instruction. It does not govern future execution. Retained state must be checked as each feature is reviewed; do not rely on an automatically empty session list or restored stock.


Status: current fast feature checks validated: 39 invocations passed, zero failures/errors/skips. Slow and rate-limit cases are deferred and excluded from this total.

Reviewed LoginSmokeTest, LoginValidationTest, RoleNavigationUiTest, incomplete-profile validation in ProfileSettingsTest, RegistrationUiTest and its existing Dev-only runner, LoginPage, AuthForm/useOtpLogin, phone/OTP schemas, CompleteProfileModal, Identity AuthController/AuthService and DevOtpAccessPolicy. Remaining Settings methods belong to phase 05. Session persistence/logout across four roles belongs to the next feature.

Confirmed and fixed the +91 form accepting short national numbers: all four roles previously sent eight-digit inputs. The UI now requires exactly ten digits without changing the wider API schema, non-admin Dev number policy or admin allowlist. Eighteen focused hook cases, typecheck and lint passed locally. User deployed; all four phone-boundary reruns passed.

## Current evidence

| Coverage | Invocations | Durable XML |
|---|---:|---|
| Four-role positive/wrong OTP; JWT phone/role/subject matches selected account; clean contexts | 9 | 02-independent-LoginSmokeTest.xml |
| Desktop/mobile role reset and repeated mobile switching without requests/page errors | 9 | 02-independent-RoleNavigationUiTest.xml |
| Four-role short OTP/Back, resend success, previous-code rejection; unknown admin denial | 13 | Selected cases in 02-independent-LoginValidationTest.xml |
| Four-role short phone rejection before submission, after deployment | 4 | Selected cases in 02-login-deployed-boundaries.xml |
| Incomplete profile invalid name/email without saving fixture | 1 | 02-profile-validation.xml |
| Explicit fresh customer/rider/restaurant signup through Dev mock approval | 3 | 02-registration.xml |

02-fast-results.json lists each of the 39 cases; historical failed rate probes and separately deferred routed-rate cases are not included. The earlier five-minute expiry case passed before the user requested deferral and will not be rerun now. New backend rate tests compile; final execution remains deliberately deferred.

Backend security regression: AuthServicePortalSecurityTest 12, DevOtpAccountPolicyTest 5 and AuthControllerAuthorizationTest 1 passed locally before adding the deferred limit cases. Those 18 checks are local evidence, not a complete deployed proof for every backend issue.

## Isolation and cleanup

The UI cleanup helper revokes only the page's session and checks the revoked token receives immediate 401. The existing signup runner allocated unused phones outside seed ranges, saved run manifest 9248d2b153ff2d41, and retired only accounts created after its start. It deactivated those users, their rider and restaurant outlets and revoked remaining sessions; profiles/onboarding/audit records were retained. An independent read-only audit confirmed all three inactive with no session keys, the test rider OFFLINE, and its successful biometric row belonged to its actual UUID. Before/after checks confirmed seeded partner/biometric states unchanged. No real government provider, order, duty, payout or financial operation was triggered.

## Deferred execution

See the owning plan's DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md. Opt-ins auth.slow.enabled/auth.limits.enabled must stay unset; current runs exclude slow-auth,auth-rate-limit. Deferred cases are not passes. Backend rate default assertions are not applied to relaxed Dev configuration.
