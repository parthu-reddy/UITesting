# 127 — Checkpoint-125 fixes verified live; GPS artifact explained; one banner fix pending deploy

2026-10-07T14:15+05:30. Item3 is still active, and M1/A5b remain paused.

## Deployment and data

The first deploy ran from a stale Deployment checkout ([126 evidence](../evidence/126-deploy-stale-pins.json)).
The redeploy at 08:04Z served UI **cfb83e2** together with customer, delivery and maps. The served
chunks contain all the new strings, and the map places known pins before calling geolocation
([served evidence](../evidence/127-postdeploy-served-and-wipe.json)).

The owner then did a **fresh install that wiped all data**, so 6fbe0289, ce254f3a and ticket 47ffbccd
are gone and their fixtures are marked `wipedAt`. The retained check on ce254f3a failed with an empty
History (200). That is expected after the wipe and is not a product defect.

## Post-deploy invocations (counted separately)

| # | Result | Order | What happened |
|---|---|---|---|
| 1 | 1 failure, 9.1s | none | First login got a non-JSON auth response. The gateway answered JSON shortly after, which fits services still starting; not proven. [evidence](../evidence/127-postdeploy-lifecycle-invocation1.json) |
| 2 | 1 failure, 269.5s | d3e0ebed | Courier pin missing at out-for-delivery. New diagnostics: page visible and focused, permission granted, restaurant lookup 200 in 1.5s, **getCurrentPosition timed out (code 3) after 15s**, and customer + restaurant pins drawn (the deployed fix working). [evidence](../evidence/127-postdeploy-lifecycle-invocation2.json) |
| resume d3e0ebed | **1/1 pass**, 53.5s | d3e0ebed | Delivered. "Slide to deliver" has no fee credit, and trip details net payout equals the row. Its map PNG was blank (pins not yet painted), which led to the geometry wait. [evidence](../evidence/127-postdeploy-resume-d3e0ebed.json) |
| 3 | 1 error, 61.8s | f01c1e92 | My new geometry wait failed at the assigned stage. The capture reloaded at desktop width and then shrank to 390px while the map loaded, so all pins sat at x = 612–871 in a 333px map. Fixed by setting the viewport first. [evidence](../evidence/127-postdeploy-lifecycle-invocation3.json) |
| resume f01c1e92 | **1/1 pass**, 57.3s | f01c1e92 | Assigned and out-for-delivery captures with all three pins drawn **inside** the map and visible in the PNGs. Real tiles show ₹21.16 / 1 order. Delivered. [evidence](../evidence/127-postdeploy-resume-f01c1e92.json) |
| retained f01c1e92 | **1/1 pass**, 30.0s | f01c1e92 | CARD/SUCCESS ₹43.02; rider net ₹21.16 (gross ₹25.80 − ₹4.64); restaurant ₹6.54; **18 lines balancing ₹92.72**; 0 refunds; trip details net payout equals the row; quote only. [evidence](../evidence/127-retained-money-f01c1e92.json) |

## The missing courier pin, explained

A throwaway read-only probe ran against the active d3e0ebed and was deleted afterwards
([evidence](../evidence/127-geolocation-probe.json)):

- **Reload is not the trigger.** Position came back in 0–1ms after login, after reload and after navigate.
- **A 70s-old emulated fix is the trigger.** After waiting 70s and reloading, the call timed out
  (code 3) and only the customer and restaurant pins appeared.
- **Re-applying the same position fixes it.** The fix was 1s old, the call answered instantly and
  the courier pin appeared.

Playwright's emulated GPS is one static, ageing fix. The map's `maximumAge: 60000` rejects it once it
is older than 60s, and the emulator never produces another fix. **This is a test-environment
artifact, not a product defect.**

`RiderVisualAudit.capture` now does the following:
1. Sets the 390×844 viewport.
2. Re-applies the rider's current emulated position, using LiveCustomerMap's moved point when present.
3. Reloads the page.
4. Waits until the courier pin's centre and part of each directions popup are inside the visible map.

The resume path now captures the assigned stage when resuming a pre-pickup order. The shared
`assertTripDetailsPayout` runs on the main, resume and retained paths.

## Checkpoint-125 fixes: live dispositions

| Fix | Live result |
|---|---|
| False ₹0.00 | **Verified.** Tiles show real ₹21.16 / 1 order, and the true zero after the wipe. The earnings-increase check now has a real baseline. |
| Pins without GPS | **Verified.** Customer and restaurant pins are drawn while GPS times out (invocation 2 diagnostics). |
| Slider credit | **Verified.** "Slide to deliver" has no amount. |
| History details net payout | **Verified.** "Your net payout" equals the trip row, ₹21.16. |

## New, fixed locally, not deployed

The dispatch banner said "Connection lost. Reconnecting to dispatch…" on **every page load** until
the first auth probe and WebSocket opened. It now says "Connecting to dispatch…" until a working
connection has dropped. Its new tests failed 4/5 on the deployed code. UI 179 files / 1,065 tests,
typecheck, lint, build, Phase4 12/12 and the locator audit pass. The SeededRiderDuty preflight also
rejects the new copy.
[Gates](../evidence/127-ui-connection-banner-local-gates.json), [deploy handoff](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ACTIVE-DEPLOYMENT.md).

## Next

1. The owner deploys FoodDeliveryAppUI (5 files). Run `git -C Deployment pull` before `deploy.sh`.
2. After it is live: a read-only rider login checks that the banner never reads "Connection lost"
   on load. No order is needed.
3. Close item3 with these dispositions, then continue to M1 and A5b.
