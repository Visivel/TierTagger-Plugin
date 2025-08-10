package net.tiertagger;

import net.tiertagger.cache.DatabaseManager;
import net.tiertagger.commands.DisplayCommand;
import net.tiertagger.commands.HideTierCommand;
import net.tiertagger.commands.TierCommand;
import net.tiertagger.commands.TierOffCommand;
import net.tiertagger.commands.TierTaggerCommand;
import net.tiertagger.config.ConfigurationManager;
import net.tiertagger.lang.LanguageManager;
import net.tiertagger.listeners.ChatListener;
import net.tiertagger.listeners.PlayerDisplayListener;
import net.tiertagger.listeners.PlayerJoinListener;
import net.tiertagger.placeholders.TierPlaceholderExpansion;
import net.tiertagger.services.ApiServiceManager;
import net.tiertagger.services.TierDisplayManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TierTaggerPlugin extends JavaPlugin {
    
    private ConfigurationManager configurationManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private ApiServiceManager apiServiceManager;
    private TierDisplayManager tierDisplayManager;
    private PlayerDisplayListener playerDisplayListener;
    private ConfigurationManager.ApiProvider previousApiProvider;
    
    @Override
    public void onEnable() {
        initializeComponents();
        registerCommands();
        registerListeners();
        registerPlaceholders();
        
        getLogger().info("TierTagger has been enabled successfully");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("TierTagger has been disabled");
    }
    
    private void initializeComponents() {
        configurationManager = new ConfigurationManager(this);
        languageManager = new LanguageManager(this);
        databaseManager = new DatabaseManager(this);
        apiServiceManager = new ApiServiceManager(this);
        tierDisplayManager = new TierDisplayManager(this);
        playerDisplayListener = new PlayerDisplayListener(this);

        previousApiProvider = configurationManager.getApiProvider();
        
        databaseManager.initialize();
    }
    
    private void registerCommands() {
        getCommand("tier").setExecutor(new TierCommand(this));
        getCommand("display").setExecutor(new DisplayCommand(this));
        getCommand("tieroff").setExecutor(new TierOffCommand(this));
        getCommand("hidetier").setExecutor(new HideTierCommand(this));
        getCommand("tiertagger").setExecutor(new TierTaggerCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(playerDisplayListener, this);
    }
    
    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TierPlaceholderExpansion(this).register();
        }
    }
    
    public void reloadConfiguration() {
        ConfigurationManager.ApiProvider currentApiProvider = configurationManager.getApiProvider();
        
        configurationManager.reloadConfig();
        languageManager.reloadLanguage();
        
        ConfigurationManager.ApiProvider newApiProvider = configurationManager.getApiProvider();
        if (previousApiProvider != null && !previousApiProvider.equals(newApiProvider)) {
            getLogger().info("API provider changed from " + previousApiProvider + " to " + newApiProvider + " - clearing all tiers");
            tierDisplayManager.clearAllTiers();
            tierDisplayManager.refetchAllTiers();
        } else {
            getLogger().info("API provider unchanged (" + newApiProvider + ") - keeping existing tiers");
        }

        previousApiProvider = newApiProvider;
        
        getLogger().info("Configuration reloaded successfully");
    }
    
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ApiServiceManager getApiServiceManager() {
        return apiServiceManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public TierDisplayManager getTierDisplayManager() {
        return tierDisplayManager;
    }
    
    public PlayerDisplayListener getPlayerDisplayListener() {
        return playerDisplayListener;
    }
}