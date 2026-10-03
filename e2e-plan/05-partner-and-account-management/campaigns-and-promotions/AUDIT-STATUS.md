# Durable audit status: campaigns-and-promotions

Updated 2026-10-03T10:27:00+05:30. Review: done in source (checkpoint34). Implementation: start step + 10 defects fixed locally, not deployed. Evidence: local only; live test written, awaits deploy.

Read [../../_handoff/START-HERE.md](../../_handoff/START-HERE.md) and [../../_handoff/CURRENT-STATE.md](../../_handoff/CURRENT-STATE.md) first. Current user instructions override older cleanup/publish/item/allowlist text. Historical passes do not prove current retained money fixtures or all feature scenarios. Preserve existing scenarios/PENDING notes and inspect only this feature's relevant source/tests before editing.

Owning portable checkpoint(s): 13,14,15 in _handoff/checkpoints for priority work; other folders still require detailed source audit.

Next: follow [../../_handoff/NEXT-STEPS.md](../../_handoff/NEXT-STEPS.md) and the local scenarios/PENDING mapping. Slow/rate/SSE work is excluded as indexed in [../../_handoff/DEFERRED.md](../../_handoff/DEFERRED.md). Never count skipped/no-op/blocked cases as passes. Do not rerun successful lifecycle coverage merely because it is referenced here.

Every continuing agent must update this feature's results and the central handoff inside this plan folder; see [mandatory agent update instructions](../../AGENTS.md).

Checkpoint33 (2026-10-03T09:55:00+05:30): see [checkpoint33](../../_handoff/checkpoints/33-restaurant-earnings-and-campaigns.md).

## Checkpoint34 (2026-10-03T10:27:00+05:30) — current

Source-reviewed end to end (UI → gateway RBAC → CampaignService → WalletService → ad billing path). Applied the user's "Start advertising" decision and fixed ten defects; table and guards in [checkpoint34](../../_handoff/checkpoints/34-campaigns-onboarding-and-ad-money.md). Scenarios rewritten to the real product in [scenarios.md](scenarios.md).

- Proven locally: vitest 771/123 (campaign files 13 tests), each fix's mutation red; CampaignService 32, WalletService 56, GovernmentIDValidationService 33 clean; `validate_role_names.py` PASS 255.
- Not yet proven live: everything in `RestaurantCampaignsLiveTest` (needs UI + campaign-service + wallet-service deployed). Dev has 0 advertiser profiles; ONBOARD writes one permanently and only with `-Dcampaign.onboard=true`.
- Gap for the user: activation is unreachable (no ad-group/creative UI; creative moderation has no caller), so no campaign serves or spends.
- Next: after deploy, run the commands in [NEXT-STEPS](../../_handoff/NEXT-STEPS.md) (checkpoint34 order).

