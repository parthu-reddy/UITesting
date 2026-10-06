# Business wallet audit status

## 2026-10-06T15:35+05:30 — BW-W2-001 PASS, BW-W3-001 PASS (deployed)

**2026-10-06T15:33:57+0530 — PASS (1 test, 0 failures/errors/skips), invocation 4** on WalletService bae747f, UI fca8a75, payment-gateway 950ba6d (verified running + healthy; 29 up: 26 healthy, 3 without checks). `BusinessWalletTopUpFlowTest#businessWalletTopUpFlow` -Dbp.w2.phone=9999406215; org Brand 1 `1dfc716b-4db7-5395-82dd-fded7dff73fc`. ₹100 top-up `b462cab3-538a-4f91-949e-212e20c37eb8` SUCCESS, balance 100 → 200; same key → same top-up, balance unchanged; same key + ₹200 → 409; Dev-declined ₹10.13 `17c242a9-b9ad-4574-9b07-9fcbe2b930f7` FAILED with reason, nothing credited; ₹5 / ₹100001 → 400; ledger: exactly one BUSINESS_WALLET_TOPUP credit for the top-up, none for the decline; MANAGER `40cee586-a695-45cc-91bd-989c969ca7a9` reads 200, top-up 403. Earlier invocations 1–3 were red on three real defects, all fixed (Feign registration; outbox aggregate_id length; Redis 409 on replay).

**Measurement — not enough samples (open).** wallet-service's own `http.server.requests` histogram for `POST /api/v1/money/business/{organisationId}/topups` since the 15:2x restart (read-only, actuator inside the VM): 200 n=4, mean 106 ms, p50 ≤ 39 ms, slowest bucket ≤ 358 ms (likely the first call after start); 400 n=2 ≤ 8 ms; 403 n=1 ≤ 11 ms; 409 n=1 ≤ 22 ms. n=4 cannot give a p95, so the ≤ 300 ms budget is **not claimed met or missed**. A real sample needs ~40 new top-ups, but the route allows 10/h per organisation and each creates retained Dev records — owner decision.

**2026-10-06T15:34:40+0530 — PASS (1 test, 0 failures/errors/skips), run 3** on WalletService bae747f, UI fca8a75, payment-gateway 950ba6d (verified running + healthy; 29 up: 26 healthy, 3 without checks). `BusinessWalletUiTest#businessWalletPage` -Dbp.w3.phone=9999283428; owner 9000000010 (random seeded), Brand 10 `eac481cc-1da3-5bb7-8d6a-04385a6d635c`. Through the UI only: Wallet tab balance = API (₹0); Add money ₹100 → payment button "Add ₹100.00 to the wallet", no ₹10,000.00; "Money added"; balance ₹100.00; newest line "Top-up" "+₹100.00" whose reference is the exact top-up `7cac25b6-63c3-4fd4-b067-e42437742d23` from the UI's own POST; disposable MANAGER 9999283428 invited/accepted through the hub sees ₹100.00, no Add money, the reason shown. Runs 1–2: harness landing fault and the null-field statement defect, both fixed.

## 2026-10-06T15:05+05:30 — after the full deploy: W2 red (Redis 409 on key replay), W3 red at statement (null field); both fixed locally

**2026-10-06T14:29:46+0530 — third run, after the deploy of WalletService e1bbe04, payment-gateway 950ba6d, UI 489e2c8 (all healthy; 29 containers up, 26 healthy + 3 without checks): RED, third root cause.** Phone 9999909097.
The first ₹100 top-up `9a4a94d5-2da4-434a-8615-eee2cd4bcda9` settled SUCCESS (order-id fix confirmed live). The replay with the
same Idempotency-Key got a plain-text 409 "Duplicate request detected." from CommonLibrary's Redis `IdempotencyFilter`
(24 h lock per key), so the service's durable replay was never reached (the harness then failed parsing it as JSON).
Fix (local): WalletService `application.yml` `idempotency.filter.bypass-routes: POST:/api/v1/money/business/*/topups` (as
LedgerService does for payouts) + `createTopup` locks the organisation's wallet row before the key lookup, so a concurrent
duplicate waits and replays instead of creating a second payment order; 404 if the organisation has no wallet. Guards:
`WalletIdempotencyBypassTest` (reads the real application.yml through the real filter) and an in-order lock test, both seen
red. No config overrides the property (platform-defaults, Deployment yml, ConfigService checked). WalletService clean 105/105.
Harness: `topUp()` keeps a non-JSON body as text. **Redeploy WalletService**, then rerun.

BW-W3-001: run 1 harness landing fault (fixed); run 2 passed login → wallet → ₹100 top-up → balance, failed at the statement (null metadata) — fixed locally. Detail: W3 validation.md.

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
