# Business wallet audit status

## 2026-10-06T14:10+05:30 — BW-W2-001 rerun RED after WalletService redeploy: second root cause, fixed locally

**2026-10-06T13:45:54+0530 — rerun after the WalletService redeploy (0962e78): RED, second root cause.** Phone
9999905929 (allocated with the runner's `candidate()`, 75 known phones excluded). The top-up now starts (Feign fix
confirmed live), but stays PENDING: top-up `16a98c16-7961-465f-8380-0ba9630832ab`, org Brand 1. payment-gateway logged
`DEV_PAYMENT_SUCCESS_FAILED … DataIntegrityViolationException: value too long for type character varying(50)` on
`insert into outbox_events`: the outbox `aggregate_id` is the payment's order id, and `WALLET_<organisationId>_<key>`
is ~80 characters (`outbox_events.aggregate_id VARCHAR(50)`, common migration). Both settlement paths (success and
decline) fail the same way. No test caught it: the test schemas are Hibernate-generated (`varchar(255)`).

Fix (local, not deployed): the order id is `WALLET_<topupId>` (43 chars); payment-gateway `create-order` refuses an
`internalOrderId` over 50 with 400 (guard, seen red without it); both messaging contracts and the consumer test pin
`WALLET_<uuid>` exactly. payment-gateway clean install 101/101 (incl. generated contract tests), WalletService clean
101/101 (consumer contract 2/2 on the new stubs); money audit 0/23, readiness all phases green, core 56/56, W2 12/12.
**Redeploy WalletService and payment-gateway (no wipe)**, then rerun. Top-up `16a98c16…` stays PENDING (Dev data left by
the fixed bug; no money moved; not repaired).

## 2026-10-06T13:35+05:30 — W1 PASS (deployed); W2 RED (fix local, redeploy pending); W3 written, not run

Folder created this date: the W1/W2 sessions recorded results only in the plan's `validation.md` files; they are
copied here as the owning feature record. Plan files remain the detailed source.

| Scenario | State | Proof |
|---|---|---|
| BW-W1-001 | **PASS** on deployed W1+A1 (2026-10-06T09:55:58+0530, invocation 4, phone 9999848324, org Brand 1 `1dfc716b-4db7-5395-82dd-fded7dff73fc`) | RandomDocuments/BusinessPlatform_2026-10-03/02_BusinessWallet/Phase1_WalletOwnedByOrganisation/validation.md § E2E result. Invocations 1–3 failed on harness faults (401 ENTITLEMENTS_CHANGED without renewal; network-idle wait; portal for a no-business person) — recorded there. p95 84 ms / 99 ms. |
| BW-W2-001 | **RED, not passed** (2026-10-06 ~12:13, phones 9999324833, 9999006041 — neither became a member) | First top-up 400 "PaymentService is down". Root cause: WalletService never registered `PaymentServiceClient` as a Feign client, so its always-failing fallback was the bean. Fixed **locally** (`WalletServiceApplication`); WalletService redeploy pending; rerun with a new phone. Plan: 02_BusinessWallet/Phase2_TopUpAndSpend/validation.md, W2-HANDOFF.md. |
| BW-W3-001 | **Not run** — written and compiled (UITesting `mvn -q -o test-compile`, 2026-10-06 13:28), locator audit `--only BusinessWalletUiTest --fail-only` PASS | Runs only after the W3 UI + WalletService (`category`) release and after BW-W2-001 is green. |

Harness changes this date (local, uncommitted): `pages/business/BusinessWalletPage.java` (new; E2E-STRATEGY names it
for W3), `tests/features/wallet/BusinessWalletUiTest.java` (new), `BusinessWalletTopUpFlowTest` prints the response
body on a status mismatch. UI test hooks added for it: `business-wallet-balance`, `add-money-amount`,
`add-money-unavailable`, `statement-category`, `statement-amount`, and `payment-confirm` on the shared PaymentModal
pay button (a coordinated test id; the label stays copy).

Fixtures: retained, none cleaned. BW-W1 members of Brand 1's organisation: 9999763656, 9999621330, 9999733049,
9999848324. BW-W2 phones above (no membership). BW-W3 will add one ₹100 top-up to the chosen seeded owner's
organisation and one disposable MANAGER per run.

Next action: owner redeploys WalletService (no wipe) → rerun BW-W2-001 with a new `9999` phone → owner deploys the
W3 UI → run BW-W3-001 (command in PENDING.md) → record here, in the checklist and in W3 validation.md.
