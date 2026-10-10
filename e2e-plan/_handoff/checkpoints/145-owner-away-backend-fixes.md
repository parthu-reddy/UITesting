# Checkpoint 145 — owner away: gateway live, three CustomerApplication fixes built (deploy pending)

2026-10-10T11:58+05:30. Owner away 11:15–14:15 IST. Full log + decisions: `RandomDocuments/OwnerAway_2026-10-10/README.md`.

- api-gateway 6a0ec23 deployed 05:46:20Z: DEPLOY_LOG == pin == HEAD == running image (healthy). Live: an anonymous
  `GET /api/v1/users/profile` logged `GlobalJwtAuthFilter REJECTED: … reason=no-token`.
- CustomerApplication (local, uncommitted; 567/0): paged order reads page in SQL (PaginatedCollectionFetch_2026-10-10),
  customer refunds in one query, page-size bounds (PageSizeBounds_2026-10-10). No schema change, no wipe.
- UITesting: TestBase admin teardown line rewritten to the form AdminRunSpeed P2.2 checks (13/13); still uncommitted.
- No E2E run since checkpoint 144's queues test. Nothing running.
