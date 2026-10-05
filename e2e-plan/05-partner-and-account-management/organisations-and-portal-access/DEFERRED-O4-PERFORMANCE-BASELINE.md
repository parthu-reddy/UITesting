# Deferred historical O4 Gateway latency comparison

Updated 2026-10-04. DEFERRED, not executed, not counted as passing proof.

| ID | Deferred assertion | Why it is deferred | Permitted replacement or unblock |
|---|---|---|---|
| O4-PERF-001 | Gateway latency unchanged compared with before O4. | The previous image did not expose an available protected request histogram; evidence23 records that absence. The old volumes/images have been replaced by the authorised fresh Dev rollout, so a historical observation cannot be recovered or invented. | Record current protected Gateway/Identity p95, bounds and sample counts from real UI traffic, prove the one-Redis-round-trip contract locally, and preserve this measured baseline for a future equivalent workload comparison. Portal p95 <=150ms remains mandatory now. |
