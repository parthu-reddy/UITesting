# Checkpoint 140 — admin sign-in once per class is live; admins 003/004 await the owner reseed

2026-10-09T17:35+05:30. Nothing running. No Dev rows created (admin read-only classes only).

## Deployed and verified

identity-service 436fef2 and food-delivery-app-ui f27091a (DEPLOY_LOG 10:05Z / 10:06Z == pins == repo HEADs, clean).

## Live run 15:38–15:40 (evidence/admin-class-session-2026-10-09)

| Class | Result | Admin step-ups |
|---|---|---|
| canaries ×3 (PageReload, RestaurantUi, RiderOnboarding) | 3/3 | 0 |
| AdminUserOpsTest | 1/1 | 1 |
| AdminLiveOpsFleetTest (incl. deployedDriverMarkersUseKnownRiderTones, the fleet-map fix) | 13/13, 42 s | 1 |
| AdminSupportUserReviewTest | 14/14, 57 s | 1 |

One step-up = one `/auth/admin-session/otp` + one `/auth/admin-session` POST, then one class sign-out
`[admin=200, everyday=200]`. Rechecked from the surefire XML on 2026-10-09 17:3x.

## Gates (rerun 17:3x)

- `RandomDocuments/AdminRunSpeed_2026-10-09/tools/validate_admin_speed.py --phase all` 13/13; `--prove` 5/5.
- `validate_fast_e2e.py --phase 4` 4/4.
- `CodingPracticesAcrossAllServices/tools/validate_practices.py` 35/35; `--prove` every mutation red.

## Waiting on the owner

1. Commit the Deployment seed changes (DummyData generator, scenario SQL, accounts JSON, seed validator).
2. `bash Deployment/OracleDeployment/DummyData/run_remote_dummy_data.sh --scenarios-only` (additive, no wipe).

Read-only check at 17:3x: Dev identity_db has ADMIN 1000000001, 1000000002 only; no user with 003/004/005.

## Then (agent)

Read-only check that 003/004 exist with ADMIN; add both to `ADMIN_PHONES` in `tools/run_e2e_batch.py`; rerun
`validate_admin_speed.py --phase 3`. **Never list a phone before Dev has it**: signing in with an unknown phone creates a
plain user, and the reseed's admin collision guard then refuses.
