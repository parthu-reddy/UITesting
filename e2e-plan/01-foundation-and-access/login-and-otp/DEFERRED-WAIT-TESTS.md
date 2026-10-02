# Deferred tests requiring intentional waits

User instruction on 2026-10-01: do not execute intentional long-wait scenarios during the feature audit. Record them per feature and run them at the end only when explicitly enabled. Ordinary waits for UI/network readiness are not duration-based business scenarios.

| Scenario | Implementation | Why deferred | Opt-in and final-run requirements |
|---|---|---|---|
| Natural OTP expiry | LoginValidationTest#expiredOtpIsRejectedWithoutCreatingASession; tag slow-auth; disabled unless auth.slow.enabled=true | Waits 305 seconds beyond the real server five-minute TTL; browser clock changes cannot prove expiry | Use a seeded customer with no concurrent OTP activity; enable only this method with -Dauth.slow.enabled=true; expect rejection, visible expiry message and no token/profile. No server-state manipulation. |

The case completed once before this instruction: 1 passed, 0 failures/errors/skips, 312.61 seconds. That evidence is retained in RandomDocuments/E2ECoverageAudit_2026-10-01/evidence/02-login-expiry.xml. It is excluded from subsequent normal runs and is not required for the current fast feature pass.

Final explicit command, only when slow cases are requested:

```sh
mvn -q '-Dtest=LoginValidationTest#expiredOtpIsRejectedWithoutCreatingASession' -Dauth.slow.enabled=true -Dcustomer.phone=8000000481 -Dapp.url='<current Dev URL>' -Dheadless=true -Dslow.mo=0 -Drecord.video=false test
```

Normal feature/suite commands should also exclude the slow-auth tag (-DexcludedGroups=slow-auth) so the intentionally deferred case is not included in passing totals. Other features will create their own DEFERRED-WAIT-TESTS.md when reviewed.
