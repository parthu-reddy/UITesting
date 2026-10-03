# Checkpoint38 — reviewed fresh Dev schema consolidation

Updated 2026-10-03T19:52:33+05:30. User explicitly requests removing migration code and taking appropriate decisions to prevent SQL failures. User reiterates **publish before deploy**. Dev-only disposable-data authority remains effective until explicit forget/production report. Nothing in this batch is published or deployed; no Oracle data was changed.

## Completed and verified

Twenty incremental SQL files in CommonLibrary, Identity, Restaurant, Chat and Notification were inspected and folded into each initial schema. Required columns, nullability, indexes, prepaid/fleet constraints, sender metadata, timestamps and append-only audit triggers remain. Removed conversion/backfill/history-merge code, including the old chat merge that depended on the individual-user unique key. Other repositories' existing SQL history remains until reviewed in its owning phase. Flyway still bootstraps and validates schemas; do not remove it or its required initial resource files.

- Actual full fresh service schemas created successfully in isolated PostgreSQL17/PostGIS3.5 unit databases; Identity and Restaurant baseline+scenario dummy seeds loaded successfully; zero naive timestamp columns in all four databases.
- Thirteen direct SQL guards passed for chat entity/nullable restaurant participants, dual-role users, one chat per order, required sender entity, nullable pre-account audit, required device users, required/unique brand organisation, outlet timezone/canonical city and prepaid/dispatch scope.
- PostgreSQL unit guards: OrganisationPostgresSchemaIT1/0/0/0; RestaurantOrganisationSchemaIT1/0/0/0. The Identity test executes the two actual Flyway resources, repeat runs zero, validates and checks OWNER/invitation/audit constraints.
- Clean CommonLibrary251/0/0/0, Restaurant91/0/0/0, Chat73/0/0/0, Notification43/0/0/0. Identity clean99/0/0/3; three conditional rate-limit unit tests were run separately with auth.limits.enabled=true and passed3/0/0/0. The original three skips remain reported separately.
- Static O1:20PASS/0FAIL/0STALE; O2:13/0/0. Validator self-test27 positive controls and2 negative ownership controls passed. Static seed validation14 organisations/554 identities/504 customers/1003 addresses/34 riders/13 brands/104 outlets/504 dishes.

First Identity integration invocation was non-clean and errored on an old SQL resource still copied in target/classes. Clean rebuild removed it and passed. Preserve this failed invocation as evidence; never use Flyway repair, checksum suppression or history deletion to conceal a packaging mistake. The disposable container bp-fresh-schema-unit was stopped and removed after SQL checks.

Evidence: [schema and seeds](../evidence/38-fresh-schema-sql.json), [constraint SQL/results](../evidence/38-fresh-schema-constraints.json), [clean suites, first failure and current/retired resource inventories](../evidence/38-fresh-schema-verification.json). Prior O2 UI/mutation/consumer reports are in [checkpoint37](37-business-platform-o2-local.md), not fresh whole-suite claims from this batch.

## Release order and safety preconditions

1. Finish O2 source and remaining local gates, starting with the pending safe brand-list DTO (ORG_VIEW must not disclose bank/PAN/GSTIN identifiers). Complete consumer clean/contracts, actual -Pspecs generation, UI generated types/typecheck/lint/full tests/build and the existing validation baselines.
2. Review scoped changes, commit/push only task changes; publish CommonLibrary and changed producer stubs through the unchanged workflows. Rebuild dependent service jars from **clean** source and publish images/UI before deploying. Because shared schema content changed, publish all affected shared-library consumer images, not only the five direct schema repositories. Verify registry image tags and recorded Deployment env tags. Preserve unrelated DEPLOY_LOG.md.
3. Use the authorised unchanged Deployment/OracleDeployment/03_clean_deploy.sh **--wipe** for a coordinated full Dev recreation. Default volume-preserving deployment or one-service deployment would encounter changed checksums/schema history. Do not deploy these consolidated baselines to existing Oracle volumes. The existing clean-deploy script publishes the checksummed Config Server bundle before application consumers start; do not duplicate its config deployment.
4. Load current fresh dummy seeds using the existing workflow after schema startup; verify service logs, health/restart counts and reconciliation, then public-Oracle O2 staff access/revocation/internal403, strict lifecycle, chat/history/earnings regressions, exact org/owner/outbox SQL and required latency/breaker measurements.

## Remaining platform work

O1 foundation was deployed/live-tested in checkpoint36; its unused internal membership latency/breaker gate carries to O2. O2 has substantial local implementation but is **not complete**, published or deployed. O3–O5 partner applications/admin approvals, one login/entitlements/admin step-up and launcher/onboarding are unstarted. W1+A1 organisation wallet/ad account, W2 top-up/spend, W3 wallet UI and A2–A4 promoted outlet serving, creatives/moderation/activation and Ads Manager portal remain unstarted. Existing Core validation findings and readiness findings still need resolution, and complete deployed regressions/production readiness remain open.

Money as-is audit semantics are inverted: a PASS means an original defect is present. Its0/23 result is not23 current failures; old moved source paths also mean it cannot replace current positive validators or deployed financial proof. Do not blindly invert/green the historical audit.
