# Pickup and delivery

Scope: verify the assigned package, pickup location, delivery destination, pickup OTP, delivery OTP and delivery completion. Item names, quantities, prices and item checklists are not rider requirements. Do not add them back to the UI/API or E2E plan without an explicit change to the rider’s responsibilities. See [the scope decision](SCOPE-DECISIONS.md).

Seven repaired browser methods passed with retained orders delivered, assignments released and idle rider OFFLINE. The offline-after option also passed. The unnecessary itemized-content assertions have been removed; E2E test compilation passed. These passes do not claim public SSE, actual provider calls or elapsed unavailable/failure behavior.

See [current pending notes](PENDING.md), [scenarios](scenarios.md) and [audit 10](../../../../RandomDocuments/E2ECoverageAudit_2026-10-01/10-pickup-and-delivery.md) for evidence and remaining work. Follow the [main plan](../../README.md) when reviewing this feature.
