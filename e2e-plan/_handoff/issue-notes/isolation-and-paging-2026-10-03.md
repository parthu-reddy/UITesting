# Isolation 500 and page-0-only history (checkpoint27)

Copy of CommonMistakesDocumentation/DataAndState/refund-retry-and-recovery-2026-10-02.md items 22-24 and CodingPracticesAcrossAllServices/10_CommonPitfalls/known-traps.md ("Not found must not be a bare RuntimeException", "A paged endpoint needs a paging client").

- Another customer's order read answered 500 because a bare RuntimeException reached the shared handler; use ResourceNotFoundException (404).
- The chat UI fetched only the newest 50 messages; the rest was unreachable after a reload. Clients of paged APIs must page; servers must bound size.
- Dev payment mocks always succeed refunds, so admin retry has no live path; a failure seam is a user decision.
- The shared handler maps no constraint-violation exceptions (possible 500 for @Max bounds; unverified, separate task).
- Checkpoint28 (item 25): failed-refund list/retry had an endpoint and generated client but no screen; now a Money Operations tab. Dev mocks decline a ₹1.13 refund once (owner-approved seam) so the retry runs live.
