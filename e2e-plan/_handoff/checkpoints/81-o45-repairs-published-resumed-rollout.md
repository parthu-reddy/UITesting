# Runtime repairs published and rollout resumed

## 2026-10-04T20:56:07+05:30 — checkpoint81: both runtime repairs published; rollout resumed

Gateway0b729f09313511d79a5ab33efe041e1f89b40c46 is published by successful GitHub37212794674; clean local58/58 includes GET/HEAD health and refusal of non-health HEAD and health POST. Reviewsd589d2f03e7ee1f94f6e3b81add917b1b3b704cc is published by successful37212449331; local CI-profile97/97 and actual packaged AuditConfiguration/AuditTrail checks pass. Both publication workflows are unchanged. Current Deployment5d0023e pins the two new tags; UI remains published3d8486e and Identity5210c6e.

Clean wipe/deployment invocation1 failed its health timeout: the old Gateway filter refused the existing HEAD probe despite GET health UP; Reviews lacked runtime common-persistence despite passing tests. Wipe completed, no dummy seeds loaded. Four strict-chain services were not started. Evidence28 retains failure and exact repairs. Selected invocation2 now deploys Gateway/Reviews and Customer→Restaurant→Delivery→Maps through the existing ordered deploy script, with no second wipe and no unneeded restart of the other23 containers. Rollout, schema/seed, hardening and UI E2E acceptance remain pending.

UITestingd80fca7 compiles (invocation11); core O4/O5 runner12/12. Required review/admin-money assertions now observe the requests caused by real dialog/page controls. Synthetic chat transport fixtures and older direct aggregate/eligibility methods are explicitly deferred, not passing proof. Prior fixture manifests remain historical after the actual wipe; retain all newly created fixtures as well. Current Oracle public tunnel is https://gulf-strike-dark-extras.trycloudflare.com.

Next: finish selected deployment, all-container logs/native bootstrap/profile/image/hardening/reconcile, load-only deterministic dummy seeds, then retained UI O4/O5 and required compliant regression journeys plus measured server/browser acceptance. The historical pre-O4 Gateway histogram baseline is unavailable; record that limit and no invented comparison. Stop when O4 and O5 are complete; do not start Wallet/Ads.
