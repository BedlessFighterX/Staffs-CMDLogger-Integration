# Security audit and remediation record

## Scope and confidence
This is a source-level review and regression-test record, not a penetration test or a guarantee that the project has no vulnerabilities. The current proposed release changes the baseline to Java 21 and Paper 1.21.4. Live server behavior, permission integration, and real webhook delivery still require staging validation.

## Remediated findings

| Severity | Finding | Remediation |
|---|---|---|
| High | Network calls could block the server thread | Network work runs on a dedicated executor with two workers, a bounded queue of 256 tasks, timeouts, and a safe rejection warning. |
| High | Sensitive command arguments could be forwarded | Added configurable `redacted-commands`; matching commands record only the root and a redaction marker. `ignored-commands` still suppresses entries entirely. |
| Medium | Webhook URL could accept ambiguous or hostile URL forms | Standalone validator requires HTTPS, exact Discord host, expected webhook path, numeric ID, and rejects user-info, query, fragment, and non-standard ports. |
| Medium | Discord mentions could be triggered by command text | Outgoing payload sets `allowed_mentions.parse=[]`. |
| Medium | Remote error body could appear in server logs | Only HTTP status or exception class is logged; the webhook URL and remote response body are not included. |
| Medium | Unbounded asynchronous work could accumulate during command bursts | Fixed two-thread executor and bounded 256-entry queue; overload drops events rather than growing without limit. |
| Medium | Test compilation depended on loading Bukkit's JavaPlugin type | URL validation extracted into a pure-Java class so validation tests do not reference the plugin entry point. |
| Low | Limited command management UX | Added styled help, protected status/reload commands, and permission-aware tab completion. |

## Safe proof-of-concept regression cases
These tests are local and do not contact a live webhook.

1. **Webhook URL validation:** HTTP, lookalike hosts, non-standard ports, query strings, user-info, malformed IDs, and extra path segments are rejected by `WebhookUrlValidationTest`.
2. **Argument redaction:** `/login MyPassword123` is transformed to `/login [arguments redacted]` when `login` is configured, as covered by `CommandFilterTest`.
3. **Command exclusion:** a configured root such as `plugins` is excluded case-insensitively and for namespaced command forms.
4. **Mention abuse:** the Discord payload explicitly disables parsed mentions so command text cannot trigger @everyone/@here/role pings.

## Residual risks and follow-up
- Root-based redaction is not semantic secret detection. Add all authentication, token, account, and moderation commands from installed plugins; aliases may require separate entries.
- The bounded in-memory queue intentionally drops events during overload and on shutdown. There is no durable delivery or retry/backoff queue.
- Bukkit's player command preprocess event does not observe every command dispatched by plugins or the console.
- A malicious or compromised server plugin can still access configuration and the webhook secret.
- Dependency, real-server compatibility, and rate-limit behavior must be verified in CI and on a staging Paper server. Do not describe the project as vulnerability-free.
