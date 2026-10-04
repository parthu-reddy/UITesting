# O3 first deployed UI gate and current repair

Recorded 2026-10-04T09:37:14.676791+05:30.

All seven images succeeded in GitHub Actions before the authorised clean Dev wipe/deployment; four producer-stub and consumer-contract publications passed; CommonLibrary published. The initial Oracle bootstrap and fresh seed passed, and hardening is 15/15. Operational image/health audit is separate from E2E: 29 containers running, 26 configured checks healthy, three without checks, zero drift/restarts/current errors. Retain four resolved infrastructure startup events.

The first four public UI methods all failed (4 tests / 2 failures / 2 errors / 0 skips). Restaurant and admin setup sent a literal organisation-ID placeholder due to the generated `get` alias. Rider selectors missed required asterisks. The second seeded restaurant login reused an authenticated browser session. No test cleanup/reseed was performed.

Local repair: generated `organisation.list` with full pagination; actual SDK transport regression; precise required-field-aware page objects; fresh empty browser context per seeded restaurant, with all business actions through visible UI. Focused Vitest 19/19; E2E 180-source compilation passed. UI typecheck/lint/build, required source commits/pushes, UI GitHub image publication and Oracle UI-only rollout are next, followed by the four-class real UI rerun and passive measurements. Do not check off live proof based on these local results.

Evidence copied here: [deployment proof](../evidence/50-o3-oracle-clean-deploy-seed.json), [first UI failure](../evidence/51-o3-first-deployed-ui-run.json). Canonical publication manifest and full phase validation remain in `RandomDocuments/BusinessPlatform_2026-10-03/01_Organisations/Phase3_PartnerApplications/evidence/`. The failed reports/screenshots are retained in `UITesting/target/business-platform/o3/first-deployed-run`; bounded failure evidence survives in this plan.

Authorization: current Business Platform task permits required commits/pushes/publication/Oracle Dev deployment and dummy clean wipe/seed, superseding older expired E2E authorization. No local Docker; images only GitHub builds. No E2E API/database/Redis/SSH or browser storage setup. Owner will handle the eleven legacy 300-line files later; global UI Phase 4 stays 11/12. No user action is currently required.
