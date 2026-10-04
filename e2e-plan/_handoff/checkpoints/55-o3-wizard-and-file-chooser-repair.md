# O3 current wizard and file chooser repair

## 2026-10-04T09:56:18.654776+05:30 — Latest public O3 UI gate and current UX repair

The corrected organisation-list image `770a732` is published/deployed; CI 843/843, Oracle 29/29 running, zero image drift/restarts/current errors, hardening/reconcile passed. Delivery timing configuration applied; Dev overlays match Oracle. The sandboxed launch attempt produced four startup errors and zero UI journeys. The next actual public UI gate was 4 tests / 1 passed / 2 failures / 1 error / 0 skips. Pending/rejected visibility passes. Brand/outlet saves succeed, but a stale hidden-file-input visibility assertion stops restaurant/admin setup. Rider's wrapping label reactivates the custom dropdown after selection and it covers Save.

Current local repair stays inside new O3 files: separate vehicle fieldset/legend and explicit name, fluid-width scrollable rider wizard with compact mobile steps, visible Upload/Replace buttons and real file chooser, plus a real browser collapsed-picker assertion. Rider lifecycle will use a 390 x 844 viewport after normal UI signup. Unit regression before fix fails the accessible name (6 pass / 1 fail); jsdom does not reproduce the browser label reactivation. Latest positive local unit result is 7/7; final current-source checks and new UI publication/Oracle deployment precede rerun. Legacy 300-line cleanup is owner-deferred.

Retained proof: `52-o3-ui-correction-publication-rollout.json`, `53-o3-browser-launch-failure.json`, `54-o3-second-deployed-ui-run.json` in handoff evidence and the canonical Business Platform phase evidence. Private upload, final E2E and measurements are still open; no user input is needed. Earlier dated entries below are history.
