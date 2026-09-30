# support-and-refund-queues

Status: deployed read-only checks passed without failures: support/refund queue 3 tests (2 fixture skips) and support/user/review 13 tests (1 fixture skip). Local browser-routed action and chat coverage compiles; target action validation remains gated on disposable fixtures.

Scope: Administrative support and refund workflows.

Current coverage: support/refund queue requests and empty states, support pagination, cancellation guards, fixture-backed approval/rejection and failure-recovery contracts, plus ticket-isolated moderator text chat and image-upload rendering. The image fixture verifies the upload request, `data.url` response contract, and matching STOMP IMAGE broadcast without creating a target-environment attachment. The local CallOverlay lifecycle repair awaits isolated cross-role browser proof.

See [validation and pending items](PENDING.md).
