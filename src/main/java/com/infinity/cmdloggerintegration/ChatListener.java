package com.infinity.cmdloggerintegration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.IOException;
import java.time.Instant;

public final class ChatListener implements Listener {
    private final CMDLoggerIntegration plugin;

    public ChatListener(CMDLoggerIntegration plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("cmdlogger.staff")) return;

        String commandLine = event.getMessage();
        if (CommandFilter.shouldIgnore(commandLine, plugin.getConfig().getStringList("ignored-commands"))) return;
        String safeCommand = CommandFilter.redactArguments(
                commandLine, plugin.getConfig().getStringList("redacted-commands"));

        // Snapshot Bukkit state on the server thread; the bounded executor does network I/O.
        final String playerName = player.getName();
        final String playerUuid = player.getUniqueId().toString();
        final String worldName = player.getWorld().getName();
        final boolean operator = player.isOp();
        final String title = plugin.getEmbedTitle();
        final java.awt.Color color = plugin.getEmbedColor();
        final String webhookUrl = plugin.getConfig().getString("webhook-url", "");
        final boolean showOperator = plugin.getConfig().getBoolean("embed.show-operator-status", true);
        final boolean showUuid = plugin.getConfig().getBoolean("embed.show-player-uuid", false);
        final boolean showWorld = plugin.getConfig().getBoolean("embed.show-world", true);
        final Instant eventTime = Instant.now();

        plugin.submitDelivery(() -> {
            try {
                DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                webhook.setUsername("Staff Command Logger");
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                        .setTitle(title)
                        .setTimestamp(eventTime)
                        .addField("⌨ Executed command", truncate(safeCommand, 1024), false)
                        .addField("👤 Staff member", truncate(playerName, 256), true);
                if (showWorld) embed.addField("🌍 World", truncate(worldName, 256), true);
                if (showUuid) embed.addField("🪪 Player UUID", playerUuid, false);
                if (showOperator) embed.addField("🛡 Operator", operator ? "Yes" : "No", true);
                embed.setFooter("CMDLoggerIntegration • Staff audit", null).setColor(color);
                webhook.addEmbed(embed);
                webhook.execute();
            } catch (IOException | IllegalArgumentException ex) {
                // Do not print the webhook URL or remote response body to logs.
                plugin.getLogger().warning("Could not deliver a command-log event to Discord (" +
                        ex.getClass().getSimpleName() + ").");
            }
        });
    }

    private static String truncate(String value, int limit) {
        if (value == null) return "(unknown)";
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
