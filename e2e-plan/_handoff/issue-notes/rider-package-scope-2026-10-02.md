# Rider package-delivery scope

An E2E-plan item-list scenario was treated as a product requirement, leading to an unnecessary rider itemized dialog and a proposed backend item projection. The rider is responsible for the assigned package, pickup and delivery locations, and verified handover; item names, quantities and prices are not required.

The user clarified this on 2026-10-02. Backend product edits were stopped before implementation; item-list E2E assertions were removed. Preserve the failed test order and resume its ordinary delivery rather than creating a replacement. The deployed UI is unchanged by this correction.
