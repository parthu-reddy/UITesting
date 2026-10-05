# O4 upload/CSP repair continuation

## 2026-10-05T05:03:22+05:30 — checkpoint85: resumed; actual O4 failures and precise CSP repair

Latest O4 UI invocation2 on published/deployed UI2d10f50 and harness9d96af6: eight tests, five passes, one failure, two errors, zero skips. All admin cases and three one-login cases pass. The approved rider's location Close races the initial empty-address response; the harness now uses the real Use Current Location control and asserts the visible selection. Both owned restaurant approval fixtures reach real upload, which CSP blocks because the presigner uses labouffe-documents-dev.80708a753745ca23eabc20b4b3dee5fa.r2.cloudflarestorage.com rather than just the account hostname. Add that exact observed hostname to connect/img/media, preserve all other policy restrictions. No wildcard or credential changes. Allocation16ce9b3de426a690 is retained; member8999507193 remains an allocated candidate, not a created/accepted member. No completed approval or order exists from this invocation.

CSP local invocation1 loaded six unrelated passing tests but its configuration-test suite failed before running (browser-transformed import.meta.url had HTTP scheme). Corrected test reads the repository config from process.cwd; invocation2/typecheck/lint are running. This is not yet published/deployed or green acceptance. Previous UI-only Dev config gate completed with all four files already synchronized, so it skipped publication/restarts; hardening15/15 and reconciliation29/0drift pass. All29 containers run,26 configured healthchecks healthy, zero drift/automatic restarts/recent errors in evidence35. No second wipe/seed or fixture cleanup.

Next: finish local CSP/harness gates, unchanged GitHub UI publish then Oracle UI deploy/runbook gates; rerun O4/O5, one retained canonical lifecycle and required UI regressions, final measurements/checklists. Stop after O4/O5; Wallet/Ads untouched. Missing historical Gateway comparison and deliberate CSP-origin-removal probe remain explicitly deferred, not passing proof. Evidence36-o4-ui-invocation2.json retains actual counts and causes.

Local follow-up: CSP config/hook/client invocation2 passes8/8, typecheck/lint pass; exact UI commit73058adca8945d8a666d00d812a29915b05cacaf pushed. GitHub37244246232 is in progress; never deploy until its exact SHA succeeds. Harnessc6d2a3c pushed; compile16 and mock rider units9/9 pass. Locator final14/14, no exemptions; initial composite secondary-label audit1FAIL is retained as diagnostic. Current static gates O4:22/22, O5:7/7.

Continuation after successful publication:

```bash
REGISTRY=hyd.ocir.io/axekmbadoczl bash Deployment/deploy.sh food-delivery-app-ui
bash Deployment/deploy.sh --config --dry-run application-dev.yml api-gateway.yml api-gateway-dev.yml identity-service-dev.yml
# Review, then apply unchanged runbook configs.
bash Deployment/deploy.sh --config --yes application-dev.yml api-gateway.yml api-gateway-dev.yml identity-service-dev.yml
python3 Deployment/validate_hardening_phase1.py --remote
bash Deployment/reconcile.sh
# In UITesting, after health/config gates and a new browser context:
python3 scripts/run_business_platform_o45_e2e.py --app-url https://gulf-strike-dark-extras.trycloudflare.com --only o4
python3 scripts/run_business_platform_o45_e2e.py --app-url https://gulf-strike-dark-extras.trycloudflare.com --only o5 --admin-phone 1000000002
```

Canonical lifecycle: only HappyDeliveryFlowTest#completeOrderLifecycle, seeded8000000001/9000000001/7000000001, SSE/map verificationfalse, recent authoritative rider readiness before checkout. Retain newly created order/money manifests immediately; failure after order creation must resume that exact ID. CustomerSettings unrated review executes on that order before OrderReviewsFlowTest#participantsReviewEachOther. Partner trip, admin money, earnings and restaurant history reuse the same order; other read-only smoke cases may spread OTP across existing seeded accounts. No second financial order.


### 2026-10-05T05:08:45+05:30 — Respect Close in the product, then prove it through UI

GPS selection works on the existing deployed UI: one targeted approved-rider portal case passes, zero failures/errors/skips, with no new profile/order. However the underlying slow empty-address response still reopens a dialog the user closed. Correct the product hook so only disappearance of a previously selected saved address prompts again; initial addressless entry already opens the dashboard dialog. A new delayed-response unit preserves Close, existing vanished-ID/GPS/reconciliation cases remain. Local14/14 pass; typecheck/lint running. Harness reverts GPS setup to the real Close control to prove the repaired UX live. Current GitHub37244246232 builds CSP-only73058ad; publish the tested address follow-up afterward and deploy the final image, preserving publication before deployment. No new data reset or Cloudflare credential changes.
