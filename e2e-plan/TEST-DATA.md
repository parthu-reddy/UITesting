# Seeded environment data and prerequisites

## 2026-10-04 — Current O4/O5 fresh Dev rollout (seed loaded)

The authorised full Dev wipe has completed; all29 containers run with26 healthy checks/three without checks, no drift/restarts/recent errors. All14 fresh deterministic O4 seed files loaded successfully through the operator workflow; native schema sentinels and cross-service seed validation passed. Current login and business behavior still require live UI proof. All order/application/member manifests dated before this wipe are historical and remain retained. Their IDs, balances/reviews and old preserved-admin-UUID statement below are not current fixtures.

Current UI login is one person followed by a portal choice. Any valid10-digit person may use normal Dev Autofill Code; ADMIN OTP additionally requires an existing active persisted staff account1000000001/1000000002. Roles come from current approvals/membership, not phone ranges. The signup runner allocates local candidates, demands visible fresh profile completion, and makes no allocation DB/Redis/API queries. Retain created records and manifests.

The older role-specific signup/read-only allocation assertions below are historical. Before checkout still prove current Home/outlet distance, category hours, live geolocation/socket and server DUTY_STATUS through observations of the actual rider UI. Never use a DB/Redis or direct API shortcut.


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
- Use a dedicated seeded rider for a state-mutating order flow. Read the rider's current state first. If an already-online rider appears to belong to another active/manual session, choose a different seeded rider instead of commandeering or cycling that session. Retain test data at the user's request. Sessions are NOT retained: since 2026-10-07 (owner) `TestBase` teardown signs every page out (`SessionSignOut`, one `POST /api/v1/auth/logout` per stored token, app origin only), because leftover 30-day sessions filled the 3-per-person limit (admin 1000000001 → 409 on step-up). Set an idle test rider OFFLINE with authoritative confirmation; preserve an active delivery.

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


## 2026-10-03T22:19:36+05:30 — Canonical Dev opening window

Food at Brand1 Outlet3 has an explicit all-day dummy seed window00:00–23:59:59 for the shared canonical order flow. Other category windows remain realistic. This seed change is not yet loaded; prove current configured hours/public orderability, Home distance and authoritative rider readiness before checkout. No application hours bypass.


Updated 2026-10-03T23:15:39+05:30: the additive Brand1Outlet3 Food window was loaded successfully at22:37 IST without a wipe. Actual canonical order e82f8c51 selected the live nearby Brand1Outlet10; its retained manifest is authoritative for downstream regression inputs. Food/category windows and outlet/rider readiness must still be verified at checkout; do not assume the preferred outlet is always selected.

## 2026-10-04T00:40:47+05:30 — O2 final retained state

Canonical e82f8c51-6041-4987-88b9-c3f02ac781a8 remains delivered at Brand1Outlet10/4a187e63-659d-4ba6-925f-52cd278f8bf1 with all six immutable reviews; driver2/4.50, product1/5.00, restaurant public1/4.00. Final read-only earnings19.11/0/19.11 and riderOFFLINE verified. Restaurant driver reviewaa4134fc-837e-4b99-8cba-4efcacbd240c retained. O2 complete; never rerun either remaining writer. Chat window ends2026-10-03T19:12:46Z; completed final chat gates preceded expiry. Evidence41-final-retained-readonly-state.json. STAFF9999887458 remains MANAGER/REMOVED.
