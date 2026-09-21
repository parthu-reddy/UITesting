# Seeded environment data and prerequisites

User-confirmed on 2026-09-19 from the deployed development setup:

| Role | Phone | Use |
| --- | --- | --- |
| Customer | `8000000001` | Existing seeded customer |
| Restaurant | `9000000001` | Existing seeded restaurant partner |
| Rider / delivery executive | `7000000001` | Existing seeded rider |
| Admin | `1000000001` | User selected earlier default; no dedicated admin account seeded |

Prefer these existing accounts when creating tests. Do not create replacement accounts merely to simplify setup. Their current login/profile state still needs live validation. The user-confirmed rider number supersedes the earlier choice `5000000001` for planned tests; it is not evidence that the older seed ranges have been removed.

TestConfig defaults now match the three confirmed seeded accounts. Override them when needed using `-Dcustomer.phone=8000000001 -Drestaurant.phone=9000000001 -Drider.phone=7000000001`. User subsequently confirmed using admin `1000000001` and said any number can be used because no dedicated admin account was created. The authorized admin profile-completion flow has now passed live validation; retain this test profile for reuse.

## Order prerequisites

- At least one rider must be logged in, available and near the selected restaurant before order placement. Recheck live availability; an earlier Chrome session is not a permanent guarantee.
- Monitor customer, restaurant and rider UIs together through isolated logged-in contexts.
- Rider acceptance becomes available after restaurant acceptance and when preparation is within 15 minutes or complete, per the user's project rule. Verify exact timing boundaries in source when defining dispatch scenarios.
- Reuse the existing **Home** address; do not create a new address for ordinary order tests.
- For **Brand1**, always open the outlet dropdown and choose an outlet under 4–5 km; backend disallows orders above 5 km. Check displayed distances live rather than assuming a particular outlet remains eligible.
- Read current account/outlet state before changing it. Preserve shared seeded data and restore test changes.

The development URL can change. Use the central TestConfig / URL overrides documented in the project README.
