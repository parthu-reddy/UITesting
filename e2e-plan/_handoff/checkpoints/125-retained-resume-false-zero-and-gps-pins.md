# 125 — ce254f3a resumed and verified; three rider money/map defects fixed locally

2026-10-07T12:40+05:30. Continuation session after the previous session hit its usage limit.
Item3 is still the only active item; M1/A5b remain paused. Owner instruction this session:
"If there are mistakes fix them" (recorded in USER-INSTRUCTIONS).

## Correction to checkpoint 124: invocations 3 and 4 were never recorded

Checkpoint 124 and NEXT-STEPS still said invocation 3 was "running". In fact:

- **Invocation 3**: 1 failure, 0 errors/skips, 27.18s. The brand-card `hasCount(1)` on exact
  "Brand 1" saw 0 matches within 5s. No order or manifest was created, and the rider was restored
  Offline through the UI. The helper was then changed to search the feed first.
  [Evidence](../evidence/124-active-invocation3.json).
- **Invocation 4**: created owned order **ce254f3a-13c6-4317-a30f-6884595aa090** (Brand 1 Outlet 5,
  customer 8000000001 / owner 9000000001 / rider 7000000001).
  - Passed: checkout, accept/cook/ready, dispatch, and the assigned 390×844 capture (pickup hint
    fits, 48px header chat, uncovered swipe, all three pins), plus AT_RESTAURANT, pickup and
    OUT_FOR_DELIVERY.
  - Failed: 1 failure, 0 errors/skips, 249.9s, at the out-for-delivery capture after a rider reload.
    No `Courier location` pin appeared within 5s. The saved DOM shows **zero markers** in the map
    region, so pin placement had not run. Whether the restaurant lookup or geolocation was pending
    is not determined.
  - [Evidence](../evidence/124-active-invocation4.json),
    [failure frame/DOM](../evidence/124-active-invocation4-missing-courier/).

## Invocation 5: retained resume of ce254f3a (no new order)

Passes **1/1, 0 failures/errors/skips, 52.0s**. The resume path now runs the out-for-delivery visual
capture after a fresh rider load, and every check passed:
- canvas;
- two 48px call targets;
- courier, customer and restaurant pins;
- the slide-to-deliver control uncovered at 10/50/90% of its width;
- header chat ≥48px;
- no horizontal overflow.

Delivery used OTP + swipe attempt 1, DELIVERED returned 200, and the rider's completed trip and the
customer receipt passed. The invocation 4 pin failure was **not reproduced** and its cause remains
undetermined. `RiderVisualAudit` now writes `<name>-missing-pins.txt` before rethrowing:
- visibility/focus;
- geolocation permission;
- the restaurant lookup's timing and status;
- a timed geolocation probe using the map's own options;
- marker labels.

[Evidence](../evidence/125-retained-resume-invocation5.json).

Retained read-only checks for ce254f3a pass **1/1, 0 failures/errors/skips, 27.23s**: delivered
receipt, CARD/SUCCESS ₹43.02, rider net ₹21.16 (gross ₹25.80 − taxes ₹4.64), restaurant ₹6.54,
**18 ledger lines balancing ₹92.72**, 0 refunds, quote only, 0 order writes.
[Evidence](../evidence/125-retained-money-ce254f3a.json).

## Defects found in these captures, fixed locally (not deployed)

1. **False ₹0.00 / 0 orders.** In the same invocation 5 capture, the map PNG shows the header at
   ₹0.00 / 0 orders and the text dump seconds later shows ₹21.16 / 1 order. Today's tiles summed an
   unloaded `history` array. A failed read (the error was only logged) and browsing another day in
   the history panel also showed zeros. A probe on the old hook confirmed 0/0 while loading, 21.16/1
   once loaded, and 0/0 for another day. The hook now exposes `todayFigures`, and the bar shows a
   figure only when it is `ready`. **Consequence for the harness:** invocation 5's "earnings rose by
   the payout" check had a not-yet-loaded ₹0.00 baseline, so the exact increase over ₹21.16 is
   **not proven**. The E2E earnings locator only matches text starting with `₹`, so after
   deployment it waits for the real figure.
2. **Map pins waited for GPS.** Customer and restaurant pins were placed only inside the
   `getCurrentPosition` callback, which has a 15s timeout. They are now placed when the restaurant
   lookup settles, and GPS adds only the courier and the route. This is a plausible but unproven
   cause of invocation 4's failure.
3. **Customer fee shown as rider money.** The slider said "Slide to deliver · credit ₹23.76"
   (the customer delivery fee), but the ledger credited the rider ₹21.16. The history details
   modal labelled the same fee "Total Earnings". The slider now reads "Slide to deliver". The modal
   shows the server's `earnings.netPayout` as "Your net payout", or "Not confirmed yet", and labels
   the fee "Delivery fee paid by customer". The dispatch card's "Est. Delivery Fee" was already
   accurate and is unchanged.

Every new test was seen red on the old code first. Full UI **177 files / 1,060 tests**, typecheck,
lint (0 warnings), build, Phase4 source 12/12, locator audit PASS and money as-is audit 0/23 (clean)
all pass. [Local gates](../evidence/125-ui-local-gates.json).

## Exact next gates

1. The owner builds and deploys **FoodDeliveryAppUI only**: 11 files, listed in
   [the deployment handoff](../../../../RandomDocuments/PendingWork_2026-10-07/ITEM-3-ACTIVE-DEPLOYMENT.md).
   No backend change, reset or schema change.
2. After it is live, read-only on ce254f3a: rider History → details modal shows "Your net payout
   ₹21.16", and today's tiles never show ₹0.00 before loading.
3. The slider label, the GPS-independent pins and the earnings baseline only appear in an active
   state. Verifying them needs one fresh lifecycle with full preflight. Retain any new identity and
   resume it on failure.
4. Then close item3 with dispositions, and continue to M1 and A5b in order.
