package net.tiertagger.commands;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TierTaggerCommand implements CommandExecutor {
    
    private final TierTaggerPlugin plugin;
    
    public TierTaggerCommand(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "help":
                sendHelpMessage(sender);
                return true;
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.unknown"));
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("tiertagger.reload")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }
        
        try {
            plugin.reloadConfiguration();
            plugin.getDatabaseManager().clearAllCache();
            
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.success"));
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.cache_cleared"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.reload.error", "error", e.getMessage()));
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.help.header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.help.reload"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.help.tier"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.help.display"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.help.tieroff"));
    }
}