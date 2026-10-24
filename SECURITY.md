# Security policy

## Supported security assumptions
- Java 21 and Paper 1.21.4 are the current build/runtime baseline.
- Discord webhook URLs are secrets and should be configured only on the server.
- Only users granted `cmdlogger.staff` have their player commands logged.
- Command exclusions and argument redaction are configurable; operators must include commands added by their server's plugins.

## Reporting a vulnerability
Please do not publish live webhook URLs, credentials, or unredacted staff command logs in issues. Revoke/rotate any exposed webhook immediately. Report the affected version, impact, and a minimal reproducible example without secrets through the repository's private maintainer contact or a private security advisory if enabled.

## Known limitations
This plugin cannot guarantee that every sensitive argument is detected. Aliases and plugin-specific commands can bypass root-name-based redaction unless explicitly configured. Delivery uses a bounded in-memory queue: under load, entries may be dropped rather than allowing unbounded memory growth or delaying the game thread. Delivery is not durable and has no persistent retry queue. Validate changes on a staging server before production deployment.
