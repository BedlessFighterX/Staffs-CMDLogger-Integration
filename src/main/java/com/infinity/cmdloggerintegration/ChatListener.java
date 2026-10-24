package com.infinity.cmdloggerintegration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public final class ChatListener implements Listener {
    private final CMDLoggerIntegration plugin;

    public ChatListener(CMDLoggerIntegration plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("cmdlogger.staff")) return;

        String playerName = player.getName();
        if (containsIgnoreCase(plugin.getConfig().getStringList("ignored-players"), playerName)) return;

        String commandLine = event.getMessage();
        if (CommandFilter.shouldIgnore(commandLine, plugin.getConfig().getStringList("ignored-commands"))) return;
        String safeCommand = CommandFilter.sanitizeForDiscord(
                plugin.getConfig().getBoolean("redact-all-arguments", true)
                        ? CommandFilter.redactAllArguments(commandLine)
                        : CommandFilter.redactArguments(commandLine, plugin.getConfig().getStringList("redacted-commands")));

        // Snapshot Bukkit state on the server thread; the bounded executor does network I/O.
        final String worldName = player.getWorld().getName();
        final boolean operator = player.isOp();
        final String title = plugin.getEmbedTitle();
        final java.awt.Color color = plugin.getEmbedColor();
        final String webhookUrl = plugin.getConfig().getString("webhook-url", "");
        final boolean showOperator = plugin.getConfig().getBoolean("embed.show-operator-status", true);
        final boolean showWorld = plugin.getConfig().getBoolean("embed.show-world", true);
        final boolean showServer = plugin.getConfig().getBoolean("embed.show-server", true);
        final boolean showTimestamp = plugin.getConfig().getBoolean("embed.show-timestamp", true);
        final String serverName = plugin.getConfig().getString("embed.server-name", "Minecraft Server");
        final Instant occurredAt = Instant.now();

        plugin.submitDelivery(() -> {
            try {
                DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                webhook.setUsername("Staff Command Logger");
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                        .setTitle(title)
                        .addField("⌨ Executed command", truncate(safeCommand, 1024), false)
                        .addField("👤 Staff member", truncate(playerName, 256), true)
                        .setFooter("CMDLoggerIntegration • Staff audit", null)
                        .setColor(color);
                if (showWorld) embed.addField("🌍 World", truncate(worldName, 256), true);
                if (showOperator) embed.addField("🛡 Operator", operator ? "Yes" : "No", true);
                if (showServer && serverName != null && !serverName.trim().isEmpty()) {
                    embed.addField("🖥 Server", truncate(serverName.trim(), 256), true);
                }
                if (showTimestamp) embed.setTimestamp(occurredAt);
                webhook.addEmbed(embed);
                webhook.execute();
            } catch (IOException | IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not deliver a command-log event to Discord (" +
                        ex.getClass().getSimpleName() + ").");
            }
        });
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onConsoleCommand(ServerCommandEvent event) {
        if (!plugin.getConfig().getBoolean("log-console-commands", false)) return;
        // Deliberately exclude command blocks and other non-console senders.
        if (!(event.getSender() instanceof ConsoleCommandSender)) return;

        String commandLine = event.getCommand();
        if (CommandFilter.shouldIgnore(commandLine, plugin.getConfig().getStringList("ignored-commands"))) return;
        String safeCommand = CommandFilter.sanitizeForDiscord(
                plugin.getConfig().getBoolean("redact-all-arguments", true)
                        ? CommandFilter.redactAllArguments(commandLine)
                        : CommandFilter.redactArguments(commandLine, plugin.getConfig().getStringList("redacted-commands")));

        final String title = plugin.getEmbedTitle();
        final java.awt.Color color = plugin.getEmbedColor();
        final String webhookUrl = plugin.getConfig().getString("webhook-url", "");
        final boolean showServer = plugin.getConfig().getBoolean("embed.show-server", true);
        final boolean showTimestamp = plugin.getConfig().getBoolean("embed.show-timestamp", true);
        final String serverName = plugin.getConfig().getString("embed.server-name", "Minecraft Server");
        final Instant occurredAt = Instant.now();

        plugin.submitDelivery(() -> {
            try {
                DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                webhook.setUsername("Staff Command Logger");
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                        .setTitle(title)
                        .addField("⌨ Executed command", truncate(safeCommand, 1024), false)
                        .addField("🖥 Command source", "Server console", true)
                        .setFooter("CMDLoggerIntegration • Console audit", null)
                        .setColor(color);
                if (showServer && serverName != null && !serverName.trim().isEmpty()) {
                    embed.addField("🌐 Server", truncate(serverName.trim(), 256), true);
                }
                if (showTimestamp) embed.setTimestamp(occurredAt);
                webhook.addEmbed(embed);
                webhook.execute();
            } catch (IOException | IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not deliver a console command-log event to Discord (" +
                        ex.getClass().getSimpleName() + ").");
            }
        });
    }

    private static boolean containsIgnoreCase(List<String> values, String target) {
        if (values == null || target == null) return false;
        return values.stream().filter(value -> value != null)
                .anyMatch(value -> value.trim().equalsIgnoreCase(target));
    }

    private static String truncate(String value, int limit) {
        if (value == null) return "(unknown)";
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
