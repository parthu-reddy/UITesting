# Rider package-delivery scope — 2026-10-02

The user clarified: the rider’s job is to deliver the assigned package. Rider coverage must verify assignment identity, pickup location, delivery destination, pickup verification and delivery verification. Do not require the rider to inspect item names, quantities or prices, or provide an item checklist.

PICKUP-02 and PICKUP-06 previously promoted itemized contents into a product requirement. That was a planning mistake. Those scenarios now cover assigned delivery identity and package handover; their item-list assertions were removed. Absence of item details in driver responses is not a defect under this scope.

Do not reintroduce this requirement or expand rider payloads to satisfy the old scenario. A future change requires an explicit product decision establishing why the rider needs those details.

Recorded in [CommonMistakesDocumentation](../../../../CommonMistakesDocumentation/UI/rider-package-scope-2026-10-02.md) and [CodingPracticesAcrossAllServices](../../../../CodingPracticesAcrossAllServices/09_Testing/role-scope-before-e2e-product-changes.md).
