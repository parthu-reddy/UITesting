# O3 UI published; required Dev configuration apply — 2026-10-04T14:04:33.403014+05:30

UI b5ab30e is published through GitHub workflow37188980191,852/852 tests and134/134 files, lint/typecheck/build passed. The exact bot tag is pulled with the existing deployment log preserved. Only the UI image changed and its selected deploy succeeded. The required deploy-ui-only Dev overlay apply is now running; it deliberately restarts18 configuration readers in sequence. No image rebuilds, migrations, wipe/reseed or fixture cleanup. E2E source00cd656 includes visible surrounding-brand approval and a normal fresh customer feed/storefront assertion. Do not run the four-method browser gate until overlays, exact digest, runtime logs/health/hardening/reconcile pass. Last four-method gate remains invocation7:2passed/2errors/0skips.

Published arm64 digest:2e49ad6240d27ba70ca64572294c71fa3c1bd200293c6a733a3ce657f46977a7. Government ID9652625 remains the private document image.

## 2026-10-04T14:05:48.665461+05:30 — Runtime verified; planned reader restarts skipped

Published UI b5ab30e is now verified on Oracle with exact digest2e49ad6240d27ba70ca64572294c71fa3c1bd200293c6a733a3ce657f46977a7, arm64 and healthy, zero startup errors. CI852/852, lint/typecheck/build passed. Required Dev overlay apply found exact matching files and skipped publishing/restarts; only UI has a changed start time. All29running,26healthy checks plus3unconfigured, zero drift/automatic restarts/recent errors; hardening15/15 and reconcile29/0 pass. The four-method public UI rerun is next, with source00cd656 and retained fixtures; last completed gate remains2passed/2errors/0skips. No user action, wipe/reseed, migration or E2E direct-state setup.

[Verified evidence](../evidence/67-o3-ui-handoff-rollout-verified.json).
