# Checkpoint75 — local O4/O5 UI and harness

## 2026-10-04T18:58:39+05:30 — checkpoint75: local O4/O5 contracts and UI; release remains pending

Oracle still runs published UI d79c33ae644b2924112332a6bba48e0c143272da. Earlier checkpoint71 closes the reopened legacy search and eleven old over-300-line components with deployed UI proof. O4/O5 are local, unpublished and undeployed; no fresh wipe, seed or live O4/O5 E2E has occurred. W1–W3 and A1–A4 remain pending.

Current stable service results: Common311/311; Identity clean+specs invocation4 158/158; Gateway clean invocation5 53/53; Customer clean+specs invocation3 491/491; Reviews clean+specs invocation2 101/101. Zero failures/errors/skips in these invocations. Current Identity wire tests verify omitted optional null reasons/names; invitation responses now carry organisation names, fetched in one batch for the person's invitation list. UI clients have been regenerated from completed current specs.

O5 local source now includes the shared launcher, complete portal choices, Business Hub, organisation/member/invitation controls, restaurant application route and delivery onboarding route. Restaurant registration moved out of settings; outlet selection spans organisations; management controls follow membership. CSP/security headers are packaged in an nginx include at server, SPA and asset locations. They are not deployed: actual portal origin and console verification remains mandatory.

Focused UI invocation1:34passed/3failed; invocation2:35passed/2failed; invocation3:55/55passed, no failures/skips. The failures exposed a real late navigation after closing the launcher during refresh and a fast-reopen request gap; both are fixed and covered. Final full UI, typecheck/lint/build, redesign and static/break gates remain pending. Initial redesign Phase4 found content glass in App, two default-size rider buttons and failed toolchain checks. Fix source; do not change the gate or exemptions. The earlier 882/882 UI total predates the O5 source and is historical local evidence only.

The person-login harness caller migration and UI-only auth/session rewrites compile (invocation2 BUILD SUCCESS); invocation1 failed one obsolete selectRole call, now removed. Existing registration runner still uses SSH/database allocation/audit and must not run until rewritten. New O4/O5 page objects/live tests, all moved-onboarding locator updates, explicit fresh-person profile checks and final harness compilation remain pending. No direct-state E2E is permitted.

Continuation: finish UI safety tests and visual fixes; replace the registration runner with retained local candidate manifests and normal UI fresh-person verification; finish O4/O5 browser gates and locator audit; run final stable local checks; publish Common and all required images through unchanged GitHub workflows; then authorised full Oracle Dev wipe/fresh deterministic seed and UI-only O4/O5/O3 regressions. Local source is not deployed acceptance. Original failures and retained fixtures remain history.

Evidence: O4/evidence/09-identity-clean-invocation4.json and 17-{gateway,customer,reviews}-clean-invocation*.json; O5/evidence/01-local-ui-invocations.json. Original checkpoint71 release acceptance remains the deployed baseline.
