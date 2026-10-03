# Checkpoint36 — Oracle startup repair and Dev login restoration

Updated 2026-10-03T17:18:41+05:30. Full Business Platform remains incomplete; O2+ is not started.

## Completed and deployed

Bidding and Tracking crashed because O1's unconditional audit components required a JPA factory,
while their actual Compose configuration intentionally excludes datasource/JPA. Both real startup
contexts were observed red. Audit beans now register only with an EntityManagerFactory, after Hibernate.
Shared 245, Bidding28, Tracking18 and Identity96 checks passed (0 failures/errors/skips in this batch).
Identity used auth.limits.enabled=true. Tested jars embed the same fixed library SHA in 36-local-results.

Required commits/pushes are explicitly authorized for this task. Common25111e0, Biddingac5bb03,
Tracking6686a80 pushed. Common package workflow37120199694 completed successfully. Existing publish.sh
and deploy.sh published/recreated just the two affected services; no workflow scripts changed.

Images: bidding aef68bb-dirty-d7bbbdc; tracking9cb6beb-dirty-47e5d1e. Deployment commitbe38a4f preserves
these tags and the two OTLP endpoint overrides. Initial repaired Tracking logs exposed a separate
localhost exporter error: shared OTLP_ENDPOINT differs from Compose's OTEL_EXPORTER_OTLP_ENDPOINT.
The two service configs now use the supplied Jaeger address + /v1/traces and were deployed with
sequential restarts. Actual Oracle files verified. At17:18:41 both healthy, restartCount0, no new ERROR
logs since their17:10 config restarts; Bidding campaign index and Kafka partitions initialized.
Config validation passes; the earlier reconcile snapshot has29 declared/running, no drift.

## Login screenshot and seed-only restoration

The requested login error was reproduced live for9000000001. Read-only Oracle checks showed Identity
users0/roles0/organisations0 and restaurant brands0/outlets0. This was missing Dev account data.
Owner explicitly authorized **Dev seed only; no reset**. Existing Deployment/dummy-data.sh --load-only
completed and its live relationship/admin-role verification passed. No reset/Redis flush/cleanup.
The standard restaurant, rider and admin roles and Brand1Outlet3 were verified afterward.
The browser then logged9000000001 in, selected its outlet and created advertiserdb69c271-dddd-4cf0-849d-39623906ed66.
Login is repaired; that run failed later at the owner's advertising-wallet read403. Retain this advertiser.

## Active follow-up

Wallet and Ledger do not register the shared money-policy ownership Feign clients; scanned fallbacks
are injected as their implementations. Wallet never contacts Campaign Service and denies its real owner.
Two real-application-context ownership checks were observed red (1failure each). Minimal client
registration corrections and clean package suites are now running. No money calculation change.
Publish/deploy these two services after green; rerun the existing campaign test against the same
advertiser (do not create another onboarding fixture), then complete checkpoint34 and O1 live gates.
O1 lifecycle runner is active with allocationc2da4ab0f39317b2, owned phones8999742712/8999019102;
retain allocation/resource manifests and every created row, even on failure. No dependent O2 work yet.

## Evidence

- evidence/36-startup-failure.json: original Oracle crash counts and cause.
- evidence/36-local-results.json: exact local before/after counts, commits, package publication.
- evidence/36-startup-success.json: initial repaired image startup and pre-config exporter error.
- evidence/36-oracle-final-health.json: bounded final two-service health/startup/restart/error proof.
- evidence/36-restored-seed-accounts.json: actual restored login roles/outlet, no credentials.
- CommonMistakesDocumentation/business-platform-audit-database-free-startup-2026-10-03.md.

D3/D4/D6/D13/D15 and scoped commit/publish/deploy/seed authorization are dated in the authoritative
RandomDocuments/BusinessPlatform_2026-10-03/DECISIONS.md beside the plan. Older checkpoints retain their
historical owner-only/no-commit/unconfirmed wording; these current user choices supersede it.

## Follow-up at17:25:13

Wallet57/0/0/0 and Ledger220/0/0/0 clean packages passed after their real ownership clients were registered.
Both pushed (wallet6443a50, ledger5140a2d), images published/deployed healthy: wallet6443a50-86df50e,
ledger5140a2d-c405f76. Existing campaign test is running against the same advertiser plus one DRAFT.
Gateway CORS regression observed red, then25/0/0/0 green; PATCH-only method addition deployed and Gateway
healthy. O1 first invocation errored on the stale Customer button (no accounts); corrected to Order Food.
Second invocation registered both fresh accounts and created orgd93d5f22-6ec4-41a1-8b40-2d146198b1fe,
then failed at the CORS PATCH403. Exact manifests retained. Its outbox3 rows remainUNPROCESSED because
Identity lacks scheduling; real-context scheduled-publisher regression now running before the fix.
Current owner authorization also permits clean --wipe + fresh seed when the phase rollout requires it;
no wipe has been executed. Non-admin Dev phones already allow any ten digits; admin policy retained.

## Follow-up at 2026-10-03T17:36:31+05:30

