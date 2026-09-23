# Sessions and role access

Session persistence and isolation coverage is implemented in `SessionUiTest`. A read-only active-device check also passes through `CustomerSettingsUiTest`, proving the current logged-in device renders with its Last Active text without removing it. See [scenarios.md](scenarios.md) and [pending work and validation notes](PENDING.md). Device eviction and backend authorization remain deferred.
