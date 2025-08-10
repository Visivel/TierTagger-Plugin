package net.tiertagger.config;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
    
    private final TierTaggerPlugin plugin;
    private FileConfiguration config;
    
    public ConfigurationManager(TierTaggerPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public ApiProvider getApiProvider() {
        String provider = config.getString("api.provider", "MCTIERS");
        try {
            return ApiProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid API provider: " + provider + ", using 'MCTIERS' as default");
            return ApiProvider.MCTIERS;
        }
    }
    
    public String getDefaultGamemode() {
        return config.getString("api.default_gamemode", "vanilla");
    }
    
    public int getApiTimeout() {
        return config.getInt("api.timeout", 10);
    }
    
    public int getCacheDuration() {
        return config.getInt("api.cache_duration", 30);
    }
    
    public boolean isDisplayEnabled() {
        return config.getBoolean("display.enabled", true);
    }
    
    public String getDisplayFormat() {
        return config.getString("display.format", "{tier} | {player}");
    }
    
    public boolean isShowInTablist() {
        return config.getBoolean("display.show_in_tablist", true);
    }
    
    public boolean isShowInChat() {
        return config.getBoolean("display.show_in_chat", true);
    }
    
    public boolean isDisplayShowNametag() {
        return config.getBoolean("display.display_show_nametag", true);
    }
    
    public boolean isDisplayShowTablist() {
        return config.getBoolean("display.display_show_tablist", false);
    }
    
    public String getLanguage() {
        return config.getString("language", "en_us");
    }
    
    public Map<String, TierColor> getTierColors() {
        Map<String, TierColor> colors = new HashMap<>();
        
        if (config.getConfigurationSection("tier_colors") != null) {
            for (String tier : config.getConfigurationSection("tier_colors").getKeys(false)) {
                String startColor = config.getString("tier_colors." + tier + ".start", "#FFFFFF");
                String endColor = config.getString("tier_colors." + tier + ".end", "#FFFFFF");
                colors.put(tier.toLowerCase(), new TierColor(startColor, endColor));
            }
        }
        
        return colors;
    }
    
    public String getDatabaseFile() {
        return config.getString("database.file", "plugins/TierTagger/database.db");
    }
    
    public int getDatabasePoolSize() {
        return config.getInt("database.pool_size", 10);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }
    
    public boolean isLogApiRequests() {
        return config.getBoolean("debug.log_api_requests", false);
    }
    
    public enum ApiProvider {
        MCTIERS,
        SOUTH_TIERS
    }
    
    public static class TierColor {
        private final String startColor;
        private final String endColor;
        
        public TierColor(String startColor, String endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }
        
        public String getStartColor() {
            return startColor;
        }
        
        public String getEndColor() {
            return endColor;
        }
        
        public Color getStartColorAsColor() {
            return Color.decode(startColor);
        }
        
        public Color getEndColorAsColor() {
            return Color.decode(endColor);
        }
    }
}