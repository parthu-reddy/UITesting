# restaurant-acceptance-and-preparation

Status: core acceptance/preparation path is covered by the passing `HappyDeliveryFlowTest`: the exact new order is selected by ID, accepted, started cooking and marked prepared before rider dispatch. REST-ACCEPT-05, REST-ACCEPT-06 and REST-ACCEPT-12 through REST-ACCEPT-15 are covered by that coordinated run.

Scope: Restaurant queue, acceptance and kitchen preparation.

`RestaurantNavigationUiTest` was added for REST-NAV-01 through REST-NAV-05 and REST-NAV-07. Its first Live Kitchen assertion passes, but switching to Menu fails against the deployed UI. The test remains executable as a regression reproduction; this folder is not marked fully validated.

See `PENDING.md` for the navigation failure and remaining acceptance cases.
