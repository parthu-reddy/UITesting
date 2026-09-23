# rider-wallet-earnings-and-history

Status: first UI-only batch implemented; see scenarios and validation notes.

Scope: Rider wallet, earnings and delivery history.

When starting this folder, follow the workflow in the [main plan](../../README.md). Record source/page-object references, prerequisites, scenario IDs, implementation links and validation results here. Add `scenarios.md` only at that point.

See [validation and pending work](PENDING.md).

`RiderUiTest` previously passed both tests live with independently randomized approved riders. The latest read-only `PartnerReadOnlyUiTest` batch passes five focused tests covering today’s earnings formatting, history filtering and empty state, settings dismissal, exact disabled phone, verification statuses, wallet rendering and nonnegative INR balance. No duty state or account data was changed in this batch.
