package net.tiertagger.listeners;

import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.models.PlayerTierData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class PlayerJoinListener implements Listener {
    
    private final TierTaggerPlugin plugin;
    
    public PlayerJoinListener(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String username = player.getName();
        
        plugin.getDatabaseManager().getCachedTierData(uuid).thenAccept(cachedData -> {
            if (cachedData.isPresent()) {
                plugin.getTierDisplayManager().setTier(player, cachedData.get());
                
                if (plugin.getConfigurationManager().isDebugEnabled()) {
                    plugin.getLogger().info("Loaded cached tier data for " + username);
                }
            } else {
                fetchAndCacheTierData(player, uuid, username);
            }
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTierDisplayManager().clearTier(player);
    }
    
    private void fetchAndCacheTierData(Player player, String uuid, String username) {
        boolean isUuid = plugin.getConfigurationManager().getApiProvider() ==
            net.tiertagger.config.ConfigurationManager.ApiProvider.MCTIERS;
        String identifier = isUuid ? uuid : username;
        
        plugin.getApiServiceManager().fetchPlayerTierData(identifier, isUuid).thenAccept(tierData -> {
            if (tierData != null) {
                plugin.getDatabaseManager().cacheTierData(uuid, username, tierData);
                plugin.getTierDisplayManager().setTier(player, tierData);
                
                if (plugin.getConfigurationManager().isDebugEnabled()) {
                    plugin.getLogger().info("Fetched and cached tier data for " + username);
                }
            } else {
                if (plugin.getConfigurationManager().isDebugEnabled()) {
                    plugin.getLogger().info("No tier data found for " + username);
                }
            }
        });
    }
}