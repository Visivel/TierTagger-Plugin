package net.tiertagger.lang;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final TierTaggerPlugin plugin;
    private FileConfiguration languageConfig;
    private final Map<String, String> messages;
    
    public LanguageManager(TierTaggerPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadLanguage();
    }
    
    public void loadLanguage() {
        String language = plugin.getConfigurationManager().getLanguage();
        loadLanguageFile(language);
    }
    
    private void loadLanguageFile(String language) {
        String fileName = "lang/" + language + ".yml";
        File langFile = new File(plugin.getDataFolder(), fileName);
        
        if (!langFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        languageConfig = YamlConfiguration.loadConfiguration(langFile);
        
        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            languageConfig.setDefaults(defaultConfig);
        }
        
        loadMessagesFromConfig(languageConfig, "");
    }
    
    private void loadMessagesFromConfig(FileConfiguration config, String path) {
        for (String key : config.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            
            if (config.isConfigurationSection(key)) {
                loadMessagesFromSection(config.getConfigurationSection(key), fullPath);
            } else {
                String value = config.getString(key, "");
                messages.put(fullPath, value);
            }
        }
    }
    
    private void loadMessagesFromSection(org.bukkit.configuration.ConfigurationSection section, String path) {
        for (String key : section.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            
            if (section.isConfigurationSection(key)) {
                loadMessagesFromSection(section.getConfigurationSection(key), fullPath);
            } else {
                String value = section.getString(key, "");
                messages.put(fullPath, value);
            }
        }
    }
    
    public String getMessage(String key) {
        String message = messages.getOrDefault(key, key);
        return message.replace('&', '§');
    }
    
    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String replacement = replacements[i + 1];
                message = message.replace(placeholder, replacement);
            }
        }
        
        return message.replace('&', '§');
    }
    
    public String getFormattedTier(String tier) {
        String key = "tiers." + tier.toLowerCase();
        return getMessage(key);
    }
    
    public String getGamemodeName(String gamemode) {
        String key = "gamemodes." + gamemode.toLowerCase();
        return getMessage(key);
    }
    
    public void reloadLanguage() {
        messages.clear();
        loadLanguage();
    }
}