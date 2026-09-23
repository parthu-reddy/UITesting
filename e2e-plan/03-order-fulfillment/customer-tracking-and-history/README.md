# customer-tracking-and-history

Status: source and UI reachability reviewed; lifecycle-backed tracker scenarios remain deferred.

Scope: Active orders, progress, history and reorder.

The current `CustomerOrderHistory` implementation is not mounted by any application component. Customer routing uses a wildcard that always renders `CustomerMainView`, so no UI-only interaction or direct URL can currently open the order-history overlay. This blocks HISTORY-01 through HISTORY-06 and reorder-from-history coverage until the UI exposes the component.

Customer home/discovery scenarios HOME-01 through HOME-06 now have UI-only coverage. The free-delivery tracker renders a bounded ARIA progress value plus either an INR remaining-amount message or the unlocked message after an item is added.
