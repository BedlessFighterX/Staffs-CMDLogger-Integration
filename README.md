# CMDLoggerIntegration

A Spigot plugin that forwards commands executed by players with the cmdlogger.staff permission to a configured Discord webhook.

## Requirements
- Minecraft server API compatible with Spigot 1.16.5 (compile API baseline).
- Java 8-compatible runtime bytecode.
- Gradle 8.10.2 or GitHub Actions CI.
- A privately configured Discord webhook URL.

Compatibility with every Minecraft/server version is not guaranteed. Verify against the exact server version before production use.

## Build

    gradle clean test build

The JAR is written to build/libs/CMDLoggerIntegration-2.0.0.jar. Gradle is the supported build system; Maven has been removed.

## Install and configure
1. Build the JAR and place it in the server plugins/ directory.
2. Start the server once to generate plugins/CMDLoggerIntegration/config.yml, then stop it.
3. Set webhook-url to a Discord webhook URL. Never commit or share this URL.
4. Configure ignored-commands for command roots that may contain secrets. Matching is case-insensitive and ignores command arguments.
5. Restart the server. Grant cmdlogger.staff only to roles whose commands should be logged.

## Commands and permissions
- /cmdlogger — display plugin information.
- /cmdlogger reload — reload config; requires cmdlogger.admin.
- cmdlogger.staff — commands executed by this player are logged; defaults to operators.

## Privacy and operations
Command logs can contain passwords, tokens, personal data, or private arguments. Use an access-controlled Discord channel, exclude sensitive command roots, and restrict access to server config. The ignore list is not a substitute for avoiding secrets in commands.

Webhook requests run asynchronously and have bounded timeouts. Delivery failures are written to the server log; no persistent queue/retry policy exists yet. Review the repository audit and security notes before production use.

## Contributing
Run gradle clean test build before submitting changes. Do not commit live webhook URLs or server secrets.
