package com.infinity.cmdloggerintegration;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;

public final class CMDLoggerIntegration extends JavaPlugin {
    private static final String DEFAULT_TITLE = "Staff Command Logger";
    private static final String DEFAULT_COLOR = "#5865F2";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!WebhookUrlValidator.isValid(getConfig().getString("webhook-url", ""))) {
            getLogger().severe("Invalid webhook-url. Set a valid Discord webhook URL in config.yml; plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand command = getCommand("cmdlogger");
        if (command == null) {
            getLogger().severe("Command cmdlogger is missing from plugin.yml; disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        MainCommand handler = new MainCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getLogger().info("CMDLoggerIntegration v" + getDescription().getVersion() + " enabled.");
    }

    String getEmbedTitle() {
        String title = getConfig().getString("embed.title", DEFAULT_TITLE);
        return title == null || title.trim().isEmpty() ? DEFAULT_TITLE : title.trim();
    }

    Color getEmbedColor() {
        String configured = getConfig().getString("embed.color", DEFAULT_COLOR);
        try {
            return Color.decode(configured == null ? DEFAULT_COLOR : configured.trim());
        } catch (NumberFormatException ex) {
            getLogger().warning("Invalid embed.color; using the default accent color.");
            return Color.decode(DEFAULT_COLOR);
        }
    }

    boolean isWebhookConfigured() {
        return WebhookUrlValidator.isValid(getConfig().getString("webhook-url", ""));
    }
}
