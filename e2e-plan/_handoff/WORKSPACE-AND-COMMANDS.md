# Workspace and execution

Workspace: /Users/parthureddy/Documents/Food Delivery.nosync. Root is not a Git repository; children are. Executable tests: UITesting/src/test/java/com/fooddelivery/e2e/{base,pages,util,tests}; plan is documentation, not a second test implementation. Key helpers: SeededRiderDuty, LiveOrderFixture, OrderChatChecks, OrderMoneyChecks, RefundQuoteChecks, RefundRecoveryChecks. Target/lifecycle manifests must not be lost when switching agents; portable copies are in fixtures/.

Relevant repositories: CustomerApplication (orders/refunds/consumer/ledger producer), PaymentGatewayIntegration (capture/refund provider mocks/consumer/webhook), LedgerService (double-entry/rejection/payout), CommunicationService (chat/WebSocket/outbox), DeliveryExecutiveApplication (duty/assignment/OTP/telemetry), RestaurantApplication, FoodDeliveryAppUI, CommonLibrary, IdentityService, ApiGateway, ConfigServer and Deployment. Confirm actual directory names via rg --files or directory inventory before opening guessed paths.

Historical reports: RandomDocuments/BackendIssues_2026-09-29, RandomDocuments/E2EFullSuite_2026-09-27. Full current audit archive: RandomDocuments/E2ECoverageAudit_2026-10-01. Do not load all reports; inventory identifies relevant method/issue documents. Source code wins over stale markdown claims.

Dev URL at last verification: https://gulf-strike-dark-extras.trycloudflare.com/ . Resolve UITesting TestConfig plus E2E_APP_URL/-Dapp.url each time. The tunnel may change; do not embed it in individual test methods. Dev Autofill Code UI is mandatory for automated login.

## Focused commands, subject to current prerequisites

Run from UITesting. PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 uses installed Chromium. Explicit phones reproduce owned state; choose current app.url. Do not run these while known fixture state is invalid. None creates another order when resume properties are present and manifests validate.

```bash
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=HappyDeliveryFlowTest#completeOrderLifecycle' -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Dresume.delivered.order.id=b83c71bd-022c-439d-8586-aa4fe5b1c0d1 -Dresume.delivered.quote.only=true -DexcludedGroups=slow-auth,auth-rate-limit test
```

Quote-only skips admin/rider money checks and requests no actual refund. For strict retained delivered money plus quote, remove quote.only only after posted ledger prerequisites are repaired. These branches are follow-up invocations, not a repeat successful delivery.

```bash
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=OrderCancellationFlowTest#customerCancelsBeforeAcceptance' -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Drefund.resume.order.id=0b22b53a-c718-4bec-b854-b736e24bee29 -DexcludedGroups=slow-auth,auth-rate-limit test
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=RestaurantRejectFlowTest#restaurantCancelsOrder' -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Drefund.resume.order.id=176fb42c-46e9-49b2-b52f-43c571603199 -DexcludedGroups=slow-auth,auth-rate-limit test
```

If target/lifecycle files are absent in a new checkout, verify fixtures/*.json ownership/current state, then copy the three exact manifests there. Do not overwrite a conflicting manifest or treat an old ID erased by database reset as current. Never copy tokens, OTPs or browser storage into this handoff.

Selected backend verification classes/counts are in evidence/15-local-results.json. Use mvn -q -Dtest=<affected existing classes> test from the changed child repo; inspect fresh Surefire XML. Direct constructor/handler/@Import/consumer searches identify affected tests. Consumer stubs already installed locally made the last selected contracts runnable without build_verify; if missing/stale, use authorized existing build tooling rather than inventing or skipping contracts. Full build_verify is not automatically required for every focused change and was not run in the 81-check batch.

## Read-only Oracle inspection

SSH key path: /Users/parthureddy/Documents/OracleSSH/ssh-key-2026-08-16.key; host ubuntu@140.245.234.137; remote cwd 'Food Delivery.nosync/Deployment'. Do not copy key material into docs. Health: docker compose ps for relevant services; use bounded logs and explicit fields. SQL via docker compose exec -T -u postgres postgres psql -X -qAt -v ON_ERROR_STOP=1 -d <db>, wrapped in BEGIN READ ONLY; ... COMMIT. Databases: identity_db, customer_db, restaurant_db, delivery_db, payment_db, ledger_db, chat_db. Outbox column is type, not event_type. Avoid full orders/assignment JSON or raw outbox order payloads because they contain OTPs; select only needed nonsecret fields. Redact logs and never print Authorization/CONNECT credentials.

Publishing/deployment remains the user's job unless authorization is renewed. Read protected workflow files on demand; never alter them or their underlying scripts without the user's explicit approval. No reset/seed/wipe command belongs to automatic validation.

## Support refund decisions (checkpoint24)

`SupportRefundResolutionFlowTest` takes one delivered CARD order with no tickets or refunds (`-Dsupport.order.id`, manifest in target/lifecycle). Full class on a fresh order, or only SUPPORT-REFUND-03 on 0554f250; exact commands in NEXT-STEPS. Read-only DB checks: `support_tickets`, `refunds`, `refund_items`, `payment_intents` (customer_db), `ledger_entries` joined to `ledger_accounts` (ledger_db). The whole-set scan for the transactional-refusal trap: `python3 UITesting/e2e-plan/_handoff/tools/scan_rollback_only.py .` from the workspace root (5 classified non-defect candidates remain).

Role names in authorization rules (checkpoint34): `python3 UITesting/e2e-plan/_handoff/tools/validate_role_names.py .` from the workspace root. Every `hasRole`/`hasAnyRole`/`hasAuthority`/`@RolesAllowed`/`@Secured` name must be a `RoleName` constant or SERVICE (both read from source). PASS 255 at checkpoint34; exits 1 and names the file:line otherwise, and STALE if it stops finding the rules.

Campaigns (checkpoint34): `RestaurantCampaignsLiveTest` takes `-Dcampaign.outlet` and writes only with `-Dcampaign.onboard=true` (creates the owner's advertiser + ad wallet, once, permanently) or `-Dcampaign.create=true` (one DRAFT campaign). Read-only DB checks: `advertiser_profiles`, `campaigns` (campaign_db); `wallets` where entity_type='ADVERTISER' (wallet_db).