Identity scheduling was absent: the real-context publisher assertion failed before @EnableScheduling.
Final clean package97/0/0/0, phone-policy selection6/0/0/0. Identity89046b7 pushed and image
89046b7-0b78a6d published/deployed healthy/restarts0. The old partial org's three outbox rows now
PROCESSED. Gateway regression commit9d633a7 pushed; PATCH configuration already deployed.

O1 third invocation4bbc471d321c081b passed1/0/0/0 through actual browser Dev signup and the public
Oracle tunnel. Org066a3981-fac7-4c49-add1-09f5e336fc0c has exactly one ACTIVE OWNER after transfer
(userB e12f1c2e-23cd-45db-ae33-216d4a8c19a1); userA0abdd8fb-edd2-4afb-abf8-dc776459611f is ADMIN.
Every exact-org outbox row PROCESSED, none FAILED/DLQ. Original manifests copied into fixtures;
36-o1-live-exact-org.json proves SQL. Retain partial and successful allocations, no cleanup.

Campaign UI3/0/0/0 live: owner wallet, cancel discards draft, one saved rupee draft. Retained advertiser,
INR0 wallet and DRAFT in36-retained-advertiser-draft.json. Earnings1/0/0/0 proves actual fresh-seed
zero balances and empty statement; historical nonzero payout/clawback proof remains open.

Final log review found localhost trace-export errors in Identity/Wallet/Ledger/Gateway; four selected
Jaeger override config restarts are active. A shared ad-events-dlt entry logged by Bidding was actually
produced by Notification's group: its deployed JsonDeserializer converted CampaignChangedEvent into
a null-filled NotificationRequestEvent before event filtering. Its retry JsonSerializer changed bytes.
Two actual-YAML codec checks observed red; corrected to String codecs and full Notification suite
running. Protected deploy.sh cannot map notification-service.yml to compose communication-integration;
use existing publish-config.sh for the exact file then existing selected image deploy --fresh, unchanged
image. Do not edit the protected workflow. No notification provider send is part of validation.

Pre-correction hardening passed and reconcile29/29 with0 drift. Recheck after final config rollout.
O1 latency/circuit-breaker measurements remain open; no O2 code yet. Full Business Platform incomplete.

## Performance and Notification continuation — 2026-10-03T17:45:40+05:30

Read-only server measurement invocation1/0/0/0:30 public GETs each for organisations/brands/outlets,
normal retained-account Dev browser login. Actual server histogram orglist p95~30.758ms (upperbucket
33.554ms) passes100ms; pre-O2 brands~30.199ms/outlets~63.753ms (52 cumulative successful samples).
Internal membership/client breaker has no real consumer in O1: explicitly unverified, carried to first
O2 consumer gate in plan DECISIONS; not waived. O2 baseline0/13 static, core50/56, reviews85/85,
readiness8/8, money audit0/23 recorded with exact output. Ownership495line enumeration recorded.
No O2 product code edits yet.

Final corrected trace logs clean for Identity/Wallet/Ledger/Gateway and stable Bidding/Tracking.
Notification config-only recreatehealthy/restarts0, but log review found missing Firebase credentials
and two pre-account OTP records rejected for missing userId. This is not clean service completion.
Existing router test observed1error, then narrow OTP exception6/0/0/0 green; full packages running.
Forward notification audit migration drops only its user_id NOT NULL; Identity OTP aggregate/log
identity changed from phone to eventUUID. Explicit Dev-only mock channels prevent dummy-provider
delivery attempts, with real providers required outsideDev and Firebase failure now fatal.
Current choice recorded in plan DECISIONS; optional user preference question still pending.
Full Notification/Identity packages active, no repaired images published yet. Do not replay old DLT
records or send external messages during these checks. Deployment selected config/tag commit248ce3f
pushed; unrelated user DEPLOY_LOG preserved. Notification codec test commit2abe984 pushed.

## Verified repaired rollout — 2026-10-03T17:55:53+05:30

Notification43/0/0/0 and Identity98/0/0/0 fully packaged. Pushed sourceNotificatione4d914c/Identity979f583;
images e4d914c-fbf55a7 /979f583-8a86798 published and deployed in that order. Actual Oracle migration
V20261003174540 applied; notification_audit_logs.user_id nullable, no reset. Existing LoginSmokeTest
successfulLogin(CUSTOMER) passed1/0/0/0 for retained8999125840 through normal browser DevAutofill.
Fresh actual SMS audit has null user, dev-mock providerID and QUEUED state (mocked dispatch, not actual
external delivery); no recipient/OTP stored in evidence36-dev-signup-notification-audit.json.
Seven repaired services healthy/restarts0/no freshERRORs from final window12:23:44Z in
36-oracle-final-follow-up-health.json. Hardening allpass/reconcile29services0drift.
Deployment image tag commitd3960c2 pushed; user DEPLOY_LOG preserved. Old DLT/history not replayed/removed.

O1 gate live/p95 verified with carried first-consumer internal measurement. O2 implementation has
now begun: canUser API/shared/local policies + membership/no service shortcut/revocation guards only,
unpublished. First targeted compilation hit test orgUUID/import name collision; corrected import.
Shared local clean install running before Identity targeted guard. No restaurant ownership/schema/UI
edits, no O2 deployment or wipe. Do not publish incomplete O2 SDK; finish dependent phase as planned.
