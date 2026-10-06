# Business wallet scenarios

Owner request: Business Platform plan, RandomDocuments/BusinessPlatform_2026-10-03/02_BusinessWallet (E2E gates
explicitly requested for that plan). One prepaid business wallet per organisation (D9); OWNER/ADMIN add money
(WALLET_TOPUP), MANAGER and above see it (WALLET_VIEW); STAFF cannot. Ads spend from it. The personal customer
wallet is separate (Settings → Wallet, covered elsewhere) and never exchanges money with it.

Source of truth for each phase's assertions: that phase's `validation.md` (§ E2E gate). Accounts follow
[TEST-DATA.md](../../TEST-DATA.md) and the plan's E2E-STRATEGY: seeded owners `9000000001`–`9000000010`, disposable
`9999xxxxxx` members invited by the test, nothing deleted, manifests retained under `UITesting/target/business-platform/`.

| ID | Phase | Executable method | Required outcome |
|---|---|---|---|
| BW-W1-001 | W1 | `BusinessWalletAccessApiTest#businessWalletAccess` (`bp-w1`) | Owner reads BUSINESS/INR wallet + statement; `size=51` 400; STAFF 403; MANAGER 200 with the same balance; another organisation's owner 403; a person with no business 403. |
| BW-W2-001 | W2 | `BusinessWalletTopUpFlowTest#businessWalletTopUpFlow` (`bp-w2`) | ₹100 CARD top-up settles SUCCESS within 10 s, balance +₹100.00, newest line references the top-up; same key → same top-up; same key + other amount 409; Dev-declined ₹10.13 ends FAILED with reason, nothing credited; ₹5 and ₹100001 → 400; ledger books exactly one BUSINESS_WALLET_TOPUP credit; MANAGER reads 200 but top-up 403. |
| BW-W3-001 | W3 | `BusinessWalletUiTest#businessWalletPage` (`bp-w3`) | Through the UI only: Business hub → organisation → Wallet tab shows the API's balance; Add money ₹100 → payment dialog button reads "Add ₹100.00 to the wallet" (no ₹10,000.00); pay (Dev mock captures) → "Money added" → balance +₹100.00, newest line "Top-up" "+₹100.00" and its reference is the exact `topupId` from the UI's own POST; a disposable MANAGER invited/accepted through the hub sees the balance, no Add money button and the reason "Owners and admins can add money". |

Not covered by E2E (recorded, with the proof used instead):
- A new organisation's wallet appears within 10 s (W1): a pre-approval organisation has no BUSINESS role, so the
  wallet is 403 through the UI path; covered by WalletService `OrganisationCreatedConsumer` contract + idempotency tests.
- Top-up polling cadence, the 15 s slow-PENDING notice and stop-on-unmount (W3 PERF-8): duration-bound; covered by
  vitest with fake timers (`useBusinessTopup.test.ts`). See PENDING.md.
- Empty-wallet notice and ad spend debits (W2 spend side): covered by WalletService/CampaignService unit + contract tests.
