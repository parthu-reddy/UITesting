# Seeded environment data and prerequisites

User-confirmed on 2026-09-19 from the deployed development setup:

| Role | Phone | Use |
| --- | --- | --- |
| Customer | random `8000000001`–`8000000500` | Existing seeded customers with saved addresses |
| Restaurant | random `9000000001`–`9000000010` | Existing seeded restaurant partners |
| Rider / delivery executive | random `7000000001`–`7000000030` | Existing seeded, approved riders |
| Admin | `1000000001` | User selected earlier default; no dedicated admin account seeded |

Prefer these existing accounts when creating tests. Do not create replacement accounts merely to simplify setup. Their current login/profile state still needs live validation. The user-confirmed rider number supersedes the earlier choice `5000000001` for planned tests; it is not evidence that the older seed ranges have been removed.

Each test run chooses one account randomly from each seeded range to spread OTP traffic. Override any random selection when reproducing a failure using `-Dcustomer.phone=8000000001 -Drestaurant.phone=9000000001 -Drider.phone=7000000001`. User subsequently confirmed using admin `1000000001` and said any number can be used because no dedicated admin account was created. The authorized admin profile-completion flow has now passed live validation; retain this test profile for reuse.

## Rider duty state and availability

The rider-duty rules in [`RandomDocuments/RiderDutyConsistency_2026-09-27`](../../RandomDocuments/RiderDutyConsistency_2026-09-27/00_master_overview.md) are mandatory setup rules for order tests:

- The backend database status is authoritative. The UI duty label is a projection of that status, while Redis availability, geo, ping and status data are dispatch/liveness projections. Do not infer that a rider is dispatch-ready from an **Online Duty** label alone.
- Treat “ensure rider is online” as an idempotent operation. After login/reconnect, wait for the authoritative `DUTY_STATUS` WebSocket snapshot. If the rider is already `ONLINE`, do **not** toggle them Offline and then Online. The old E2E Offline→Online cycling workaround is obsolete and must not be copied into new setup logic.
- Going online requires a current browser geolocation fix. Grant geolocation and notification permissions before login, use the intended test location, and invoke the UI control once only when the authoritative state is `OFFLINE`. The request must carry the current latitude and longitude; never fabricate coordinates or force the UI state locally.
- Keep the rider browser/context open and its WebSocket and location tracking active throughout checkout and the order lifecycle. Before customer checkout, verify the rider is `ONLINE`, has a current location fix, has no connection/location-loss warning, and is geographically near the selected restaurant.
- If the server sends `DUTY_STATUS OFFLINE LOCATION_LOST`, accept the server state. Restore location tracking and deliberately go online through the UI again; do not force the client back to Online. Transient position errors do not authorize the client to change the duty state, while permission denial can trigger an offline request.
- An `ON_DELIVERY` rider must remain on duty across telemetry or connectivity loss. Do not take a rider offline or log them out during an active delivery. The backend may correctly refuse such an offline request.
- Use a dedicated seeded rider for a state-mutating order flow. Read the rider's current state first. If an already-online rider appears to belong to another active/manual session, choose a different seeded rider instead of commandeering or cycling that session. Restore reversible shared state after the test.

## Order prerequisites

- At least one rider must satisfy the full authoritative duty, live-location and proximity checks above before order placement. Recheck immediately before checkout; an earlier browser session or Online label is not a permanent guarantee.
- Monitor customer, restaurant and rider UIs together through isolated logged-in contexts.
- Rider acceptance becomes available after restaurant acceptance and when preparation is within 15 minutes or complete, per the user's project rule. Verify exact timing boundaries in source when defining dispatch scenarios.
- Reuse the existing **Home** address; do not create a new address for ordinary order tests.
- For **Brand1**, always open the outlet dropdown and choose an outlet under 4–5 km; backend disallows orders above 5 km. Check displayed distances live rather than assuming a particular outlet remains eligible.
- Read current account/outlet state before changing it. Preserve shared seeded data and restore test changes.

The development URL can change. Use the central TestConfig / URL overrides documented in the project README.
