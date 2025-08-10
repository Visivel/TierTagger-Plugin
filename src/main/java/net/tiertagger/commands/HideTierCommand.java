package net.tiertagger.commands;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideTierCommand implements CommandExecutor {
    
    private final TierTaggerPlugin plugin;
    
    public HideTierCommand(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("tiertagger.hidetier")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission"));
            return true;
        }
        
        plugin.getTierDisplayManager().switchTierVisibility(player);
        
        boolean isHidden = plugin.getTierDisplayManager().isTierHidden(player);
        if (isHidden) {
            player.sendMessage(plugin.getLanguageManager().getMessage("hidetier.hidden", new String[0]));
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessage("hidetier.shown", new String[0]));
        }
        
        return true;
    }
}