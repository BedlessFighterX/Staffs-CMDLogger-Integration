# CMDLoggerIntegration

A Bukkit plugin that sends selected staff command events to a Discord webhook. Console command logging is optional, and command arguments are redacted by default.

[Build workflow](https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/actions/workflows/ci.yml) · [Releases](https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/releases)

## Features

- Send staff command events to a Discord webhook.
- Optionally log commands entered in the server console.
- Ignore selected players and command roots.
- Redact command arguments, including arguments for configured sensitive commands.
- Reload the configuration and check delivery status from in-game commands.
- Send webhook requests asynchronously using a bounded queue.

## Compatibility

The CI workflow compiles and runs unit tests against these Spigot API checkpoints:

| API checkpoint | CI checks |
|---|---|
| 1.8.8 | Compile and unit tests |
| 1.12.2 | Compile and unit tests |
| 1.16.5 | Compile and unit tests |
| 1.20.6 | Compile and unit tests |
| 1.21.11 | Compile and unit tests |

The plugin is compiled to Java 8 bytecode. The server must still use the Java runtime required by its Minecraft version. Paper and other Bukkit-compatible forks may work, but they are not all tested.

These are API-level CI checks, not live tests on every Minecraft release or server fork. Compatibility outside the listed checkpoints has not been verified.

## Installation

1. Download the JAR from [GitHub Releases](https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration/releases).
2. Stop the server and back up the server and plugin configuration.
3. Put the JAR in the server's `plugins/` directory.
4. Start the server once to generate `plugins/CMDLoggerIntegration/config.yml`.
5. Add your Discord webhook URL to `webhook-url` in the generated configuration.
6. Give `cmdlogger.staff` to the players whose commands should be logged.
7. Review the privacy settings, then restart the server or run `/cmdlogger reload`.

The plugin keeps existing configuration values when it adds new defaults during an upgrade. Review the configuration after updating, especially `redact-all-arguments`.

## Configuration

The default settings keep command arguments on the server and leave console logging disabled:

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

See [docs/USER_GUIDE.md](docs/USER_GUIDE.md) for the full configuration reference, webhook setup, and troubleshooting.

## Commands and permissions

| Command or permission | Access | Description |
|---|---|---|
| `/cmdlogger help` | `cmdlogger.use` | Show the help message |
| `/cmdlogger reload` | `cmdlogger.admin` | Validate and reload the configuration |
| `/cmdlogger status` | `cmdlogger.admin` | Show configuration and queue status |
| `/cmdlogger diagnostics` | `cmdlogger.admin` | Show detailed queue information |
| `/cmdlogger test` | `cmdlogger.admin` | Send a test embed |
| `cmdlogger.staff` | Operators by default | Log commands issued by the player |
| `cmdlogger.use` | Everyone by default | Allow use of the plugin command |
| `cmdlogger.admin` | Operators by default | Allow administrative commands |

## Privacy

Command arguments can contain passwords, tokens, or other private information. With `redact-all-arguments: true`, only the command root is sent to Discord. This is the recommended setting for most servers.

When argument logging is enabled, review `redacted-commands` and add authentication commands from other plugins. The built-in list is only a starting point and may not include every command or alias used by your server.

Treat the Discord webhook URL like a password. Do not commit it to the repository or post it in issues. Restrict access to the Discord channel and rotate the webhook if it is exposed.

## Build

The current build uses JDK 25 and Gradle 9.1.0. The plugin itself is compiled to Java 8 bytecode.

```bash
gradle clean test build
```

To build against a particular API checkpoint:

```bash
gradle clean test build -PserverApiVersion=1.21.11
```

The default JAR is written to `build/libs/CMDLoggerIntegration-3.3.0.jar`.

## Limitations

- Events can be lost if the network request fails, the queue fills, or the server shuts down. The queue is not persistent.
- The Bukkit player command event does not capture every command run internally by other plugins.
- Console logging covers standard server-console commands; it does not guarantee coverage of RCON, command blocks, or internal plugin dispatch.
- CI checks selected API versions, but you should test the plugin on the server versions you actually use.

This plugin should not be treated as a tamper-proof or lossless audit system.

## More information

- [User guide](docs/USER_GUIDE.md)
- [Security policy](SECURITY.md)
- [Audit notes](AUDIT.md)
- [MIT License](LICENSE)
