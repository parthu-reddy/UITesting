# Checkpoint 115 — business wallet after the full deploy — 2026-10-06T15:05+05:30

Deployed and verified (read-only): WalletService e1bbe04, payment-gateway 950ba6d, UI 489e2c8 (all healthy; 29 containers up, 26 healthy + 3 without checks).

- AM-A1-002 `RestaurantCampaignsLiveTest` **PASS 3/3** on the W3 UI.
- BW-W2-001 **RED** (third root cause: Redis `IdempotencyFilter` 409 on a replayed key) — fixed locally.
- BW-W3-001 **RED at the statement** (service sends null; generated schema refuses null) — fixed locally; all earlier steps passed live.

Waiting on the owner: deploy WalletService + UI (no wipe). Then rerun BW-W2-001 and BW-W3-001 with new phones
(business-wallet/PENDING.md). Fixtures retained: top-ups 9a4a94d5… (Brand 1, SUCCESS), 6e8eae2b… (Brand 4, SUCCESS),
16a98c16… (Brand 1, PENDING from the pre-fix run); phones 9999905929, 9999909097, 9999145089; one new Brand 2 DRAFT campaign.
