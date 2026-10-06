# Business wallet — pending

## 2026-10-06T15:35+05:30

BW-W1/W2/W3 all PASS on Dev. Remaining:

- W2 measurement: p95 of `POST …/topups` (n=4) — **PARKED by the owner 2026-10-06**; do not run until asked.
- Deferred by policy (duration-bound): the 15 s slow-PENDING notice and background polling; proven by `useBusinessTopup.test.ts` with fake timers.

Rerun commands (new `9999` phone each, allocated with `scripts/run_registration_e2e.candidate`):

```bash
cd UITesting && PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q -Dtest='BusinessWalletTopUpFlowTest#businessWalletTopUpFlow' -Dapp.url=<Dev URL> -Dbp.w2.preflight=true -Dbp.w2.phone=9999xxxxxx -Dheadless=true -Dslow.mo=0 -De2e.otp.enabled=false -DexcludedGroups=slow-auth,auth-rate-limit test
```

```bash
cd UITesting && PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q -Dtest='BusinessWalletUiTest#businessWalletPage' -Dapp.url=<Dev URL> -Dbp.w3.preflight=true -Dbp.w3.phone=9999xxxxxx -Dheadless=true -Dslow.mo=0 -De2e.otp.enabled=false -DexcludedGroups=slow-auth,auth-rate-limit test
```
