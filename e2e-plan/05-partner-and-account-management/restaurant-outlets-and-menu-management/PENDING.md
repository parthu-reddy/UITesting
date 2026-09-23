# Scenarios and validation

## Restaurant stock-control UI

Current deployed validation on 2026-09-23 fails because clicking Menu Stock Toggles returns to Live Kitchen; In-Stock Dish Toggles never appears. This matches the restaurant dashboard route override recorded under restaurant acceptance/navigation. A previous run reached the stock switches, but that result is stale for the current deployment. No switch was toggled. Stock propagation and outlet edits remain pending.
