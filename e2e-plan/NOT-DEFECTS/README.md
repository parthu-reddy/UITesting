# Not defects — scenarios the product deliberately does not implement

Written 2026-09-24 while closing the `PENDING.md` backlog.

Several scenarios in `scenarios.md` files describe behaviour this application has never had.
They are not bugs and not missing implementations; they are **scenarios written against an
imagined product**. Leaving them in the pending lists makes the backlog look worse than it is
and invites the same investigation repeatedly.

Each entry below says what the product actually does, with the source that proves it, and what
would have to change for the scenario to become valid. **If you disagree with any of these, say
so — each one names its evidence so it can be checked rather than argued.**

A scenario here is *not* closed as "won't fix". It is closed as "does not describe this
product". If the product decides to build the behaviour, the scenario becomes valid again.

---

## CART-14, CART-15, CART-16 — cart conflict dialog on a second restaurant

**Scenario assumes:** adding an item from a second restaurant prompts to clear the first cart.

**The product:** `useCustomerCart` keys carts by `restaurantId`
(`model/useCustomerCart.ts` — `prevLocationCarts[restaurantId]`), so a customer holds
**independent carts per restaurant within a location**. Adding from a second restaurant creates
a second cart; nothing is cleared and nothing is prompted. The E2E suite already proves this:
one Brand 1 item and one Brand 2 item stayed in separate outlet sections with independent
Checkout actions.

**Note the near-miss.** A cart-clearing confirmation *does* exist — in
`CustomerOutletSelectorModal`, for switching **outlet within one brand** ("Switch outlet? Your
cart from X will be cleared"). That is a different action from adding a second restaurant's
item, and it is easy to mistake one for the other when reading test reports.

**To make the scenario valid:** the product would have to move to a single active cart. That is
a product decision, not a bug fix.

---

## CHECKOUT-07 through CHECKOUT-10 — separate UPI / card forms and back-navigation persistence

**Scenario assumes:** distinct payment forms (card fields, a UPI QR view) and an intermediate
checkout page whose state survives back-navigation.

**The product:** the payment modal offers exactly three simulated method tiles — Credit Card,
UPI / Netbanking, Wallet — and a single `Pay ₹… Now` action. There are no card fields, no QR
view and no intermediate page. Confirmed by reading the modal live on 2026-09-19: the dialog
`Complete Your Order` renders the three tiles, a bill breakdown and one Pay button.

**Already correctly adapted.** `CheckoutUiTest.paymentChoicesAndCheckoutReentry` tests the real
contract instead. No further action.

---

## CHECKOUT-13 — separate confirmation screen with Track Order / Done

**Scenario assumes:** a confirmation screen after successful payment.

**The product:** a successful payment transitions directly to the order tracker after a short
success state. There is no separate confirmation route.

**To make the scenario valid:** the product would have to add that screen. Do not assert one.

---

## CHECKOUT-02 — opening an empty cart

**Scenario assumes:** an empty cart can be opened and shows an empty state.

**The product:** the `View Cart` trigger is rendered only when there is something in the cart —
`CustomerHomeChrome.tsx`: `{totalCartItems > 0 && (`. An empty cart has no entry point, so the
scenario cannot be reached through the UI.

This is a defensible design (no control for a view with nothing in it), but it is worth an
explicit product decision rather than silence: the empty state exists in the drawer for the
moment the **last item is removed**, which the suite does cover.

---

## NAV-02 — deep-linking to customer settings

**Scenario assumes:** a settings URL that renders role-correct settings directly.

**The product:** `CustomerDashboard.tsx:289` is the whole customer router —
`<Route path="*" element={<CustomerMainView {...view} />} />`. Account Settings is in-memory
view state driven by the profile button, not a route. A direct settings URL cannot show
settings because no such route exists.

**To make the scenario valid:** the customer dashboard needs real routes. Until then there is
nothing to test. Note this differs from the **restaurant** dashboard, which *is* route-driven —
and whose routing had a genuine defect, fixed separately.

---

## PROFILE-13 through PROFILE-15 — rider theme switching

**Scenario assumes:** the rider UI has a theme toggle and an observable `dark` class.

**The product:** rider settings exposes no theme control and the rider document root carries no
`dark` class. The rider surface is dark by design — the plan specifies dark, high-contrast
surfaces for sunlight legibility, not a user preference.

**To make the scenario valid:** the product would have to add a rider theme toggle. The
equivalent **customer** theme scenarios were real defects and have been fixed.

---

## DISPATCH-17, DISPATCH-19, DISPATCH-21, DISPATCH-22 — old rider tab layout

**Scenario assumes:** a tabbed rider dashboard with named earnings/history/wallet tabs.

**The product:** the current rider dashboard shows earnings as a stats tile, completed trips as
a panel, and the wallet inside Profile settings.

**These are not "not defects" so much as stale scenarios.** They describe a real need against a
layout that no longer exists, and should be rewritten against the current UI rather than
deleted. Listed here so they stop being read as product failures.

---

## CAMPAIGN-ADM-01 through CAMPAIGN-ADM-10, PERF-01 — an admin campaigns screen

**Scenario assumes:** the admin portal has a Campaigns tab with campaign cards, budgets, a
wallet balance and an ad-performance dashboard.

**The product:** the admin sidebar (`pages/admin/AdminPortal.tsx`) has ten items and none is
campaigns. `CampaignManagement` is imported only by `RestaurantTabPanels` — campaigns are a
**restaurant** tab. `AdminCampaignTest` is `@Disabled` with this reason and
`AdminPortalPage.openCampaignsTab()` throws rather than clicking whatever `has-text('Campaigns')`
happened to match.

**To make the scenario valid:** either build an admin campaigns screen, or rewrite these
scenarios against the restaurant's Campaigns tab.

---

## LEDGER-ADV-11 — a ledger statement / detail panel

**Scenario assumes:** clicking a ledger row opens a statement or transaction-detail panel.

**The product:** `AdminLedgerView.tsx` renders a filter form over a plain table. A row's only
action is a button that copies its transaction id; there is no panel. The test is `@Disabled`
and `AdminLedgerPage.openStatement()` throws.

---

## Reclassified — this one IS now implemented

**REORDER-01 through REORDER-03** were recorded as "unavailable through the UI: the reachable
tracker exposes no Reorder action". That was accurate when written. It is no longer true.

The redesign added an **"Order it again"** strip to the customer home
(`features/customer-orders/components/ReorderStrip.tsx`, wired into
`CustomerRestaurantBrowser`), backed by `useReorderSuggestions` reading
`GET /api/v1/orders/history`. Each card is a real `<button>` reading `Reorder · ₹<total>`.

It renders nothing when the customer has no completed orders, so a test needs a customer with
delivery history — the same prerequisite that currently blocks the history scenarios.

`ReorderStripPage.java` already exists and its locator `button:has-text('Reorder')` now
resolves. Its doc comment is wrong about the location: the strip is on the **customer home
feed**, above the restaurant list — not on order history.
