package net.tiertagger.listeners;

import net.tiertagger.TierTaggerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerDisplayListener implements Listener {
    
    private final TierTaggerPlugin plugin;
    
    public PlayerDisplayListener(TierTaggerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getTierDisplayManager().isPlayerDisplayEnabled(player)) {
            createCleanScoreboardForPlayer(player);
        }
    }
    
    private void createCleanScoreboardForPlayer(Player player) {
        Scoreboard cleanScoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            String cleanTeamName = "clean_" + onlinePlayer.getName().toLowerCase();
            Team cleanTeam = cleanScoreboard.getTeam(cleanTeamName);
            if (cleanTeam == null) {
                cleanTeam = cleanScoreboard.registerNewTeam(cleanTeamName);
            }
            cleanTeam.setPrefix("");
            cleanTeam.setSuffix("");
            cleanTeam.addEntry(onlinePlayer.getName());
        }
        
        player.setScoreboard(cleanScoreboard);
    }
    
    public void updatePlayerScoreboard(Player player) {
        if (!plugin.getTierDisplayManager().isPlayerDisplayEnabled(player)) {
            createCleanScoreboardForPlayer(player);
        } else {
            player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        }
    }
}