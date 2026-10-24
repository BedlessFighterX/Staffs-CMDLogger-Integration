package com.infinity.cmdloggerintegration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
public final class MainCommand implements CommandExecutor {
 private final CMDLoggerIntegration plugin;
 public MainCommand(CMDLoggerIntegration plugin) { this.plugin = plugin; }
 @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
  if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
   if (!sender.hasPermission("cmdlogger.admin")) { sender.sendMessage(ChatColor.RED + "You do not have permission to reload CMDLogger."); return true; }
   plugin.reloadConfig();
   if (!CMDLoggerIntegration.isValidWebhookUrl(plugin.getConfig().getString("webhook-url", ""))) { sender.sendMessage(ChatColor.RED + "Invalid webhook URL. Logging will not work until fixed."); return true; }
   sender.sendMessage(ChatColor.GREEN + "CMDLogger configuration reloaded."); return true;
  }
  sender.sendMessage(ChatColor.GOLD + "CMDLoggerIntegration " + plugin.getDescription().getVersion());
  sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " reload");
  sender.sendMessage(ChatColor.GRAY + "Project: https://github.com/BedlessFighterX/Staffs-CMDLogger-Integration"); return true;
 }
}
