# Deferred rate-limit tests

User instruction on 2026-10-01: ignore rate limiting during the current Dev audit. Implemented cases are opt-in and must be skipped in normal execution. Dev configuration deliberately raises authentication limits to one million, so exhausting production-default limits in Dev is inappropriate.

All cases below are tagged auth-rate-limit and disabled unless auth.limits.enabled=true. Do not count deferred cases as passed. Run them only in the final separately requested limits pass, with an appropriate configuration and isolated data.

| Test | Evidence type | Final-run requirements |
|---|---|---|
| UITesting LoginValidationTest#verificationLimitResponseKeepsTheBrowserLoggedOut | Browser-routed error contract, not deployed exhaustion | Returns a simulated 400 failed-attempt-limit response after normal Dev initiation; requires visible error and no browser session. |
| UITesting LoginValidationTest#otpRequestLimitResponseLeavesResendAvailable | Browser-routed error contract, not deployed exhaustion | Returns a simulated 429 for resend; requires visible rejection, usable form and no browser session. |
| IdentityService AuthServicePortalSecurityTest#defaultInitiateLimitStopsOtpStorageAndNotifications | Backend unit | Simulated attempt count 11 exceeds production-default 10; no OTP storage or notification. |
| IdentityService AuthServicePortalSecurityTest#defaultVerifyLimitInvalidatesOtpBeforeUserOrSessionCreation | Backend unit | Count 6 exceeds production-default 5; invalidate OTP and issue no user/role/session writes. |
| IdentityService AuthControllerAuthorizationTest#emptyControllerBucketReturns429WithoutInitiatingOrVerifying | Backend unit | Empty bucket returns 429 without invoking authentication service. |

Actual deployed exhaustion/recovery is not implemented for the current relaxed Dev target. It needs a separate rate-enabled configuration and isolated OTP-only numbers; never lock shared seeded accounts. Thresholds must be read from that target rather than assumed from Java defaults.

Historical attempts in this audit: two bounded live probes incorrectly used production thresholds and failed to hit a limit; they created no session/account. Their reports remain evidence of an unsuitable test configuration, not a product defect. The browser-routed replacements ran once before this deferral instruction. One new backend limit assertion initially used an old service-name OTP key; the source assertion is corrected, but its final rerun is deliberately deferred.

Final opt-in: use -Dauth.limits.enabled=true, a focused selector or -Dgroups=auth-rate-limit, and explicitly preserve the scope distinction above. Ordinary audit commands exclude auth-rate-limit and slow-auth groups.
