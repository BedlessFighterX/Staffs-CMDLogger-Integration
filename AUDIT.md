# Security audit and remediation record

## Scope and confidence

This is a source-level review and regression-test record, not a penetration test or a guarantee that the project has no vulnerabilities. The current runtime baseline is Java 21 and Paper 1.21.4. Live server behavior, permission integration, and real webhook delivery still require staging validation.

## Remediated findings

| Severity | Finding | Remediation |
|---|---|---|
| High | Network calls could block the server thread | Network work runs on a dedicated executor with two workers, a bounded queue of 256 tasks, timeouts, and a safe rejection warning. |
| High | Sensitive command arguments could be forwarded | Added configurable redacted-commands plus optional redact-all-arguments to suppress every argument. |
| Medium | Webhook URL could accept ambiguous or hostile URL forms | Standalone validator requires HTTPS, exact Discord host, expected webhook path, numeric ID, and rejects user-info, query, fragment, and non-standard ports. |
| Medium | Webhook POSTs could follow redirects | HTTP redirects are disabled so a POST containing command data is not forwarded to another location. |
| Medium | Invalid reload configuration could replace active settings | Candidate configuration is validated before the live configuration is reloaded. |
| Medium | Discord mentions could be triggered by command text | Outgoing payload sets allowed_mentions.parse to an empty list. |
| Medium | Remote error body could appear in server logs | Only HTTP status or exception class is logged; the webhook URL and remote response body are not included. |
| Medium | Unbounded asynchronous work could accumulate during command bursts | Fixed two-thread executor and bounded 256-entry queue; overload drops events rather than growing without limit. |
| Medium | Staff audit context was limited | Optional server name, world, operator status, and timestamp are available in the embed. |
| Low | Administrators lacked visibility into queue pressure | Added permission-protected queue depth and completed-task diagnostics. |
| Low | Command-root parsing only split on spaces | Root matching and redaction now treat whitespace consistently, including tabs. |

## Safe proof-of-concept regression cases

These tests are local and do not contact a live webhook.

1. Webhook URL validation rejects HTTP, lookalike hosts, non-standard ports, query strings, user-info, malformed IDs, and extra path segments.
2. Argument redaction transforms /login MyPassword123 to /login [arguments redacted] when login is configured; global redaction tests prove /give player diamond 64 is reduced to /give [arguments redacted].
3. A configured root such as plugins is excluded case-insensitively and for namespaced command forms.
4. The Discord payload disables parsed mentions so command text cannot trigger @everyone, @here, or role pings.
5. The HTTPS connection explicitly disables instance redirects before sending the webhook request.

## Residual risks and follow-up

- Root-based redaction is not semantic secret detection. Aliases and plugin-specific commands may need to be added; use redact-all-arguments: true for the strongest argument privacy.
- The bounded in-memory queue intentionally drops events during overload and shutdown. There is no durable delivery or retry/backoff queue.
- Bukkit's player command preprocess event does not observe every command dispatched by plugins or the console.
- A malicious or compromised server plugin can still access configuration and the webhook secret.
- The webhook validator limits the URL host, but operational security still depends on protecting the server and webhook secret.
- Dependency, real-server compatibility, Discord rate-limit behavior, and reload behavior must be verified in CI and on a staging Paper server. Do not describe the project as vulnerability-free.
