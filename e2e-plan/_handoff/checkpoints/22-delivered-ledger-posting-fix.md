# Checkpoint22: resume passes; delivered earnings were never postable on PostgreSQL

Updated 2026-10-02T21:00:00+05:30. The user deployed checkpoint21 (customer 8b10f5a, delivery 8429333, UI a7b204f; healthy, matching HEAD) and asked to resume the order and continue. The user also asked about SSE: per [the quick-tunnel note](../issue-notes/quick-tunnel-sse-validation-2026-10-02.md), Cloudflare Quick Tunnels exclude SSE (zero chunks on the public URL, working at the origin). SSE stays parked.

## Runs on retained order 7f7af6a5 (no new order created)

1. Resume (`-Dresume.order.id`). It got past the rider's active trip (the checkpoint21 fix holds on resume), then **failed** at HappyDeliveryFlowTest:265 waiting for "Order 7f7af6a5 is now ready for pickup!". Source: that toast comes **only** from the restaurant-status SSE stream (`useDeliveryOrders.ts:669`). The resume path asserted it without the `SSE_ENABLED` guard used elsewhere. **Test fix**: assert the toast only when `e2e.sse.enabled`; otherwise reload the rider and wait for the server-backed active order, as the main path does.
2. Resume rerun: **PASS**, 1/1, 0 skip, 68s. Covered pickup OTP, delivery OTP, DELIVERED, the rider's completed trip and payout, the rider's earnings increase, and the customer's receipt. It does not cover admin money or the quote.
3. Delivered follow-up (`-Dresume.delivered.order.id`, strict money plus quote): **FAIL** at OrderMoneyChecks:55, "delivered order must post RESTAURANT_PAYABLE earnings".

## Root cause (databases, logs, source, and a real local PostgreSQL)

At delivery the customer service published LEDGER_TRANSACTION_REQUEST `a7862d1a…` (tx `d1d5d411…`, 8 legs). The ledger rejected it: `duplicate key … ledger_entries_transaction_id_account_id_direction_key`. V1 has `UNIQUE(transaction_id, account_id, direction)`, which allows one debit and one credit per account per transaction. The delivered distribution is a compound entry: PLATFORM_CLEARING is debited 4×, TAX_PAYABLE credited 4×, and the payables are touched several times. **No delivered order could ever post its earnings on PostgreSQL**; the "posted payables absent" seen before the reset had the same cause. The ledger's local tests never ran `record()` against a database: it uses PostgreSQL-only SQL (`ON CONFLICT`, the numeric cast in `updateBalance`, row locks) that H2 can't execute, and existing tests mock the repositories.

## Fix (LedgerService, local, uncommitted)

- Migration `V20261002210000__ledger_entries_unique_per_leg.sql`: adds `leg_index`, backfills existing rows (per transaction and direction, in posting order), sets it NOT NULL, drops the account-level constraint, and adds `UNIQUE(transaction_id, leg_index, direction)`. That keeps the database backstop against posting a transaction twice and admits compound entries.
- `LedgerEntry.legIndex` plus the matching entity constraint; `DoubleEntryLedgerService.record` writes each leg's debit and credit under its index.
- Tests: `DeliveredDistributionPostingTest` (the exact rejected 8-leg command: 16 entries, unique leg/direction, ₹50.05 balanced, restaurant net 9.49, rider net 18.86, idempotent re-record) and `LedgerEntryLegUniquenessTest` (H2: an account may be debited once per leg; the same leg twice is refused). Two existing builders got `.legIndex(0)`.
- The regenerated ledger `openapi.json` and UI ledger types (`legIndex` on the exposed entry; the rest is property reordering).

## Verification

- **Real PostgreSQL 18 (throwaway local cluster in scratchpad, stopped afterwards):** applied V1→V20261001 and seeded old-shape rows. The 8-leg shape reproduced the production error. Applied the new migration: backfill CREDIT:0/DEBIT:0, NOT NULL, constraint swapped. The 8-leg shape then posted 16 rows, 50.05/50.05. A duplicate leg 0 DEBIT was refused. (Production runs PostgreSQL 16; the migration uses only features that 16 supports.) My first attempt left one stray row from test sequencing; it was cleared and the proof rerun.
- Ledger full `clean test`: 219 tests in 40 classes, 0 fail/error/skip. Guards seen red: the old constraint (uniqueness test), no per-leg index (posting test), the entity column without its migration (`LedgerSchemaConsistencyTest`).
- UI after regeneration: typecheck and lint pass, 737/121. Money audit 0/23. Readiness unchanged (4.1 and 5.4 pre-existing).
- Observation, not fixed: the ledger `OpenApiGenerationTest` passes when run explicitly but did not run in the default `mvn clean test` (no Surefire report). Investigate separately.

## Pending

1. The user deploys **ledger-service** (includes the migration) and the UI optionally (types only).
2. Post 7f7af6a5's rejected distribution through the existing admin DLT replay: `{"dltTopic":"ledger-events-dlt","partition":0,"offset":0}`, confirmed in logs as eventId a7862d1a. This is a financial mutation on an owned order; **needs explicit user approval**. Then rerun the delivered follow-up (strict money plus quote) on 7f7af6a5.
3. Run one fresh full happy lifecycle to prove the accept→active gap and the delivered posting live.

## Update 2026-10-02T21:20:00+05:30: deployed, replayed (user-approved), verified

- ledger-service 1e256ef is running and healthy, matching HEAD. Flyway 20261002210000 success; constraint `ledger_entries_transaction_leg_direction_key`; all 10 existing entries backfilled (0 NULL leg_index).
- **Replay (approved by the user):** admin 1000000001 signed in through the normal Dev Autofill UI in the browser pane, then one `POST /api/v1/internal/admin/orders/dlq/retry` with `{"dltTopic":"ledger-events-dlt","partition":0,"offset":0}` returned 200 and replayed eventId a7862d1a (key d1d5d411, LEDGER_TRANSACTION_REQUEST) to `ledger-events`. Before replaying I confirmed the ledger held only a `dlt_event:` key, not `processed_event:`, so it would not be dropped as a duplicate.
- **Ledger result (read-only DB):** 16 entries, leg_index 0–7 with one debit and one credit each, debits = credits = 50.05; RESTAURANT_PAYABLE 9.49, DRIVER_PAYABLE 18.86. The original rejection row stays unresolved as the audit record; resolving it is a separate admin decision that was not requested.
- **Delivered follow-up** (`-Dresume.delivered.order.id=7f7af6a5…`, strict money plus quote): **PASS** 1/1, 0 skip, 36s; `newOrderCreated=false, quotePassed=true, moneyChecked=true` ([evidence](../evidence/22-7f7af6a5-retained-checks.json)). Admin money: restaurant 17.05 − 7.56 = 9.49; rider 23.00 − 4.14 = 18.86.
- A fresh full happy lifecycle (no resume) is running to prove the accept→active gap and the delivered posting live.

## Update 2026-10-02T21:35:00+05:30: fresh full lifecycle PASS

`HappyDeliveryFlowTest#completeOrderLifecycle` with no resume: **PASS** 1/1, 0 skip, 352.8s, order c463191b. The order is DELIVERED and the assignment RELEASED. The delivered distribution posted **directly** (no rejection, no replay): debits = credits = 93.40; restaurant net 9.49, rider net 18.86. The rider is OFFLINE. This proves checkpoint21 (accept→active) and checkpoint22 (ledger per-leg key) live. Evidence: [22-fresh-happy-lifecycle.json](../evidence/22-fresh-happy-lifecycle.json).
