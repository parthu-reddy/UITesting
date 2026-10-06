# Business wallet — pending

## 2026-10-06T13:35+05:30

Preconditions: read the current Dev URL from `CustomerApplication/.agents/rules/deployment-context.md`; check the VM
read-only for a recent wipe before reusing any recorded id (see `_handoff/START-HERE.md`). Allocate unused `9999`
phones the way `scripts/run_registration_e2e.py` does; never seed that pool.

1. **BW-W2-001 rerun** — after the WalletService redeploy (owner):
   ```bash
   cd UITesting && PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q -Dtest='BusinessWalletTopUpFlowTest#businessWalletTopUpFlow' -Dapp.url=<Dev URL> -Dbp.w2.preflight=true -Dbp.w2.phone=9999xxxxxx -Dheadless=true -Dslow.mo=0 -De2e.otp.enabled=false -DexcludedGroups=slow-auth,auth-rate-limit test
   ```
   Then measure p95 of `POST …/topups` (≤ 300 ms) and record in W2 validation.md.
2. **BW-W3-001** — only after (1) is green and the W3 UI is deployed (WalletService before UI):
   ```bash
   cd UITesting && PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q -Dtest='BusinessWalletUiTest#businessWalletPage' -Dapp.url=<Dev URL> -Dbp.w3.preflight=true -Dbp.w3.phone=9999xxxxxx -Dheadless=true -Dslow.mo=0 -De2e.otp.enabled=false -DexcludedGroups=slow-auth,auth-rate-limit test
   ```
   Optional `-Drestaurant.phone=900000000N` to pin the seeded owner (default: random 1–10).

Deferred (not E2E, by policy — duration-bound): the 15 s slow-PENDING notice and background polling (W3); proven by
`useBusinessTopup.test.ts` with fake timers. Not deferred-wait candidates: the Dev mock settles in seconds.
