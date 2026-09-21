# Validation and pending work

## Admin read-only coverage

AdminReadOnlyUiTest: categoriesEditorOpensWithoutChangingData and reviewModerationIsReadOnly passed. findExistingCustomerByPhone FAILED: searching 8000000001 did not display the seeded customer. The visible field says User ID / Phone, but source passes search text to users/:id. This source observation may explain the failure; server behavior was not investigated through backend access. Do not change accounts or weaken the test.

Evidence: UITesting/target/surefire-reports (reports are overwritten by focused reruns); failure screenshots/HTML in target/screenshots. Broader coverage remains pending.

Phone-search failure evidence: `UITesting/target/screenshots/findExistingCustomerByPhone___admin.html` and `.png`. A later focused AdminReadOnlyUiTest run overwrote the XML report; this failure was not rerun or resolved. The source's debounced lookup passes the search text to users/:id despite the field promising User ID / Phone. Keep the failing test as a regression signal. No user changes were made.
