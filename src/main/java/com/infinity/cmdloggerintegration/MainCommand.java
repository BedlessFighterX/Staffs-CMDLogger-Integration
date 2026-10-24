package com.infinity.cmdloggerintegration;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MainCommand implements CommandExecutor, TabCompleter {
    private final CMDLoggerIntegration plugin;

    public MainCommand(CMDLoggerIntegration plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("cmdlogger.admin")) {
                sender.sendMessage(ChatColor.RED + "✖ You do not have permission to reload CMDLogger.");
                return true;
            }
            plugin.reloadConfig();
            if (!plugin.isWebhookConfigured()) {
                sender.sendMessage(ChatColor.RED + "✖ Invalid webhook URL. Fix config.yml, then reload again.");
                return true;
            }
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            sender.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.WHITE + "CMDLogger configuration reloaded.");
            sender.sendMessage(ChatColor.GRAY + "  Excluded commands: " + plugin.getConfig().getStringList("ignored-commands").size());
            sender.sendMessage(ChatColor.GRAY + "  Redacted command roots: " + plugin.getConfig().getStringList("redacted-commands").size());
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        if (args.length == 1 && "status".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("cmdlogger.admin")) {
                sender.sendMessage(ChatColor.RED + "✖ You do not have permission to view plugin status.");
                return true;
            }
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━ " + ChatColor.AQUA + " CMDLogger Status " + ChatColor.DARK_GRAY + "━━━━");
            sender.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Webhook: " + (plugin.isWebhookConfigured() ? ChatColor.GREEN + "configured" : ChatColor.RED + "invalid"));
            sender.sendMessage(ChatColor.GRAY + "Staff command logging: " + ChatColor.GREEN + "enabled");
            sender.sendMessage(ChatColor.GRAY + "Excluded roots: " + ChatColor.WHITE + plugin.getConfig().getStringList("ignored-commands").size());
            sender.sendMessage(ChatColor.GRAY + "Redacted roots: " + ChatColor.WHITE + plugin.getConfig().getStringList("redacted-commands").size());
            return true;
        }

        if (args.length == 0 || (args.length == 1 && "help".equalsIgnoreCase(args[0]))) {
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            sender.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "  CMDLogger " + ChatColor.GRAY + "v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "  Secure staff command auditing");
            sender.sendMessage(ChatColor.DARK_GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " help " + ChatColor.GRAY + "— show this help");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload " + ChatColor.GRAY + "— reload configuration");
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " status " + ChatColor.GRAY + "— show audit status (admin)");
            sender.sendMessage(ChatColor.DARK_GRAY + "Project: " + ChatColor.BLUE + "github.com/BedlessFighterX/Staffs-CMDLogger-Integration");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use " + ChatColor.YELLOW + "/" + label + " help" + ChatColor.RED + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return Collections.emptyList();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        options.add("help");
        if (sender.hasPermission("cmdlogger.admin")) {
            options.add("reload");
            options.add("status");
        }
        options.removeIf(option -> !option.startsWith(prefix));
        return options;
    }
}
