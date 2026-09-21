Whenever you write, generate, record, extend, or fix end-to-end (E2E), UI, browser, Playwright, Cypress, or Selenium tests, you MUST follow the principles of the `e2e-live-test-authoring` skill:

1. **Drive the Real UI:** Write tests while driving the application in a live, persistent browser session. Do NOT write tests "blind" by guessing selectors or imagining form rules from memory.
2. **UI Only:** Tests must interact exclusively through the browser UI. No API calls, database seeding, or direct service access from within the test code.
3. **Keep the Browser Session Open:** Never close or navigate away from a live page when something fails. The failing page is your best evidence.
4. **Verify Every Selector:** Every locator and asserted string must be confirmed in *both* the live UI (using DevTools/DOM snapshot) and the source code before you write the test step. Zero or multiple matches means the locator is wrong.
5. **Phase-by-Phase Process:**
    - **Preflight:** Discover frameworks, source code, target roles, login credentials, and deployment workflows before starting.
    - **Page Loop:** For every page: observe -> compare with source -> fill dummy data -> write test step -> submit -> verify network/console.
    - **Stop-and-Wait:** Stop immediately if you hit a backend issue, 5xx error, unknown validation, or production-like environment. Freeze, capture evidence (screenshots, network logs), and report to the user for a decision. Do NOT retry blindly.
6. **Backend Changes:** If a dev-profile override or backend fix is needed (and approved), make the smallest possible change, commit, push, and use the project's single-service publish/deploy workflows.
7. **Finalize:** Re-run the test from a clean state to verify it passes without accidental dependencies on your manual session.

Always check the full `e2e-live-test-authoring` skill documentation (located in `UITesting/e2e-live-test-authoring/SKILL.md`) for detailed rules on dummy data, locator priority, error triage, and templates.
