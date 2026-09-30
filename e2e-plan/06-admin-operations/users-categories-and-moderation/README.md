# users-categories-and-moderation

Status: deployed read-only checks passed: support/user/review 13 tests (1 fixture skip) and user operations 1 test. Local browser-routed mutation coverage compiles; deployed role/catalog mutation checks require isolated fixtures.

Scope: User management, categories, and read-only review investigation. The admin portal has no campaigns screen.

Current coverage: seeded user phone lookup and details, role filtering, user pagination, role/status success and error recovery through fixtures, category create/update/error paths, review entity/author lookup, star ratings, and the read-only policy.

See [validation and pending work](PENDING.md).
