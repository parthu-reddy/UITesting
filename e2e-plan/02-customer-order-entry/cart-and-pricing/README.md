# cart-and-pricing

Cart coverage is partially complete. Passing cases include final-item removal, menu stepper behavior, single- and two-item subtotal accuracy, drawer close/reopen retention, and cart persistence through Settings navigation. The drawer's Add one control still leaves quantity at 1 instead of 2, including with a normal-duration click, so the later decrement assertions in that combined case are not reached.

See [scenarios.md](scenarios.md) and [pending work and validation notes](PENDING.md) for evidence and remaining coverage. UI-only coverage now includes distinct-item subtotal arithmetic, navigation and reload persistence, five sequential increments, menu decrement/removal, removing the final drawer item, the empty state, hidden empty-cart trigger, and free-delivery progress. No order is submitted.
