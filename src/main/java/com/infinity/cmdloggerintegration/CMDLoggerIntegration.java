package com.infinity.cmdloggerintegration;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.URI;

public final class CMDLoggerIntegration extends JavaPlugin {
 private static final String DEFAULT_TITLE = "Staff Command Logger";
 private static final String DEFAULT_COLOR = "#020101";
 @Override public void onEnable() {
  saveDefaultConfig();
  if (!isValidWebhookUrl(getConfig().getString("webhook-url", ""))) {
   getLogger().severe("Invalid webhook-url. Configure a Discord webhook URL in config.yml; plugin disabled.");
   getServer().getPluginManager().disablePlugin(this); return;
  }
  getServer().getPluginManager().registerEvents(new ChatListener(this), this);
  PluginCommand command = getCommand("cmdlogger");
  if (command == null) { getLogger().severe("Command cmdlogger is missing from plugin.yml; disabling plugin."); getServer().getPluginManager().disablePlugin(this); return; }
  command.setExecutor(new MainCommand(this)); getLogger().info("CMDLoggerIntegration enabled.");
 }
 static boolean isValidWebhookUrl(String value) {
  if (value == null || value.trim().isEmpty() || "your-webhook-url".equalsIgnoreCase(value.trim())) return false;
  try {
   URI uri = URI.create(value.trim()); String host = uri.getHost();
   if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) return false;
   boolean discordHost = "discord.com".equalsIgnoreCase(host) || "discordapp.com".equalsIgnoreCase(host);
   return discordHost && uri.getPath() != null && uri.getPath().matches("/api/webhooks/[0-9]+/[^/]+/?");
  } catch (IllegalArgumentException ex) { return false; }
 }
 String getEmbedTitle() { String title = getConfig().getString("embed.title", DEFAULT_TITLE); return title == null || title.trim().isEmpty() ? DEFAULT_TITLE : title.trim(); }
 java.awt.Color getEmbedColor() {
  String configured = getConfig().getString("embed.color", DEFAULT_COLOR);
  try { return java.awt.Color.decode(configured == null ? DEFAULT_COLOR : configured.trim()); }
  catch (NumberFormatException ex) { getLogger().warning("Invalid embed.color; using " + DEFAULT_COLOR + "."); return java.awt.Color.decode(DEFAULT_COLOR); }
 }
}
