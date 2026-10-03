# Durable audit status: reviews-and-support

Updated 2026-10-02T17:13:01+05:30. Review: Pending. Implementation/next scope: Pending. Evidence: Not run.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

## Checkpoint32 (2026-10-03T09:47:00+05:30): reviewed and proven live

- Reviews validator (`RandomDocuments/ReviewsIntegration_2026-09-11/tools/validate_reviews_integration.py`) read 81/85 + 1 warning. All five were stale against the 2026-09-28 multi-actor design (ReviewsService 0227db8) or moved files, not regressions: outbox key now includes the author; access policy is private-by-default; the controller uses Spring `Authentication`; the UI refusal type is derived from the generated API; pins moved to `env_deployments/dev`. Checks updated to the current design: **85/85**. Each updated check was seen red on a reintroduced defect (key without author; DRIVER made public; self comparison neutered — which first exposed a weakness, then fixed; X-User-Id read; a refusal message removed), except the warn-level tag check (not mutated: it reads a live deploy pin).
- New `OrderReviewsFlowTest` (REVIEW-01..06) PASS on bb43e2a4; REVIEW-AGG-01, REVIEW-13 and the rider history read PASS against that data. SUPPORT-01..11 retired (their UI was deleted at checkpoint23; support is covered in support-and-refund-queues).
- Not covered: REVIEW-12 (pagination with many reviews).
