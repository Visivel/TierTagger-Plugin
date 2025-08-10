package net.tiertagger.commands;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisplayCommand implements CommandExecutor {
    
    private final TierTaggerPlugin plugin;
    
    public DisplayCommand(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tiertagger.display")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getTierDisplayManager().toggleDisplay(player);
        
        boolean displayEnabled = plugin.getTierDisplayManager().isPlayerDisplayEnabled(player);
        
        if (displayEnabled) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("display.enabled"));
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessage("display.disabled"));
        }
        
        return true;
    }
}