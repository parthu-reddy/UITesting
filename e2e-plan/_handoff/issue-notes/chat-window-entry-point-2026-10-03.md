# The support entry point outlived the chat it opens (checkpoint25)

Copy of CommonMistakesDocumentation/DataAndState/refund-retry-and-recovery-2026-10-02.md items 19-21 and CodingPracticesAcrossAllServices/12_Frontend/ui-standards.md ("An entry point and the thing it opens share one predicate").

- The customer UI offers a finished order's chat for two hours after `updatedAt` (`isOrderChatOffered`). Support refund tests need an order inside that window; `SupportRefundResolutionFlowTest` now asserts it.
- "Something wrong with this order?" was rendered regardless and did nothing after the window. Fixed locally by sharing the predicate; UI deploy pending.
- The window is UI-only; a server-side window is a pending product decision.
- A lifecycle that errors after a successful server action is resumed on its own order id.
