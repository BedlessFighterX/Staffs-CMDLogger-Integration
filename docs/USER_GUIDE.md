# CMDLoggerIntegration User Documentation

**Version:** 3.3.0  
**Platforms:** Bukkit-compatible Spigot and Paper servers  
**Repository:** https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration

CMDLoggerIntegration audits commands issued by authorized staff players and forwards audit embeds to a configured Discord webhook. Console command auditing is optional. This guide documents installation, configuration, permissions, commands, privacy controls, troubleshooting, and known limitations.

## Contents

- [Requirements and compatibility](#requirements-and-compatibility)
- [Installation](#installation)
- [Discord webhook setup](#discord-webhook-setup)
- [Configuration reference](#configuration-reference)
- [Commands and permissions](#commands-and-permissions)
- [Privacy and security](#privacy-and-security)
- [Performance and delivery limitations](#performance-and-delivery-limitations)
- [Troubleshooting](#troubleshooting)
- [Building from source](#building-from-source)

## Requirements and compatibility

The project targets a broad range of Bukkit-compatible server APIs. Automated compilation and unit-test checkpoints currently include:

- Minecraft / Spigot API 1.8.8
- Minecraft / Spigot API 1.12.2
- Minecraft / Spigot API 1.16.5
- Minecraft / Spigot API 1.20.6
- Minecraft / Spigot API 1.21.11

These are API-level CI checkpoints, not proof of live-server testing on every Minecraft release or server fork. Compatibility outside these checkpoints is not verified by the matrix. Paper and other Bukkit-compatible forks may work, but behavior can vary.

The plugin is compiled to Java 8 bytecode. Your server itself must run on a Java version supported by its Minecraft version; newer server versions require newer Java runtimes. The plugin descriptor omits `api-version` to avoid excluding legacy servers, so test startup behavior on the exact server build you operate.

## Installation

1. Download the plugin JAR from the project's release page.
2. Stop the server and make a backup of the server and existing plugin configuration.
3. Put the JAR in the server's `plugins/` directory.
4. Start the server once. The plugin generates `plugins/CMDLoggerIntegration/config.yml`.
5. Create a Discord webhook and set its URL in `webhook-url` (see below).
6. Grant `cmdlogger.staff` to the staff players whose commands should be audited.
7. Review privacy and exclusion settings before using the plugin on a production server.
8. Restart the server, or run `/cmdlogger reload` after editing the configuration.

Existing configuration files are retained and missing defaults are added. Existing explicit values are not necessarily replaced with newer defaults. After upgrading, review the generated configuration manually—especially `redact-all-arguments`.

## Discord webhook setup

1. In Discord, open the channel where you want audit logs delivered.
2. Open the channel settings and find **Integrations → Webhooks**.
3. Create a webhook, choose the destination channel, and copy its URL.
4. Set `webhook-url` in `plugins/CMDLoggerIntegration/config.yml`.
5. Save the file and run `/cmdlogger reload` as an administrator.
6. Run `/cmdlogger test` to verify delivery.

Treat the webhook URL like a password. Anyone with the URL may be able to post to its channel. Do not share it, paste it into public issues, or commit a live URL to source control. If it leaks, delete or regenerate the webhook in Discord immediately.

## Configuration reference

The following is the default configuration shipped with version 3.3.0:

```yaml
# Treat this URL as a secret; anyone who has it can post to the target Discord channel.
# Prefer setting this privately and never commit a live webhook URL.
webhook-url: 'your-webhook-url'

# Privacy-first default: only command roots are sent; all arguments stay on the server.
# Set to false only if your audit policy explicitly requires arguments, and configure redacted-commands.
redact-all-arguments: true

# Console commands are not logged unless explicitly enabled.
# Global argument redaction still applies to console commands.
log-console-commands: false

# Player names listed here are excluded from command logging (case-insensitive).
ignored-players: []

embed:
  title: 'Staff Command Audit'
  color: '#5865F2'
  server-name: 'Minecraft Server'
  show-server: true
  show-world: true
  show-timestamp: true
  show-operator-status: true

# Root commands to exclude entirely. Matching ignores case, leading slash, namespace, and arguments.
ignored-commands:
  - 'examplecommand1'

# When redact-all-arguments is disabled, these commands still have every argument redacted.
redacted-commands:
  - 'login'
  - 'register'
  - 'l'
  - 'log'
  - 'changepassword'
  - 'changepass'
  - 'resetpassword'
  - 'auth'
  - 'token'
  - 'password'
  - 'passwd'
  - '2fa'
  - 'totp'
  - 'email'
  - 'verify'
  - 'pin'
```

### Top-level settings

| Key | Default | Description |
|---|---|---|
| `webhook-url` | `your-webhook-url` | Discord webhook endpoint. Replace the placeholder with your real webhook URL. |
| `redact-all-arguments` | `true` | If true, only the command root is sent to Discord; arguments remain on the server. |
| `log-console-commands` | `false` | Enables auditing of standard server-console commands. |
| `ignored-players` | `[]` | Player names excluded from logging. Matching is case-insensitive. |
| `ignored-commands` | `[examplecommand1]` | Command roots excluded entirely from logging. |
| `redacted-commands` | Built-in authentication list | When global argument redaction is false, arguments for these command roots remain redacted. |

### Embed settings

| Key | Default | Description |
|---|---|---|
| `embed.title` | `Staff Command Audit` | Title shown on the Discord embed. |
| `embed.color` | `#5865F2` | Embed accent color in hexadecimal format. |
| `embed.server-name` | `Minecraft Server` | Configured server name. |
| `embed.show-server` | `true` | Include the configured server name. |
| `embed.show-world` | `true` | Include the player's world when available. |
| `embed.show-timestamp` | `true` | Include the event timestamp. |
| `embed.show-operator-status` | `true` | Include operator-status information. |

### Redaction behavior

- With `redact-all-arguments: true`, command arguments are not sent to Discord. This is the recommended setting for most servers.
- With `redact-all-arguments: false`, arguments may be included for commands not covered by `redacted-commands`. Add server-specific authentication commands and any commands that handle secrets to that list.
- The built-in sensitive-command list is a useful baseline, not a guarantee that every third-party plugin command, alias, or custom authentication command is covered.
- `ignored-commands` suppresses matching command roots entirely. Matching ignores case, a leading slash, namespaces, and arguments.
- `ignored-players` excludes listed player names without case sensitivity.

## Commands and permissions

### Commands

| Command | Permission | Description |
|---|---|---|
| `/cmdlogger help` | `cmdlogger.use` | Displays plugin help. |
| `/cmdlogger reload` | `cmdlogger.admin` | Validates and reloads configuration. An invalid webhook URL is rejected. |
| `/cmdlogger status` | `cmdlogger.admin` | Displays configuration, privacy, and delivery queue status. |
| `/cmdlogger diagnostics` | `cmdlogger.admin` | Provides detailed status and queue diagnostics. |
| `/cmdlogger test` | `cmdlogger.admin` | Sends a test embed to verify webhook delivery. |

The plugin command itself is controlled by `cmdlogger.use`. Administrative subcommands require `cmdlogger.admin`; tab completion only suggests administrative subcommands to senders with that permission.

### Permissions

| Permission | Default | Purpose |
|---|---|---|
| `cmdlogger.staff` | Operators | Enables auditing for commands issued by that player. |
| `cmdlogger.use` | Everyone | Allows use of the `/cmdlogger` command. |
| `cmdlogger.admin` | Operators | Allows reload, test delivery, and diagnostic commands. |

Use a permissions manager such as your server's existing permissions plugin to assign these nodes. Give `cmdlogger.staff` only to the players you intend to audit, and restrict `cmdlogger.admin` to trusted administrators.

## Privacy and security

Command logs may contain passwords, tokens, personal information, or other sensitive values. Discord is an external service, so decide which data your staff-audit policy permits leaving the server.

Recommended practices:

1. Keep `redact-all-arguments: true` unless there is a documented need to transmit arguments.
2. Restrict access to the Discord channel that receives audit logs.
3. Keep the webhook URL private and rotate it immediately if exposed.
4. Review `ignored-commands`, `redacted-commands`, and `ignored-players` after adding new plugins or aliases.
5. Give staff and admin permissions according to least privilege.
6. Protect access to the server filesystem and configuration backups.
7. Test configuration changes on a staging server before production deployment.

The plugin restricts webhook URLs to HTTPS Discord webhook endpoints and disables redirects for webhook requests. Outgoing mentions are disabled and command text is sanitized to reduce formatting and mention abuse. These safeguards reduce risk but do not replace appropriate permissions, configuration review, or operational security.

## Performance and delivery limitations

- Webhook requests execute asynchronously rather than blocking the main game thread.
- Delivery uses a fixed-size queue. If the queue fills, new audit events may be dropped and a warning is logged to avoid unbounded memory growth.
- Network failures and server shutdown can result in lost events. There is no persistent queue or guaranteed delivery/replay mechanism.
- Bukkit player command preprocessing does not observe every command dispatched internally by other plugins.
- Optional console logging covers standard server-console commands; it does not guarantee complete coverage of RCON, command blocks, or internal plugin dispatch.
- CI compiles and runs unit tests against selected API checkpoints; it does not live-test every server version or fork.

Do not treat this plugin as a tamper-proof or legally complete audit system. If your requirements demand durable, lossless, or independently verifiable records, add an external durable audit pipeline and validate it for your environment.

## Troubleshooting

### The plugin disables itself on startup

- Check the console for the webhook validation error.
- Confirm `webhook-url` contains a valid Discord HTTPS webhook URL, not the placeholder.
- Check that `config.yml` is valid YAML and that indentation uses spaces.
- Verify that the JAR matches the server's supported Java runtime and API environment.

### Discord receives no messages

- Run `/cmdlogger test` with `cmdlogger.admin`.
- Confirm the webhook still exists and points to the intended channel.
- Check whether the command root is listed in `ignored-commands`.
- Check whether the player is listed in `ignored-players`.
- Confirm the executing player has `cmdlogger.staff`.
- Review console logs for queue saturation or network errors without sharing the webhook URL publicly.

### Command arguments are missing

This is expected when `redact-all-arguments: true`. Only command roots are sent to Discord by design. Only disable this setting if your policy explicitly permits transmitting arguments, and make sure sensitive roots are listed in `redacted-commands`.

### Reload does not apply the configuration

Check the result reported by `/cmdlogger reload` and validate the YAML. The plugin validates the candidate webhook URL before replacing active settings. Correct the configuration and try again.

### Some commands are not logged

The plugin does not guarantee visibility into every command dispatch path. Commands run internally by other plugins, command blocks, RCON, or other execution mechanisms may not generate the same Bukkit player command event. Verify the command source and consult server logs.

## Building from source

The current build uses Gradle and requires JDK 25 for the build tooling. The resulting plugin bytecode targets Java 8.

Run the standard checks:

```bash
gradle clean test build
```

To compile against a particular API checkpoint:

```bash
gradle clean test build -PserverApiVersion=1.21.11
```

The default artifact is:

```text
build/libs/CMDLoggerIntegration-3.3.0.jar
```

CI checks multiple API checkpoints. Run the full test suite before proposing code changes, and test on the actual server versions you intend to support.

## Support and contributions

When reporting a problem, include:

- CMDLoggerIntegration version
- Minecraft version and server software (Spigot, Paper, or fork)
- Java runtime version
- Relevant error messages or stack traces
- Reproduction steps, with webhook URLs and sensitive command arguments removed

Never include a live Discord webhook URL, credentials, or private player data in public reports.
