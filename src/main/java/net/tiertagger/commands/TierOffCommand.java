package net.tiertagger.commands;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TierOffCommand implements CommandExecutor {
    
    private final TierTaggerPlugin plugin;
    
    public TierOffCommand(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tiertagger.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }
        
        plugin.getTierDisplayManager().switchGlobalMode();
        
        boolean globalEnabled = plugin.getTierDisplayManager().isGlobalDisplayEnabled();
        
        if (globalEnabled) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("tieroff.enabled"));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("tieroff.disabled"));
        }
        
        return true;
    }
}