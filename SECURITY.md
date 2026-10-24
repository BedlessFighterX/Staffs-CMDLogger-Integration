# Security policy

## Supported security assumptions
- Java 21 and Paper 1.21.4 are the current build/runtime baseline.
- Discord webhook URLs are secrets and should be configured only on the server.
- Only players granted cmdlogger.staff have their player commands logged. Standard console command logging is separately opt-in with log-console-commands: true and remains disabled by default.
- Command exclusions and argument redaction are configurable. Review commands and aliases added by server plugins.
- New installations default redact-all-arguments to true, so command arguments are not sent to Discord. Existing configurations with an explicit false value retain that choice during upgrade; review config.yml and enable it for stronger privacy.
- Use ignored-players only for explicit policy exceptions; excluding a player means their commands will not be sent to the audit channel.

## Reporting a vulnerability
Please do not publish live webhook URLs, credentials, or unredacted staff command logs in issues. Revoke/rotate any exposed webhook immediately. Report the affected version, impact, and a minimal reproducible example without secrets through the repository's private maintainer contact or a private security advisory if enabled.

## Known limitations
When an administrator explicitly disables global argument redaction, this plugin cannot guarantee that every sensitive argument is detected. Command text is sanitized for Discord Markdown/control characters and parsed mentions are disabled, but sanitization is not a substitute for argument redaction. Aliases and plugin-specific commands can bypass root-name-based redaction unless explicitly configured. Webhook POST redirects are disabled, the configured URL is restricted to Discord webhook hosts, and outgoing mentions are suppressed, but the webhook URL must still be protected as a credential. Delivery uses a bounded in-memory queue: under load, entries may be dropped rather than allowing unbounded memory growth or delaying the game thread. Delivery is not durable and has no persistent retry queue. Bukkit's player command preprocess event does not capture every command dispatched internally by plugins. Optional console auditing observes standard console commands only, not all RCON, command-block, or internal plugin dispatch paths. Validate changes on a staging server before production deployment.
