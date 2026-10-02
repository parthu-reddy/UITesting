# Checkpoint23: rejection resolved; support refunds found an item-refund defect

Updated 2026-10-02T22:15:00+05:30. The user asked to resolve the ledger rejection and continue with support refunds.

## Ledger rejection resolved (user-requested)

Admin 1000000001, through the UI (Money Operations → Ledger Rejections → Resolve, with a note, then the "Resolve movement" confirmation): a7862d1a resolved. The note cites the pre-fix constraint, migration V20261002210000, the replay of ledger-events-dlt p0 o0, and the posted amounts. The UI shows "No unresolved rejections."

## Support refund coverage

- **Source map:** customers can raise a support refund **only** through chat: item quote, then "Submit Refund Request" on the quote card (CHAT_REFUND_REQUESTED → ChatRefundProcessorService creates an OPEN item ticket). `useChatSession` hard-codes `refundType: "PARTIAL"`. **Unreachable code:** `CustomerOrderHistory.tsx` (the "Report Issue / Request Refund" button and `PostDeliverySupportModal` → `POST /api/v1/customer/orders/{id}/refund-request`) and `shared/ui/RefundModal.tsx` (FULL/PARTIAL) are rendered nowhere; `SettingsHistoryTab` already notes this. So customers cannot raise a full-order ticket, and the remaining-balance cap at approval is not reachable from the UI (backend unit tests cover it). **Decision for the user:** delete the dead UI and endpoint, or wire a full-order path into the rendered history.
- **New test:** `tests/flows/SupportRefundResolutionFlowTest` (SUPPORT-REFUND-01 reduced award with restaurant fault, 02 denial, 03 an already-refunded item refused). Scenarios are in `06-admin-operations/support-and-refund-queues/scenarios.md` Batch 5.
- **First live run, before the fix (it found the defect):** 3 tests: 01 error (no refund within 30s), 02 error (my selector targeted the unrendered history button; the test was then rewritten to the real chat path), 03 failure (follows from 01). The chat ticket path itself worked live: ticket 2f5d7acf on c463191b, OPEN, item quote 17.91, `requested_refund_items` set. **It is still OPEN** and will be reused by the rerun.

## Defect: every item-level refund failed on PostgreSQL

Admin approval → `RefundService.request` → insert into `refund_items` → `null value in column "amount" of relation "refund_items"` (23502). The whole resolve rolled back and the ticket stayed OPEN. V1 declares `refund_items.amount DECIMAL(15,2) NOT NULL`, but the `RefundItem` entity never mapped it, and nothing reads it (`sumCompletedQuantity` uses quantity). Tests build the schema from entities in H2, so they never saw it. The schema-consistency check is one-directional (entity columns exist in the migrations), so it missed it too. A heuristic scan of the customer migrations against the entities found this as the only required column with no writer.

**Fix (local, uncommitted):** forward migration `CustomerApplication/.../V20261002220000__drop_unused_refund_item_amount.sql` drops the column. A per-item amount is undefined for a reduced award; the refund's own amount is the money.

**Verification:**
- On real PostgreSQL 18 (local cluster) with the exact V1 `refund_items` definition (FKs stripped; customer V1 needs PostGIS, which isn't installed locally): the RefundItem insert reproduced the production error; after the migration the insert succeeds and the columns are id, refund_id, order_item_id, quantity, created_at.
- Customer full `clean test`: 458 tests in 90 classes, 0 fail/error/skip.

## Next

1. The user deploys **customer-service** (migration V20261002220000). Verify the image, the flyway row, and that the column is dropped.
2. Rerun: `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -q '-Dtest=SupportRefundResolutionFlowTest' -Dapp.url=https://gulf-strike-dark-extras.trycloudflare.com/ -Dcustomer.phone=8000000484 -Drestaurant.phone=9000000001 -Drider.phone=7000000026 -Dadmin.phone=1000000001 -Dsupport.partial.order.id=c463191b-a5ff-4fc5-9805-6a39320b49cc -Dsupport.deny.order.id=7f7af6a5-1d86-4f29-b4cc-36eefe69a74b -DexcludedGroups=slow-auth,auth-rate-limit test`
3. User decisions: the dead full-order support UI and endpoint (delete or wire in); the shared reverse schema guard (CommonLibrary `common-test` `SchemaConsistency`: flag NOT NULL columns without a default that no entity maps; needs a library publish).

## Update 2026-10-02T23:05:00+05:30: user decisions implemented (local, uncommitted)

**Decision 1, delete the dead full-order support path (user chose "Delete them").** Deleted:
- UI: `features/customer-orders/components/CustomerOrderHistory.tsx`, `PostDeliverySupportModal.tsx`, `shared/ui/RefundModal.tsx` and its index export.
- Backend: `CustomerApplication/.../CustomerOrderController.java` (its only endpoint was `POST /api/v1/customer/orders/{id}/refund-request`) and `CustomerOrderControllerTest`.
- UITesting: `PostDeliverySupportModalPage`, `RefundModalPage` (no users).
- Gateway: the `/api/v1/customer/**` route predicate and the `/api/v1/customer` RBAC prefix in `Deployment/api-gateway.yml`, plus the matching entries in `ApiGateway/src/main/resources/application.yml`.
- Three comments citing the deleted component were reworded.

Kept (live): the restaurant refund-requests endpoint and `CustomerOrderHistoryPage` (which targets the rendered `SettingsHistoryTab`). The customer `openapi.json` and UI generated types were regenerated (endpoint gone). A workspace grep finds no remaining references.

**Decision 2, add the reverse schema guard (user chose "Yes").** In `CommonLibrary/common-test` `SchemaConsistency`: `parseRequiredSql` follows migrations in order (CREATE; ALTER ADD/DROP/RENAME COLUMN, SET/DROP NOT NULL/DEFAULT; DROP TABLE; SERIAL/GENERATED; table-level PRIMARY KEY; comma-safe clause splitting). `writtenColumns` covers inherited (@MappedSuperclass) and @Embedded fields and skips insertable=false. `unwrittenRequiredColumns` is folded into `mismatches()`, so every service's existing schema test enforces it with no signature change. 3 new unit tests; SchemaConsistencyTest 5/5.

## Verification

- `common-test` installed to the local ~/.m2 (CommonLibrary had only my two files changed). Customer, Ledger, Payment and Wallet schema tests all pass with the new check. **Guard seen red:** with V20261002220000 moved aside, CustomerSchemaConsistencyTest fails with `refund_items.amount is NOT NULL with no default, but no entity mapping 'refund_items' writes it`; restored → pass.
- Customer full `clean test`: 456 tests in 89 classes, 0 fail/error/skip (458 minus the deleted controller test's 2). ApiGateway `clean test`: 20 in 6 classes, 0 fail. UI typecheck and lint pass, vitest 737/121.
- `FoodDeliveryContracts/validate_core_services.py`: 50/56. The same 6 findings occur on the committed code with all my changes stashed (I-16, I-19c, SCHEMA-STRICT ×3 incl. LedgerTransactionDto, whose required set is identical before and after my ledger regen; SPEC-DRIFT ×4; SCHEMA-IMMUTABLE needs the removed `.versions`; `check_gateway_rate_limits` TypeError). None was introduced here; they need a separate validator-maintenance pass.

## Pending

1. The user deploys **customer-service** (migration + controller removal), **api-gateway** config (route/RBAC removal), and the **UI** (deleted dead components, regenerated types). The user **publishes CommonLibrary** (common-test) for CI.
2. Rerun SupportRefundResolutionFlowTest (command above). Ticket 2f5d7acf (c463191b) is still OPEN and will be the one approved.
