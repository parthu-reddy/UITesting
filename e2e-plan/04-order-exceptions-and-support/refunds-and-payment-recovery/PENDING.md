# Pending refund and payment recovery work

Read ../../_handoff/CURRENT-STATE.md and NEXT-STEPS.md first. Historical scenario/checkpoint passes do not close these gaps.

- New callback exact-amount guard: 32 selected local payment checks passed; payment-gateway deployment confirmed healthy at checkpoint 17 (../../_handoff/CURRENT-STATE.md). No reset/migration/config needed.
- The three retained order/refund IDs and manifests are in the handoff. Cancellation and rejection UI outcomes passed, but each original-method refund is FAILED with no completion. Do not create replacements or duplicate the canonical happy delivery.
- Exact-ID atomic admin retry is implemented locally and awaits deployment; verify it before use. The separate automatic sweep still has destination/race/enqueue-error concerns. Respect destination and remaining cap. Resolve initiation-vs-callback state safely; held gateway idempotency keys cannot simply be deleted.
- Checkpoint19 implements exact ADMIN-only capture restoration from verified stored PROCESSED confirmation evidence;58local checks passed. Payment-gateway deployment/live original-ID restoration is pending. This does not automatically recover held refund requests or repair delivered ledger/status.
- Delivered fixture needs audited delivery/ledger correction and separate accidental-refund disposition. Do not retry that accidental refund or use raw status edits as product recovery. Original deterministic ledger transaction identity/economics must be preserved.
- Once eligibility is valid, prove real quote roundtrip, intentional owned support approval/partial/denial, and separate customer/refund/payment/ledger completion. Current reduced-amount override is an API capability; admin UI has no editable amount control.
- Customer refund read-error/retry/open-view refresh is fixed locally with fake-clock tests and awaits UI deployment; deployed UI proof remains open.
- New callback guard rejects inconsistencies; it does not prove a real provider did not move money. Production reconciliation/idempotency requires source/provider evidence, separate from Dev mock proof.

Natural retry/expiry/grace/reset waits and rate-limit exhaustion are excluded. See this folder's DEFERRED-WAIT-TESTS.md and DEFERRED-RATE-LIMIT-TESTS.md. Skipped cases are not passes.
