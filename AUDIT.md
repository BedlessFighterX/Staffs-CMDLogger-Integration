# Repository audit and remediation record

## Scope
Reviewed the original master source tree: Maven build, plugin entry point, command listener, webhook implementation, command executor, plugin metadata, configuration, and README. This is a source review, not a penetration test of a live server.

## Findings

| Severity | Finding | Evidence / impact | Remediation |
|---|---|---|---|
| High | Network request on server event thread | Original ChatListener called webhook.execute() inside the command event; network delays could stall a tick. | Snapshot player state on the server thread and send asynchronously with timeouts. |
| High | JSON strings not escaped | Original JSONObject.quote only wrapped values in quotes. Quotes/backslashes in commands could produce malformed JSON. | Proper JSON string escaping and UTF-8 payload. |
| Medium | Ignore list unused | Original listener read ignored-commands but never checked it. | Root-command matching and unit tests. |
| Medium | Unsafe configuration assumptions | Original code used requireNonNull and did not safely handle malformed colors/titles. | Validate webhook at startup and provide title/color fallbacks. |
| Medium | HTTP errors/timeouts poorly handled | Original webhook had no connect/read timeout and did not process error responses. | Bounded timeouts, status checks, bounded error body. |
| Medium | Hard-coded remote HTTP assets | Original listener sent an IP-based HTTP image URL. | Remove third-party HTTP assets. |
| Medium | Build metadata contradictory | Maven targeted Java 8, README claimed Java 16; no tests/CI. | Gradle build, Java 8 bytecode, JDK 17 CI intended. |
| Low | No protected reload command | Existing command only advertised project links. | Add /cmdlogger reload guarded by cmdlogger.admin. |
| Low | Missing command null check | Original code dereferenced getCommand without checking. | Explicit startup validation. |

## Safe proof-of-concept examples
These examples are local demonstrations and do not contact a real webhook.

1. JSON encoding: command text /say \"hello\" made the original quote-only encoder emit an unescaped quote, invalidating JSON. The replacement encoder escapes quotes, backslashes, and control characters.
2. Ignored command bypass: with ignored-commands containing plugins, executing /plugins still sent a log in the original because the list was never consulted. CommandFilterTest verifies the fix.
3. Server-thread blocking: an unreachable or slow endpoint made the original synchronous webhook call delay command processing. The replacement schedules network work asynchronously and sets 5-second connect/read timeouts.

## Migration and validation
- Gradle migration is started on the hardening branch; Java bytecode compatibility remains Java 8.
- Unit tests cover command filtering.
- A live server smoke test and test webhook are still required.

## Residual risks
Command logging can expose credentials in command arguments. Configure exclusions and restrict Discord channel access. There is no durable retry queue or rate-limit backoff. The API baseline remains Spigot 1.16.5; other versions must be tested, not assumed compatible.
