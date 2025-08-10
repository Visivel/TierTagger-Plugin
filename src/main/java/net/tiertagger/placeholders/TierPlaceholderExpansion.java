package net.tiertagger.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.models.PlayerTierData;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TierPlaceholderExpansion extends PlaceholderExpansion {

    // God forgive my code and my sins
    
    private final TierTaggerPlugin plugin;
    
    public TierPlaceholderExpansion(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "tiertagger";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        if (!plugin.getTierDisplayManager().isPlayerDisplayEnabled(player) ||
            plugin.getTierDisplayManager().isTierHidden(player) ||
            !plugin.getTierDisplayManager().isGlobalDisplayEnabled()) {
            return getDefaultPlaceholderValue(params);
        }
        
        String uuid = player.getUniqueId().toString();
        
        try {
            Optional<PlayerTierData> cachedData = plugin.getDatabaseManager().getCachedTierData(uuid).get();
            
            if (!cachedData.isPresent()) {
                return getDefaultPlaceholderValue(params);
            }
            
            PlayerTierData tierData = cachedData.get();
            
            switch (params.toLowerCase()) {
                case "tier":
                case "best_tier":
                    return tierData.getBestTier();
                    
                case "tier_formatted":
                case "best_tier_formatted":
                    return formatTierDisplay(tierData.getBestTier());
                    
                case "points":
                    return String.valueOf(tierData.getPoints());
                    
                case "overall":
                case "overall_rank":
                    return String.valueOf(tierData.getOverall());
                    
                case "region":
                    return tierData.getRegion() != null ? tierData.getRegion() : "";
                    
                case "vanilla":
                case "vanilla_tier":
                    return tierData.getTierForGamemode("vanilla");
                    
                case "sword":
                case "sword_tier":
                    return tierData.getTierForGamemode("sword");
                    
                case "pot":
                case "pot_tier":
                    return tierData.getTierForGamemode("pot");
                    
                case "uhc":
                case "uhc_tier":
                    return tierData.getTierForGamemode("uhc");
                    
                case "axe":
                case "axe_tier":
                    return tierData.getTierForGamemode("axe");
                    
                case "nethop":
                case "nethop_tier":
                    return tierData.getTierForGamemode("nethop");
                    
                case "mace":
                case "mace_tier":
                    return tierData.getTierForGamemode("mace");
                    
                case "smp":
                case "smp_tier":
                    return tierData.getTierForGamemode("smp");
                    
                case "crystal":
                case "crystal_tier":
                    return tierData.getTierForGamemode("crystal");
                    
                case "ht1":
                    return tierData.getBestTier().equals("HT1") ? "HT1" : "";
                    
                case "lt1":
                    return tierData.getBestTier().equals("LT1") ? "LT1" : "";
                    
                case "ht2":
                    return tierData.getBestTier().equals("HT2") ? "HT2" : "";
                    
                case "lt2":
                    return tierData.getBestTier().equals("LT2") ? "LT2" : "";
                    
                case "ht3":
                    return tierData.getBestTier().equals("HT3") ? "HT3" : "";
                    
                case "lt3":
                    return tierData.getBestTier().equals("LT3") ? "LT3" : "";
                    
                case "ht4":
                    return tierData.getBestTier().equals("HT4") ? "HT4" : "";
                    
                case "lt4":
                    return tierData.getBestTier().equals("LT4") ? "LT4" : "";
                    
                case "ht5":
                    return tierData.getBestTier().equals("HT5") ? "HT5" : "";
                    
                case "lt5":
                    return tierData.getBestTier().equals("LT5") ? "LT5" : "";
                    
                default:
                    return getDefaultPlaceholderValue(params);
            }
            
        } catch (Exception e) {
            return getDefaultPlaceholderValue(params);
        }
    }
    
    private String getDefaultPlaceholderValue(String params) {
        switch (params.toLowerCase()) {
            case "tier":
            case "best_tier":
            case "vanilla":
            case "sword":
            case "pot":
            case "uhc":
            case "axe":
            case "nethop":
            case "mace":
            case "smp":
            case "crystal":
                return "UNRANKED";
                
            case "tier_formatted":
            case "best_tier_formatted":
                return "§fUnranked";
                
            case "points":
            case "overall":
            case "overall_rank":
                return "0";
                
            case "region":
                return "";
                
            case "ht1":
            case "lt1":
            case "ht2":
            case "lt2":
            case "ht3":
            case "lt3":
            case "ht4":
            case "lt4":
            case "ht5":
            case "lt5":
                return "";
                
            default:
                return "";
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
}