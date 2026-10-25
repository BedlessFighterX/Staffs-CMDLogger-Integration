# CMDLoggerIntegration

A Paper/Spigot plugin that forwards staff player commands—and optionally server-console commands—to a configured Discord webhook, with configurable exclusions and privacy-first argument redaction.

## Minecraft and Java compatibility

The goal is one universal JAR for **Minecraft Java Edition 1.8.8 through the latest stable server API**. The code is compiled to Java 8 bytecode and deliberately uses the legacy Bukkit/Spigot API surface, while CI compiles and tests against API checkpoints spanning 1.8.8, 1.12.2, 1.16.5, 1.20.6, 1.21.11, and 26.3.

- **Minecraft 1.8.8–1.16.5:** Java 8 is supported by the server; the plugin bytecode targets Java 8.
- **Minecraft 1.17–1.21.11:** use the Java version required by your server (generally Java 17/21 depending on version).
- **Minecraft 26.1+:** use Java 25 or newer, as required by current server software.
- **Server software:** Spigot and Paper are the intended Bukkit-compatible platforms. Other forks may work but are not guaranteed.
- **Compatibility testing:** CI validates compilation and unit tests against API checkpoints. It does not replace runtime testing on every server build. Test on a staging server before production use.

The plugin descriptor intentionally omits api-version so legacy servers can load the same JAR. That makes it a legacy Bukkit plugin on newer servers; test startup warnings and behavior on your target Paper/Spigot build.

## Build

Requires JDK 25 and Gradle 9.1.0 for the current build configuration.

    gradle clean test build

By default, the universal JAR is compiled against the Spigot 1.8.8 API and written to build/libs/CMDLoggerIntegration-3.3.0.jar. To compile against a specific API checkpoint:

    gradle clean test build -PserverApiVersion=1.21.11
    gradle clean test build -PserverApiVersion=26.3

Gradle is the supported build system; Maven has been removed.

## Install and configure

1. Back up the server and existing plugin config.
2. Place the JAR in plugins/ and start the server once to generate config.
3. Set webhook-url in plugins/CMDLoggerIntegration/config.yml. Treat it as a password; never commit or share it.
4. The default redact-all-arguments: true setting records command roots while keeping every argument on the server. Set it to false only if your audit policy explicitly requires arguments.
5. Review ignored-commands (fully suppress a command), redacted-commands (redact arguments for sensitive roots when global redaction is disabled), and ignored-players.
6. Restart the server and grant cmdlogger.staff only to staff whose commands should be audited. Existing explicit config values are preserved during upgrades; change redact-all-arguments manually if you want the privacy-first setting on an older installation.

Existing configurations are retained and new default keys are merged on startup. Review the generated defaults after upgrading.

## Commands and permissions

- /cmdlogger help — styled help.
- /cmdlogger reload — validate and reload configuration; invalid webhook URLs are rejected without replacing active settings. Requires cmdlogger.admin.
- /cmdlogger status — show configuration, privacy, and delivery queue status; requires cmdlogger.admin.
- /cmdlogger diagnostics — alias for detailed status and queue diagnostics; requires cmdlogger.admin.
- /cmdlogger test — send a test embed to verify webhook delivery; requires cmdlogger.admin.
- cmdlogger.staff — commands from this player are logged; defaults to operators.
- cmdlogger.use — use the plugin command; defaults to everyone.
- cmdlogger.admin — reload configuration, test webhook delivery, and inspect diagnostics; defaults to operators.

Tab completion only suggests administrative subcommands to senders with the admin permission.

## Audit embeds and performance

Discord embeds can include the configured server name, world, operator status, and event timestamp. Each is configurable under embed. Console command auditing is opt-in via log-console-commands: true; it is disabled by default. Network calls run away from the game thread with bounded timeouts and a fixed-size queue. If the queue fills, new events are dropped and a warning is logged rather than allowing unbounded memory growth. Queue depth and completed tasks are visible in diagnostics.

## Security and privacy

Command logs can contain passwords, tokens, personal data, or private arguments. The default redaction list covers common authentication roots, but plugin-specific commands and aliases still need review. redact-all-arguments: true is the safer setting if command arguments should never leave the server. ignored-players can exclude selected player names from logging. Use an access-controlled Discord channel, restrict filesystem access, and rotate the webhook immediately if it leaks.

Webhook URLs are restricted to HTTPS Discord webhook endpoints. Redirects are disabled for webhook POSTs so payloads cannot be forwarded to a redirect target. Webhook credentials and remote response bodies are not printed in errors. Discord mentions are disabled in outgoing payloads and untrusted command text has Markdown/control characters sanitized to reduce spoofing. New installations redact all arguments by default. Reload validates the candidate webhook URL before replacing active configuration.

## Validation and limitations

Run gradle clean test build before submitting changes. CI compiles against multiple server API checkpoints and runs unit tests. Releases are generated by the release workflow for master updates after the build succeeds.

Delivery is not durable: network failures, overload, and shutdown can lose audit events; there is no persistent retry queue. Bukkit's player command preprocess event does not observe every command dispatched internally by plugins. Optional console auditing observes standard server-console commands only; it does not promise complete coverage of RCON, command blocks, or internal plugin dispatch. A source review cannot prove that software is vulnerability-free. Compatibility claims are bounded by the tested API checkpoints and do not mean every historical/current fork has been runtime-tested.
