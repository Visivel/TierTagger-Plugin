package net.tiertagger.commands;

import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.models.PlayerTierData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TierCommand implements CommandExecutor {
    
    private final TierTaggerPlugin plugin;
    
    public TierCommand(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tiertagger.tiersearch")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.no_permission", new String[0]));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("commands.tier.usage", new String[0]));
            return true;
        }
        
        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        sender.sendMessage(plugin.getLanguageManager().getMessage("commands.tier.searching", "player", targetPlayerName));
        
        boolean isUuid = plugin.getConfigurationManager().getApiProvider() ==
            net.tiertagger.config.ConfigurationManager.ApiProvider.MCTIERS;
        
        if (targetPlayer != null && isUuid) {
            searchPlayerTier(sender, targetPlayer.getUniqueId().toString(), targetPlayerName, true);
        } else {
            searchPlayerTier(sender, null, targetPlayerName, false);
        }
        
        return true;
    }
    
    private void searchPlayerTier(CommandSender sender, String uuid, String username, boolean hasUuid) {
        boolean isUuid = plugin.getConfigurationManager().getApiProvider() ==
            net.tiertagger.config.ConfigurationManager.ApiProvider.MCTIERS;
        
        if (hasUuid && isUuid && uuid != null) {
            plugin.getDatabaseManager().getCachedTierData(uuid).thenAccept(cachedData -> {
                if (cachedData.isPresent()) {
                    displayTierInformation(sender, cachedData.get(), username, true);
                } else {
                    fetchFromApi(sender, uuid, username, true);
                }
            });
        } else {
            fetchFromApi(sender, null, username, false);
        }
    }
    
    private void fetchFromApi(CommandSender sender, String uuid, String username, boolean hasUuid) {
        String identifier = hasUuid ? uuid : username;
        
        plugin.getApiServiceManager().fetchPlayerTierData(identifier, hasUuid).thenAccept(tierData -> {
            if (tierData != null) {
                if (hasUuid) {
                    plugin.getDatabaseManager().cacheTierData(uuid, username, tierData);
                }
                displayTierInformation(sender, tierData, username, false);
            } else {
                sender.sendMessage(plugin.getLanguageManager().getMessage("commands.tier.not_found", "player", username));
            }
        });
    }
    
    private void displayTierInformation(CommandSender sender, PlayerTierData tierData, String username, boolean fromCache) {
        String cacheIndicator = fromCache ? " §7(cached)" : "";
        
        sender.sendMessage("§6=== Tier Information for " + username + cacheIndicator + " ===");
        
        String bestTier = tierData.getBestTier();
        sender.sendMessage("§eBest Tier: §f" + formatTierDisplay(bestTier));
        
        if (tierData.getGamemodes() != null && !tierData.getGamemodes().isEmpty()) {
            sender.sendMessage("§eGamemode Tiers:");
            
            String[] gamemodes = {"vanilla", "sword", "pot", "uhc", "axe", "nethop", "mace", "smp"};
            
            for (String gamemode : gamemodes) {
                String tier = tierData.getTierForGamemode(gamemode);
                if (!tier.equals("UNRANKED")) {
                    PlayerTierData.GamemodeTier gamemodeData = tierData.getGamemodes().get(gamemode);
                    if (gamemodeData != null) {
                        sender.sendMessage("§7  " + capitalizeFirst(gamemode) + ": §f" + 
                            formatTierDisplay(tier) + " §7(#" + gamemodeData.getPosition() + ")");
                    }
                }
            }
        }
        
        if (tierData.getRegion() != null && !tierData.getRegion().isEmpty()) {
            sender.sendMessage("§eRegion: §f" + tierData.getRegion());
        }
        
        if (tierData.getPoints() > 0) {
            sender.sendMessage("§ePoints: §f" + tierData.getPoints());
        }
        
        if (tierData.getOverall() > 0) {
            sender.sendMessage("§eOverall Rank: §f#" + tierData.getOverall());
        }
    }
    
    private String formatTierDisplay(String tier) {
        switch (tier.toUpperCase()) {
            case "HT1": return "§c§lHigh Tier 1";
            case "LT1": return "§6§lLow Tier 1";
            case "HT2": return "§e§lHigh Tier 2";
            case "LT2": return "§a§lLow Tier 2";
            case "HT3": return "§b§lHigh Tier 3";
            case "LT3": return "§9§lLow Tier 3";
            case "HT4": return "§d§lHigh Tier 4";
            case "LT4": return "§5§lLow Tier 4";
            case "HT5": return "§7§lHigh Tier 5";
            case "LT5": return "§8§lLow Tier 5";
            default: return "§fUnranked";
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}