package net.tiertagger.listeners;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    private final TierTaggerPlugin plugin;
    
    public ChatListener(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigurationManager().isDisplayEnabled() ||
            !plugin.getTierDisplayManager().isGlobalDisplayEnabled() ||
            !plugin.getConfigurationManager().isShowInChat()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        if (!plugin.getTierDisplayManager().isPlayerDisplayEnabled(player)) {
            return;
        }
        
        String tier = plugin.getTierDisplayManager().getPlayerTier(player);
        
        if (tier != null && !tier.equals("UNRANKED")) {
            String coloredTier = getColoredTier(tier);
            String displayName = coloredTier + " §7| §f" + player.getName();
            
            event.setFormat(displayName + "§f: %2$s");
        }
    }
    
    private String getColoredTier(String tier) {
        switch (tier.toUpperCase()) {
            case "HT1": return "§c" + tier;
            case "LT1": return "§6" + tier;
            case "HT2": return "§e" + tier;
            case "LT2": return "§a" + tier;
            case "HT3": return "§b" + tier;
            case "LT3": return "§9" + tier;
            case "HT4": return "§d" + tier;
            case "LT4": return "§5" + tier;
            case "HT5": return "§7" + tier;
            case "LT5": return "§8" + tier;
            default: return "§f" + tier;
        }
    }
}