# Organisation and portal access audit status

Updated 2026-10-03T15:50:36+05:30. ORG-01..06 map to OrganisationLifecycleApiTest#organisationLifecycle (scenarios.md).
GatewayApi uses two same-origin browser sessions; registration uses existing LoginPage/Dev Autofill Code.
The scenario retains owned records, without cleanup.

O1 local code/unit/security/transaction/query/rate/generated contracts and real PostgreSQL checks pass.
CommonLibrary and all consumer/gateway clean suites pass with existing skips described in
[checkpoint35](../../_handoff/checkpoints/35-business-platform-o1-local.md). O1 validator20/20;
existing baselines unchanged; E2E test-compile passes. Feign ordering and redundant UTC Clock defects
were caught/fixed. Exact per-class counts and mutations are linked by checkpoint35.

**Live scenario not run; O1 release incomplete.** Checkpoint34 deployment, D15 confirmation, O1 owner
rollout, lifecycle/SQL/outbox and latency/breaker measurements remain open. No live fixtures created.
O2+ not started. UI consistency belongs to O5/W3/A4. Local passes do not prove production readiness.
