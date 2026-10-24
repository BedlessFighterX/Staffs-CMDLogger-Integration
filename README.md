# CMDLoggerIntegration

A Paper plugin that forwards commands executed by players with the `cmdlogger.staff` permission to a configured Discord webhook, with configurable exclusions and argument redaction.

## Requirements

- Paper 1.21.4 (or a compatible server API) and Java 21.
- Gradle 8.14.3 or GitHub Actions CI.
- A privately configured Discord webhook URL.

This is a major-version compatibility change from the old Spigot 1.16 / Java 8 baseline. Back up your server and verify on a staging server before upgrading.

## Build

```sh
gradle clean test build
```

The JAR is written to `build/libs/CMDLoggerIntegration-3.0.0.jar`. Gradle is the supported build system; Maven has been removed.

## Install and configure

1. Back up the server and existing plugin config.
2. Place the JAR in `plugins/` and start the server once to generate config.
3. Set `webhook-url` in `plugins/CMDLoggerIntegration/config.yml`. Treat it as a password; never commit or share it.
4. Review `ignored-commands` (fully suppress a command) and `redacted-commands` (record the root but redact all arguments).
5. Restart the server and grant `cmdlogger.staff` only to staff whose commands should be audited.

## Commands and permissions

- `/cmdlogger help` — styled help.
- `/cmdlogger reload` — reload config; requires `cmdlogger.admin`.
- `/cmdlogger status` — show configuration status without revealing the webhook; requires `cmdlogger.admin`.
- `cmdlogger.staff` — commands from this player are logged; defaults to operators.
- `cmdlogger.use` — use the command; defaults to everyone.

Tab completion only suggests administrative subcommands to senders with the admin permission.

## Security and privacy

Command logs can contain passwords, tokens, personal data, or private arguments. The default redaction list covers common authentication roots, but **you must add the commands used by your installed plugins**. Unknown commands and aliases may still expose arguments. Use an access-controlled Discord channel, restrict filesystem access, and rotate the webhook immediately if it leaks.

Webhook URLs are restricted to HTTPS Discord webhook endpoints; webhook credentials are never printed in the plugin's error messages. Discord mentions are disabled in outgoing payloads to prevent commands containing `@everyone` or role mentions from triggering notifications. Requests run off the main server thread with bounded connection/read timeouts. Delivery is not durable: network failures can still cause lost audit events, and this plugin does not yet implement a persistent retry queue.

## Validation

Run `gradle clean test build` before submitting changes. CI compiles against Java 21 and Paper 1.21.4, runs unit tests, and stores the built JAR as an Actions artifact. Versioned GitHub Releases are created by the release workflow when a matching `vX.Y.Z` tag is pushed.

A code review cannot prove the absence of every vulnerability. Test against your exact server version and review any server-specific commands that handle secrets.
