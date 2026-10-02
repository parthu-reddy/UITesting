## Current deployed validation — 2026-10-01

The user deployed menu recovery. Both routed UI cases passed with zero failures/errors/skips (04-recovery-deployed.xml): the forced catalog 502 displays Couldn't load menu, UI retry fetches 200 and restores rows; a successful empty catalog displays Menu unavailable without a retry/error. Total required discovery/menu/outlet coverage is now 23 unique passed invocations, selected in 04-fast-results.json. No deployment is pending for this feature. Earlier pending notes below describe the predeployment checkpoint. Backend-outage recovery remains distinct from routed UI proof.

# Current pending validation

Normal discovery/menu/outlet coverage passed: 21 final required invocations. The blank-menu recovery fix is ready locally; deploy FoodDeliveryAppUI and run the two additional routed UI cases. Local compilation and 34 menu checks do not establish deployed recovery.

The plan formerly assumed discovery was restricted to 5 km; source requests 10 km. Actual delivery eligibility remains 5 km. Named placeholder fallbacks are valid; every visual need not remain an img element. Existing out-of-stock fixtures are reused and retained.

Original failed attempts and prior historical findings remain in the audit evidence. Backend catalog outage injection, menu-owner authorization, stock override persistence across independent roles and real checkout readiness belong to the appropriate backend/partner/order phases; routed UI proof is distinct.
