# Current validation and remaining scope

Current cart scope: 13 live invocations passed without skips, plus 5 local monetary-boundary UI checks. See README.md for mapping and selected 05-fast-results.json evidence. No deployment is needed: cart changes strengthen tests and correct plan statements; product cart code was unchanged.

Historical drawer increment defect was already fixed/deployed and now passes. Historical quote 409/unsettled tax and incorrect Place order readiness assumptions are superseded by final successful quoted bill checks with authoritative rider readiness. The original historical report remains unchanged in E2EFullSuite_2026-09-27.

Real long-duration and rate-limit execution stays deferred per the user. Checkout order/payment effects, authoritative quote expiry/tamper, stock/price revalidation and fee policy are assigned to checkout-and-payment. Per-location carts and cross-tab behavior also appear in saved-address/resilience and must be checked in those owning features.
