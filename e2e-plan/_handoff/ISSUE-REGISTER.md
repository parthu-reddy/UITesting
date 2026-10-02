# Issue and fix register

Detailed preserved checkpoints and issue-notes accompany this summary. Status means the precise proof stated, not full product sign-off.

| Issue | Fix/status | Next proof |
|---|---|---|
| Dev random login blocked; admin provisioning/wrong portal | Non-admin valid 10 digits allowed; only admin allowlist; portal roles remain authoritative. Seed/admin and migration/seed readiness corrected historically | Current seeded roles/login prerequisite; no need duplicate successful registration |
| Short +91 phone accepted / session collision modal absent | UI exact 10-digit validation and Axios error contract fixed/deployed; focused role/session checks passed | Retained-state security followups remain |
| Catalog failure rendered blank menu | Strict menu fetch/error/retry vs genuine empty; deployed routed outage/empty passes | Real outage proof separate |
| Checkout unavailable-item and wallet stale balance | Recovery and refresh fixed/deployed; fast checkout checks passed | True transactional rollback/double-submit/security followups assigned |
| Driver money reconciliation / WebSocket readiness / stream pool | Driver net payout guards and delivery stream/JPA transaction lifetime fixes deployed; existing flow/origin checks passed | SSE parked; telemetry accepted-event binding remains |
| Rider item details added without role need | User withdrew requirement; assertions/plan removed, no backend item exposure | Do not reinstate; earlier dialog deployment remains historical |
| Admin order-money HTTP 500/lazy items | Bounded repository graph with remote ledger outside readtxn; deployed200verified | Real posted settlement missing |
| Chat initialization loading race / image URL contract | Separate history effect/merge, correct data.url, stable selectors; local/routed repairs | Full owned chat aggregate/isolation/history/media still not all proved |
| Chat outbox publisher absent / Kafka envelope mismatch | Boot scan filters restored; actual publisherprocessed; String+headers/key listener contracts fixed/deployed | Real quote blocked by corrupted fixture |
| Dev refund callback amount / absent success flags / partial result event | Normalized per-operation rupees; explicit isSuccess; supported PAYMENT_REFUNDED; customer locked completionstatus fixed/deployed | Old initiation keys/stuckrefund recovery not repaired by restart |
| Delivered distributions double-debited customer and unordered deductions | Clearing funding/order fix deployed; local51ledgerchecks passed | Original rejected payload needs rebuilt audited movement; do not blind replay |
| PostgreSQL jsonb rejection insert / DLT retry storm | JSON binding, stable terminal recorder, invalid-JSON envelope/bounded logs; PostgreSQL row now exists | Actual ledgerbook/recovery still pending |
| Missed LedgerDltRejectionTest dependency | Another agent adjusted mock; 16 direct tests passed after user flagged omission | Always dependency-search/run existing tests, not only new ones |
| Completed HANDED_OVER swept after 2 hours | Exclude delivery outcome/timestamp, locked freshness recheck and late/duplicate handlers; latest deployment healthy | Doesn't undo corrupted order/accidental refund |
| Refund command treated as capture | Explicit event-type boundary, real binder contract; latest deployment healthy | Observe actual recovered flow, do not use fake PAYMENT_SUCCESS fixtures |
| Capture SUCCESS without Transaction | Confirmed capture audit transaction atomically persisted; duplicates preserve refund state; latest deployment healthy | Checkpoint19 stored-confirmation recovery locally validated; payment-gateway deployment/live restoration pending; refund/ledger actual completion separate |
| Excessive callback amount clamped to completion | Exact-amount rejection implemented; existing committed fixture extended; 32 affected local checks passed | Payment-gateway deployment confirmed; mismatched provider result still needs reconciliation |
| Admin failedrefund retry and gateway initiation key | Selected exact-ID queueing repaired locally in checkpoint18; global sweeper and heldrefund_req downstream recovery remain open | Implement exact identity safe recovery and prove no unrelated refund/multiple provider action |
| Terminal delivery assignment and telemetry batch binding | Source/runtime followups preserved in issue-notes | Immediate/fakeclock local proof and owned integration; no wallclock2hourtest |
| Refund UI read failure→[] and no open view refresh | Local error/retry/refresh repaired in checkpoint18; deployment pending | Validate owned routed errors/refresh, then actual completed-state proof |
| Feign duplicate specification under overridesfalse | Separate diagnostic; configurednormal startup passes | Source fix/review if prioritized; do not change deployment defaults silently |

The historical 103 failed cases and 40 backend issue docs remain indexed in inventory. This table is not a closure report for all of them. For any newly encountered issue, update CommonMistakesDocumentation and applicable CodingPracticesAcrossAllServices as the user requested, and copy/link its concise handoff here.


Checkpoint18 updates: selected-refund admin retry now implemented locally with atomic exact-ID enqueue and remaining/destination checks; customer refund UI read errors/retry/refresh locally repaired; long-chat newest window/stable ordering locally repaired. 120customer/22chat/30UI final passes; customer/chat/UI deployment pending. Gateway held initiation key/capture history and automatic sweeper concerns remain open. No live recovery/signoff inferred.

Checkpoint20: the recovery endpoint was removed (user decision). Admin refund retry now works end to end, locally: the gateway releases `refund_req` on definitive failure, and the customer service has a separate `sweep_attempts` budget, routes the sweeper by destination (fixing the store-credit-to-gateway bug) and uses one enqueue helper. Refund UI polling stops once refunds settle; `usePolling` duplicate-chain bug fixed; chat OpenAPI test unbroken. Open: a lost provider callback keeps the key held (needs reconciliation); Razorpay real callback id mismatch; chat `size` unbounded. Deployment and reset pending. See checkpoint20.


Checkpoint21: after the reset, cancellation and rejection pass end to end on fresh orders (refund COMPLETED, payment REFUNDED, ledger balanced). New issue: the rider's own trip vanished right after accept, because `/orders/active` read only the customer service's copy, which trails DRIVER_ASSIGNED. Fixed locally in the delivery service (sends held assignments, overlays ASSIGNED) and the customer service (includes unassigned confirmed ids, never another rider's). Also unbroken: delivery OpenApiGenerationTest (scannable test configuration). Deployment pending.

Checkpoint22: delivered orders never posted restaurant/rider earnings on PostgreSQL. The ledger UNIQUE(transaction_id, account_id, direction) can't hold a compound distribution (clearing debited 4×). The fix is a per-leg unique key plus migration (local, deploy pending). The resume-path test asserted an SSE-only toast without the SSE guard; fixed.

Checkpoint23: item-level refunds (every admin approval of a chat support ticket) failed on refund_items.amount NOT NULL, a column no entity wrote and no code read. Fixed with a drop-column migration (deploy pending). Also: the full-order customer support UI (CustomerOrderHistory/PostDeliverySupportModal, shared RefundModal) and its /refund-request endpoint are unreachable dead code; decision pending.
