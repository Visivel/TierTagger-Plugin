package net.tiertagger.services;

import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.config.ConfigurationManager;
import net.tiertagger.models.PlayerTierData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TierDisplayManager {
    
    private final TierTaggerPlugin plugin;
    private final Map<UUID, String> playerTiers;
    private final Set<UUID> displayDisabledPlayers;
    private final Set<UUID> tierHiddenPlayers;
    private boolean globalDisplayEnabled;
    
    public TierDisplayManager(TierTaggerPlugin plugin) {
        this.plugin = plugin;
        this.playerTiers = new ConcurrentHashMap<>();
        this.displayDisabledPlayers = ConcurrentHashMap.newKeySet();
        this.tierHiddenPlayers = ConcurrentHashMap.newKeySet();
        this.globalDisplayEnabled = true;
        loadHiddenUsers();
        loadHiddenTiers();
    }
    
    public void setTier(Player player, PlayerTierData tierData) {
        if (!plugin.getConfigurationManager().isDisplayEnabled() || !globalDisplayEnabled) {
            return;
        }
        
        String tier = tierData.getTierForGamemode(plugin.getConfigurationManager().getDefaultGamemode());
        if (tier.equals("UNRANKED")) {
            tier = tierData.getBestTier();
        }
        
        playerTiers.put(player.getUniqueId(), tier);
        

        if (!tierHiddenPlayers.contains(player.getUniqueId())) {
            showTierName(player, tier);
            
            if (plugin.getConfigurationManager().isDisplayShowTablist()) {
                showTabList(player, tier);
            }
        }
    }
    
    public void clearTier(Player player) {
        playerTiers.remove(player.getUniqueId());
        hideTierName(player);
        hideTabList(player);
    }
    
    public void toggleDisplay(Player player) {
        UUID playerId = player.getUniqueId();
        if (displayDisabledPlayers.contains(playerId)) {
            displayDisabledPlayers.remove(playerId);
        } else {
            displayDisabledPlayers.add(playerId);
        }
        if (plugin.getPlayerDisplayListener() != null) {
            plugin.getPlayerDisplayListener().updatePlayerScoreboard(player);
        }
        
        saveHiddenUsers();
    }
    
    private void loadHiddenUsers() {
        File dataFolder = new File(plugin.getDataFolder(), "disabled_players.txt");
        if (!dataFolder.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFolder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    UUID uuid = UUID.fromString(line.trim());
                    displayDisabledPlayers.add(uuid);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load disabled players: " + e.getMessage());
        }
    }
    
    private void saveHiddenUsers() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File file = new File(dataFolder, "disabled_players.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (UUID uuid : displayDisabledPlayers) {
                writer.write(uuid.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save disabled players: " + e.getMessage());
        }
    }
    
    public void switchGlobalMode() {
        globalDisplayEnabled = !globalDisplayEnabled;
        
        if (globalDisplayEnabled) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String tier = playerTiers.get(player.getUniqueId());
                if (tier != null && !displayDisabledPlayers.contains(player.getUniqueId())) {
                    showTierName(player, tier);
                    showTabList(player, tier);
                }
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                hideTierName(player);
            }
        }
    }
    
    private void showTierName(Player player, String tier) {
        if (tierHiddenPlayers.contains(player.getUniqueId())) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            return;
        }
        
        String coloredTier = applyTierColor(tier);
        String displayName = coloredTier + " §7| §f" + player.getName();
        
        player.setDisplayName(displayName);
        
        if (plugin.getConfigurationManager().isShowInChat()) {
            player.setPlayerListName(displayName);
        }

        if (plugin.getConfigurationManager().isDisplayShowNametag()) {
            showNameTag(player, tier);
        }
    }
    
    private void showNameTag(Player player, String tier) {
        if (tierHiddenPlayers.contains(player.getUniqueId())) {
            hideNameTag(player);
            return;
        }
        
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "tier_" + tier.toLowerCase() + "_" + player.getName().toLowerCase();
        
        Team team = mainScoreboard.getTeam(teamName);
        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
        }
        
        String coloredTier = applyTierColor(tier);
        team.setPrefix(coloredTier + " §8| ");
        team.setSuffix("§f");
        
        team.addEntry(player.getName());
    }
    
    private void showTabList(Player player, String tier) {
        if (!plugin.getConfigurationManager().isShowInTablist() ||
            !plugin.getConfigurationManager().isDisplayShowTablist()) {
            return;
        }
        
        if (tierHiddenPlayers.contains(player.getUniqueId())) {
            return;
        }
        
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "tier_" + tier.toLowerCase();
        Team team = mainScoreboard.getTeam(teamName);
        
        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
            String coloredTier = applyTierColor(tier);
            team.setPrefix(coloredTier + " §7| §f");
        }
        
        team.addEntry(player.getName());
    }
    
    private void hideTierName(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());
        
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            for (Team team : scoreboard.getTeams()) {
                if (team.getName().startsWith("tier_")) {
                    team.removeEntry(player.getName());
                }
            }
        }
        
        hideNameTag(player);
    }
    
    private void hideNameTag(Player player) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : mainScoreboard.getTeams()) {
            if (team.getName().startsWith("tier_") && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        }
    }

    private void hideTabList(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            for (Team team : scoreboard.getTeams()) {
                if (team.getName().startsWith("tier_") && team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                    if (team.getEntries().isEmpty()) {
                        team.unregister();
                    }
                }
            }
        }
        
        player.setPlayerListName(player.getName());
    }
    
    private String applyTierColor(String tier) {
        ConfigurationManager.TierColor tierColor = plugin.getConfigurationManager().getTierColors().get(tier.toLowerCase());
        
        if (tierColor != null) {
            return createGradientText(tier, tierColor.getStartColorAsColor(), tierColor.getEndColorAsColor());
        }
        
        return getDefaultTierColor(tier) + tier;
    }
    
    private String createGradientText(String text, Color startColor, Color endColor) {
        if (text.length() <= 1) {
            return colorToMinecraftColor(startColor) + text;
        }
        
        StringBuilder gradientText = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            Color interpolatedColor = interpolateColor(startColor, endColor, ratio);
            gradientText.append(colorToMinecraftColor(interpolatedColor)).append(text.charAt(i));
        }
        
        return gradientText.toString();
    }
    
    private String colorToMinecraftColor(Color color) {
        return String.format("§x§%x§%x§%x§%x§%x§%x",
            (color.getRed() >> 4) & 0xF, color.getRed() & 0xF,
            (color.getGreen() >> 4) & 0xF, color.getGreen() & 0xF,
            (color.getBlue() >> 4) & 0xF, color.getBlue() & 0xF);
    }
    
    private Color interpolateColor(Color start, Color end, float ratio) {
        int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
        int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));
        
        return new Color(Math.max(0, Math.min(255, red)), 
                        Math.max(0, Math.min(255, green)), 
                        Math.max(0, Math.min(255, blue)));
    }
    
    private String getDefaultTierColor(String tier) {
        switch (tier.toUpperCase()) {
            case "HT1": return "§c";
            case "LT1": return "§6";
            case "HT2": return "§e";
            case "LT2": return "§a";
            case "HT3": return "§b";
            case "LT3": return "§9";
            case "HT4": return "§d";
            case "LT4": return "§5";
            case "HT5": return "§7";
            case "LT5": return "§8";
            default: return "§f";
        }
    }
    
    public boolean isGlobalDisplayEnabled() {
        return globalDisplayEnabled;
    }
    
    public boolean isPlayerDisplayEnabled(Player player) {
        return !displayDisabledPlayers.contains(player.getUniqueId());
    }
    
    public String getPlayerTier(Player player) {
        return playerTiers.get(player.getUniqueId());
    }
    
    public void switchTierVisibility(Player player) {
        UUID playerId = player.getUniqueId();
        if (tierHiddenPlayers.contains(playerId)) {
            tierHiddenPlayers.remove(playerId);
            String tier = playerTiers.get(playerId);
            if (tier != null) {
                showTierName(player, tier);
                if (plugin.getConfigurationManager().isDisplayShowTablist()) {
                    showTabList(player, tier);
                }
            }
        } else {
            tierHiddenPlayers.add(playerId);
            hideTierName(player);
            hideNameTag(player);
            hideTabList(player);
        }
        
        saveHiddenTiers();
    }
    
    public boolean isTierHidden(Player player) {
        return tierHiddenPlayers.contains(player.getUniqueId());
    }
    
    private void loadHiddenTiers() {
        File dataFolder = new File(plugin.getDataFolder(), "hidden_players.txt");
        if (!dataFolder.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFolder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    UUID uuid = UUID.fromString(line.trim());
                    tierHiddenPlayers.add(uuid);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load hidden players: " + e.getMessage());
        }
    }
    
    private void saveHiddenTiers() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File file = new File(dataFolder, "hidden_players.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (UUID uuid : tierHiddenPlayers) {
                writer.write(uuid.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save hidden players: " + e.getMessage());
        }
    }
    
    public void clearAllTiers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hideTierName(player);
            hideNameTag(player);
            hideTabList(player);
        }
        playerTiers.clear();
    }
    
    public void refetchAllTiers() {
        plugin.getLogger().info("Starting refetch for all online players...");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getLogger().info("Refetching tier for player: " + player.getName());
            
            plugin.getApiServiceManager().fetchPlayerTierData(player.getUniqueId().toString(), true)
                .thenAccept(tierData -> {
                    if (tierData != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            setTier(player, tierData);
                            plugin.getLogger().info("Successfully refetched tier for " + player.getName() + ": " + tierData.getBestTier());
                        });
                    } else {
                        plugin.getLogger().warning("No tier data found for " + player.getName() + " after refetch");
                    }
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().warning("Failed to refetch tier for player " + player.getName() + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
        }
        
        plugin.getLogger().info("Refetch requests sent for all online players");
    }
    
    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String tier = playerTiers.get(player.getUniqueId());
            if (tier != null) {
                showTierName(player, tier);
                showTabList(player, tier);
            }
        }
    }
}