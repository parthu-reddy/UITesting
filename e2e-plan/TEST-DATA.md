# Seeded environment data and prerequisites

User-confirmed on 2026-09-19 from the deployed development setup:

| Role | Phone | Use |
| --- | --- | --- |
| Customer | random `8000000001`–`8000000500` | Existing seeded customers with saved addresses |
| Restaurant | random `9000000001`–`9000000010` | Existing seeded restaurant partners |
| Rider / delivery executive | random `7000000001`–`7000000030` | Existing seeded, approved riders |
| Admin | `1000000001`, `1000000002` | Provisioned Dev test administrators; default remains `1000000001` |

Prefer these existing accounts when creating tests. Do not create replacement accounts merely to simplify setup. Their current login/profile state still needs live validation. The user-confirmed rider number supersedes the earlier choice `5000000001` for planned tests; it is not evidence that the older seed ranges have been removed.

Each test run chooses one account randomly from each seeded range to spread OTP traffic. Override any random selection when reproducing a failure using `-Dcustomer.phone=8000000001 -Drestaurant.phone=9000000001 -Drider.phone=7000000001`. Administrator login requires an active account and an assigned ADMIN role. Arbitrary phone numbers cannot become administrators. The existing `1000000001` UUID and profile were preserved when adding the second administrator.

## Rider duty state and availability

The rider-duty rules in [`RandomDocuments/RiderDutyConsistency_2026-09-27`](../../RandomDocuments/RiderDutyConsistency_2026-09-27/00_master_overview.md) are mandatory setup rules for order tests:

- The backend database status is authoritative. The UI duty label is a projection of that status, while Redis availability, geo, ping and status data are dispatch/liveness projections. Do not infer that a rider is dispatch-ready from an **Online Duty** label alone.
- Treat “ensure rider is online” as an idempotent operation. After login/reconnect, wait for the authoritative `DUTY_STATUS` WebSocket snapshot. If the rider is already `ONLINE`, do **not** toggle them Offline and then Online. The old E2E Offline→Online cycling workaround is obsolete and must not be copied into new setup logic.
- Going online requires a current browser geolocation fix. Grant geolocation and notification permissions before login, use the intended test location, and invoke the UI control once only when the authoritative state is `OFFLINE`. The request must carry the current latitude and longitude; never fabricate coordinates or force the UI state locally.
- Keep the rider browser/context open and its WebSocket and location tracking active throughout checkout and the order lifecycle. Before customer checkout, verify the rider is `ONLINE`, has a current location fix, has no connection/location-loss warning, and is geographically near the selected restaurant.
- If the server sends `DUTY_STATUS OFFLINE LOCATION_LOST`, accept the server state. Restore location tracking and deliberately go online through the UI again; do not force the client back to Online. Transient position errors do not authorize the client to change the duty state, while permission denial can trigger an offline request.
- An `ON_DELIVERY` rider must remain on duty across telemetry or connectivity loss. Do not take a rider offline or log them out during an active delivery. The backend may correctly refuse such an offline request.
- Use a dedicated seeded rider for a state-mutating order flow. Read the rider's current state first. If an already-online rider appears to belong to another active/manual session, choose a different seeded rider instead of commandeering or cycling that session. Retain test data and sessions at the user's request. Set an idle test rider OFFLINE with authoritative confirmation; preserve an active delivery.

## Order prerequisites

- At least one rider must satisfy the full authoritative duty, live-location and proximity checks above before order placement. Recheck immediately before checkout; an earlier browser session or Online label is not a permanent guarantee.
- Monitor customer, restaurant and rider UIs together through isolated logged-in contexts.
- Rider acceptance becomes available after restaurant acceptance and when preparation is within 15 minutes or complete, per the user's project rule. Verify exact timing boundaries in source when defining dispatch scenarios.
- Reuse the existing **Home** address; do not create a new address for ordinary order tests.
- For **Brand1**, always open the outlet dropdown and choose an outlet under 4–5 km; backend disallows orders above 5 km. Check displayed distances live rather than assuming a particular outlet remains eligible.
- Read current account/outlet state before changing it. Preserve shared seeded data. Do not delete, deactivate, revoke or restore server data as automatic teardown; explicit changes remain only when they are the behavior under test.

The development URL can change. Use the central TestConfig / URL overrides documented in the project README.

## Registration and scenario fixtures — 2026-10-01

Baseline random account selection remains at 500 customers, 30 riders and 10 restaurant owners.
The additive scenario pack is separate: customers `8000000501`–`8000000504`, riders
`7000000031`–`7000000034`, and restaurant owners `9000000011`–`9000000014`.
See `Deployment/OracleDeployment/DummyData/scenario_accounts.json` for scenario-to-account mappings.
Do not randomly select these negative-state fixtures for ordinary checkout tests.

Existing `RegistrationUiTest` requires explicit opt-in and unused generated numbers. After
IdentityService, ApiGateway and FoodDeliveryAppUI have been deployed, run:

```bash
python3 scripts/run_registration_e2e.py --app-url '<current Dev URL>'
```

The runner checks Dev, allocates unused `8999` + six-digit customer, `7999` + six-digit rider,
and `9999` + six-digit restaurant phones, and invokes existing browser signup coverage.
These pools must never be seeded; they are a test-isolation convention, not an authentication restriction.
With the existing Dev feature flag enabled, customer, rider and restaurant autofill supports any
valid 10-digit number. Only admin autofill is restricted to `1000000001` and `1000000002`.
Production uses ordinary SMS. The parked runner-secret OTP facility
continues to be disabled and does not admit these disposable pools.

The runner retains all created accounts, partner/outlet records and sessions at the user's request.
It performs only read-only allocation/audit queries outside the browser tests and writes manifests
under target/registration with dataPolicy=retain and cleanupPerformed=false, including on failure.
It never deactivates accounts or issues Redis cleanup writes. Browser contexts close normally;
only idle rider duty is automatically set OFFLINE and verified against the server.

New signup grants only the selected customer/partner enrollment role. Existing rider KYC,
biometric and duty gates remain authoritative. Administrator signup is forbidden.
The coordinated deployment was verified on 2026-10-01: all three fresh-account registration
flows and all eight four-role login smoke cases passed without skips. See the login-and-OTP
pending notes for the exact scope and remaining validation.
