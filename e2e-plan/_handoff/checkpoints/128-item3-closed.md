# 128 — Item3 closed; every fix from this run verified live

2026-10-07T14:45+05:30. UI **8c5b15e** is deployed: the pin matches origin, CI 37597411296 succeeded
and the served chunk contains the new copy. A read-only probe with the rider on duty, run through
`SeededRiderDuty` and restored to Offline afterwards, saw only "Connecting to dispatch…" on three
reloads (~0.8–1.1s) and never "Connection lost". [Evidence](../evidence/128-banner-fix-live.json).

## Item3 dispositions

| Item | Disposition | Proof |
|---|---|---|
| Rider canvas / admin sidebar / F12 | Fixed live (5a1e97c) | [122 green](../evidence/122-role-visual-invocation5-green.json) |
| Delivered receipt hidden by stale active cache | Fixed live (6eb4ef6) | [retained receipt](../evidence/123-retained-receipt-invocation1.json) (fixture since wiped) |
| Pickup hint, header chat, uncovered swipes | Fixed live | [f01c1e92 resume](../evidence/127-postdeploy-resume-f01c1e92.json) |
| Populated admin refund detail/confirm/audit | Verified once (ticket 47ffbccd REJECTED, since wiped) | [same-ticket resume](../evidence/123-owned-refund-invocation2-readonly-green.json) |
| False ₹0.00 today tiles | Fixed live (cfb83e2) | [checkpoint127](127-postdeploy-active-rider-verified.md) |
| Known pins waited for GPS | Fixed live (cfb83e2) | [invocation2 diagnostics](../evidence/127-postdeploy-lifecycle-invocation2.json) |
| Customer fee shown as rider credit/earnings | Fixed live (cfb83e2) | [f01c1e92 money](../evidence/127-retained-money-f01c1e92.json) |
| "Connection lost" on every load | Fixed live (8c5b15e) | [128](../evidence/128-banner-fix-live.json) |
| Intermittent missing courier pin | Test artifact (Playwright static GPS fix); harness fixed | [probe](../evidence/127-geolocation-probe.json) |
| Order money after deploy | Green: 18 lines balance ₹92.72 | [f01c1e92 money](../evidence/127-retained-money-f01c1e92.json) |

Still deferred, as recorded before and unchanged: C7/B10/B11/F11 deletion, timing, CSP, SSE, rate,
duration, provider and load. There is one unclaimed observation: resizing the viewport while the map
is loading left the pins outside the view
([invocation3](../evidence/127-postdeploy-lifecycle-invocation3.json)).

Owned Dev fixtures: d3e0ebed and f01c1e92 are DELIVERED and retained. Older fixtures were wiped by
the owner's fresh install.

## Next

Item4 **M1**, then item5 **A5b**, in order. Both are paused and have not been started in this
session. Starting M1 in a fresh session is suggested.
