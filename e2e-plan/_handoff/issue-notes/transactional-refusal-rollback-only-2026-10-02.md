# A refusal thrown through a transactional proxy cannot be caught (checkpoint24)

Copy of the canonical notes: CommonMistakesDocumentation/DataAndState/refund-retry-and-recovery-2026-10-02.md items 16-18, CodingPracticesAcrossAllServices/10_CommonPitfalls/known-traps.md ("A refusal thrown through a transactional proxy cannot be caught"), 04_Messaging/consumer-rules.md rule 9, 09_Testing/testing-standards.md ("Transaction semantics need real proxies and real commits").

- When a RuntimeException leaves a `@Transactional` proxy that joined the caller's transaction, Spring marks that transaction rollback-only. Catching it does not help: the caller's commit throws `UnexpectedRollbackException` and its writes roll back.
- Live: a second chat quote for an already-refunded item lost its CHAT_REFUND_ERROR reply and went to chat-events.DLT (p0 o0). The same shape existed in OrderEventConsumer and PaymentEventConsumer for refund routing refusals.
- Fix: decide refusals before the write transaction; when a caller must keep its own writes, use a variant that *returns* pre-write refusals (`RefundService.requestUnlessRefused`). Post-write failures must still throw.
- Tests that mock the TransactionTemplate and the transactional collaborator cannot see this; use a `@DataJpaTest` with real services and `Propagation.NOT_SUPPORTED`, with a control that reproduces the rollback.
- Check the whole set: `tools/scan_rollback_only.py`, then read each hit. Helpers that hide the try/catch need reading.
- Handoff ids can vanish: the deploy that shipped checkpoint23 also reset the Dev data. Check live state before running a recorded command.
