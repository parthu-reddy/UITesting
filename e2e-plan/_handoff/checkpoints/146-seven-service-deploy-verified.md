# Checkpoint 146 — seven-service deploy verified

2026-10-10 16:28 IST. Owner: "deployed all seven, go ahead with the checks". Read-only checks; nothing run against
Dev data.

## Image == pin == HEAD (all seven)

Each repo: one commit 15:25 IST "add PageBounds to controllers and fix tests", clean tree, HEAD == origin/main, and
its changed-file count equals checkpoint145's uncommitted count. Pin = `Deployment/env_deployments/dev/<svc>.env` on
origin/main (local Deployment == origin/main). DEPLOY_LOG (local) 2026-10-10T10:25:15Z rows carry the same SHAs.
Running image from `docker compose ps` on the VM.

| Service | Files | SHA (HEAD = pin = DEPLOY_LOG = running) | Container |
|---|---|---|---|
| customer-service | 16 | a7ddf50 | healthy, created 10:22:53Z |
| delivery-service | 7 | 302de8d | healthy, created 10:24:13Z |
| identity-service | 3 | caafeb7 | healthy, created 10:17:16Z |
| ledger-service | 17 | 838424c | healthy, created 10:17:16Z |
| payment-gateway | 3 | 85a9c64 | healthy, created 10:17:16Z |
| wallet-service | 4 | 514b5cd | healthy, created 10:17:16Z |
| communication-integration | 2 | eaa1b50 | healthy, created 10:17:16Z |

Note: the VM's own `Deployment` checkout is at 2d12b72 (2026-09-26) and its `env_deployments/dev` pins are stale.
It does not affect what runs (images above are correct), but don't read pins from the VM checkout.

## Logs since the containers started (to 10:57Z)

- customer-service `applying in memory`: **0**; `HHH90003004`: **0**. The running jar's `application.yml` has
  `fail_on_pagination_over_collection_fetch: true` (read from the container), and no Deployment config overrides it,
  so the old query shape would now throw. ERROR lines: 0.
  What this shows: the scheduler is live (RefundRetrySweeper ran 12× on `scheduling-*` since 10:31Z). The paged
  sweepers (RestaurantTimeoutSweeper, UnpaidOrderCanceller) log only when they find rows, so their runs are inferred
  from the shared scheduler, not observed.
- JSON `"level":"ERROR"` count, all seven: **0**. (A first count with a space-delimited pattern returned 0 for every
  level and was blind; recounted on the JSON field.)
- WARN: dominated by `SecurityContextFilter - Missing headers!` (health checks, already in Suggestions).
- customer-service: 2× Hikari `ProxyLeakTask` at 10:28:38/40Z, thread `main`, stack ends at
  `FoodDeliveryApplication.main`. Both connections returned ("unleaked") 10:28:40.9Z. Startup only.
- All seven containers: RestartCount 0, OOMKilled false, StartedAt 10:26:21Z, the same second. Each logged "Started"
  twice: 10:23 (first start), then 10:31 after a 278 s boot. So the whole stack was stopped and started together after
  the deploy. Consistent with a stack-wide restart and boot contention, which would also explain the startup-thread
  leak warnings. Not proven: I have no record of the restart command.

## Remaining

E2E for the changed features: needs the owner's OK (checkpoint145 step 3).
