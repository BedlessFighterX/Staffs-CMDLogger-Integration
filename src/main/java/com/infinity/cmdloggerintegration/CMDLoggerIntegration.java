package com.infinity.cmdloggerintegration;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CMDLoggerIntegration extends JavaPlugin {
    private static final String DEFAULT_TITLE = "Staff Command Logger";
    private static final String DEFAULT_COLOR = "#5865F2";
    private ThreadPoolExecutor deliveryExecutor;
    private final AtomicBoolean queueWarningShown = new AtomicBoolean(false);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!WebhookUrlValidator.isValid(getConfig().getString("webhook-url", ""))) {
            getLogger().severe("Invalid webhook-url. Set a valid Discord webhook URL in config.yml; plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Bound worker count and queue size so command bursts cannot create unlimited network tasks.
        deliveryExecutor = new ThreadPoolExecutor(
                2, 2, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(256), runnable -> {
                    Thread thread = new Thread(runnable, "CMDLogger-Webhook");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());

        PluginCommand command = getCommand("cmdlogger");
        if (command == null) {
            getLogger().severe("Command cmdlogger is missing from plugin.yml; disabling plugin.");
            deliveryExecutor.shutdownNow();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        MainCommand handler = new MainCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getLogger().info("CMDLoggerIntegration v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (deliveryExecutor != null) deliveryExecutor.shutdownNow();
    }

    boolean submitDelivery(Runnable task) {
        if (deliveryExecutor == null || deliveryExecutor.isShutdown()) return false;
        try {
            deliveryExecutor.execute(task);
            queueWarningShown.set(false);
            return true;
        } catch (RejectedExecutionException ex) {
            if (queueWarningShown.compareAndSet(false, true)) {
                getLogger().warning("Webhook queue is full; some command logs are being dropped to protect server performance.");
            }
            return false;
        }
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
