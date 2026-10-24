# CMDLoggerIntegration

A Paper plugin that forwards commands executed by players with the `cmdlogger.staff` permission to a configured Discord webhook, with configurable exclusions, argument redaction, timestamped embeds, and a permission-gated webhook test.

## Requirements

- Paper 1.21.4 (or a compatible server API) and Java 21.
- Gradle 8.14.3 or GitHub Actions CI.
- A privately configured Discord webhook URL.

This is a major-version compatibility change from the old Spigot 1.16 / Java 8 baseline. Back up your server and verify on a staging server before upgrading.

## Build

```sh
gradle clean test build
```

The JAR is written to `build/libs/CMDLoggerIntegration-3.1.0.jar`. Gradle is the supported build system; Maven has been removed.

## Install and configure

1. Back up the server and existing plugin config.
2. Place the JAR in `plugins/` and start the server once to generate config.
3. Set `webhook-url` in `plugins/CMDLoggerIntegration/config.yml`. Treat it as a password; never commit or share it.
4. Review `ignored-commands` (fully suppress a command) and `redacted-commands` (record the root but redact all arguments).
5. Configure embed display options; new options are added to existing configs without overwriting server customizations.
6. Restart the server and grant `cmdlogger.staff` only to staff whose commands should be audited.

## Commands and permissions

- `/cmdlogger help` — show commands and usage.
- `/cmdlogger reload` — reload config; requires `cmdlogger.admin`.
- `/cmdlogger status` — show configuration status without revealing the webhook; requires `cmdlogger.admin`.
- `/cmdlogger test` — queue a test embed to verify delivery; requires `cmdlogger.admin`.
- `cmdlogger.staff` — commands from this player are logged; defaults to operators.
- `cmdlogger.use` — use the command; defaults to everyone.
- `cmdlogger.admin` — reload, inspect status, and test webhook delivery; defaults to operators.

Tab completion only suggests administrative subcommands to senders with the admin permission.

## Audit embed options

- `embed.title`, `embed.color` — customize the embed title and accent color.
- `embed.show-operator-status` — include whether the staff member is an operator.
- `embed.show-world` — include the player's world.
- `embed.show-player-uuid` — include the player's UUID. Disabled by default because UUIDs are persistent identifiers; enable only when you need them.

Audit embeds include an event timestamp. Discord mentions are disabled in the outgoing payload so command text cannot ping `@everyone`, `@here`, or roles.

## Security and privacy

Command logs can contain passwords, tokens, personal data, or private arguments. The default redaction list covers common authentication roots, but **you must add the commands used by your installed plugins**. Unknown commands and aliases may still expose arguments. Review aliases and third-party command syntax on your server, and use `ignored-commands` to suppress especially sensitive commands entirely. Use an access-controlled Discord channel and restrict filesystem access. Rotate the webhook immediately if it leaks.

Webhook URLs are restricted to HTTPS Discord webhook endpoints; credentials are never printed in plugin error messages. HTTP redirects are disabled to avoid forwarding webhook payloads to a redirect target. Requests run off the main server thread with bounded connection/read timeouts and a bounded work queue. Discord embed field sizes are validated before sending. Delivery is not durable: network failures or queue saturation can still cause lost audit events, and this plugin does not yet implement a persistent retry queue.

Only authorized administrators should have `cmdlogger.admin`, since the test command posts to the configured audit channel. Do not grant it to untrusted users.

## Validation

Run `gradle clean test build` before submitting changes. CI compiles against Java 21 and Paper 1.21.4 and runs unit tests. The release workflow publishes a uniquely tagged build release after successful pushes to `master`, and also supports version-tag and manual release runs.

A code review cannot prove the absence of every vulnerability. Test against your exact server version and review server-specific commands that handle secrets.
