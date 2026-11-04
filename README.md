<p align="center">
  <h1 align="center">CMDLoggerIntegration</h1>
  <p align="center">Privacy-conscious staff command auditing for Bukkit-compatible Minecraft servers.</p>
</p>

<p align="center">
  <a href="https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/actions/workflows/ci.yml"><img src="https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/actions/workflows/ci.yml/badge.svg?branch=master" alt="Build and test" /></a>
  <img src="https://img.shields.io/badge/version-3.3.0-2563EB?style=flat-square" alt="Version 3.3.0" />
  <img src="https://img.shields.io/badge/Java_Bytecode-8-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 8 bytecode" />
  <img src="https://img.shields.io/badge/Server-Paper_%7C_Spigot-2D2D2D?style=flat-square" alt="Paper and Spigot" />
  <img src="https://img.shields.io/badge/Build-Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle" />
</p>

## Overview

**CMDLoggerIntegration** is a Java plugin that forwards configured staff-player command events to a Discord webhook. Optional console-command auditing, configurable exclusions, and privacy-first argument redaction help server administrators control what leaves the server.

## Highlights

- **Discord audit embeds** — send configured command activity to a designated webhook channel.
- **Privacy-first defaults** — command arguments are redacted by default; console auditing is opt-in.
- **Flexible filtering** — exclude selected command roots and player names, and maintain a list of sensitive command roots.
- **Operational controls** — reload configuration, check status, inspect queue diagnostics, and send a test event.
- **Bounded asynchronous delivery** — network work runs away from the game thread and uses a fixed-size queue to avoid unbounded memory growth.
- **Safer webhook handling** — webhook URLs are validated, redirects are disabled, and untrusted command text is sanitized before it is sent.

## Compatibility

The project compiles and runs unit tests against selected Spigot API checkpoints:

| Minecraft / API checkpoint | CI validation |
|---|---|
| 1.8.8 | Compile + unit tests |
| 1.12.2 | Compile + unit tests |
| 1.16.5 | Compile + unit tests |
| 1.20.6 | Compile + unit tests |
| 1.21.11 | Compile + unit tests |

The plugin is compiled to **Java 8 bytecode**. The server must still run on the Java version required by its Minecraft release. Paper and other Bukkit-compatible forks may work, but are not guaranteed.

These are API-level CI checkpoints, not live-server tests for every release or fork. Compatibility outside the listed checkpoints has not been verified. The plugin descriptor intentionally omits `api-version` for legacy compatibility; test startup behavior on your target server.

## Installation

1. Download the JAR from [GitHub Releases](https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/releases).
2. Stop the server and back up the server and existing plugin configuration.
3. Place the JAR in the server's `plugins/` directory.
4. Start the server once to generate `plugins/CMDLoggerIntegration/config.yml`.
5. Add a Discord webhook URL to `webhook-url` in the generated configuration.
6. Grant `cmdlogger.staff` to the players whose commands should be audited.
7. Review privacy and exclusion settings, then restart or run `/cmdlogger reload`.

Existing configuration values are preserved during upgrades and new default keys are merged. Review the generated configuration after upgrading, especially `redact-all-arguments`.

## Configuration essentials

The safest default configuration keeps arguments on the server and leaves console auditing disabled:

```yaml
webhook-url: 'your-webhook-url'
redact-all-arguments: true
log-console-commands: false

ignored-players: []
ignored-commands:
  - 'examplecommand1'

redacted-commands:
  - 'login'
  - 'register'
  - 'changepassword'
  - 'token'
  - 'password'
```

See the [full user guide](docs/USER_GUIDE.md) for every setting, setup instructions, troubleshooting, and privacy guidance.

## Commands and permissions

| Command / permission | Access | Purpose |
|---|---|---|
| `/cmdlogger help` | `cmdlogger.use` | Display plugin help |
| `/cmdlogger reload` | `cmdlogger.admin` | Validate and reload configuration |
| `/cmdlogger status` | `cmdlogger.admin` | Show configuration and delivery status |
| `/cmdlogger diagnostics` | `cmdlogger.admin` | Inspect queue and diagnostic details |
| `/cmdlogger test` | `cmdlogger.admin` | Send a test Discord embed |
| `cmdlogger.staff` | Operators by default | Enable auditing for a player's commands |
| `cmdlogger.use` | Everyone by default | Allow use of the plugin command |
| `cmdlogger.admin` | Operators by default | Allow administrative commands |

Administrative subcommands are only suggested in tab completion to senders with the admin permission.

## Privacy and security

Command lines can contain passwords, tokens, personal data, or other sensitive values. The default `redact-all-arguments: true` setting sends command roots while keeping all arguments on the server. Disable it only when your audit policy requires argument-level logging, and review `redacted-commands` for authentication commands added by other plugins.

Treat the Discord webhook URL like a password. Keep the receiving channel access-controlled, never commit a live webhook URL, and rotate it immediately if it is exposed. The built-in sensitive-command list is a baseline, not a guarantee that every third-party command or alias is covered.

## Build and test

The current build requires **JDK 25** and **Gradle 9.1.0**. The resulting plugin bytecode targets Java 8.

```bash
gradle clean test build
```

To compile against a specific API checkpoint:

```bash
gradle clean test build -PserverApiVersion=1.21.11
```

The default artifact is:

```text
build/libs/CMDLoggerIntegration-3.3.0.jar
```

## Known limitations

- Delivery is not durable. Network failures, queue overload, or server shutdown can result in lost events.
- The queue is bounded; new events may be dropped when it is full.
- Bukkit player command preprocessing does not observe every command dispatched internally by other plugins.
- Optional console auditing covers standard console commands; it does not promise complete coverage of RCON, command blocks, or internal plugin dispatch.
- CI validates compilation and unit tests at selected API checkpoints; it does not replace testing on the exact server builds you operate.

Do not treat the plugin as a tamper-proof or legally complete audit system. Use a durable external audit pipeline when lossless or independently verifiable records are required.

## Documentation and license

- [Full user guide](docs/USER_GUIDE.md)
- [Security policy](SECURITY.md)
- [Audit notes](AUDIT.md)
- [MIT License](LICENSE)

---

<p align="center">
  <sub>Built for clearer staff operations — with privacy and operational limits kept visible.</sub>
</p>
