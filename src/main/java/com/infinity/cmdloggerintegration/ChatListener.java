package com.infinity.cmdloggerintegration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.io.IOException;
import java.util.List;

public final class ChatListener implements Listener {
 private final CMDLoggerIntegration plugin;
 public ChatListener(CMDLoggerIntegration plugin) { this.plugin = plugin; }
 @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 public void onCommandSend(PlayerCommandPreprocessEvent event) {
  Player player = event.getPlayer();
  if (!player.hasPermission("cmdlogger.staff")) return;
  String commandLine = event.getMessage();
  List<String> ignored = plugin.getConfig().getStringList("ignored-commands");
  if (CommandFilter.shouldIgnore(commandLine, ignored)) return;
  // Snapshot Bukkit state on the main thread; do not access Player from the worker.
  final String playerName = player.getName();
  final String worldName = player.getWorld().getName();
  final boolean operator = player.isOp();
  final String title = plugin.getEmbedTitle();
  final java.awt.Color color = plugin.getEmbedColor();
  final String webhookUrl = plugin.getConfig().getString("webhook-url", "");
  plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
   try {
    DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
    webhook.setUsername("Staff Command Logger");
    webhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle(title)
      .addField("Executed Command", truncate(commandLine, 1024), false)
      .addField("Player Username", truncate(playerName, 256), true)
      .addField("World", truncate(worldName, 256), true)
      .addField("Is player an operator?", operator ? "Yes" : "No", true)
      .setFooter("Powered by CMDLoggerIntegration", null).setColor(color));
    webhook.execute();
   } catch (IOException | IllegalArgumentException ex) {
    plugin.getLogger().warning("Could not deliver a command-log event to Discord: " + ex.getMessage());
   }
  });
 }
 private static String truncate(String value, int limit) { if (value == null) return "(unknown)"; return value.length() <= limit ? value : value.substring(0, limit - 1) + "…"; }
}
