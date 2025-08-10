package net.tiertagger.cache;

import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.models.PlayerTierData;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    
    private final TierTaggerPlugin plugin;
    private final ExecutorService executorService;
    private Connection connection;
    
    public DatabaseManager(TierTaggerPlugin plugin) {
        this.plugin = plugin;
        this.executorService = Executors.newFixedThreadPool(
            plugin.getConfigurationManager().getDatabasePoolSize()
        );
    }
    
    public void initialize() {
        try {
            Class.forName("org.h2.Driver");
            setupDatabase();
            createTables();
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void setupDatabase() throws SQLException {
        String databasePath = plugin.getConfigurationManager().getDatabaseFile();
        File databaseFile = new File(databasePath);
        
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }
        
        String url = "jdbc:h2:" + databaseFile.getAbsolutePath().replace(".db", "") + ";AUTO_SERVER=TRUE";
        connection = DriverManager.getConnection(url);
    }
    
    private void createTables() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS player_tiers (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL,
                tier_data TEXT NOT NULL,
                cached_at TIMESTAMP NOT NULL,
                expires_at TIMESTAMP NOT NULL
            )
        """;
        
        try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
            statement.executeUpdate();
        }
    }
    
    public CompletableFuture<Optional<PlayerTierData>> getCachedTierData(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String selectSQL = "SELECT * FROM player_tiers WHERE uuid = ? AND expires_at > ?";
                try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
                    statement.setString(1, uuid);
                    statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            String username = resultSet.getString("username");
                            String tierDataJson = resultSet.getString("tier_data");
                            Timestamp cachedAt = resultSet.getTimestamp("cached_at");
                            
                            PlayerTierData tierData = PlayerTierData.fromJson(tierDataJson);
                            tierData.setUsername(username);
                            tierData.setUuid(uuid);
                            tierData.setCachedAt(cachedAt.toLocalDateTime());
                            
                            return Optional.of(tierData);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get cached tier data: " + e.getMessage());
            }
            return Optional.empty();
        }, executorService);
    }
    
    public CompletableFuture<Void> cacheTierData(String uuid, String username, PlayerTierData tierData) {
        return CompletableFuture.runAsync(() -> {
            try {
                String insertSQL = """
                    MERGE INTO player_tiers (uuid, username, tier_data, cached_at, expires_at)
                    KEY (uuid)
                    VALUES (?, ?, ?, ?, ?)
                """;
                
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresAt = now.plusMinutes(plugin.getConfigurationManager().getCacheDuration());
                
                try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
                    statement.setString(1, uuid);
                    statement.setString(2, username);
                    statement.setString(3, tierData.toJson());
                    statement.setTimestamp(4, Timestamp.valueOf(now));
                    statement.setTimestamp(5, Timestamp.valueOf(expiresAt));
                    
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to cache tier data: " + e.getMessage());
            }
        }, executorService);
    }
    
    public CompletableFuture<Void> cleanExpiredCache() {
        return CompletableFuture.runAsync(() -> {
            try {
                String deleteSQL = "DELETE FROM player_tiers WHERE expires_at < ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
                    statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    int deletedRows = statement.executeUpdate();
                    
                    if (deletedRows > 0 && plugin.getConfigurationManager().isDebugEnabled()) {
                        plugin.getLogger().info("Cleaned " + deletedRows + " expired cache entries");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to clean expired cache: " + e.getMessage());
            }
        }, executorService);
    }
    
    public CompletableFuture<Void> clearAllCache() {
        return CompletableFuture.runAsync(() -> {
            try {
                String deleteSQL = "DELETE FROM player_tiers";
                try (PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
                    int deletedRows = statement.executeUpdate();
                    plugin.getLogger().info("Cleared " + deletedRows + " cache from database");
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to clear cache: " + e.getMessage());
            }
        }, executorService);
    }
    
    public void shutdown() {
        try {
            executorService.shutdown();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing database connection: " + e.getMessage());
        }
    }
}